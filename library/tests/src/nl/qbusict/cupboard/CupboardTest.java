package nl.qbusict.cupboard;

import java.util.HashMap;

import nl.qbusict.cupboard.convert.ConverterHolder;
import nl.qbusict.cupboard.convert.DefaultConverter;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.test.AndroidTestCase;

public class CupboardTest extends AndroidTestCase {

    private Cupboard mStore;

    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context, int version) {
            super(context, "test_ls.db", null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
           mStore.withDatabase(db).createTables();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            mStore.withDatabase(db).upgradeTables();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mStore = new Cupboard();
        mStore.register(TestEntity.class);
        getContext().deleteDatabase("test_ls.db");
    }

    public void testPut() {
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        TestEntity entity = new TestEntity();
        entity.stringProperty = "Test";
        entity.booleanObjectProperty = Boolean.FALSE;
        mStore.withDatabase(db).put(entity);
        assertNotNull(entity._id);
        Cursor cursor = db.query(mStore.getTable(TestEntity.class), null, null, null, null, null, null);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        String[] columns = cursor.getColumnNames();
        assertEquals("Test", cursor.getString(cursor.getColumnIndexOrThrow("stringProperty")));
        assertEquals(String.valueOf(0), cursor.getString(cursor.getColumnIndexOrThrow("booleanObjectProperty")));
        cursor.close();
        TestEntity stored = mStore.withDatabase(db).get(TestEntity.class, entity._id);
        assertEquals("Test", stored.stringProperty);
        assertEquals(Boolean.FALSE, stored.booleanObjectProperty);
    }

    public void testBooleanQuery() {
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        TestEntity entity = new TestEntity();
        entity.booleanObjectProperty = true;
        mStore.withDatabase(db).put(entity);
        Cursor cursor = db.query(mStore.getTable(TestEntity.class), null,  "booleanObjectProperty = ?", new String[]{String.valueOf(1)}, null, null, null, null);
        assertEquals(1, cursor.getCount());
    }

    public void testBooleanUpdate() {
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        TestEntity entity = new TestEntity();
        entity.booleanProperty = true;
        mStore.withDatabase(db).put(entity);
        TestEntity stored = mStore.withDatabase(db).get(TestEntity.class, entity._id);
        assertEquals(true, stored.booleanProperty);
        ContentValues values = new ContentValues(2);
        values.put(BaseColumns._ID, entity._id);
        values.put("booleanProperty", false);
        mStore.withDatabase(db).update(TestEntity.class, values);
        stored = mStore.withDatabase(db).get(TestEntity.class, entity._id);
        assertEquals(false, stored.booleanProperty);
        QueryResultIterable<TestEntity> itr = mStore.withDatabase(db).query(TestEntity.class).withSelection("booleanProperty = ?", String.valueOf(0)).query();
        assertTrue(itr.iterator().hasNext());
        itr.close();
    }

    public void testPartialFetch() {
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        TestEntity entity = new TestEntity();
        entity.booleanProperty = true;
        entity.stringProperty = "bla";
        entity.floatProperty = 123;
        mStore.withDatabase(db).put(entity);
        TestEntity stored = mStore.withDatabase(db).query(TestEntity.class).withProjection(BaseColumns._ID, "booleanProperty").query().get();
        assertNotNull(stored);
        assertEquals(true, stored.booleanProperty);
        assertEquals(null, stored.stringProperty);
        assertEquals(0f, stored.floatProperty);
        Long id = stored._id;
        stored._id = null;
        // get without id just returns the object
        try {
            stored = mStore.withDatabase(db).get(stored);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {

        }
        stored._id = id;
        stored = mStore.withDatabase(db).get(stored);
        assertEquals(entity.booleanProperty, stored.booleanProperty);
        assertEquals(entity.stringProperty, stored.stringProperty);
        assertEquals(entity.floatProperty, stored.floatProperty);

    }

    public void testIteratorKeepsCursorPosition() {
        MatrixCursor cursor = new MatrixCursor(new String[] {"_id"});
        cursor.addRow(new Object[] {1L});
        cursor.addRow(new Object[] {2L});
        QueryResultIterable<TestEntity> iterable = new QueryResultIterable<TestEntity>(cursor, new DefaultConverter<TestEntity>(TestEntity.class, new HashMap<Class<?>, ConverterHolder<?>>()));
        TestEntity te = iterable.get();
        assertEquals(1L, te._id.longValue());
        te = iterable.get();
        assertEquals(1L, te._id.longValue());
        cursor.moveToPosition(-1);
        cursor.moveToNext();
        iterable = new QueryResultIterable<TestEntity>(cursor, new DefaultConverter<TestEntity>(TestEntity.class, new HashMap<Class<?>, ConverterHolder<?>>()));
        te = iterable.get();
        assertEquals(1L, te._id.longValue());
        te = iterable.get();
        assertEquals(1L, te._id.longValue());
    }

}
