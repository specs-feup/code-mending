#ifndef DIAGNOSTICEXPORTER_JSONDIAGNOSTICCONSUMER_H
#define DIAGNOSTICEXPORTER_JSONDIAGNOSTICCONSUMER_H

#include <clang/Basic/Diagnostic.h>
#include <clang/Basic/TokenKinds.h>
#include <clang/Frontend/CompilerInstance.h>
#include <nlohmann/json.hpp>

#include "QualTypeJsonConverter.h"

using ordered_json = nlohmann::ordered_json;

class JsonDiagnosticConsumer : public clang::DiagnosticConsumer {
private:
    clang::CompilerInstance &compilerInstance;

    QualTypeJsonConverter qualTypeJsonConverter;

    ordered_json diagsInfo;

    unsigned totalDiagsCount{};

    unsigned ignoredCount{};

    unsigned noteCount{};

    unsigned remarkCount{};

    unsigned warningCount{};

    unsigned errorCount{};

    unsigned fatalCount{};

    static const std::string diagDescriptionsTable[clang::diag::DIAG_UPPER_LIMIT];

    static constexpr unsigned diagsNum = std::size(diagDescriptionsTable);

    /***************************************************************************/

    void updateDiagnosticCounts(clang::DiagnosticsEngine::Level level);

    static ordered_json getDescription(unsigned diagID);

    static ordered_json getLevelAsString(clang::DiagnosticsEngine::Level diagLevel);

    static std::string getCategory(unsigned diagID);

    static int getGroup(unsigned diagID);

    ordered_json getMessageInfo(const clang::Diagnostic &info);

    static ordered_json getPresumedLocInfo(const clang::SourceLocation &sLoc, const clang::SourceManager &sManager);

    ordered_json getLocationInfo(const clang::Diagnostic &info) const;

    ordered_json getSourceRanges(const clang::Diagnostic &info) const;

    ordered_json getCodeSnippet(const clang::Diagnostic &info) const;

    static ordered_json getTokenCategory(clang::tok::TokenKind tokenKind);

    static ordered_json getTokenKindSpelling(clang::tok::TokenKind tokenKind);

public:
    explicit JsonDiagnosticConsumer(clang::CompilerInstance &compilerInstance_)
                : compilerInstance(compilerInstance_),
                    qualTypeJsonConverter(compilerInstance_.getASTContext()) {
        const auto fileEntry =
            compilerInstance.getSourceManager().getFileEntryForID(compilerInstance.getSourceManager().getMainFileID());

        diagsInfo = ordered_json::object({
            {"file", fileEntry? ordered_json(fileEntry->getName()) : ordered_json(nullptr)},
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

    void HandleDiagnostic(clang::DiagnosticsEngine::Level diagLevel, const clang::Diagnostic &info) override;

    bool IncludeInDiagnosticCounts() const override;

    void EndSourceFile() override;

    const ordered_json &getDiagsInfo() const { return diagsInfo; }
};

#endif // DIAGNOSTICEXPORTER_JSONDIAGNOSTICCONSUMER_H
