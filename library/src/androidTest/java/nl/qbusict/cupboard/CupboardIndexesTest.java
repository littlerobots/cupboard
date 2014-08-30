package nl.qbusict.cupboard;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nl.qbusict.cupboard.annotation.CompositeIndex;
import nl.qbusict.cupboard.annotation.Index;

public class CupboardIndexesTest extends AndroidTestCase {

    private Cupboard mStore;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mStore = new CupboardBuilder().useAnnotations().build();
        getContext().deleteDatabase("test_ls.db");
    }

    public void testCreateIndexes() {
        mStore.register(TestIndexAnnotatedEntity.class);
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cursor = db.rawQuery("PRAGMA INDEX_LIST('TestIndexAnnotatedEntity')", null);

        Map<String, Map<String, Object>> indexes = new HashMap<String, Map<String, Object>>();

        while (cursor.moveToNext()) {
            Map<String, Object> indexMap = new HashMap<String, Object>();
            indexMap.put("unique", cursor.getInt(cursor.getColumnIndex("unique")) == 1);
            indexes.put(cursor.getString(cursor.getColumnIndex("name")), indexMap);
        }
        cursor.close();

        for (Entry<String, Map<String, Object>> indexEntry : indexes.entrySet()) {
            cursor = db.rawQuery("PRAGMA index_info(" + indexEntry.getKey() + ")", null);
            String[] columnNames = new String[cursor.getCount()];
            int j = 0;
            while (cursor.moveToNext()) {
                columnNames[j] = cursor.getString(cursor.getColumnIndex("name"));
                j++;
            }
            indexEntry.getValue().put("columns", columnNames);
            cursor.close();
        }
        assertEquals(indexes.size(), 6);

        Map<String, Object> index = indexes.get("TestIndexAnnotatedEntity_simpleIndex");
        assertEquals(index.get("unique"), false);
        String[] columnNames = (String[]) index.get("columns");
        assertEquals(columnNames.length, 1);
        assertEquals(columnNames[0], "simpleIndex");

        index = indexes.get("TestIndexAnnotatedEntity_uniqueIndex");
        assertEquals(index.get("unique"), true);
        columnNames = (String[]) index.get("columns");
        assertEquals(columnNames.length, 1);
        assertEquals(columnNames[0], "uniqueIndex");

        index = indexes.get("sharedIndex");
        assertEquals(index.get("unique"), false);
        columnNames = (String[]) index.get("columns");
        assertEquals(columnNames.length, 3);
        assertEquals(columnNames[0], "sharedIndexOne");
        assertEquals(columnNames[1], "sharedIndexThree");
        assertEquals(columnNames[2], "sharedIndexTwo");

        index = indexes.get("singleIndexThree");
        assertEquals(index.get("unique"), false);
        columnNames = (String[]) index.get("columns");
        assertEquals(columnNames.length, 1);
        assertEquals(columnNames[0], "sharedIndexThree");

        index = indexes.get("sharedUniqueIndex");
        assertEquals(index.get("unique"), true);
        columnNames = (String[]) index.get("columns");
        assertEquals(columnNames.length, 3);
        assertEquals(columnNames[0], "sharedUniqueOne");
        assertEquals(columnNames[1], "sharedUniqueTwo");
        assertEquals(columnNames[2], "sharedUniqueThree");

        index = indexes.get("sharedUniqueIndexTwo");
        assertEquals(index.get("unique"), true);
        columnNames = (String[]) index.get("columns");
        assertEquals(columnNames.length, 2);
        assertEquals(columnNames[0], "sharedUniqueThree");
        assertEquals(columnNames[1], "sharedUniqueFour");
        db.close();
    }

    public void testCreateFailByDuplicateIndexOrder() {
        mStore.register(DuplicateIndexOrderEntity.class);
        DBHelper helper = new DBHelper(getContext(), 1);
        try {
            helper.getWritableDatabase();
            fail("Two columns under the same composite index cannot have the same order");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCreateFailByDuplicateIndexName() {
        mStore.register(DuplicateIndexNameEntity.class);
        DBHelper helper = new DBHelper(getContext(), 1);
        try {
            helper.getWritableDatabase();
            fail("Unique and non-unique indexes cannot use the same name");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCreateFailByDuplicateIndexNameInColumn() {
        mStore.register(DuplicateIndexNameInColumnEntity.class);
        DBHelper helper = new DBHelper(getContext(), 1);
        try {
            helper.getWritableDatabase();
            fail("Cannot have two indexes on a column under the same name");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testUpgradeAddIndexes() {
        mStore = new CupboardBuilder().build();
        mStore.register(TestIndexAnnotatedEntity.class);
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("PRAGMA INDEX_LIST('TestIndexAnnotatedEntity')", null);
        assertTrue(cursor.getCount() == 0);
        db.close();

        mStore = new CupboardBuilder().useAnnotations().build();
        mStore.register(TestIndexAnnotatedEntity.class);
        helper = new DBHelper(getContext(), 2);
        db = helper.getWritableDatabase();
        cursor = db.rawQuery("PRAGMA INDEX_LIST('TestIndexAnnotatedEntity')", null);
        assertTrue(cursor.getCount() >= 0);
        db.close();
    }

    public void testDropIndexesWithTables() {
        mStore.register(TestIndexAnnotatedEntity.class);
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("PRAGMA INDEX_LIST('TestIndexAnnotatedEntity')", null);
        assertTrue(cursor.getCount() > 0);
        cursor.close();
        mStore.withDatabase(db).dropAllTables();
        cursor = db.rawQuery("pragma INDEX_LIST('TestIndexAnnotatedEntity')", null);
        assertFalse(cursor.getCount() > 0);
        cursor.close();

        // we can repeat calls to dropAllTables without it throwing exceptions
        mStore.withDatabase(db).dropAllTables();
        cursor = db.rawQuery("pragma INDEX_LIST('TestIndexAnnotatedEntity')", null);
        assertFalse(cursor.getCount() > 0);
        cursor.close();
        db.close();
    }

    public void testUpgradeAddColumnIndex() {
        mStore.register(TestIndexAnnotatedEntity.class);
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        // Remove TestIndexAnnotatedEntity_uniqueIndex
        db.execSQL("drop index 'TestIndexAnnotatedEntity_uniqueIndex'");

        Cursor cursor = db.rawQuery("PRAGMA INDEX_LIST('TestIndexAnnotatedEntity')", null);
        assertEquals(cursor.getCount(), 5);
        db.close();

        // UPGRADE, index should be added back again
        helper = new DBHelper(getContext(), 2);
        db = helper.getWritableDatabase();
        cursor = db.rawQuery("select name, sql from sqlite_master where type = 'index' and tbl_name = 'TestIndexAnnotatedEntity'", null);
        assertEquals(cursor.getCount(), 6);

        // 'uniqueIndex' TEXT column should be added with index "create unique index TestIndexAnnotatedEntity_uniqueIndex on TestIndexAnnotatedEntity ('uniqueIndex' ASC)"
        boolean found = false;
        String creationStatement = "create unique index TestIndexAnnotatedEntity_uniqueIndex on TestIndexAnnotatedEntity ('uniqueIndex' ASC)";
        while (cursor.moveToNext() && !found) {
            found = cursor.getString(cursor.getColumnIndex("name")).equalsIgnoreCase("TestIndexAnnotatedEntity_uniqueIndex") &&
                    cursor.getString(cursor.getColumnIndex("sql")).equalsIgnoreCase(creationStatement);
        }
        assertTrue(found);
        cursor.close();
        db.close();
    }

    public void testUpgradeRemoveColumnIndex() {
        mStore.register(TestIndexAnnotatedEntity.class);
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        // This is added by hand, should be removed on upgrade
        db.execSQL("create unique index extra on TestIndexAnnotatedEntity ('uniqueIndex' DESC)");

        Cursor cursor = db.rawQuery("PRAGMA INDEX_LIST('TestIndexAnnotatedEntity')", null);
        assertEquals(cursor.getCount(), 7);
        db.close();

        helper = new DBHelper(getContext(), 2);
        db = helper.getWritableDatabase();
        cursor = db.rawQuery("select name, sql from sqlite_master where type = 'index' and tbl_name = 'TestIndexAnnotatedEntity'", null);
        assertEquals(cursor.getCount(), 6);

        boolean extraFound = false;
        while (cursor.moveToNext() && !extraFound) {
            extraFound = cursor.getString(cursor.getColumnIndex("name")).equalsIgnoreCase("extra");
        }
        assertFalse(extraFound);
        cursor.close();
        db.close();
    }

    public void testUpgradeModifyColumnIndex() {
        mStore.register(TestIndexAnnotatedEntity.class);
        DBHelper helper = new DBHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        // This is modified by hand, should be modified back on upgrade
        db.execSQL("drop index TestIndexAnnotatedEntity_simpleIndex");
        db.execSQL("create unique index TestIndexAnnotatedEntity_simpleIndex on TestIndexAnnotatedEntity ('simpleIndex' DESC)");

        Cursor cursor = db.rawQuery("select name, sql from sqlite_master where type = 'index' and tbl_name = 'TestIndexAnnotatedEntity'", null);
        assertEquals(cursor.getCount(), 6);
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex("name")).equalsIgnoreCase("TestIndexAnnotatedEntity_simpleIndex")) {
                assertEquals(cursor.getString(cursor.getColumnIndex("sql")).toLowerCase(),
                        "create unique index TestIndexAnnotatedEntity_simpleIndex on TestIndexAnnotatedEntity ('simpleIndex' DESC)".toLowerCase());
            }
        }
        db.close();

        helper = new DBHelper(getContext(), 2);
        db = helper.getWritableDatabase();
        cursor = db.rawQuery("select name, sql from sqlite_master where type = 'index' and tbl_name = 'TestIndexAnnotatedEntity'", null);
        assertEquals(cursor.getCount(), 6);

        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex("name")).equalsIgnoreCase("TestIndexAnnotatedEntity_simpleIndex")) {
                assertEquals(cursor.getString(cursor.getColumnIndex("sql")).toLowerCase(),
                        "create index TestIndexAnnotatedEntity_simpleIndex on TestIndexAnnotatedEntity ('simpleIndex' ASC)".toLowerCase());
            }
        }
        cursor.close();
        db.close();
    }

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
}

class DuplicateIndexOrderEntity {
    public Long _id;

    @Index(indexNames = {@CompositeIndex(ascending = true, indexName = "foobar")})
    public String foo;

    @Index(indexNames = {@CompositeIndex(ascending = false, indexName = "foobar")})
    public String bar;
}

class DuplicateIndexNameEntity {
    public Long _id;

    @Index(indexNames = {@CompositeIndex(ascending = true, indexName = "foobar")})
    public String foo;

    @Index(uniqueNames = {@CompositeIndex(ascending = false, indexName = "foobar")})
    public String bar;
}

class DuplicateIndexNameInColumnEntity {
    public Long _id;

    @Index(indexNames = {@CompositeIndex(ascending = true, indexName = "foobar"),
            @CompositeIndex(ascending = false, indexName = "foobar")})
    public String foo;

}

