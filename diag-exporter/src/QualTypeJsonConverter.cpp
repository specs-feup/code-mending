#include "QualTypeJsonConverter.h"

#include <sstream>
#include <clang/Frontend/CompilerInstance.h>

const std::string QualTypeJsonConverter::builtinTypesTable[clang::BuiltinType::LastKind+2] = {
    // OpenCL image types
#define IMAGE_TYPE(ImgType, Id, SingletonId, Access, Suffix) #Id,
#include "clang/Basic/OpenCLImageTypes.def"
    // OpenCL extension types
#define EXT_OPAQUE_TYPE(ExtType, Id, Ext) #Id,
#include "clang/Basic/OpenCLExtensionTypes.def"
    // SVE Types
#define SVE_TYPE(Name, Id, SingletonId) #Id,
#include "clang/Basic/AArch64SVEACLETypes.def"
    // PPC MMA Types
#define PPC_VECTOR_TYPE(Name, Id, Size) #Id,
#include "clang/Basic/PPCTypes.def"
    // RVV Types
#define RVV_TYPE(Name, Id, SingletonId) #Id,
#include "clang/Basic/RISCVVTypes.def"
    // WebAssembly reference types
#define WASM_TYPE(Name, Id, SingletonId) #Id,
#include "clang/Basic/WebAssemblyReferenceTypes.def"
    // All other builtin types
#define BUILTIN_TYPE(Id, SingletonId) #Id,
#define LAST_BUILTIN_TYPE(Id) #Id,
#include "clang/AST/BuiltinTypes.def"
};

const ordered_json *QualTypeJsonConverter::lookupTrackedTypes(const void *addr) const {
    if (const auto trackedType = trackedTypes.find(addr);
        trackedType != trackedTypes.end()) {
        return &(trackedType->second);
    }

    return nullptr;
}

const ordered_json *QualTypeJsonConverter::lookupTrackedTypes(const clang::QualType &qualType) const {
    return lookupTrackedTypes(qualType.getAsOpaquePtr());
}

std::string QualTypeJsonConverter::getTypeUsageInDecls(const clang::QualType &qualType) const {
    const clang::IdentifierInfo &identiferInfo = context.Idents.get("diag_exporter_id"); // TODO specify as a program argument
    clang::TranslationUnitDecl *tuDecl = context.getTranslationUnitDecl();

    const clang::VarDecl *varDecl = clang::VarDecl::Create(
        const_cast<clang::ASTContext&>(context),
        tuDecl,
        clang::SourceLocation(),
        clang::SourceLocation(),
        &identiferInfo,
        qualType,
        nullptr,
        clang::SC_None);

    //clang::LangOptions langOpts;
    //clang::PrintingPolicy policy(langOpts);
    std::string declString;
    llvm::raw_string_ostream os(declString);
    //varDecl->print(os, policy);
    varDecl->print(os, context.getPrintingPolicy());
    //llvm::errs() << os.str() << "\n";
    return declString;
}

ordered_json QualTypeJsonConverter::Visit(const clang::Type *type) {
    if (const auto trackedType = lookupTrackedTypes(type); trackedType != nullptr) {
        return *trackedType;
    }

    const auto typeJson = TypeVisitor::Visit(type);

    trackedTypes[type] = typeJson;

    return typeJson;
}

ordered_json QualTypeJsonConverter::VisitType(const clang::Type *type) {
    return nullptr;
}

ordered_json QualTypeJsonConverter::VisitBuiltinType(const clang::BuiltinType *type) {
    return ordered_json::object({
        {"class", "builtin"},
        {"builtinKind", builtinTypesTable[static_cast<std::underlying_type_t<clang::BuiltinType::Kind>>(type->getKind())]},
        {"name", type->getName(context.getPrintingPolicy())}
    });
}

ordered_json QualTypeJsonConverter::VisitPointerType(const clang::PointerType *type) {
    return ordered_json::object({
        {"class", "pointer"},
        {"pointeeType", convertQualTypeToJson(type->getPointeeType())}
    });
}

ordered_json QualTypeJsonConverter::VisitConstantArrayType(const clang::ConstantArrayType *type) {
    return ordered_json::object({
        {"class", "array"},
        {"arrayKind", "constant"},
        {"size", type->getSize().getZExtValue()},
        {"elementType", convertQualTypeToJson(type->getElementType())}
    });
}

ordered_json QualTypeJsonConverter::VisitVariableArrayType(const clang::VariableArrayType *type) {
    return ordered_json::object({
        {"class", "array"},
        {"arrayKind", "variable"},
        {"sizeExpr", nullptr}, // TODO (so far we are not serializing expressions) limitation of the current implementation
        // besides, for the purpose of the CMender, we never declare a variable array size because VLAs are only supported outside
        // of file scope/level and if the symbol is missing, it must be a global variable
        {"elementType", convertQualTypeToJson(type->getElementType())}
    });
}

