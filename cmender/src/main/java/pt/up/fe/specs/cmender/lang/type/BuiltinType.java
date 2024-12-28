package pt.up.fe.specs.cmender.lang.type;

import com.fasterxml.jackson.annotation.JsonCreator;

import pt.up.fe.specs.cmender.lang.symbol.Symbol;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record BuiltinType(
        BuiltinKind builtinKind,
        String name
) implements Type {

    public enum BuiltinKind {
        VOID,
        UCHAR,
        CHAR,
        USHORT,
        SHORT,
        UINT,
        INT,
        ULONG,
        LONG,
        ULONGLONG,
        LONG_LONG,
        FLOAT,
        DOUBLE,
        LONG_DOUBLE,
        _BOOL,
        DEPENDENT, // not used. this is needed because Clang is giving bogus C++ diagnostics
        OVERLOAD; // not used. this is needed because Clang is giving bogus C++ diagnostics

        @JsonCreator
        public static BuiltinKind toBuiltinKind(String kind) {
            return switch (kind) {
                case "Void" -> VOID;
                case "Char_U", "UChar" -> UCHAR;
                case "Char_S", "Char", "SChar" -> CHAR;
                case "UShort" -> USHORT;
                case "Short"-> SHORT;
                case "UInt" -> UINT;
                case "Int" -> INT;
                case "ULong" -> ULONG;
                case "Long" -> LONG;
                case "ULongLong" -> ULONGLONG;
                case "LongLong" -> LONG_LONG;
                case "Float" -> FLOAT;
                case "Double" -> DOUBLE;
                case "LongDouble" -> LONG_DOUBLE;
                case "Bool" -> _BOOL;
                case "Dependent" -> DEPENDENT;
                case "Overload" -> OVERLOAD;
                default -> throw new IllegalArgumentException("Not supported builtin type: " + kind);
            };
        }
    }

    @Override
    public TypeKind kind() {
        return TypeKind.BUILTIN;
    }

    @Override
    public boolean isBuiltinType() {
        return true;
    }

    @Override
    public boolean isVoid() {
        return builtinKind == BuiltinKind.VOID;
    }

    @Override
    public boolean isNumericType() {
        return isIntegralType() || isFloatingPointType();
    }

    @Override
    public boolean isIntegralType() {
        return builtinKind == BuiltinKind.CHAR || builtinKind == BuiltinKind.SHORT
                || builtinKind == BuiltinKind.INT || builtinKind == BuiltinKind.LONG ||
                builtinKind == BuiltinKind.LONG_LONG || builtinKind == BuiltinKind.UCHAR ||
                builtinKind == BuiltinKind.USHORT || builtinKind == BuiltinKind.UINT ||
                builtinKind == BuiltinKind.ULONG || builtinKind == BuiltinKind.ULONGLONG;
    }

    @Override
    public boolean isFloatingPointType() {
        return builtinKind == BuiltinKind.FLOAT || builtinKind == BuiltinKind.DOUBLE
                || builtinKind == BuiltinKind.LONG_DOUBLE;
    }

    @Override
    public boolean isBooleanType() {
        return builtinKind == BuiltinKind._BOOL;
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        return new HashSet<>();
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        // do nothing: no dependencies (these are mostly keywords)
    }
}
