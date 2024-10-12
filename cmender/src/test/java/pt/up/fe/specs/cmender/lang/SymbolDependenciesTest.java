package pt.up.fe.specs.cmender.lang;

import org.junit.jupiter.api.Test;

import pt.up.fe.specs.cmender.lang.symbol.FunctionSymbol;
import pt.up.fe.specs.cmender.lang.symbol.RecordSymbol;
import pt.up.fe.specs.cmender.lang.symbol.TypedefSymbol;
import pt.up.fe.specs.cmender.lang.symbol.VariableSymbol;
import pt.up.fe.specs.cmender.mending.MendingTable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class SymbolDependenciesTest {
    @Test
    public void testFunctionWithoutDependencies() {
        /*var function = new FunctionSymbol("my_function", BasicDataType.VOID);
        assertThat(function.getDirectDependencies(), hasSize(0));

        function = new FunctionSymbol("my_function", BasicDataType.INT,
                List.of(new FunctionSymbol.Parameter("param1", BasicDataType.INT),
                        new FunctionSymbol.Parameter("param2", BasicDataType.FLOAT)));
        assertThat(function.getDirectDependencies(), hasSize(0));*/
    }

    @Test
    public void testFunctionWithReturnTypedefDependencies() {
        /*var typedef = new TypedefSymbol("My_Typedef", BasicDataType.INT);
        var function = new FunctionSymbol("my_function", typedef);
        assertThat(function.getDirectDependencies(), hasSize(1));
        assertThat(function.getDirectDependencies().stream().toList().getFirst(), is(typedef));

        typedef = new TypedefSymbol("My_Typedef", new TypedefSymbol("My_Typedef2", BasicDataType.INT));
        function = new FunctionSymbol("my_function", typedef);
        assertThat(function.getDirectDependencies(), hasSize(1));
        assertThat(function.getDirectDependencies().stream().toList().getFirst(), is(typedef));*/
    }

    @Test
    public void testFunctionWithReturnStructDependencies() {
        /*var struct = new RecordSymbol("My_Struct");
        var function = new FunctionSymbol("my_function", struct);
        assertThat(function.getDirectDependencies(), hasSize(1));
        assertThat(function.getDirectDependencies().stream().toList().getFirst(), is(struct));

        struct = new RecordSymbol("My_Struct",
                List.of(new RecordSymbol.Member("member1", new RecordSymbol("My_Struct2"))));
        function = new FunctionSymbol("my_function", struct);
        assertThat(function.getDirectDependencies(), hasSize(1));
        assertThat(function.getDirectDependencies().stream().toList().getFirst(), is(struct));*/
    }

    @Test
    public void testFunctionWithReturnPtrDependencies() {
        /*var typedef = new TypedefSymbol("My_Typedef", BasicDataType.INT);
        var ptr = new PtrType(typedef);
        var function = new FunctionSymbol("my_function", ptr);
        assertThat(function.getDirectDependencies(), hasSize(1));

        ptr = new PtrType(ptr);
        function = new FunctionSymbol("my_function", ptr);
        assertThat(function.getDirectDependencies(), hasSize(1));
        assertThat(function.getDirectDependencies().stream().toList().getFirst(), is(typedef));

        var struct = new RecordSymbol("My_Struct");
        ptr = new PtrType(struct);
        function = new FunctionSymbol("my_function", ptr);
        assertThat(function.getDirectDependencies(), hasSize(1));

        ptr = new PtrType(ptr);
        function = new FunctionSymbol("my_function", ptr);
        assertThat(function.getDirectDependencies(), hasSize(1));
        assertThat(function.getDirectDependencies().stream().toList().getFirst(), is(struct));*/
    }

    /*@Test
    public void testFunctionWithParameterDependencies() {
        var typedefParam = new Typedef("My_Typedef", new Typedef("My_Typedef2", BasicDataType.INT));
        var structParam = new Struct("My_Struct");
        var function = new Function("my_function", BasicDataType.VOID,
                List.of(new Function.Parameter("param1", typedefParam),
                        new Function.Parameter("param2", structParam)));
        assertThat(function.getDirectDependencies(), hasSize(2));
    }*/

    @Test
    public void testTypedefWithoutDependencies() {
        /*var typedef = new TypedefSymbol("My_Typedef", BasicDataType.INT);
        assertThat(typedef.getDirectDependencies(), hasSize(0));*/
    }

    @Test
    public void test() throws IOException {
        /*var stringWriter = new StringWriter();
        var table = new MendingTable();

        var parentTypedef = new TypedefSymbol("My_Parent_Typedef", BasicDataType.INT);
        var leafTypedef = new TypedefSymbol("My_Leaf_Typedef", parentTypedef);

        var struct = new RecordSymbol("My_Struct",
                List.of(new RecordSymbol.Member("member1", new ArrayType(BasicDataType.INT, 10)),
                        new RecordSymbol.Member("member2", new PtrType(leafTypedef))));

        struct.addMember(new RecordSymbol.Member("member3", new PtrType(struct)));
        var var1 = new VariableSymbol("var1", new PtrType(BasicDataType.INT));
        var var2 = new VariableSymbol("var2", new PtrType(parentTypedef));

        table.functions().put("my_function", new FunctionSymbol("my_function", BasicDataType.VOID));
        table.typedefs().put(leafTypedef.name(), leafTypedef);
        table.typedefs().put(parentTypedef.name(), parentTypedef);
        table.structs().put(struct.name(), struct);
        table.variables().put(var1.name(), var1);
        table.variables().put(var2.name(), var2);

        table.writeSymbolDecls(new BufferedWriter(new FileWriter("symbol_decls.h")));

        var a = struct.getDirectDependencies();

        System.out.println("results:\n" + stringWriter.toString());*/
    }

    @Test
    void testSelfReferencingStruct() throws IOException { // referential?
    /*//void testStructWithNestedPtrToItself() throws IOException {
        var struct = new RecordSymbol("My_Struct");
        var ptr = new PtrType(struct);
        struct.addMember(new RecordSymbol.Member("member1", ptr));

        var table = new MendingTable();
        table.structs().put(struct.name(), struct);
        var writer = new StringWriter();
        table.writeSymbolDecls(writer);
        System.out.println(writer.toString());
        //assertThat(struct.getDirectDependencies(), hasSize(1));
        //assertThat(struct.getDirectDependencies().stream().toList().getFirst(), is(ptr));*/
    }

    @Test
    void testStructsWithMutualRecursion() throws IOException {
        /*var struct1 = new RecordSymbol("My_Struct1");
        var struct2 = new RecordSymbol("My_Struct2");
        struct1.addMember(new RecordSymbol.Member("member1", struct2));
        struct2.addMember(new RecordSymbol.Member("member2", struct1));

        var table = new MendingTable();
        table.structs().put(struct1.name(), struct1);
        table.structs().put(struct2.name(), struct2);
        var writer = new StringWriter();
        table.writeSymbolDecls(writer);
        System.out.println(writer.toString());
        //assertThat(struct1.getDirectDependencies(), hasSize(1));
        //assertThat(struct1.getDirectDependencies().stream().toList().getFirst(), is(struct2));
        //assertThat(struct2.getDirectDependencies(), hasSize(1));
        //assertThat(struct2.getDirectDependencies().stream().toList().getFirst(), is(struct1));*/
    }
}
