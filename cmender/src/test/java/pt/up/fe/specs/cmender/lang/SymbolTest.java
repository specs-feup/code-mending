package pt.up.fe.specs.cmender.lang;

import org.junit.jupiter.api.Test;


import pt.up.fe.specs.cmender.lang.symbol.FunctionSymbol;
import pt.up.fe.specs.cmender.lang.symbol.RecordSymbol;
import pt.up.fe.specs.cmender.lang.symbol.TypedefSymbol;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SymbolTest {

    @Test
    public void testEmptyStruct() {
        var struct = new RecordSymbol("My_Struct");
        assertThat(struct.asDeclarationString(), is("struct My_Struct;"));
        assertThat(struct.asDefinitionString(), is("struct My_Struct {};"));
    }

    @Test
    public void testStructWithFields() {
        /*var struct = new RecordSymbol("My_Struct", List.of(
                new RecordSymbol.Member("int_member", BasicDataType.INT)));

        assertThat(struct.asDeclarationString(), is("struct My_Struct;"));
        assertThat(struct.asDefinitionString(), is("struct My_Struct {\n\tint int_member;\n};"));

        struct = new RecordSymbol("My_Struct", List.of(
                new RecordSymbol.Member("int_member", BasicDataType.INT),
                new RecordSymbol.Member("float_member", BasicDataType.FLOAT),
                new RecordSymbol.Member("array_member", new ArrayType(BasicDataType.CHAR, 5)),
                new RecordSymbol.Member("ptr_member", new PtrType(BasicDataType.DOUBLE))));

        assertThat(struct.asDeclarationString(), is("struct My_Struct;"));
        assertThat(struct.asDefinitionString(), is("struct My_Struct {\n\tint int_member;\n\tfloat float_member;\n\tchar array_member[5];\n\tdouble *ptr_member;\n};"));*/
    }

    @Test
    public void testBasicTypeTypedef() {
        /*var typedef = new TypedefSymbol("My_Typedef", BasicDataType.INT);
        assertThat(typedef.asDeclarationString(), is("typedef int My_Typedef;"));

        typedef = new TypedefSymbol("My_Typedef", BasicDataType.FLOAT);
        assertThat(typedef.asDeclarationString(), is("typedef float My_Typedef;"));*/
    }

    @Test
    public void testArrayTypedef() {
        /*var typedef = new TypedefSymbol("My_Typedef", new ArrayType(BasicDataType.INT, 5));
        assertThat(typedef.asDeclarationString(), is("typedef int My_Typedef[5];"));

        typedef = new TypedefSymbol("My_Typedef", new ArrayType(BasicDataType.FLOAT));
        assertThat(typedef.asDeclarationString(), is("typedef float My_Typedef[];"));*/
    }

    @Test
    public void testPtrTypedef() {
        /*var typedef = new TypedefSymbol("My_Typedef", new PtrType(BasicDataType.INT));
        assertThat(typedef.asDeclarationString(), is("typedef int *My_Typedef;"));*/
    }

    @Test
    public void testTypedefTypedef() {
      /*var typedef = new TypedefSymbol("My_Typedef", new TypedefSymbol("My_Typedef2", BasicDataType.INT));
      assertThat(typedef.asDeclarationString(), is("typedef My_Typedef2 My_Typedef;"));*/
    }

    @Test
    public void testVoidFunction() {
        /*var function = new FunctionSymbol("void_func", BasicDataType.VOID);
        assertThat(function.asDeclarationString(), is("void void_func();"));
        assertThat(function.asDefinitionString(), is("void void_func() {}"));*/
    }

    @Test
    public void testIntegralRetFunction() {
        /*var charFunction = new FunctionSymbol("char_func", BasicDataType.CHAR);
        assertThat(charFunction.asDeclarationString(), is("char char_func();"));
        assertThat(charFunction.asDefinitionString(), is("char char_func() {\nreturn 0;\n}"));

        var shortFunction = new FunctionSymbol("short_func", BasicDataType.SHORT);
        assertThat(shortFunction.asDeclarationString(), is("short short_func();"));
        assertThat(shortFunction.asDefinitionString(), is("short short_func() {\nreturn 0;\n}"));

        var intFunction = new FunctionSymbol("int_func", BasicDataType.INT);
        assertThat(intFunction.asDeclarationString(), is("int int_func();"));
        assertThat(intFunction.asDefinitionString(), is("int int_func() {\nreturn 0;\n}"));

        var longFunction = new FunctionSymbol("long_func", BasicDataType.LONG);
        assertThat(longFunction.asDeclarationString(), is("long long_func();"));
        assertThat(longFunction.asDefinitionString(), is("long long_func() {\nreturn 0;\n}"));*/
    }

    @Test
    public void testFloatingPointRetFunction() {
        /*var floatFunction = new FunctionSymbol("float_func", BasicDataType.FLOAT);
        assertThat(floatFunction.asDeclarationString(), is("float float_func();"));
        assertThat(floatFunction.asDefinitionString(), is("float float_func() {\nreturn 0.0;\n}"));

        var doubleFunction = new FunctionSymbol("double_func", BasicDataType.DOUBLE);
        assertThat(doubleFunction.asDeclarationString(), is("double double_func();"));
        assertThat(doubleFunction.asDefinitionString(), is("double double_func() {\nreturn 0.0;\n}"));*/
    }

    @Test
    public void testBooleanFunction() {
        /*var boolFunction = new FunctionSymbol("bool_func", BasicDataType._BOOL);
        assertThat(boolFunction.asDeclarationString(), is("_Bool bool_func();"));
        assertThat(boolFunction.asDefinitionString(), is("_Bool bool_func() {\nreturn 1;\n}"));*/
    }

    @Test
    public void testVoidPtrFunction() {
        /*var voidPtrFunction = new FunctionSymbol("voidPtrFunc", new PtrType(BasicDataType.VOID));
        assertThat(voidPtrFunction.asDeclarationString(), is("void *voidPtrFunc();"));
        assertThat(voidPtrFunction.asDefinitionString(), is("void *voidPtrFunc() {\nreturn (void *)0;\n}"));*/
    }

    @Test
    public void testStructFunction() {
        /*var structFunction = new FunctionSymbol("struct_func", new RecordSymbol("My_Struct"));
        assertThat(structFunction.asDeclarationString(), is("struct My_Struct struct_func();"));
        assertThat(structFunction.asDefinitionString(), is("struct My_Struct struct_func() {\nstruct My_Struct ret;\nreturn ret;\n}"));*/
    }

    @Test
    public void testTypedefFunction() {
        /*var typedefFunction = new FunctionSymbol("typedef_func", new TypedefSymbol("My_Typedef", BasicDataType.INT));
        assertThat(typedefFunction.asDeclarationString(), is("My_Typedef typedef_func();"));
        assertThat(typedefFunction.asDefinitionString(), is("My_Typedef typedef_func() {\nMy_Typedef ret;\nreturn ret;\n}"));*/
    }
}
