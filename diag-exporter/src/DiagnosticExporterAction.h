#ifndef DIAGNOSTICEXPORTER_DIAGNOSTICEXPORTERACTION_H
#define DIAGNOSTICEXPORTER_DIAGNOSTICEXPORTERACTION_H

#include <clang/Tooling/Tooling.h>
#include <clang/Frontend/FrontendActions.h>
#include <nlohmann/json.hpp>

#include <mutex>
#include <chrono>
#include <iostream>

#include "JsonDiagnosticConsumer.h"

using ordered_json = nlohmann::ordered_json;

class DiagnosticExporterAction : public clang::SyntaxOnlyAction {
private:
    // static std::string outputFilepath;

    static unsigned totalFiles;

    // static unsigned filesDone;

    JsonDiagnosticConsumer *diagConsumer;

    // A DiagnosticExporter action is created for each input source file so we need to make this static
    // static ordered_json diagsInfos;

    // std::mutex mutex; // TODO this is probably incorrect, the mutex needed to be shared among all DiagnosticExporterAction instances

    const unsigned fileId;

    const ordered_json &severityMapping;

    std::string currentReducedFile;

    std::chrono::time_point<std::chrono::steady_clock> start;

public:
    explicit DiagnosticExporterAction(const unsigned fileId, const ordered_json &severityMapping) :
            diagConsumer(nullptr), fileId(fileId), severityMapping(severityMapping) { }

    bool BeginInvocation(clang::CompilerInstance &compilerInstance) override;

    bool BeginSourceFileAction(clang::CompilerInstance &compilerInstance) override;

    void ExecuteAction() override;

    void EndSourceFileAction() override;

    static void setTotalFiles(unsigned totalFiles);

    static std::vector<ordered_json> individualDiagsInfos;

    // static void setOutputFilepath(std::string outputFilepath);

    // static void setConfig(std::string outputFilepath, unsigned totalFiles, const std::vector<ThreadSourceBatch> &threadSourceBatches);
};

class DiagnosticExporterActionFactory : public clang::tooling::FrontendActionFactory {
private:
    static std::atomic<unsigned> fileIdCounter;

    const ordered_json &severityMapping;
public:
    explicit DiagnosticExporterActionFactory(const ordered_json &severityMapping) :
                                                severityMapping(severityMapping) {} // created once per thread

    std::unique_ptr<clang::FrontendAction> create() override { // called for each file
        return std::make_unique<DiagnosticExporterAction>(fileIdCounter.fetch_add(1), severityMapping);
    }
};

#endif // DIAGNOSTICEXPORTER_DIAGNOSTICEXPORTERACTION_H
