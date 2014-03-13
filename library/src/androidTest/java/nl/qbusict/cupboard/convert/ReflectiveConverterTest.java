package nl.qbusict.cupboard.convert;

import android.content.ContentValues;
import android.database.MatrixCursor;
import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import nl.qbusict.cupboard.*;

import static nl.qbusict.cupboard.TestHelper.newPreferredColumnOrderCursorWrapper;

public class ReflectiveConverterTest extends AndroidTestCase {

    public void testPrivateFields() {
        Cupboard cupboard = new Cupboard();
        cupboard.register(PrivateEntity.class);
        ReflectiveEntityConverter<PrivateEntity> converter = new ReflectiveEntityConverter<PrivateEntity>(cupboard, PrivateEntity.class);
        assertEquals(3, converter.getColumns().size());
    }

    public void testInheritedPrivateFields() {
        Cupboard cupboard = new Cupboard();
        cupboard.register(PrivateInheritedEntity.class);
        ReflectiveEntityConverter<PrivateInheritedEntity> converter = new ReflectiveEntityConverter<PrivateInheritedEntity>(cupboard, PrivateInheritedEntity.class);
        assertEquals(4, converter.getColumns().size());
    }

    public void testAnnotatedEntity() {
        Cupboard cupboard = new Cupboard();
        cupboard.register(TestAnnotatedEntity.class);

        Cupboard annotatedCupboard = new CupboardBuilder().useAnnotations().build();
        annotatedCupboard.register(TestAnnotatedEntity.class);

        ReflectiveEntityConverter<TestAnnotatedEntity> converter = new ReflectiveEntityConverter<TestAnnotatedEntity>(cupboard, TestAnnotatedEntity.class);
        ReflectiveEntityConverter<TestAnnotatedEntity> annotatedConverter = new ReflectiveEntityConverter<TestAnnotatedEntity>(annotatedCupboard, TestAnnotatedEntity.class);

        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "myStringValue", "data1"});
        cursor.addRow(new Object[]{1L, "test", "test2"});
        cursor.moveToPosition(0);
        TestAnnotatedEntity entity = converter.fromCursor(TestHelper.newPreferredColumnOrderCursorWrapper(cursor, converter.getColumns()));
        assertEquals(1L, entity._id.longValue());
        assertEquals("test", entity.myStringValue);
        assertNull(entity.renamedStringValue);
        entity = annotatedConverter.fromCursor(TestHelper.newPreferredColumnOrderCursorWrapper(cursor, annotatedConverter.getColumns()));
        assertEquals(1L, entity._id.longValue());
        assertEquals("test", entity.myStringValue);
        assertEquals("test2", entity.renamedStringValue);

        ContentValues values = new ContentValues();
        annotatedConverter.toValues(entity, values);
        assertTrue(values.containsKey("data1"));
        assertEquals("test2", values.getAsString("data1"));
        assertTrue(values.containsKey("_id"));
        assertTrue(values.containsKey("myStringValue"));
    }

    public void testIgnoreAnnotation() {
        Cupboard cupboard = new Cupboard();
        cupboard.register(TestAnnotatedEntity.class);

        Cupboard annotatedCupboard = new CupboardBuilder().useAnnotations().build();
        annotatedCupboard.register(TestAnnotatedEntity.class);

        ReflectiveEntityConverter<TestAnnotatedEntity> converter = new ReflectiveEntityConverter<TestAnnotatedEntity>(cupboard, TestAnnotatedEntity.class);
        ReflectiveEntityConverter<TestAnnotatedEntity> annotatedConverter = new ReflectiveEntityConverter<TestAnnotatedEntity>(annotatedCupboard, TestAnnotatedEntity.class);

        assertEquals(4, converter.getColumns().size());
        assertEquals(3, annotatedConverter.getColumns().size());
    }

    public void testCaseInsensitiveColumnMapping() {
        Cupboard cupboard = new Cupboard();
        cupboard.register(TestEntity.class);
        ReflectiveEntityConverter<TestEntity> translator = new ReflectiveEntityConverter<TestEntity>(cupboard, TestEntity.class);
        TestEntity te = new TestEntity();
        te.booleanObjectProperty = true;
        te.booleanProperty = false;
        te.doubleObjectProperty = 100d;
        te.doubleProperty = 200.2d;
        te.floatObjectProperty = 101f;
        te.floatProperty = 202f;
        te.intObjectProperty = 10;
        te.intObjectProperty = 11;
        te.longObjectProperty = 12L;
        te.longProperty = 13L;
        te.shortObjectProperty = 1;
        te.shortProperty = 2;
        te.stringProperty = "test";

        ContentValues values = new ContentValues(50);
        translator.toValues(te, values);
        Set<Entry<String, Object>> vs = values.valueSet();
        String[] cols = new String[vs.size() - 1];
        int index = 0;
        List<Object> vals = new ArrayList<Object>(vs.size());
        for (Entry<String, Object> entry : vs) {
            if (!"bytearrayproperty".equalsIgnoreCase(entry.getKey())) {
                cols[index++] = entry.getKey().toUpperCase();
                vals.add(entry.getValue());
            }
        }
        MatrixCursor cursor = new MatrixCursor(cols);
        cursor.addRow(vals);
        cursor.moveToFirst();
        TestEntity converted = translator.fromCursor(newPreferredColumnOrderCursorWrapper(cursor, translator.getColumns()));
        assertEquals(te, converted);
    }
}
