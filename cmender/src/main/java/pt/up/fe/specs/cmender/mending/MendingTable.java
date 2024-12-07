package pt.up.fe.specs.cmender.mending;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.diag.Diagnostic;
import pt.up.fe.specs.cmender.lang.symbol.EnumSymbol;
import pt.up.fe.specs.cmender.lang.symbol.FunctionSymbol;
import pt.up.fe.specs.cmender.lang.symbol.RecordSymbol;
import pt.up.fe.specs.cmender.lang.symbol.Symbol;
import pt.up.fe.specs.cmender.lang.symbol.TypedefSymbol;
import pt.up.fe.specs.cmender.lang.symbol.VariableSymbol;
import pt.up.fe.specs.cmender.lang.type.TagTypeKind;
import pt.up.fe.specs.cmender.lang.type.TypeName;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@ToString
@Accessors(fluent = true)
public class MendingTable {

    private final Map<String, VariableSymbol> variables;

    private final Map<String, FunctionSymbol> functions;

    private final Map<String, TypedefSymbol> typedefs;

    private final Map<String, RecordSymbol> records;

    private final Map<String, EnumSymbol> enums;

    // TODO is this necessary? we can just use the typedefs map to change the aliased type
    private final Map<String, Symbol> typeNameToCorrespondingSymbol;

    private final Map<String, Symbol> arraySubscriptToCorrespondingSymbol;

    private final List<Diagnostic> handledDiagnostics;

    private long fileSize;

    private final List<DiagnosticShortInfo> unknownDiags;

