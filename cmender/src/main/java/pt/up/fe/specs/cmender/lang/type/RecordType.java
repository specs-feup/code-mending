package pt.up.fe.specs.cmender.lang.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.lang.symbol.Symbol;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Accessors(fluent = true)
public class RecordType implements Type {

    public enum RecordKind {
        STRUCT,
        CLASS,
        UNION;

        @JsonCreator
        public static RecordKind toRecordKind(String kind) {
            return switch (kind) {
                case "struct" -> STRUCT;
                case "class" -> CLASS;
                case "union" -> UNION;
                default -> throw new IllegalArgumentException("Unknown record type: " + kind);
            };
        }
    }

    private final String name;

    private final RecordKind recordKind;

    // Reference to the symbol that represents this record type
    // private RecordSymbol recordSymbol;

    @JsonCreator
    public RecordType(@JsonProperty("name") String name,
                      @JsonProperty("recordKind") RecordKind recordKind) {
        this.name = name;
        this.recordKind = recordKind;
    }

    /*public RecordType(String name, RecordKind recordKind, RecordSymbol recordSymbol) {
        this.name = name;
        this.recordKind = recordKind;
        this.recordSymbol = recordSymbol;
    }

    public void setRecordSymbol(RecordSymbol recordSymbol) {
        this.recordSymbol = recordSymbol;
    }*/

    @Override
    public TypeKind kind() {
        return TypeKind.RECORD;
    }

    @Override
    public boolean isDerivedType() {
        return true;
    }

    @Override
    public boolean isTagType() {
        return true;
    }

    @Override
    public boolean isRecordType() {
        return true;
    }

    @Override
    public Set<Symbol> getDirectDependencies(MendingTable table) {
        var recordSymbol = table.structs().get(name);

        // If the symbol is not found, it might mean it is already a declared type on the code (TODO is this correct?)
        if (recordSymbol == null) {
            return new HashSet<>();
        }

        return new HashSet<>(List.of(recordSymbol));
        /*if (recordSymbol == null) {
            return new HashSet<>();
        }

        return recordSymbol.getDirectDependencies();*/
    }

    @Override
    public void addDirectDependencies(List<Symbol> dependencies, MendingTable table) {
        var recordSymbol = table.structs().get(name);

        // If the symbol is not found, it might mean it is already a declared type on the code (TODO is this correct?)
        if (recordSymbol == null) {
            return;
        }

        dependencies.add(recordSymbol);
    }
}
