#include <clang/Basic/TokenKinds.h>
#include <clang/AST/Type.h>
#include "clang/Basic/IdentifierTable.h"
#include <clang/AST/Attr.h>
#include <clang/AST/DeclarationName.h>
#include <clang/Lex/Lexer.h>
#include <clang/Basic/DiagnosticCategories.h>
#include <clang/Basic/AllDiagnostics.h>
#include <clang/Basic/DiagnosticIDs.h>
#include <clang/Basic/SourceLocation.h>
#include <clang/Basic/SourceManager.h>

#include <iostream>
#include <sstream>
#include <algorithm>
#include <cctype>

#include "JsonDiagnosticConsumer.h"
#include "QualTypeJsonConverter.h"

const std::string JsonDiagnosticConsumer::diagLabelIdsTable[clang::diag::DIAG_UPPER_LIMIT] = {
    #define DIAG(ENUM, CLASS, DEFAULT_SEVERITY, DESC, GROUP, SFINAE, NOWERROR,     \
                 SHOWINSYSHEADER, SHOWINSYSMACRO, DEFERRABLE, CATEGORY)            \
        #ENUM,
    #include "clang/Basic/DiagnosticCommonKinds.inc"
    #include "clang/Basic/DiagnosticDriverKinds.inc"
    #include "clang/Basic/DiagnosticFrontendKinds.inc"
    #include "clang/Basic/DiagnosticSerializationKinds.inc"
    #include "clang/Basic/DiagnosticLexKinds.inc"
    #include "clang/Basic/DiagnosticParseKinds.inc"
    #include "clang/Basic/DiagnosticASTKinds.inc"
    #include "clang/Basic/DiagnosticCommentKinds.inc"
    #include "clang/Basic/DiagnosticCrossTUKinds.inc"
    #include "clang/Basic/DiagnosticSemaKinds.inc"
    #include "clang/Basic/DiagnosticAnalysisKinds.inc"
    #include "clang/Basic/DiagnosticRefactoringKinds.inc"
    #undef DIAG
};

const std::string interestingIdentifiers[clang::tok::NUM_INTERESTING_IDENTIFIERS] = {
    #define INTERESTING_IDENTIFIER(X) #X,
    #include "clang/Basic/TokenKinds.def"
};


void JsonDiagnosticConsumer::HandleDiagnostic(const clang::DiagnosticsEngine::Level diagLevel, const clang::Diagnostic& info) {
    // TODO maybe allow the user to configure the diag level(s) to ignore

    /*if (diagLevel != clang::DiagnosticsEngine::Error) {
        return;
    }*/

    updateDiagnosticCounts(diagLevel);

    const auto diagID = info.getID();

    diagsInfo["diags"].push_back(
        ordered_json::object({
            {"id", diagID},
            {"labelId", getLabelId(diagID)},
            {"level", getLevelAsString(diagLevel)},
            {"category", getCategory(diagID)},
            {"group", getGroup(diagID)},
            {"description", getDescriptionInfo(info)},
            {"location", getLocationInfo(info)},
            {"sourceRanges", getSourceRanges(info)},
            {"codeSnippet", getCodeSnippet(info)}
    }));
}

bool JsonDiagnosticConsumer::IncludeInDiagnosticCounts() const {
    return true;
}

void JsonDiagnosticConsumer::EndSourceFile() {
    diagsInfo["totalDiagsCount"] = totalDiagsCount;
    diagsInfo["ignoredCount"] = ignoredCount;
    diagsInfo["noteCount"] = noteCount;
    diagsInfo["remarkCount"] = remarkCount;
    diagsInfo["warningCount"] = warningCount;
    diagsInfo["errorCount"] = errorCount;
    diagsInfo["fatalCount"] = fatalCount;
}

void JsonDiagnosticConsumer::updateDiagnosticCounts(clang::DiagnosticsEngine::Level level) {
    totalDiagsCount++;

    switch (level) {
        case clang::DiagnosticsEngine::Ignored: {
            ignoredCount++;
            break;
        }
        case clang::DiagnosticsEngine::Note: {
            noteCount++;
            break;
        }
        case clang::DiagnosticsEngine::Remark: {
            remarkCount++;
            break;
        }
        case clang::DiagnosticsEngine::Warning: {
            warningCount++;
            break;
        }
        case clang::DiagnosticsEngine::Error: {
            errorCount++;
            break;
        }
        case clang::DiagnosticsEngine::Fatal: {
            fatalCount++;
            break;
        }
    }
}

