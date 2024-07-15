#ifndef DIAGNOSTICEXPORTER_DIAGNOSTICEXPORTERACTION_H
#define DIAGNOSTICEXPORTER_DIAGNOSTICEXPORTERACTION_H

#include <clang/Frontend/FrontendActions.h>
#include <nlohmann/json.hpp>
#include <mutex>

#include "JsonDiagnosticConsumer.h"

using ordered_json = nlohmann::ordered_json;

class DiagnosticExporterAction : public clang::SyntaxOnlyAction {
private:
    static std::string outputFilepath;

    static unsigned totalFiles;

    static unsigned filesDone;

    JsonDiagnosticConsumer *diagConsumer;

    // A DiagnosticExporter action is created for each input source file so we need to make this static
    static ordered_json diagsInfos;

    std::mutex mutex;

public:
    explicit DiagnosticExporterAction() : diagConsumer(nullptr) { }

    void ExecuteAction() override;

    void EndSourceFileAction() override;

    static void setConfig(std::string outputFilepath, unsigned totalFiles);
};

#endif // DIAGNOSTICEXPORTER_DIAGNOSTICEXPORTERACTION_H
