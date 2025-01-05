#include <clang/Frontend/CompilerInstance.h>
#include <sstream>
#include <fstream>
#include <filesystem>
#include <chrono>


#include "DiagnosticExporterAction.h"

#include <iostream>

std::atomic<unsigned> DiagnosticExporterActionFactory::fileIdCounter{0};

// std::string DiagnosticExporterAction::outputFilepath = "output.json";

unsigned DiagnosticExporterAction::totalFiles = 0;

// unsigned DiagnosticExporterAction::filesDone = 0;

// ordered_json DiagnosticExporterAction::diagsInfos = ordered_json::array();

std::vector<ordered_json> DiagnosticExporterAction::individualDiagsInfos;

static std::vector<std::string> split(const std::string &path, char delimiter) {
    std::vector<std::string> tokens;
    std::string token;
    std::istringstream tokenStream(path);

    while (std::getline(tokenStream, token, delimiter)) {
        tokens.push_back(token);
    }

    return tokens;
}

static std::string reducePath(const std::string &path) {
    const std::vector<std::string> parts = split(path, '/');

    if (constexpr int threshold = 4; parts.size() <= threshold) {
        return path;
    }

    std::string reducedPath = ".../" + parts[parts.size() - 3] + "/" + parts[parts.size() - 2] + "/" + parts.back();

    return reducedPath;
}

bool DiagnosticExporterAction::BeginInvocation(clang::CompilerInstance &compilerInstance) {
    if (severityMapping.is_null()) {
        return true;
    }

    auto &diagnosticsEngine = compilerInstance.getDiagnostics();

    for (auto it = severityMapping.begin(); it != severityMapping.end(); ++it) {
        const auto id = std::stoi(it.key());
        const auto severityStr = it.value().get<std::string>();

        // TODO validate at the start the file
        clang::diag::Severity severity = clang::diag::Severity::Ignored;

        if (severityStr == "ignored") {
            severity = clang::diag::Severity::Ignored;
        } else if (severityStr == "remark") {
            severity = clang::diag::Severity::Remark;
        } else if (severityStr == "warning") {
            severity = clang::diag::Severity::Warning;
        } else if (severityStr == "error") {
            severity = clang::diag::Severity::Error;
        } else if (severityStr == "fatal") {
            severity = clang::diag::Severity::Fatal;
        }

        diagnosticsEngine.setSeverity(id, severity, clang::SourceLocation());
    }

    return true;
}

bool DiagnosticExporterAction::BeginSourceFileAction(clang::CompilerInstance &compilerInstance) {
    //diagConsumer = new JsonDiagnosticConsumer(compilerInstance);
    //compilerInstance.getDiagnostics().setClient(diagConsumer, /*ShouldOwnClient=*/false);
    currentReducedFile = reducePath(std::string(getCurrentFile().data(), getCurrentFile().size()));
    return true;
}

void DiagnosticExporterAction::ExecuteAction() {
    clang::CompilerInstance &compilerInstance = getCompilerInstance();

    // Needs to be created here because ASTContext is not available in BeginSourceFileAction
    diagConsumer = new JsonDiagnosticConsumer(compilerInstance);

    compilerInstance.getDiagnostics().setClient(diagConsumer, /*ShouldOwnClient=*/false);

    // TODO this is not thread safe
    // llvm::outs() << "Executing diagnostic exporter action for file " << currentFile << "\n";
    std::cout << "Executing diagnostic exporter action for file " << currentReducedFile << "\n";

    start = std::chrono::high_resolution_clock::now();

    SyntaxOnlyAction::ExecuteAction();
}

void DiagnosticExporterAction::EndSourceFileAction() {
    const auto duration = std::chrono::duration_cast<std::chrono::nanoseconds>(
                                std::chrono::high_resolution_clock::now() - start);

    //std::cout << "Elapsed time: " << duration.count() / 1000.0  << " milliseconds for file of size " << (double) diagConsumer->getDiagsInfo()["size"] / 1024.0 << " KB\n";

    // todo this is not thread safe
    // llvm::outs() << "Diagnostic exporter action finished for file " << currentFile << "\n";
    std::cout << "Diagnostic exporter action finished for file " << currentReducedFile << "\n";

    assert(diagConsumer != nullptr && "Diagnostic Consumer must not be a nullptr");

    diagConsumer->setActionTimeNanos(duration.count());

    //diagConsumer->setActionTimeMs();
    individualDiagsInfos[fileId] = diagConsumer->getDiagsInfo();
    // TODO delete diagConsumer;
}

/*void DiagnosticExporterAction::EndSourceFileAction() {
    const auto currentFile = reducePath(std::string(getCurrentFile().data(), getCurrentFile().size()));
    llvm::outs() << "Diagnostic exporter action finished for file " << currentFile << "\n";

    {
        // TODO this isn't doing anything because parallel processing is not enabled for this action
        std::lock_guard lock(mutex);

        ++filesDone;

        assert(diagConsumer != nullptr && "Diagnostic Consumer must not be a nullptr");

        diagsInfos.push_back(diagConsumer->getDiagsInfo());

        if (filesDone == totalFiles) {
            llvm::outs() << "Finished processing all files. Saving results..\n";

            std::filesystem::path filePathObj(outputFilepath);

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
        }
    }
}*/

void DiagnosticExporterAction::setTotalFiles(const unsigned totalFiles) {
    DiagnosticExporterAction::totalFiles = totalFiles;
    individualDiagsInfos.resize(totalFiles);

    for (unsigned i = 0; i < totalFiles; i++) {
        individualDiagsInfos[i] = ordered_json::object({
            {"file", ordered_json(nullptr)},
            {"size", -1},
            {"actionTimeNs", -1},
            {"totalDiagsCount", 0},
            {"ignoredCount", 0},
            {"noteCount", 0},
            {"remarkCount", 0},
            {"warningCount", 0},
            {"errorCount", 0},
            {"fatalCount", 0},
            {"diags", ordered_json::array()},
        });
    }
}

/*void DiagnosticExporterAction::setOutputFilepath(std::string outputFilepath) {
    DiagnosticExporterAction::outputFilepath = std::move(outputFilepath);
}*/

/*void DiagnosticExporterAction::setConfig(std::string outputFilepath, const unsigned totalFiles) {
    DiagnosticExporterAction::outputFilepath = std::move(outputFilepath);
    DiagnosticExporterAction::totalFiles = totalFiles;
}*/
