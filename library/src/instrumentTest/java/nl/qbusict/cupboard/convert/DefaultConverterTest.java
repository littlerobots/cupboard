package nl.qbusict.cupboard.convert;

import android.content.ContentValues;
import android.content.Context;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.qbusict.cupboard.*;
import nl.qbusict.cupboard.TestEntity.TestEnum;

import static nl.qbusict.cupboard.TestHelper.newPreferredColumnOrderCursorWrapper;

public class DefaultConverterTest extends AndroidTestCase {

    private static class TestSQLiteOpenHelper extends SQLiteOpenHelper {

        private final Cupboard mCupboard;

        public TestSQLiteOpenHelper(Context context) {
            super(context, "test", null, 1);
            mCupboard = new Cupboard();
            mCupboard.register(TestEntity.class);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mCupboard.withDatabase(db).createTables();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            mCupboard.withDatabase(db).upgradeTables();
        }


        public Cupboard getCupboard() {
            return mCupboard;
        }
    }

    public void testBooleanMapping() {
        Map<Class<?>, ConverterHolder<?>> entities = Collections.emptyMap();
        DefaultConverter<TestEntity> translator = new DefaultConverter<TestEntity>(TestEntity.class, entities, false);
        TestEntity entity = new TestEntity();
        entity.booleanProperty = true;
        ContentValues values = new ContentValues();
        translator.toValues(entity, values);
        assertNotNull(values.get("booleanProperty"));
        assertEquals(Boolean.TRUE, Boolean.valueOf(values.getAsString("booleanProperty")));
        MatrixCursor cursor = new MatrixCursor(new String[]{"booleanProperty"});
        cursor.addRow(new Object[]{1});
        cursor.moveToFirst();
        TestEntity converted = translator.fromCursor(newPreferredColumnOrderCursorWrapper(cursor, translator.getColumns()));
        assertEquals(true, converted.booleanProperty);

        entity.booleanObjectProperty = true;
        translator.toValues(entity, values);
        assertNotNull(values.get("booleanObjectProperty"));
        cursor = new MatrixCursor(new String[]{"booleanObjectProperty"});
        cursor.addRow(new Object[]{1});
        cursor.moveToFirst();
        converted = translator.fromCursor(newPreferredColumnOrderCursorWrapper(cursor, translator.getColumns()));
        assertEquals(Boolean.TRUE, converted.booleanObjectProperty);
    }