    public MendingTable() {
        variables = new HashMap<>();
        functions = new HashMap<>();
        typedefs = new HashMap<>();
        records = new HashMap<>();
        enums = new HashMap<>();
        typeNameToCorrespondingSymbol = new HashMap<>();
        arraySubscriptToCorrespondingSymbol = new HashMap<>();
        handledDiagnostics = new ArrayList<>();
        unknownDiags = new ArrayList<>();
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void addUnknownDiag(DiagnosticShortInfo diag) {
        unknownDiags.add(diag);
    }

    public void put(VariableSymbol variable) {
        variables.put(variable.name(), variable);
    }

    public void put(FunctionSymbol function) {
        functions.put(function.name(), function);
    }

    public void put(TypedefSymbol typedef) {
        typedefs.put(typedef.name(), typedef);
    }

    public void put(RecordSymbol record) {
        records.put(record.name(), record);
    }

    public void put(EnumSymbol enumSymbol) {
        enums.put(enumSymbol.name(), enumSymbol);
    }

    public void putControlledTypedefAliasMapping(String typeName, Symbol symbol) {
        typeNameToCorrespondingSymbol.put(typeName, symbol);
    }

    public void putControlledArraySubscriptMapping(String arraySubscript, Symbol symbol) {
        arraySubscriptToCorrespondingSymbol.put(arraySubscript, symbol);
    }

    // 1) CONTROLLED TYPE SYMBOLS: symbols representing types that are used in the code but ARE NOT FULLY DEFINED.
    //     As such, they need to be defined by this tool, which has the liberty to change their definitions if necessary.
    //     They can be of two categories:
    //       a) tags (i.e., structs, enums, and unions) that ARE NOT FULLY DEFINED but are used in the code.
    //       b) typedef aliases that ARE NOT FULLY TYPE ALIASED but are used in the code.

    // 2) CONTROLLED TAG SYMBOLS can be of two types:
    //      a) NAMED in the code. We know what they are, and how their forward declaration looks like in the code.
    //         They appear on non-type symbol declarations present in the code.
    //              e.g., in 'struct A a;' where 'struct A' is not fully defined in the code, but we know it is a struct type.
    //      b) UNNAMED in the code. We know they exist and are used, but we don't know what they are, and how their forward declaration looks like in the code.
    //          They appear on missing non-type declarations in the code.
    //              e.g., in 'a = b.memb' where 'b' is not declared but can be deduced that it is a variable whose type is a struct and 'memb' is a member of it.

    // 3) CONTROLLED TYPEDEF ALIAS SYMBOLS:
    //     Don't need the NAMED/UNNAMED distinction because they are always referred to by their alias name in the code. For missing variables,
    //       functions, and struct members, when not declared, we can't distinguish that those symbols' types are indeed defined as typedef type aliases
    //       or just tag types.
    //     We only care what the name of the alias is used in the code but not defined, and what the underlying type is.

    // 4) UNCONTROLLED TYPE SYMBOLS: symbols representing types that ARE FULLY DEFINED in the code.
    //     As such, they don't need to be defined or changed by this tool (nor can't), and their definitions are the utmost source of truth.
    //     Since we deal only with canonical types, we don't need to worry about aliases

    // 5) Dealing with missing types:
    //     a) All CONTROLLED and UNNAMED TAG SYMBOLS will first receive a generated tag type name that is unique (e.g., cmender_struct_type_0),
    //



    //  ) All controlled and UNNAMED tag types (and their derived types) will have a generated typedef alias name that is unique, where
    //       the underlying type is aliased to this name, and able to be changed if necessary.

    //  ) Controlled NAMED tags have significant of source of truth in the code, more so than the UNNAMED ones. This is true because if a non-type
    //       symbol is declared in the code, and its type is fully named in the code, we can't really change the tag to another thing, only change its definition.
    //       For unnamed tags, we can replace them by changing the underlying type of the typedef alias.
    //       The same applies to non generated typedef aliases, as their name should be constant, but its underlying type can be changed.



      //  ) Since they are declared in the code (or derived types which dont have that concept), we don't need to generate a name for them, nor change them
    //        (e.g., by changing the underlying type of a typedef alias or add members to a tag type, or change the derived type).

    //  )  undeclared symbols should converge to these types



    // TODO think about derived types that mix controlled and uncontrolled tags and typedefs

    // INSIGHT we can assume that if we don't find symbol with a lookup based on the typeName, then it's uncontrolled
    //    because by the time we reach this point then the controlled types should have been added to the table
    //    by the corresponding handlers and then can we use these lookups to determine if a type is controlled or not
    //    on the handlers that change the types of the symbols

    public boolean isControlled(TypeName typeName) {
        return isControlledTagType(typeName) || isControlledTypedefAlias(typeName);
    }

    public boolean isControlledTagType(TypeName typeName) {
        if (!typeName.isTag()) {
            return false;
        }

        if (typeName.tagKind() == TagTypeKind.STRUCT || typeName.tagKind() == TagTypeKind.UNION) {
            return records.containsKey(typeName.identifier());
        }

        if (typeName.tagKind() == TagTypeKind.ENUM) {
            return enums.containsKey(typeName.identifier());
        }

        return false;
    }

    public boolean isControlledTypedefAlias(TypeName typeName) {
        return typeName.isTypedefAlias() && typedefs.containsKey(typeName.identifier());
    }

    public boolean isControlledAndNamedTagType(TypeName typeName) {
        return isControlledTagType(typeName) && !MendingTypeNameGenerator.isGeneratedTagTypeName(typeName.identifier());
    }

    public boolean isControlledAndUnnamedTagType(TypeName typeName) {
        return isControlledTagType(typeName) && MendingTypeNameGenerator.isGeneratedTagTypeName(typeName.identifier());
    }

    public boolean isControlledAndGeneratedTypedefAlias(TypeName typeName) {
        return isControlledTypedefAlias(typeName) && MendingTypeNameGenerator.isGeneratedTypedefAliasName(typeName.identifier());
    }

    public boolean isControlledAndNotGeneratedTypedefAlias(TypeName typeName) {
        return isControlledTypedefAlias(typeName) && !MendingTypeNameGenerator.isGeneratedTypedefAliasName(typeName.identifier());
    }

    public boolean isUncontrolled(TypeName typeName) {
        // TODO Should we consider builtin types as uncontrolled? from the definition above it doesn't include
        //     but it might be useful because they have equal source of truth
        if (typeName.isBuiltin()) {
            return true;
        }

        if (typeName.isTag()) {
            return isUncontrolledTagType(typeName);
        }

        return isUncontrolledTypedefAlias(typeName);
    }

    public boolean isUncontrolledTagType(TypeName typeName) {
        if (!typeName.isTag()) {
            return false;
        }

        if (typeName.tagKind() == TagTypeKind.STRUCT || typeName.tagKind() == TagTypeKind.UNION) {
            return !records.containsKey(typeName.identifier());
        }

        if (typeName.tagKind() == TagTypeKind.ENUM) {
            return !enums.containsKey(typeName.identifier());
        }

        return false;
    }

    public boolean isUncontrolledTypedefAlias(TypeName typeName) {
        return typeName.isTypedefAlias() && !typedefs.containsKey(typeName.identifier());
    }

    // TODO we will also require symbols here which are already declared in the code
    //   Because if our controlled types depend on those we will need to forward declare them first
    //   since the include of the mendfile will be expanded at the top of the file and will not be able
    //   to see the declarations
    // Functions and variables are not included in the dependencies because
    // they do not declare symbols that tags and typedefs use in their definitions
    public Map<Symbol, Set<Symbol>> buildDependencyGraph() {
        var graph = new HashMap<Symbol, Set<Symbol>>();

        var symbols = new ArrayList<Symbol>();
        symbols.addAll(typedefs.values());
        symbols.addAll(records.values());
        symbols.addAll(enums.values());

        for (var symbol : symbols) {
            graph.put(symbol, new HashSet<>());
        }

        for (var symbol : symbols) {
            for (var dep : symbol.getDirectDependencies(this)) {
                graph.get(dep).add(symbol);
            }
        }

        return graph;
    }

    public void writeSymbolDecls(Writer writer) throws IOException {
        var bufferedWriter = new BufferedWriter(writer);

        var depGraph = buildDependencyGraph();

        var recordsForForwardDecl = getRequiredrecordsForForwardDecl(depGraph);

        var orderedSymbols = topologicalSort(depGraph);

        if (orderedSymbols == null) {
            throw new MendingEngineFatalException(MendingEngineFatalException.FatalType.MENDFILE_WRITER,
                    "Circular dependency detected", 0); // TODO iteration
            //throw new IllegalStateException("Circular dependency detected");
        }

        writeDecls(bufferedWriter, recordsForForwardDecl);
        writeDefs(bufferedWriter, orderedSymbols);
        writeDefs(bufferedWriter, new ArrayList<>(variables.values()));
        writeDefs(bufferedWriter, new ArrayList<>(functions.values()));

        // TODO not close here (because it's not opened here and we might not want to give
        //  the responsibility of closing to this method)
        bufferedWriter.close();
    }

    // Circular dependencies are allowed when a struct includes a pointer to itself or when
    // two records include pointers to each other (mutual recursion with ptrs to records)
    // In other words, with pointers, we don't require full definitions of records (and thus no forward declarations)


    // Forward declarations are required sometimes when two declarations are legal

    // TODO all of this needs to be well researched. find exactly what declarations are illegal and when forward declarations are required (of records and typedefs)
    private List<Symbol> getRequiredrecordsForForwardDecl(Map<Symbol, Set<Symbol>> depGraph) throws IOException {
        var recordsForForwardDecl = new ArrayList<Symbol>();

        for (var symbol : depGraph.keySet()) {
            /*if (symbol instanceof Struct && depGraph.get(symbol).contains(symbol)) {
                recordsForForwardDecl.add(symbol);
            }*/
        }

        return recordsForForwardDecl;
    }

    private List<Symbol> topologicalSort(Map<Symbol, Set<Symbol>> depGraph) {
        var ordering = new ArrayList<Symbol>();

        var permanentlyVisited = new HashSet<Symbol>();
        var temporarilyVisited = new HashSet<Symbol>();

        for (var symbol : depGraph.keySet()) {
            if (!visit(symbol, depGraph, permanentlyVisited, temporarilyVisited, ordering)) {
                return null;
            }
        }

        return ordering;
    }

    private boolean visit(Symbol symbol, Map<Symbol, Set<Symbol>> depGraph,
                       Set<Symbol> permanentlyVisited, Set<Symbol> temporarilyVisited,
                       List<Symbol> ordering) {
        if (permanentlyVisited.contains(symbol)) {
            return true;
        }

        if (temporarilyVisited.contains(symbol)) {
            return false;
        }

        temporarilyVisited.add(symbol);

        for (var dep : depGraph.get(symbol)) {
            if (symbol instanceof TypedefSymbol && dep instanceof TypedefSymbol && symbol.name().equals(dep.name())) {
                continue;
            }

            /*if (symbol instanceof Struct && depGraph.get(symbol).contains(symbol)) {
                continue;
            }*/

            if (!visit(dep, depGraph, permanentlyVisited, temporarilyVisited, ordering)) {
                return false;
            }
        }

        permanentlyVisited.add(symbol);

        ordering.addFirst(symbol);

        return true;
    }

    private void writeDecls(BufferedWriter writer, List<Symbol> symbols) throws IOException {
        for (var symbol : symbols) {
            writer.write(symbol.asDeclarationString());
            writer.newLine();
            writer.newLine();
        }
    }

    private void writeDefs(BufferedWriter writer, List<Symbol> symbols) throws IOException {
        for (var symbol : symbols) {
            writer.write(symbol.asDefinitionString());
            writer.newLine();
            writer.newLine();
        }
    }
}
