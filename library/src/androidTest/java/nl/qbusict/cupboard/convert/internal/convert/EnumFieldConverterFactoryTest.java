package nl.qbusict.cupboard.convert.internal.convert;

import android.database.MatrixCursor;
import android.test.AndroidTestCase;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import nl.qbusict.cupboard.*;
import nl.qbusict.cupboard.convert.FieldConverter;
import nl.qbusict.cupboard.internal.convert.EnumFieldConverterFactory;

public class EnumFieldConverterFactoryTest extends AndroidTestCase {

    public enum TestEnum {TEST1, TEST2}

    public List<Enum<TestEnum>> test;
    public Enum<TestEnum> test2;
    public TestEnum test3;

    // this test is actually equivalent to testConverterGenericEnum
    public void testCreateConverterGenericArgument() throws NoSuchFieldException {
        Field field = getClass().getField("test");
        ParameterizedType pt = (ParameterizedType) field.getGenericType();
        testWithType(pt.getActualTypeArguments()[0]);
    }

    public void testConverterGenericEnum() throws NoSuchFieldException {
        Field field = getClass().getField("test2");
        testWithType(field.getGenericType());
    }

    public void testConverterEnum() throws NoSuchFieldException {
        Field field = getClass().getField("test3");
        testWithType(field.getGenericType());
    }

    private void testWithType(Type type) {
        EnumFieldConverterFactory factory = new EnumFieldConverterFactory();
        FieldConverter<TestEnum> fact = (FieldConverter<TestEnum>) factory.create(new Cupboard(), type);
        MatrixCursor cursor = new MatrixCursor(new String[]{"val"}, 1);
        cursor.addRow(new String[]{"TEST2"});
        cursor.moveToFirst();
        TestEnum v = fact.fromCursorValue(cursor, 0);
        assertEquals(TestEnum.TEST2, v);
    }
}
