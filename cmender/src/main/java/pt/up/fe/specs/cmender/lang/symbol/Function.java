package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
@Accessors(fluent = true)
public class Function extends Symbol {

    private Type returnType;

    private List<Parameter> parameters;

    public Function(String name, Type returnType, List<Parameter> parameters) {
        super(name);
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public Function(String name, Type returnType) {
        this(name, returnType, new ArrayList<>());
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public void addParameter(Parameter parameter) {
        this.parameters.add(parameter);
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Getter
    @ToString
    @Accessors(fluent = true)
    public static class Parameter extends Symbol {

        private Type type;

        public Parameter(String name, Type type) {
            super(name);
            this.type = type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        @Override
        public String asDeclarationString() {
            return asDefinitionString();
        }

        @Override
        public String asDefinitionString() {
            return type.modifyVariable(name);
        }
    }

    @Override
    public String asDeclarationString() {
        return returnType.getName() + (returnType.isPtrType()? "" : " ") + name + "(" +
                parameters.stream()
                        .map(Parameter::asDefinitionString)
                        .collect(Collectors.joining(",")) +
                ");";
    }

    @Override
    public String asDefinitionString() {
        var body = getBody();

        return returnType.getName() + (returnType.isPtrType()? "" : " ") + name + "(" +
                parameters.stream()
                        .map(Parameter::asDefinitionString)
                        .collect(Collectors.joining(",")) +
                ") " + (body.isEmpty() ? "{}" : "{\n" + body + "\n}");
    }

    // TODO dont forget that we can also not have a return statement at all
    // Returning arrays should not be supported
    //    they decay to pointer to a stack-allocated mem region)
    private String getBody() {
        if (returnType.isIntegralType()) {
            return "return 0;";
        }

        if (returnType.isFloatingPointType()) {
            return "return 0.0;";
        }

        if (returnType.isBooleanType()) {
            return "return 1;";
        }

        if (returnType.isVoid()) {
            return "";
        }

        if (returnType.isPtrType()) {
            return "return (void *)0;";
        }

        var ret = new Variable("ret", returnType);

        return ret.asDefinitionString() + "\n" + "return ret;";
    }
}
