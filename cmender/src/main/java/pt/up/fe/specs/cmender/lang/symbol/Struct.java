package pt.up.fe.specs.cmender.lang.symbol;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@ToString
@Accessors(fluent = true)
public class Struct extends Symbol implements Type {

    private List<Member> members;

    public Struct(String name, List<Member> members) {
        super(name);
        this.members = new ArrayList<>(members);
    }

    public Struct(String name) {
        this(name, new ArrayList<>());
    }

    public void addMember(Member member) {
        this.members.add(member);
    }

    public void setMembers(List<Member> members) {
        this.members = new ArrayList<>(members);
    }

    @Getter
    @ToString
    @Accessors(fluent = true)
    public static class Member extends Symbol {

        private Type type;

        public Member(String name, Type type) {
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
            return type.modifyVariable(name) + ";";
        }

        @Override
        public Set<Symbol> getDirectDependencies() {
            return type.getDirectDependencies();
        }

        @Override
        public void addDirectDependencies(List<Symbol> dependencies) {
            type.addDirectDependencies(dependencies);
        }
    }

    @Override
    public String asDeclarationString() {
        return "struct " + name + ";";
    }

    @Override
    public String asDefinitionString() {
        return "struct " + name + " {" +
                members.stream()
                        .map(Member::asDefinitionString)
                        .collect(Collectors.joining("\n\t",
                                members.isEmpty()? "" : "\n\t", members.isEmpty()? "" : "\n")) +
                "};";
    }

    @Override
    public String getName() {
        return "struct " + name;
    }

    @Override
    public String modifyVariable(String varName) {
        return getName() + " " + varName;
    }

    @Override
    public boolean isCompositeDataType() {
        return true;
    }

    @Override
    public boolean isStructType() {
        return true;
    }

    @Override
    public Set<Symbol> getDirectDependencies() {
        var dependencies = new ArrayList<Symbol>();
        members.forEach(member -> member.addDirectDependencies(dependencies));
        return new HashSet<>(dependencies);
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies) {
        dependencies.add(this);
    }
}