ordered_json JsonDiagnosticConsumer::getLabelId(const unsigned diagID) {
    if (diagID >= clang::diag::DIAG_UPPER_LIMIT || diagID <= clang::diag::DIAG_START_COMMON) {
        return nullptr;
    }

    // Compute the index of the requested diagnostic in the static table.
    // 1. Add the number of diagnostics in each category preceding the
    //    diagnostic and of the category the diagnostic is in. This gives us
    //    the offset of the category in the table.
    // 2. Subtract the number of IDs in each category from our ID. This gives us
    //    the offset of the diagnostic in the category.
    // This is cheaper than a binary search on the table as it doesn't touch
    // memory at all.
    unsigned offset = 0;
    unsigned id = diagID - clang::diag::DIAG_START_COMMON - 1;

    #define CATEGORY(NAME, PREV) \
    if (diagID > clang::diag::DIAG_START_##NAME) { \
        offset += clang::diag::NUM_BUILTIN_##PREV##_DIAGNOSTICS - clang::diag::DIAG_START_##PREV - 1; \
        id -= clang::diag::DIAG_START_##NAME - clang::diag::DIAG_START_##PREV; \
    }
    CATEGORY(DRIVER, COMMON)
    CATEGORY(FRONTEND, DRIVER)
    CATEGORY(SERIALIZATION, FRONTEND)
    CATEGORY(LEX, SERIALIZATION)
    CATEGORY(PARSE, LEX)
    CATEGORY(AST, PARSE)
    CATEGORY(COMMENT, AST)
    CATEGORY(CROSSTU, COMMENT)
    CATEGORY(SEMA, CROSSTU)
    CATEGORY(ANALYSIS, SEMA)
    CATEGORY(REFACTORING, ANALYSIS)
    #undef CATEGORY

    if (id + offset >= diagsNum) {
        return nullptr;
    }

    assert(id < diagsNum && offset < diagsNum);

    return diagLabelIdsTable[id + offset];
}

ordered_json JsonDiagnosticConsumer::getLevelAsString(const clang::DiagnosticsEngine::Level diagLevel) {
    switch (diagLevel) {
        case clang::DiagnosticsEngine::Ignored: return "ignored";
        case clang::DiagnosticsEngine::Note: return "note";
        case clang::DiagnosticsEngine::Remark: return "remark";
        case clang::DiagnosticsEngine::Warning: return "warning";
        case clang::DiagnosticsEngine::Error: return "error";
        case clang::DiagnosticsEngine::Fatal: return "fatal";
    }

    return nullptr;
}

std::string JsonDiagnosticConsumer::getCategory(const unsigned diagID) {
    const auto categoryID = clang::DiagnosticIDs::getCategoryNumberForDiag(diagID);
    return std::string(clang::DiagnosticIDs::getCategoryNameFromID(categoryID));
}

int JsonDiagnosticConsumer::getGroup(const unsigned diagID) {
    // TODO find group name and other info
    const auto group = clang::DiagnosticIDs::getGroupForDiag(diagID);
    return static_cast<int>(group.value());
}