ordered_json QualTypeJsonConverter::VisitIncompleteArrayType(const clang::IncompleteArrayType *type) {
    return ordered_json::object({
        {"class", "array"},
        {"arrayKind", "incomplete"},
        {"elementType", convertQualTypeToJson(type->getElementType())}
    });
}

ordered_json QualTypeJsonConverter::VisitRecordType(const clang::RecordType *type) {
    return ordered_json::object({
        {"class", "record"},
        {"recordKind", "struct"}, // TODO (so far we are not serializing the record kind) limitation of the current implementation
        {"name", type->getDecl()->getNameAsString()}
    });
}

ordered_json QualTypeJsonConverter::VisitEnumType(const clang::EnumType *type) {
    return ordered_json::object({
        {"class", "enum"},
        {"name", type->getDecl()->getNameAsString()}
    });
}

ordered_json QualTypeJsonConverter::VisitFunctionProtoType(const clang::FunctionProtoType *type) {
    ordered_json typeJson = ordered_json::object();

    typeJson["class"] = "function";
    typeJson["functionKind"] = "proto";
    typeJson["returnType"] = convertQualTypeToJson(type->getReturnType());

    ordered_json paramTypes = ordered_json::array();
    for (const auto paramType : type->param_types()) {
        paramTypes.push_back(convertQualTypeToJson(paramType));
    }
    typeJson["paramTypes"] = paramTypes;

    return typeJson;
}

ordered_json QualTypeJsonConverter::VisitFunctionNoProtoType(const clang::FunctionNoProtoType *type) {
    return ordered_json::object({
        {"class", "function"},
        {"functionKind", "no_proto"},
        {"returnType", convertQualTypeToJson(type->getReturnType())}
    });
}

ordered_json QualTypeJsonConverter::convertQualTypeToJson(const clang::QualType &qualType) {
    // TODO currently we convert a qualtype to a canonical qualtype. this simplifies the serialization process
    //    should we keep this or should we serialize the qualtype as is?

    // if doesn't point to a type yet
    if (qualType.isNull()) {
        return nullptr;
    }

    const auto canonicalQualType = qualType.getCanonicalType();

    if (const auto trackedType = lookupTrackedTypes(canonicalQualType); trackedType != nullptr) {
        return ordered_json::object({
            {"typeAsString", qualType.getAsString()},
            {"canonicalTypeAsString", canonicalQualType.getAsString()},
            {"typeUsageInDecls", getTypeUsageInDecls(canonicalQualType)},
            {"qual", convertQualifiersToJson(canonicalQualType.getQualifiers())},
            {"type", *trackedType},
            {"langAddressSpace", convertAddressSpaceToJson(canonicalQualType.getAddressSpace())}
        });
    }

    return ordered_json::object({
        {"typeAsString", qualType.getAsString()},
        {"canonicalTypeAsString", canonicalQualType.getAsString()},
        {"typeUsageInDecls", getTypeUsageInDecls(canonicalQualType)},
        {"qual", convertQualifiersToJson(canonicalQualType.getQualifiers())},
        {"type", Visit(canonicalQualType.getLocalUnqualifiedType().getTypePtr())},
        {"langAddressSpace", convertAddressSpaceToJson(canonicalQualType.getAddressSpace())}
    });
}

ordered_json QualTypeJsonConverter::convertQualifiersToJson(const clang::Qualifiers &qualifiers) {
    return {
        {"spelling", qualifiers.getAsString()},
        {"hasQualifiers", qualifiers.hasQualifiers()},
        {"hasConst", qualifiers.hasConst()},
        {"hasVolatile", qualifiers.hasVolatile()},
        {"hasRestrict", qualifiers.hasRestrict()},
        {"hasUnaligned", qualifiers.hasUnaligned()}
    };
}

ordered_json QualTypeJsonConverter::convertAddressSpaceToJson(clang::LangAS addressSpace) {
    std::string addrspaceString;

    switch (addressSpace) {
        case clang::LangAS::Default: addrspaceString = "none"; break;
        case clang::LangAS::opencl_global: addrspaceString = "opencl_global"; break;
        case clang::LangAS::opencl_local: addrspaceString = "opencl_local"; break;
        case clang::LangAS::opencl_constant: addrspaceString = "opencl_constant"; break;
        case clang::LangAS::opencl_generic: addrspaceString = "opencl_generic"; break;
        case clang::LangAS::opencl_private: addrspaceString = "opencl_private"; break;
        case clang::LangAS::cuda_constant: addrspaceString = "cuda_constant"; break;
        case clang::LangAS::cuda_device: addrspaceString = "cuda_device"; break;
        case clang::LangAS::cuda_shared: addrspaceString = "cuda_shared"; break;
        default: addrspaceString = "other";
    }

    return ordered_json::object({
        {"addressSpace", addrspaceString},
        {"targetAddressSpace", isTargetAddressSpace(addressSpace) ? toTargetAddressSpace(addressSpace) : 0}
    });
}
