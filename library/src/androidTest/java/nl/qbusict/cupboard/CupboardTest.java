package nl.qbusict.cupboard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.test.AndroidTestCase;

import java.util.HashMap;
import java.util.Map;

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

    public void testPutReplaces() {
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        TestEntity entity = new TestEntity();
        entity.stringProperty = "Test";
        long id = mStore.withDatabase(db).put(entity);
        assertNotNull(entity._id);
        mStore.withDatabase(db).put(entity);
        assertEquals(id, entity._id.longValue());
        Cursor cursor = db.query(mStore.getTable(TestEntity.class), null, null, null, null, null, null);
        assertEquals(1, cursor.getCount());
        cursor.close();
    }

    public void testBooleanQuery() {
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        TestEntity entity = new TestEntity();
        entity.booleanObjectProperty = true;
        mStore.withDatabase(db).put(entity);
        Cursor cursor = db.query(mStore.getTable(TestEntity.class), null, "booleanObjectProperty = ?", new String[]{String.valueOf(1)}, null, null, null, null);
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
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id"});
        cursor.addRow(new Object[]{1L});
        cursor.addRow(new Object[]{2L});
        QueryResultIterable<TestEntity> iterable = new QueryResultIterable<TestEntity>(cursor, mStore.getEntityConverter(TestEntity.class));
        TestEntity te = iterable.get();
        assertEquals(1L, te._id.longValue());
        te = iterable.get();
        assertEquals(1L, te._id.longValue());
        cursor.moveToPosition(-1);
        cursor.moveToNext();
        iterable = new QueryResultIterable<TestEntity>(cursor, mStore.getEntityConverter(TestEntity.class));
        te = iterable.get();
        assertEquals(1L, te._id.longValue());
        te = iterable.get();
        assertEquals(1L, te._id.longValue());
    }

    public void testUpgradeTableCaseInsensitive() {
        SQLiteOpenHelper helper = new SQLiteOpenHelper(getContext(), "test_ls.db", null, 1) {

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                // create the table using some mixed casing
                db.execSQL("create table TESTENTITY (_ID integer primary key autoincrement, StringPropertY text)");
            }
        };

        helper.getWritableDatabase().close();
        helper = new DBHelper(getContext(), 2);
        // upgrade should work
        helper.getWritableDatabase().close();
    }

    public void testJoinCursor() {
        mStore.register(ReferencedEntity.class);
        mStore.register(TestEntityWithReference.class);
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        ReferencedEntity ref = new ReferencedEntity();
        ref.prop = "abc";
        TestEntityWithReference test = new TestEntityWithReference();
        test.prop = "efg";
        test.ref = ref;
        mStore.withDatabase(db).put(ref, test);
        test = mStore.withDatabase(db).get(TestEntityWithReference.class, test._id);
        assertNotNull(test.ref);
        assertNotNull(test.ref._id);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(mStore.getTable(TestEntityWithReference.class) + " as t, " + mStore.getTable(ReferencedEntity.class) + " as r");
        qb.appendWhere("t.ref = r._id");
        Map<String, String> projectionMap = new HashMap<String, String>();
        projectionMap.put("_id", "t._id");
        projectionMap.put("prop", "r.prop");
        projectionMap.put("t.prop", "t.prop");
        qb.setProjectionMap(projectionMap);
        Cursor cursor = qb.query(db, new String[]{"_id", "prop", "t.prop as t_prop"}, null, null, null, null, null, null);
        test = mStore.withCursor(cursor).get(TestEntityWithReference.class);
        assertEquals("abc", test.prop);
    }

    public void testLimit() {
        TestEntity te = new TestEntity();
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        mStore.withDatabase(db).put(te);
        te._id = null;
        mStore.withDatabase(db).put(te);
        te._id = null;
        mStore.withDatabase(db).put(te);
        Cursor cursor = mStore.withDatabase(db).query(TestEntity.class).getCursor();
        assertEquals(3, cursor.getCount());
        cursor.close();
        cursor = mStore.withDatabase(db).query(TestEntity.class).limit(1).getCursor();
        assertEquals(1, cursor.getCount());
    }

    public void testDistinct() {
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        TestEntity te = new TestEntity();
        te.stringProperty = "test";
        mStore.withDatabase(db).put(te);
        te._id = null;
        mStore.withDatabase(db).put(te);
        te.stringProperty = "test2";
        te._id = null;
        mStore.withDatabase(db).put(te);
        Cursor cursor = mStore.withDatabase(db).query(TestEntity.class).withProjection("stringProperty").getCursor();
        assertEquals(3, cursor.getCount());
        cursor.close();
        cursor = mStore.withDatabase(db).query(TestEntity.class).withProjection("stringProperty").distinct().getCursor();
        assertEquals(2, cursor.getCount());
    }

    public void testCreateKeywordTable() {
        mStore.register(Group.class);
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("pragma table_info('Group')", null);
        assertTrue(cursor.getCount() > 0);
        Group group = new Group();
        mStore.withDatabase(db).put(group);
        mStore.withDatabase(db).query(Group.class).get();
        ContentValues values = new ContentValues();
        values.put("value", "hi");
        mStore.withDatabase(db).update(Group.class, values);
        mStore.withDatabase(db).delete(Group.class, null);
    }

    public void testDropTables() {
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("pragma table_info('TestEntity')", null);
        assertTrue(cursor.getCount() > 0);
        cursor.close();
        mStore.withDatabase(db).dropAllTables();
        cursor = db.rawQuery("pragma table_info('TestEntity')", null);
        assertFalse(cursor.getCount() > 0);
        cursor.close();
        TestEntity testEntity = new TestEntity();
        try {
            mStore.withDatabase(db).put(testEntity);
            fail("Should throw exception due to missing table");
        } catch (SQLiteException expected) {

        }
        // we can repeat calls to dropAllTables without it throwing exceptions
        mStore.withDatabase(db).dropAllTables();
        cursor = db.rawQuery("pragma table_info('TestEntity')", null);
        assertFalse(cursor.getCount() > 0);
        cursor.close();
    }

}
