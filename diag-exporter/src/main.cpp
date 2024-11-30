#include <llvm/Support/CommandLine.h>
#include <clang/Tooling/CommonOptionsParser.h>
#include <clang/Tooling/Tooling.h>
#include <clang/Frontend/CompilerInvocation.h>

#include <iostream>
#include <thread>
#include <filesystem>
#include <sstream>
#include <fstream>

#include "DiagnosticExporterAction.h"
#include "SourceBatchThreadAllocator.h"

// Apply a custom category to all command-line options so that they are the only ones displayed.
static llvm::cl::OptionCategory tuDiagExporterCategory("tu-diag-exporter options");

// CommonOptionsParser declares HelpMessage with a description of the common
// command-line options related to the compilation database and input files.
static llvm::cl::extrahelp commonHelp(clang::tooling::CommonOptionsParser::HelpMessage);

static llvm::cl::opt<std::string> outputFilepath(
                "o",
                llvm::cl::desc("Specify output file"),
                llvm::cl::Optional,
                llvm::cl::value_desc("output"),
                llvm::cl::init("output.json"),
                llvm::cl::cat(tuDiagExporterCategory));

static llvm::cl::opt<unsigned> threads(
                "t",
                llvm::cl::desc("Specify number of threads"),
                //llvm::cl::Optional,
                llvm::cl::value_desc("threads"),
                llvm::cl::init(1),
                llvm::cl::cat(tuDiagExporterCategory));

unsigned validateThreadNum(const std::vector<SourceFile> &sourceFiles) {
    const auto hardwareThreads = std::thread::hardware_concurrency();

    auto threadNum = std::min(threads.getValue(), hardwareThreads);

    if (threads.getValue() > hardwareThreads) {
        llvm::outs() << "diag-exporter: warning: number of selected threads exceeds hardware threads. Using available hardware threads instead ("
            << hardwareThreads << " threads)\n";
    }

    if (sourceFiles.size() < threadNum) {
        llvm::outs() << "diag-exporter: warning: number of available/selected threads exceeds number of files. Using "
                << sourceFiles.size() << " threads instead\n";

        threadNum = sourceFiles.size();
    }

    return threadNum;
}

// TODO find if calling the tool just one time is more efficient than calling it multiple times with 1 file each
void runExporterInThread(llvm::Expected<clang::tooling::CommonOptionsParser> &optionsParser,
                         const unsigned id, const ThreadSourceBatch &threadSourceBatch) {

    std::vector<std::string> sourceFiles;

    sourceFiles.reserve(threadSourceBatch.sourceFiles.size());

    for (const auto &[path, size, id] : threadSourceBatch.sourceFiles) {
        sourceFiles.push_back(path);
    }

    clang::tooling::ClangTool tool(optionsParser->getCompilations(), sourceFiles);
    DiagnosticExporterActionFactory actionFactory;

    tool.run(&actionFactory);
}

int main(int argc, const char **argv) {
    llvm::cl::SetVersionPrinter(
        [](llvm::raw_ostream &OS) {
            OS << TOOL_NAME << " version " << TOOL_VERSION << "    " << TOOL_RELEASE_DATE << "\n"; });

    auto optionsParser = clang::tooling::CommonOptionsParser::create(argc, argv, tuDiagExporterCategory);

    if (!optionsParser) {
        // Fail gracefully for unsupported options.
        llvm::errs() << optionsParser.takeError();
        return 1;
    }

    std::vector<SourceFile> sourceFiles{};

    for (const auto &sourcePath : optionsParser->getSourcePathList()) {
        try {
            if (std::filesystem::exists(sourcePath) && std::filesystem::is_regular_file(sourcePath)) {
                const auto fileSize = std::filesystem::file_size(sourcePath);
                sourceFiles.emplace_back(SourceFile{sourcePath, static_cast<unsigned>(fileSize)});
                // llvm::outs() << "File: " << sourcePath << " | Size: " << fileSize << " bytes" << "\n";
            } else {
                llvm::errs() << "Skipping non-regular file: " << sourcePath << "\n";
            }
        } catch (const std::filesystem::filesystem_error &e) {
            llvm::errs() << "Error accessing file " << sourcePath << ": " << e.what() << "\n";
        }
    }

    // DiagnosticExporterAction::setOutputFilepath(outputFilepath.getValue());

    DiagnosticExporterAction::setTotalFiles(sourceFiles.size());

    const auto threadNum = validateThreadNum(sourceFiles);

    if (threadNum == 1) {
        llvm::outs() << "diag-exporter: note: number of threads is 1. Using single-threaded mode\n";
        clang::tooling::ClangTool tool(optionsParser->getCompilations(), optionsParser->getSourcePathList());

        DiagnosticExporterActionFactory actionFactory;
        int ret = tool.run(&actionFactory);
    } else {
        auto sourceBatchAllocation = SourceBatchThreadAllocator(threadNum, sourceFiles);

        auto threadSourceBatches = sourceBatchAllocation.allocateGreedyMinHeap();

        std::vector<std::thread> threads;

        for (unsigned i = 0; i < threadNum; i++) {
            threads.emplace_back(runExporterInThread, std::ref(optionsParser), i, threadSourceBatches[i]);
        }

        for (auto &thread : threads) {
            thread.join();
        }
    }

    auto individualDiagsInfos = DiagnosticExporterAction::individualDiagsInfos;

    ordered_json diagsInfos = ordered_json::array();
    for (const auto &individualDiagsInfo : individualDiagsInfos) {
        diagsInfos.push_back(individualDiagsInfo);
    }

    llvm::outs() << "Finished processing all files. Saving results..\n";

    // TODO in the future maybe separate into different outputs
    std::filesystem::path filePathObj(outputFilepath.getValue());

    if (std::filesystem::path directory = filePathObj.parent_path(); !exists(directory)) {
        try {
            create_directories(directory);
            llvm::outs() << "Directory created: " << directory << "\n";
        } catch (const std::filesystem::filesystem_error& e) {
            llvm::outs().flush();
            llvm::errs() << "Error creating directory: " << e.what() << "\n";
            std::exit(1);
        }
    }

    std::ofstream file(outputFilepath);

    if (!file.is_open()) {
        llvm::outs().flush();
        llvm::errs() << "Could not open the output file for writing!\n";
        std::exit(1);
    }

    file << diagsInfos.dump(2);

    file.close();

    /*clang::tooling::ClangTool tool(optionsParser->getCompilations(), optionsParser->getSourcePathList());

    DiagnosticExporterAction::setConfig(outputFilepath.getValue(), optionsParser->getSourcePathList().size());
    return tool.run(clang::tooling::newFrontendActionFactory<DiagnosticExporterAction>().get());*/
}
