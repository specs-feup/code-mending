#include <llvm/Support/CommandLine.h>
#include <clang/Tooling/CommonOptionsParser.h>
#include <clang/Tooling/Tooling.h>
#include <clang/Frontend/CompilerInvocation.h>
#include <iostream>

#include "DiagnosticExporterAction.h"

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
                llvm::cl::cat(tuDiagExporterCategory));

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

    clang::tooling::ClangTool tool(optionsParser->getCompilations(), optionsParser->getSourcePathList());

    DiagnosticExporterAction::setConfig(outputFilepath.getValue(), optionsParser->getSourcePathList().size());
    return tool.run(clang::tooling::newFrontendActionFactory<DiagnosticExporterAction>().get());
}