    public void testCaseInsensitiveColumnMapping() {
        Map<Class<?>, ConverterHolder<?>> entities = Collections.emptyMap();
        DefaultConverter<TestEntity> translator = new DefaultConverter<TestEntity>(TestEntity.class, entities, false);
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

    public void testConvertDate() {
        Date now = new Date();
        TestEntity entity = new TestEntity();
        entity.dateProperty = now;
        Map<Class<?>, ConverterHolder<?>> entities = Collections.emptyMap();
        DefaultConverter<TestEntity> translator = new DefaultConverter<TestEntity>(TestEntity.class, entities, false);
        ContentValues values = new ContentValues();
        translator.toValues(entity, values);
        assertEquals(now.getTime(), values.getAsLong("dateProperty").longValue());

        MatrixCursor cursor = new MatrixCursor(new String[]{"dateproperty"});
        cursor.addRow(new Object[]{now.getTime()});
        cursor.moveToNext();
        TestEntity result = translator.fromCursor(newPreferredColumnOrderCursorWrapper(cursor, translator.getColumns()));
        assertNotNull(result.dateProperty);
        assertEquals(now.getTime(), result.dateProperty.getTime());
    }

    public void testConvertBoolean() {
        TestSQLiteOpenHelper helper = new TestSQLiteOpenHelper(new RenamingDelegatingContext(getContext(), "test"));
        SQLiteDatabase db = helper.getWritableDatabase();
        TestEntity entity = new TestEntity();
        helper.getCupboard().withDatabase(db).put(entity);
        ContentValues values = new ContentValues();
        values.put("booleanProperty", true);
        values.put(BaseColumns._ID, entity._id);
        helper.getCupboard().withDatabase(db).put(TestEntity.class, values);
    }

    public void testEmbeddedEntity() {
        Map<Class<?>, ConverterHolder<?>> entities = new HashMap<Class<?>, ConverterHolder<?>>();
        entities.put(TestEntityWithReference.class, new ConverterHolder<TestEntityWithReference>(TestEntityWithReference.class, new DefaultConverterFactory(), entities));
        entities.put(ReferencedEntity.class, new ConverterHolder<ReferencedEntity>(ReferencedEntity.class, new DefaultConverterFactory(), entities));
        DefaultConverter<TestEntityWithReference> translator = new DefaultConverter<TestEntityWithReference>(TestEntityWithReference.class, entities, false);
        TestEntityWithReference entity = new TestEntityWithReference();
        ContentValues values = new ContentValues();
        translator.toValues(entity, values);
        assertNull(values.get("ref"));
        entity.ref = new ReferencedEntity();
        entity.ref._id = 100L;
        translator.toValues(entity, values);
        assertEquals(values.getAsLong("ref"), (Long) 100L);
    }

    public void testAnnotatedEntity() {
        Map<Class<?>, ConverterHolder<?>> entities = new HashMap<Class<?>, ConverterHolder<?>>();
        DefaultConverter<TestAnnotatedEntity> converter = new DefaultConverter<TestAnnotatedEntity>(TestAnnotatedEntity.class, entities, false);
        DefaultConverter<TestAnnotatedEntity> annotatedConverter = new DefaultConverter<TestAnnotatedEntity>(TestAnnotatedEntity.class, entities, true);
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
        Map<Class<?>, ConverterHolder<?>> entities = new HashMap<Class<?>, ConverterHolder<?>>();
        DefaultConverter<TestAnnotatedEntity> converter = new DefaultConverter<TestAnnotatedEntity>(TestAnnotatedEntity.class, entities, false);
        DefaultConverter<TestAnnotatedEntity> annotatedConverter = new DefaultConverter<TestAnnotatedEntity>(TestAnnotatedEntity.class, entities, true);
        assertEquals(4, converter.getColumns().size());
        assertEquals(3, annotatedConverter.getColumns().size());
    }

    public void testEnum() {
        Map<Class<?>, ConverterHolder<?>> entities = Collections.emptyMap();
        DefaultConverter<TestEntity> converter = new DefaultConverter<TestEntity>(TestEntity.class, entities, false);
        TestEntity entity = new TestEntity();
        entity.enumProperty = TestEnum.TEST1;
        ContentValues values = new ContentValues();
        converter.toValues(entity, values);
        assertEquals("TEST1", values.getAsString("enumProperty"));
        MatrixCursor cursor = new MatrixCursor(new String[]{"enumProperty"});
        cursor.addRow(new Object[]{"TEST2"});
        cursor.moveToPosition(0);
        TestEntity entityFromCursor = converter.fromCursor(TestHelper.newPreferredColumnOrderCursorWrapper(cursor, converter.getColumns()));
        assertEquals(TestEnum.TEST2.toString(), entityFromCursor.enumProperty.toString());
    }

    public void testPrivateFields() {
        Map<Class<?>, ConverterHolder<?>> entities = Collections.emptyMap();
        DefaultConverter<PrivateEntity> converter = new DefaultConverter<PrivateEntity>(PrivateEntity.class, entities, false);
        assertEquals(3, converter.getColumns().size());
    }

    public void testInheritedPrivateFields() {
        Map<Class<?>, ConverterHolder<?>> entities = Collections.emptyMap();
        DefaultConverter<PrivateInheritedEntity> converter = new DefaultConverter<PrivateInheritedEntity>(PrivateInheritedEntity.class, entities, false);
        assertEquals(4, converter.getColumns().size());
    }

}
