package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import pt.up.fe.specs.cmender.lang.type.BuiltinType;
import pt.up.fe.specs.cmender.lang.type.QualType;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@ToString
@Accessors(fluent = true)
public class FunctionSymbol extends Symbol {

    private QualType returnType;

    private List<Parameter> parameters;

    public FunctionSymbol(String name, QualType returnType, List<Parameter> parameters) {
        super(name);
        this.returnType = returnType;
        this.parameters = new ArrayList<>(parameters);
    }

    public FunctionSymbol(String name, QualType returnType) {
        this(name, returnType, new ArrayList<>());
    }

    public FunctionSymbol(String name) {
        this(name, new QualType(
                "void",
                "void",
                "void diag_exporter_id",
                null, // TODO implement this
                new BuiltinType(BuiltinType.BuiltinKind.VOID, "void"),
                null));
    }

    public void setReturnType(QualType returnType) {
        this.returnType = returnType;
    }

    public void addParameter(Parameter parameter) {
        this.parameters.add(parameter);
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = new ArrayList<>(parameters);
    }

    @Getter
    @ToString
    @Accessors(fluent = true)
    public static class Parameter extends Symbol {

        private QualType qualType;

        public Parameter(String name, QualType qualType) {
            super(name);
            this.qualType = qualType;
        }

        public void setType(QualType qualType) {
            this.qualType = qualType;
        }

        @Override
        public String asDeclarationString() {
            return asDefinitionString();
        }

        @Override
        public String asDefinitionString() {
            return qualType.substituteTypeUsageWithId(name);
        }

        @Override
        public Set<Symbol> getDirectDependencies(MendingTable table) {
            var dependencies = new ArrayList<Symbol>();
            qualType.addDirectDependencies(dependencies, table);
            return new HashSet<>(dependencies);
        }

        @Override
        public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
            qualType.addDirectDependencies(dependencies, table);
        }
    }

    @Override
    public String asDeclarationString() {
        // TODO space between return type and name

        //  function returning a function ptr syntax:
        //      int (*func(char op))(int, int);

        var usageReplacement = name + "(" +
                parameters.stream()
                        .map(Parameter::asDefinitionString)
                        .collect(Collectors.joining(",")) +
                ")";

        return returnType.substituteTypeUsageWithId(usageReplacement) + ";";
        /*return returnType.canonicalTypeAsString() + " " + name + "(" +
                parameters.stream()
                        .map(Parameter::asDefinitionString)
                        .collect(Collectors.joining(",")) +
                ");";*/
    }

    @Override
    public String asDefinitionString() {
        var body = getBody();

        //  function returning a function ptr syntax:
        //      int (*func(char op))(int, int);

        var usageReplacement = name + "(" +
                parameters.stream()
                        .map(Parameter::asDefinitionString)
                        .collect(Collectors.joining(",")) +
                ")";

        return returnType.substituteTypeUsageWithId(usageReplacement)
                + " " + (body.isEmpty() ? "{}" : "{\n" + body + "\n}");

        /*return returnType.canonicalTypeAsString() + " " + name + "(" +
                parameters.stream()
                        .map(Parameter::asDefinitionString)
                        .collect(Collectors.joining(",")) +
                ") " + (body.isEmpty() ? "{}" : "{\n" + body + "\n}");*/
    }

    // TODO dont forget that we can also not have a return statement at all
    // Returning arrays should not be supported
    //    they decay to pointer to a stack-allocated mem region)
    private String getBody() {
        if (returnType.type().isIntegralType()) {
            return "return 0;";
        }

        if (returnType.type().isFloatingPointType()) {
            return "return 0.0;";
        }

        if (returnType.type().isBooleanType()) {
            return "return 1;";
        }

        if (returnType.type().isVoid()) {
            return "";
        }

        // if the return type is not a void* this still works because of casting
        if (returnType.type().isPointerType()) {
            return "return (void *)0;";
        }

        var ret = new VariableSymbol("ret", returnType);

        return ret.asDefinitionString() + "\n" + "return ret;";
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        var dependencies = new ArrayList<Symbol>();

        returnType.addDirectDependencies(dependencies, table);
        parameters.forEach(p -> p.addDirectDependencies(dependencies, table));

        return new HashSet<>(dependencies);
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        returnType.addDirectDependencies(dependencies, table);
        parameters.forEach(p -> p.addDirectDependencies(dependencies, table));
    }
}