ordered_json JsonDiagnosticConsumer::getDescriptionInfo(const clang::Diagnostic &info) {
    const auto engine = info.getDiags();

    llvm::SmallVector<char, 256> message{};
    info.FormatDiagnostic(message);

    auto descriptionInfo = ordered_json::object({
        {"message", std::string(message.data(), message.size())},
        {"format", engine->getDiagnosticIDs()->getDescription(info.getID())},
        {"args", ordered_json::array()}
    });

    // currently there is only support for diagnostics with up to 10 arguments (%0-%9).
    for (unsigned i = 0; i < info.getNumArgs(); i++) {
        switch (info.getArgKind(i)) {
            case clang::DiagnosticsEngine::ak_std_string: {
                descriptionInfo["args"].push_back({
                    {"kind", "std_string"},
                    {"string", info.getArgStdStr(i)}
                });
                break;
            }
            case clang::DiagnosticsEngine::ak_c_string: {
                descriptionInfo["args"].push_back({
                    {"kind", "c_string"},
                    {"string", info.getArgCStr(i)}
                });
                break;
            }
            case clang::DiagnosticsEngine::ak_sint: {
                descriptionInfo["args"].push_back({
                    {"kind", "sint"},
                    {"integer", info.getArgSInt(i)}
                });
                break;
            }
            case clang::DiagnosticsEngine::ak_uint: {
                descriptionInfo["args"].push_back({
                    {"kind", "uint"},
                    {"integer", info.getArgUInt(i)}
                });
                break;
            }
            case clang::DiagnosticsEngine::ak_tokenkind: {
                const auto tokenKind = static_cast<clang::tok::TokenKind>(info.getRawArg(i));

                descriptionInfo["args"].push_back({
                    {"kind", "token_kind"},
                    {"name", clang::tok::getTokenName(tokenKind)},
                    {"spelling", getTokenKindSpelling(tokenKind)},
                    {"category", getTokenCategory(tokenKind)}
                });
                break;
            }
            case clang::DiagnosticsEngine::ak_identifierinfo: {
                const auto identifierInfo = info.getArgIdentifier(i);

                descriptionInfo["args"].push_back({
                    {"kind", "identifier"},
                    {"name", identifierInfo->getName()},
                    {"isReserved", identifierInfo->isReserved(compilerInstance.getLangOpts())},
                    {"hadMacroDef", identifierInfo->hadMacroDefinition()},
                    {"hasMacroDef", identifierInfo->hasMacroDefinition()},
                    {"isFinalMacro", identifierInfo->isFinal()},
                    {"isModulesImport", identifierInfo->isModulesImport()},
                    {"isRestrictExpansion", identifierInfo->isRestrictExpansion()},
                    {"builtinID", identifierInfo->getBuiltinID()},
                    {"tokenID", {
                        {"name", clang::tok::getTokenName(identifierInfo->getTokenID())},
                        {"spelling", getTokenKindSpelling(identifierInfo->getTokenID())},
                        {"category", getTokenCategory(identifierInfo->getTokenID())}
                    }},
                    {"interestingIdentifier", interestingIdentifiers[identifierInfo->getInterestingIdentifierID()]},
                    {"isCppKeyword", identifierInfo->isCPlusPlusKeyword(compilerInstance.getLangOpts())},
                    {"isCppOperatorKeyword", identifierInfo->isCPlusPlusOperatorKeyword()}
                });
                break;
            }
            case clang::DiagnosticsEngine::ak_addrspace: {
                const auto langAs = static_cast<clang::LangAS>(info.getRawArg(i));
                auto addrSpace = clang::Qualifiers::getAddrSpaceAsString(langAs);

                if (addrSpace.empty()) {
                    addrSpace = compilerInstance.getASTContext().getLangOpts().OpenCL? "default" : "generic";
                }

                descriptionInfo["args"].push_back({
                    {"kind", "addr_space"},
                    {"langAddressSpace", addrSpace}
                });
                break;
            }
            case clang::DiagnosticsEngine::ak_qual: {
                auto qualifiers = clang::Qualifiers::fromOpaqueValue(info.getRawArg(i));

                descriptionInfo["args"].push_back({
                    {"kind", "qual"},
                    {"qual", QualTypeJsonConverter::convertQualifiersToJson(qualifiers)}
                });
                break;
            }
            case clang::DiagnosticsEngine::ak_qualtype: {
                auto qualType = clang::QualType::getFromOpaquePtr(reinterpret_cast<void *>(info.getRawArg(i)));

                descriptionInfo["args"].push_back({
                    {"kind", "qualtype"},
                    {"qualType", qualTypeJsonConverter.convertQualTypeToJson(qualType)}
                });
                break;
            }
            case clang::DiagnosticsEngine::ak_declarationname: {
                const auto declName = clang::DeclarationName::getFromOpaqueInteger(info.getRawArg(i));

                descriptionInfo["args"].push_back({
                    {"kind", "declaration_name"},
                    {"name", declName.getAsString()}
                });
                // llvm::errs() << "      declName: " << clang::DeclarationName::getFromOpaqueInteger(info.getRawArg(i)) << "\n";
                break;
            }
            case clang::DiagnosticsEngine::ak_nameddecl: {
                const auto *namedDecl = reinterpret_cast<const clang::NamedDecl *>(info.getRawArg(i));

                descriptionInfo["args"].push_back({
                    {"kind", "named_decl"},
                    {"idName", namedDecl->getName()},
                    {"readableName", namedDecl->getNameAsString()},
                    {"qualName", namedDecl->getQualifiedNameAsString()},
                    //{"declName", namedDecl->getDeclName()}
                });
                // llvm::errs() << "      declName: " << namedDecl->getDeclName() << "\n";
                // llvm::errs() << "      declKindName: " << namedDecl->getDeclKindName() << "\n";
                // llvm::errs() << "      nameForDiagnostic: ";
                // namedDecl->getNameForDiagnostic(llvm::errs(), compilerInstance.getASTContext().getPrintingPolicy(), true);
                // namedDecl->printName(llvm::errs(), compilerInstance.getASTContext().getPrintingPolicy());
                break;
            }
            case clang::DiagnosticsEngine::ak_nestednamespec: {
                // TODO
                descriptionInfo["args"].push_back({
                    {"kind", "nested_name_spec"},
                });
                break;
            }
            case clang::DiagnosticsEngine::ak_declcontext: {
                const auto *declContext = reinterpret_cast<clang::DeclContext *>(info.getRawArg(i));

                descriptionInfo["args"].push_back({
                    {"kind", "decl_context"},
                    {"declContext", getDeclContextInfo(declContext)}
                });
                break;
            }
            case clang::DiagnosticsEngine::ak_qualtype_pair: {
                // TODO
                descriptionInfo["args"].push_back({
                    {"kind", "qualtype_pair"},
                });
                break;
            }
            case clang::DiagnosticsEngine::ak_attr: {
                const auto *attr = reinterpret_cast<clang::Attr *>(info.getRawArg(i));

                descriptionInfo["args"].push_back({
                    {"kind", "attr"},
                    {"spelling", attr->getSpelling()}
                });
                break;
            }
        }
    }

    return descriptionInfo;
}

