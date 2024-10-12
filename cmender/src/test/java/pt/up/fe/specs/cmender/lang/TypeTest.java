package pt.up.fe.specs.cmender.lang;

import org.junit.jupiter.api.Test;

import pt.up.fe.specs.cmender.lang.symbol.TypedefSymbol;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeTest {

    @Test
    public void testBasicDataTypeIsTrue() {
        /*assertTrue(BasicDataType.VOID.isBasicDataType());
        assertTrue(BasicDataType.CHAR.isBasicDataType());
        assertTrue(BasicDataType.SHORT.isBasicDataType());
        assertTrue(BasicDataType.INT.isBasicDataType());
        assertTrue(BasicDataType.LONG.isBasicDataType());
        assertTrue(BasicDataType.FLOAT.isBasicDataType());
        assertTrue(BasicDataType.DOUBLE.isBasicDataType());
        assertTrue(BasicDataType._BOOL.isBasicDataType());*/
    }

    @Test
    public void testBasicDataTypeIsVoid() {
        /*assertTrue(BasicDataType.VOID.isVoid());
        assertFalse(BasicDataType.CHAR.isVoid());
        assertFalse(BasicDataType.SHORT.isVoid());
        assertFalse(BasicDataType.INT.isVoid());
        assertFalse(BasicDataType.LONG.isVoid());
        assertFalse(BasicDataType.FLOAT.isVoid());
        assertFalse(BasicDataType.DOUBLE.isVoid());
        assertFalse(BasicDataType._BOOL.isVoid());*/
    }

    @Test
    public void testBasicDataTypeIsIntegralType() {
        /*assertFalse(BasicDataType.VOID.isIntegralType());
        assertTrue(BasicDataType.CHAR.isIntegralType());
        assertTrue(BasicDataType.SHORT.isIntegralType());
        assertTrue(BasicDataType.INT.isIntegralType());
        assertTrue(BasicDataType.LONG.isIntegralType());
        assertFalse(BasicDataType.FLOAT.isIntegralType());
        assertFalse(BasicDataType.DOUBLE.isIntegralType());
        assertFalse(BasicDataType._BOOL.isIntegralType());*/
    }

    @Test
    public void testBasicDataTypeIsFloatingType() {
        /*assertFalse(BasicDataType.VOID.isFloatingPointType());
        assertFalse(BasicDataType.CHAR.isFloatingPointType());
        assertFalse(BasicDataType.SHORT.isFloatingPointType());
        assertFalse(BasicDataType.INT.isFloatingPointType());
        assertFalse(BasicDataType.LONG.isFloatingPointType());
        assertTrue(BasicDataType.FLOAT.isFloatingPointType());
        assertTrue(BasicDataType.DOUBLE.isFloatingPointType());
        assertFalse(BasicDataType._BOOL.isFloatingPointType());*/
    }

    @Test
    public void testBasicDataTypeIsBooleanType() {
        /*assertFalse(BasicDataType.VOID.isBooleanType());
        assertFalse(BasicDataType.CHAR.isBooleanType());
        assertFalse(BasicDataType.SHORT.isBooleanType());
        assertFalse(BasicDataType.INT.isBooleanType());
        assertFalse(BasicDataType.LONG.isBooleanType());
        assertFalse(BasicDataType.FLOAT.isBooleanType());
        assertFalse(BasicDataType.DOUBLE.isBooleanType());
        assertTrue(BasicDataType._BOOL.isBooleanType());*/
    }

    @Test
    public void testArrayType() {
        /*var array1 = new ArrayType(BasicDataType.INT, null);
        assertThat(array1.getName(), is("int[]"));

        var array2 = new ArrayType(BasicDataType.INT, 8);
        assertTrue(array2.isCompositeDataType());
        assertTrue(array2.isArrayType());
        assertThat(array2.getName(), is("int[8]"));

        assertThat(array2.modifyVariable("var"), is("int var[8]"));*/
    }

    @Test
    public void testPtrType() {
        /*var ptr1 = new PtrType(BasicDataType.INT);
        assertTrue(ptr1.isCompositeDataType());
        assertTrue(ptr1.isPtrType());
        assertThat(ptr1.getName(), is("int *"));

        assertThat(ptr1.modifyVariable("var"), is("int *var"));*/
    }

    @Test
    public void testTypedefType() {
        /*var typedef1 = new TypedefSymbol("myType", BasicDataType.INT);
        assertTrue(typedef1.isCompositeDataType());
        assertTrue(typedef1.isTypedefType());

        assertThat(typedef1.getName(), is("myType"));

        assertThat(typedef1.modifyVariable("var"), is("myType var"));

        var ptr1 = new PtrType(typedef1);
        assertThat(ptr1.getName(), is("myType *"));
        assertThat(ptr1.modifyVariable("var"), is("myType *var"));

        var array1 = new ArrayType(typedef1, 8);
        assertThat(array1.getName(), is("myType[8]"));
        assertThat(array1.modifyVariable("var"), is("myType var[8]"));*/
    }


    /*
    @Test
    public void testArrayType() {
        var array1 = new ArrayType(BasicDataType.INT, null);
        assertThat(array1.getName(), is("int[]"));

        var array2 = new ArrayType(BasicDataType.INT, 8);
        assertTrue(array2.isCompositeDataType());
        assertTrue(array2.isArrayType());
        assertThat(array2.getName(), is("int[8]"));

        var array3 = new ArrayType(array2, 3);
        assertThat(array3.getName(), is("int[3][8]"));

        var array4 = new ArrayType(array3, 2);
        assertThat(array4.getName(), is("int[2][3][8]"));

        var array5 = new ArrayType(array4, 4);
        assertThat(array5.getName(), is("int[4][2][3][8]"));
    }

    @Test
    public void testPtrType() {
        var ptr1 = new PtrType(BasicDataType.INT);
        assertTrue(ptr1.isCompositeDataType());
        assertTrue(ptr1.isPtrType());
        assertThat(ptr1.getName(), is("int *"));

        var ptr2 = new PtrType(ptr1);
        assertThat(ptr2.getName(), is("int **"));

        var ptr3 = new PtrType(ptr2);
        assertThat(ptr3.getName(), is("int ***"));

        var ptr4 = new PtrType(ptr3);
        assertThat(ptr4.getName(), is("int ****"));
    }

    @Test
    public void testArrayOfNPtrs() {
        var ptr1 = new PtrType(BasicDataType.INT);
        var array1 = new ArrayType(ptr1, 8);
        assertThat(array1.getName(), is("int *[8]"));

        var ptr2 = new PtrType(ptr1);
        var array2 = new ArrayType(ptr2, 3);
        assertThat(array2.getName(), is("int **[3]"));

        var ptr3 = new PtrType(ptr2);
        var array3 = new ArrayType(ptr3, 2);
        assertThat(array3.getName(), is("int ***[2]"));
    }

    @Test
    public void testNArrayOfPtrs() {
        var ptr1 = new PtrType(BasicDataType.INT);

        var array1 = new ArrayType(ptr1, 8);
        assertThat(array1.getName(), is("int *[8]"));

        var array2 = new ArrayType(array1, 3);
        assertThat(array2.getName(), is("int *[3][8]"));

        var array3 = new ArrayType(array2, 2);
        assertThat(array3.getName(), is("int *[2][3][8]"));
    }

    @Test
    public void testNArrayOfNPtrs() {

    }


    @Test
    public void testPtrToNArray() {
        var array1 = new ArrayType(BasicDataType.INT, 8);

        var ptr1 = new PtrType(array1);
        assertThat(ptr1.getName(), is("int (*)[8]"));

        var array2 = new ArrayType(array1, 3);

        var ptr2 = new PtrType(array2);
        assertThat(ptr2.getName(), is("int (*)[3][8]"));

        var array3 = new ArrayType(array2, 2);

        var ptr3 = new PtrType(array3);
        assertThat(ptr3.getName(), is("int (*)[2][3][8]"));
    }

    @Test
    public void testNPtrToArray() {
        var array1 = new ArrayType(BasicDataType.INT, 8);

        var ptr1 = new PtrType(array1);
        assertThat(ptr1.getName(), is("int (*)[8]"));

        var ptr2 = new PtrType(ptr1);
        assertThat(ptr2.getName(), is("int (**)[8]"));

        var ptr3 = new PtrType(ptr2);
        assertThat(ptr3.getName(), is("int (***)[8]"));

        var ptr4 = new PtrType(ptr3);
        assertThat(ptr4.getName(), is("int (****)[8]"));
    }

    @Test
    public void testNPtrToNArray() {
        var array1 = new ArrayType(BasicDataType.INT, 8);

        var ptr1 = new PtrType(array1);
        assertThat(ptr1.getName(), is("int (*)[8]"));

        var ptr2 = new PtrType(ptr1);
        assertThat(ptr2.getName(), is("int (**)[8]"));

        var array2 = new ArrayType(array1, 3);

        var ptr3 = new PtrType(array2);
        assertThat(ptr3.getName(), is("int (*)[3][8]"));

        var ptr4 = new PtrType(ptr3);
        assertThat(ptr4.getName(), is("int (**)[3][8]"));

        var array3 = new ArrayType(array2, 2);

        var ptr5 = new PtrType(array3);
        assertThat(ptr5.getName(), is("int (*)[2][3][8]"));

        var ptr6 = new PtrType(ptr5);
        assertThat(ptr6.getName(), is("int (**)[2][3][8]"));

        var ptr7 = new PtrType(ptr6);
        assertThat(ptr7.getName(), is("int (***)[2][3][8]"));
    }
     */
}
