package pt.up.fe.specs.cmender.mending;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.lang.symbol.EnumSymbol;
import pt.up.fe.specs.cmender.lang.symbol.FunctionSymbol;
import pt.up.fe.specs.cmender.lang.symbol.RecordSymbol;
import pt.up.fe.specs.cmender.lang.symbol.Symbol;
import pt.up.fe.specs.cmender.lang.symbol.TypedefSymbol;
import pt.up.fe.specs.cmender.lang.symbol.VariableSymbol;

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

    private final Map<String, RecordSymbol> structs;

    private final Map<String, EnumSymbol> enums;

    public MendingTable() {
        variables = new HashMap<>();
        functions = new HashMap<>();
        typedefs = new HashMap<>();
        structs = new HashMap<>();
        enums = new HashMap<>();
    }

    public void add(VariableSymbol variable) {
        variables.put(variable.name(), variable);
    }

    public void add(FunctionSymbol function) {
        functions.put(function.name(), function);
    }

    public void add(TypedefSymbol typedef) {
        typedefs.put(typedef.name(), typedef);
    }

    public void add(RecordSymbol struct) {
        structs.put(struct.name(), struct);
    }

    // Functions and variables are not included in the dependencies because
    // they do not declare symbols that structs and typedefs use in their definitions
    public Map<Symbol, Set<Symbol>> buildDependencyGraph() {
        var graph = new HashMap<Symbol, Set<Symbol>>();

        var symbols = new ArrayList<Symbol>();
        symbols.addAll(typedefs.values());
        symbols.addAll(structs.values());
        symbols.addAll(enums.values());

        for (var symbol : symbols) {
            graph.put(symbol, new HashSet<>());

            for (var dep : symbol.getDirectDependencies(this)) {
                if (graph.containsKey(dep)) {
                    graph.get(dep).add(symbol);
                } else {
                    graph.put(dep, new HashSet<>(List.of(symbol)));
                }
            }
        }

        return graph;
    }

    public void writeSymbolDecls(Writer writer) throws IOException {
        var bufferedWriter = new BufferedWriter(writer);

        var depGraph = buildDependencyGraph();

        var structsForForwardDecl = getRequiredStructsForForwardDecl(depGraph);

        var orderedSymbols = topologicalSort(depGraph);

        if (orderedSymbols == null) {
            throw new IllegalStateException("Circular dependency detected");
        }

        writeDecls(bufferedWriter, structsForForwardDecl);
        writeDefs(bufferedWriter, orderedSymbols);
        writeDefs(bufferedWriter, new ArrayList<>(variables.values()));
        writeDefs(bufferedWriter, new ArrayList<>(functions.values()));

        // TODO not close here (because it's not opened here and we might not want to give
        //  the responsibility of closing to this method)
        bufferedWriter.close();
    }

    // Circular dependencies are allowed when a struct includes a pointer to itself or when
    // two structs include pointers to each other (mutual recursion with ptrs to structs)
    // In other words, with pointers, we don't require full definitions of structs (and thus no forward declarations)


    // Forward declarations are required sometimes when two declarations are legal

    // TODO all of this needs to be well researched. find exactly what declarations are illegal and when forward declarations are required (of structs and typedefs)
    private List<Symbol> getRequiredStructsForForwardDecl(Map<Symbol, Set<Symbol>> depGraph) throws IOException {
        var structsForForwardDecl = new ArrayList<Symbol>();

        for (var symbol : depGraph.keySet()) {
            /*if (symbol instanceof Struct && depGraph.get(symbol).contains(symbol)) {
                structsForForwardDecl.add(symbol);
            }*/
        }

        return structsForForwardDecl;
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
        }
    }

    private void writeDefs(BufferedWriter writer, List<Symbol> symbols) throws IOException {
        for (var symbol : symbols) {
            writer.write(symbol.asDefinitionString());
            writer.newLine();
        }
    }
}