ordered_json JsonDiagnosticConsumer::getPresumedLocInfo(const clang::SourceLocation &sLoc, const clang::SourceManager &sManager) {
    const auto presumedLocation = sManager.getPresumedLoc(sLoc);

    if (presumedLocation.isInvalid()) {
        return nullptr;
    }

    std::stringstream ss;
    ss << presumedLocation.getFilename() << ':' << presumedLocation.getLine() << ":" << presumedLocation.getColumn();

    return ordered_json::object({
        {"line", presumedLocation.getLine()},
        {"column", presumedLocation.getColumn()},
        {"file", presumedLocation.getFilename()},
        {"path", ss.str()}
    });
}

ordered_json JsonDiagnosticConsumer::getLocationInfo(const clang::Diagnostic &info) const {
    const clang::SourceLocation sourceLoc = info.getLocation();
    const clang::SourceManager &sourceManager = info.getSourceManager();

    ordered_json locationInfo = ordered_json::object({
        {"type", sourceLoc.isInvalid()? "none" : sourceLoc.isFileID()? "file" : "macro"},
        {"presumedLoc", nullptr},
        {"expansionLoc", nullptr},
        {"spellingLocs", nullptr}
    });

    // Invalid SourceLocations are often used when events have no corresponding
    // location in the source (e.g. a diagnostic is required for a command line option)
    if (sourceLoc.isInvalid()) {
        return locationInfo;
    }

    if (sourceLoc.isFileID()) {
        if (auto presumedLocInfo  = getPresumedLocInfo(sourceLoc, sourceManager);
                !presumedLocInfo.is_null()) {
            presumedLocInfo["encompassingCode"] = clang::Lexer::getSourceText(
            sourceManager.getExpansionRange(sourceManager.getExpansionLoc(sourceLoc)),
                sourceManager,
                compilerInstance.getLangOpts());
            locationInfo["presumedLoc"] = presumedLocInfo;
        }
    } else { // macro ID
        if (auto expansionLocInfo  = getPresumedLocInfo(sourceManager.getExpansionLoc(sourceLoc), sourceManager);
                !expansionLocInfo.is_null()) {
            const auto source = clang::Lexer::getSourceText(
                sourceManager.getExpansionRange(sourceLoc),
                sourceManager,
                compilerInstance.getLangOpts());
            expansionLocInfo["encompassingCode"] = source;
            locationInfo["expansionLoc"] = expansionLocInfo;
        }

        locationInfo["spellingLocs"] = ordered_json::array();

        auto currImmediateCaller = sourceLoc;

        while (currImmediateCaller.isMacroID()) {
            const auto spellingLoc = sourceManager.getSpellingLoc(currImmediateCaller);
            auto spellingLocInfo = getPresumedLocInfo(spellingLoc, sourceManager);

            if (!spellingLocInfo.is_null()) {
                const auto source =
                    clang::Lexer::getSourceText(
                        sourceManager.getExpansionRange(spellingLoc),
                        sourceManager,
                        compilerInstance.getLangOpts());
                spellingLocInfo["encompassingCode"] = source;
            }

            locationInfo["spellingLocs"].insert(locationInfo["spellingLocs"].begin(), spellingLocInfo);

            currImmediateCaller = sourceManager.getImmediateMacroCallerLoc(currImmediateCaller);
        }
    }

    return locationInfo;
}

