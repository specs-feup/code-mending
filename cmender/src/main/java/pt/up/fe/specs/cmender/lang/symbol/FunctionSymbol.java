package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.lang.type.QualType;
import pt.up.fe.specs.cmender.lang.type.TypedefType;
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

    private final TypedefType returnType;

    private List<Parameter> parameters;

    public FunctionSymbol(String name, TypedefType returnType, List<Parameter> parameters) {
        super(name);
        this.returnType = returnType;
        this.parameters = new ArrayList<>(parameters);
    }

    public FunctionSymbol(String name, TypedefType returnType) {
        this(name, returnType, new ArrayList<>());
    }

    /*
    public FunctionSymbol(String name) {
        this(name, new QualType(
                "void",
                "void",
                "void diag_exporter_id",
                null, // TODO implement this
                new BuiltinType(BuiltinType.BuiltinKind.VOID, "void"),
                null));
    }
    */

    public void setReturnType(QualType returnType) {
        this.returnType.setAliasedType(returnType);
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

        private final TypedefType type;

        public Parameter(String name, TypedefType type) {
            super(name);
            this.type = type;
        }

        public void setType(QualType qualType) {
            type.setAliasedType(qualType);
        }

        @Override
        public String asDeclarationString() {
            return asDefinitionString();
        }

        @Override
        public String asDefinitionString() {
            return type.aliasedType().substituteTypeUsageId(name);
        }

        @Override
        public Set<Symbol> getDirectDependencies(MendingTable table) {
            var dependencies = new ArrayList<Symbol>();
            type.addDirectDependencies(dependencies, table);
            return new HashSet<>(dependencies);
        }

        @Override
        public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
            type.addDirectDependencies(dependencies, table);
        }
    }

    @Override
    public String asDeclarationString() {
        // TODO space between return type and name

        //  function returning a function ptr syntax:
        //      int (*func(char op))(int, int);

        return returnType.name() + " " + name + "(" +
                parameters.stream()
                        .map(Parameter::asDefinitionString)
                        .collect(Collectors.joining(",")) +
                ");";
        /*

        var usageReplacement = name + "(" +
                parameters.stream()
                        .map(Parameter::asDefinitionString)
                        .collect(Collectors.joining(",")) +
                ")";

        return returnType.aliasedType().substituteTypeUsageId(usageReplacement) + ";";
        */


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

        return returnType.name() + " " + name + "(" +
                parameters.stream()
                        .map(Parameter::asDefinitionString)
                        .collect(Collectors.joining(",")) +
                ") " + (body.isEmpty() ? "{}" : "{\n" + body + "\n}");
        /*

        var usageReplacement = name + "(" +
                parameters.stream()
                        .map(Parameter::asDefinitionString)
                        .collect(Collectors.joining(",")) +
                ")";

        return returnType.aliasedType().substituteTypeUsageId(usageReplacement)
                + " " + (body.isEmpty() ? "{}" : "{\n" + body + "\n}");
       */


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
        if (returnType.aliasedType().type().isIntegralType()) {
            return "\treturn 0;";
        }

        if (returnType.aliasedType().type().isFloatingPointType()) {
            return "\treturn 0.0;";
        }

        if (returnType.aliasedType().type().isBooleanType()) {
            return "\treturn 1;";
        }

        if (returnType.aliasedType().type().isVoid()) {
            return "";
        }

        // if the return type is not a void* this still works because of casting
        if (returnType.aliasedType().type().isPointerType()) {
            return "\treturn (void *)0;";
        }

        var ret = new VariableSymbol("ret", returnType);

        return "\t" + ret.asDefinitionString() + "\n" + "\treturn ret;";
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
