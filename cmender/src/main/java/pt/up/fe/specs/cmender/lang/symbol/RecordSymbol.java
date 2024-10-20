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
public class RecordSymbol extends Symbol {

    private List<Member> members;

    public RecordSymbol(String name, List<Member> members) {
        super(name);
        this.members = new ArrayList<>(members);
    }

    public RecordSymbol(String name) {
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

        private final TypedefType type;

        public Member(String name, TypedefType type) {
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
            return type.name() + " " + name + ";";
            //return type.aliasedType().substituteTypeUsageId(name) + ";";
        }

        @Override
        public Set<Symbol> getDirectDependencies(MendingTable table) {
            return type.getDirectDependencies(table);
        }

        @Override
        public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
            type.addDirectDependencies(dependencies, table);
        }
    }

    @Override
    public String asDeclarationString() {
        return "struct " + name + ";";
    }

    @Override
    public String asDefinitionString() {
        // TODO support other records
        return "struct " + name + " {" +
                members.stream()
                        .map(Member::asDefinitionString)
                        .collect(Collectors.joining("\n\t",
                                members.isEmpty()? "" : "\n\t", members.isEmpty()? "" : "\n")) +
                "};";
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        var dependencies = new ArrayList<Symbol>();
        members.forEach(member -> member.addDirectDependencies(dependencies, table));
        return new HashSet<>(dependencies);
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        // we don't add recursively the types of the struct members because we only care about the
        //  direct dependency (this symbol) to build a dependency graph
        dependencies.add(this);
    }
}