ordered_json JsonDiagnosticConsumer::getSourceRanges(const clang::Diagnostic &info) const {
    // Clang diags have a location (shown as ^) and 0 or more ranges (show as ~~~~).

    auto sourceRanges = ordered_json::array();
    const clang::SourceManager &sourceManager = info.getSourceManager();

    for (unsigned i = 0; i < info.getNumRanges(); i++) {
        const auto charSourceRange = info.getRange(i);

        // No available source code
        if (charSourceRange.isInvalid()) {
            continue;
        }

        sourceRanges.push_back(ordered_json::object({
            {"begin", getPresumedLocInfo(
                        charSourceRange.getAsRange().getBegin(), sourceManager)},
            {"end", getPresumedLocInfo(
                        charSourceRange.getAsRange().getEnd(), sourceManager)},
            {"encompassingCode", clang::Lexer::getSourceText(
                        charSourceRange, sourceManager, compilerInstance.getLangOpts())},
        }));
    }

    return sourceRanges;
}

ordered_json JsonDiagnosticConsumer::getCodeSnippet(const clang::Diagnostic &info) const {
    const clang::SourceLocation sourceLoc = info.getLocation();
    const clang::SourceManager &sourceManager = info.getSourceManager();

    if (sourceLoc.isInvalid() || sourceLoc.isMacroID()) {
        return nullptr;
    }

    const auto fileID = sourceManager.getFileID(sourceLoc);
    const unsigned offset = sourceManager.getFileOffset(sourceLoc);
    const unsigned line = sourceManager.getLineNumber(fileID, offset);

    const auto lineBegin = sourceManager.translateLineCol(fileID, line, 1);
    const auto lineEnd = sourceManager.translateLineCol(fileID, line + 1, 1).getLocWithOffset(-1);

    const auto codeSnippet = clang::Lexer::getSourceText(
        clang::CharSourceRange::getTokenRange(lineBegin, lineEnd),
        sourceManager, compilerInstance.getLangOpts());

    return std::string(codeSnippet);
}

ordered_json JsonDiagnosticConsumer::getTokenCategory(const clang::tok::TokenKind tokenKind) {
    if (clang::tok::getPunctuatorSpelling(tokenKind)) {
        return "punctuator";
    }

    if (clang::tok::getKeywordSpelling(tokenKind)) {
        return "keyword";
    }

    if (clang::tok::isAnyIdentifier(tokenKind)) {
        return "identifier";
    }

    if (clang::tok::isLiteral(tokenKind)) {
        return "literal";
    }

    if (clang::tok::isStringLiteral(tokenKind)) {
        return "string_literal";
    }

    if (clang::tok::isAnnotation(tokenKind)) {
        return "annotation";
    }

    if (clang::tok::isPragmaAnnotation(tokenKind)) {
        return "pragma_annotation";
    }

    return nullptr;
}

ordered_json JsonDiagnosticConsumer::getTokenKindSpelling(const clang::tok::TokenKind tokenKind) {
    if (const auto spelling = clang::tok::getPunctuatorSpelling(tokenKind)) {
        return spelling;
    }

    if (const auto spelling = clang::tok::getKeywordSpelling(tokenKind)) {
        return spelling;
    }

    if (clang::tok::isAnyIdentifier(tokenKind)) {
        return "identifier";
    }

    return nullptr;
}

ordered_json JsonDiagnosticConsumer::getDeclContextInfo(const clang::DeclContext *declContext) {
    if (declContext == nullptr) {
        return nullptr;
    }

    std::string declKind(declContext->getDeclKindName());

    std::for_each(declKind.begin(), declKind.end(), [](char &c) {
        c = std::tolower(c);
    });

    auto ret = ordered_json::object({
        {"kind", declKind},
    });

    if (declKind == "record") {
        const auto recordDecl = llvm::dyn_cast<clang::RecordDecl>(declContext);

        std::string tagKind = "unknown";

        switch (recordDecl->getTagKind()) {
            case clang::TTK_Struct: tagKind = "struct"; break;
            case clang::TTK_Union: tagKind = "union"; break;
            case clang::TTK_Class: tagKind = "class"; break;
            case clang::TTK_Interface: tagKind = "interface"; break;
            case clang::TTK_Enum: tagKind = "enum"; break;
        }

        ret["tagKind"] = tagKind;
        ret["name"] = recordDecl->getName();

    } else if (declKind == "enum") {
        const auto enumDecl = llvm::dyn_cast<clang::EnumDecl>(declContext);
        ret["tagKind"] = "enum";
        ret["name"] = enumDecl->getName();
    }

    // TODO add more info

    return ret;
}
