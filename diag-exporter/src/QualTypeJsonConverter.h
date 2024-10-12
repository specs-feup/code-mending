#ifndef QUALTYPEJSONSCONVERTER_H
#define QUALTYPEJSONSCONVERTER_H

#include <clang/AST/Type.h>
#include <clang/AST/ASTContext.h>
#include <clang/AST/TypeVisitor.h>

#include <nlohmann/json.hpp>

#include <map>

using ordered_json = nlohmann::ordered_json;

class QualTypeJsonConverter : public clang::TypeVisitor<QualTypeJsonConverter, ordered_json> {
private:
    const clang::ASTContext &context;

    std::map<const void *, ordered_json> trackedTypes;

    static const std::string builtinTypesTable[clang::BuiltinType::LastKind+2];

    /***************************************************************************/

    // returns nullptr if not tracked (not already visited)
    const ordered_json *lookupTrackedTypes(const void *addr) const;

    const ordered_json *lookupTrackedTypes(const clang::QualType &qualType) const;

    std::string getTypeUsageInDecls(const clang::QualType &qualType) const;

public:
    explicit QualTypeJsonConverter(const clang::ASTContext &context): context(context) {}

    ordered_json convertQualTypeToJson(const clang::QualType &qualType);

    static ordered_json convertQualifiersToJson(const clang::Qualifiers &qualifiers);

    static ordered_json convertAddressSpaceToJson(clang::LangAS addressSpace);

    // Visit methods
    ordered_json Visit(const clang::Type *type);

    ordered_json VisitType(const clang::Type *type);

    ordered_json VisitBuiltinType(const clang::BuiltinType *type);

    ordered_json VisitPointerType(const clang::PointerType *type);

    ordered_json VisitConstantArrayType(const clang::ConstantArrayType *type);

    ordered_json VisitVariableArrayType(const clang::VariableArrayType *type);

    ordered_json VisitIncompleteArrayType(const clang::IncompleteArrayType *type);

    ordered_json VisitRecordType(const clang::RecordType *type);

    ordered_json VisitEnumType(const clang::EnumType *type);

    ordered_json VisitFunctionProtoType(const clang::FunctionProtoType *type);

    ordered_json VisitFunctionNoProtoType(const clang::FunctionNoProtoType *type);
};

#endif //QUALTYPEJSONSCONVERTER_H
