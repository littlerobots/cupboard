package nl.qbusict.cupboard;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;

import nl.qbusict.cupboard.annotation.Index;

public class DatabaseCompartmentTest extends AndroidTestCase {

    private Cupboard mCupboard;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mCupboard = new CupboardBuilder().useAnnotations().build();
        mCupboard.register(TestIndexedEntity.class);
        mCupboard.register(AnotherIndexedEntity.class);
        getContext().deleteDatabase("test_cupboard.db");
    }

    public void testDropIndices() {
        TestDatabaseHelper helper = new TestDatabaseHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("create index custom_unindexed on TestIndexedEntity(unindexedProperty)");
        db.execSQL("create table mycustomtable(id integer primary key autoincrement, test text)");
        db.execSQL("create index cant_touch on mycustomtable(test)");

        // verify indices are created
        Cursor cursor = db.rawQuery("select name, sql from sqlite_master where type = 'index' and tbl_name = 'TestIndexedEntity'", null);
        assertEquals(2, cursor.getCount());
        cursor.close();

        cursor = db.rawQuery("select name, sql from sqlite_master where type = 'index' and tbl_name = 'AnotherIndexedEntity'", null);
        assertEquals(1, cursor.getCount());
        cursor.close();

        mCupboard.withDatabase(db).dropAllIndices();

        cursor = db.rawQuery("select name, sql from sqlite_master where type = 'index' and tbl_name = 'TestIndexedEntity'", null);
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = db.rawQuery("select name, sql from sqlite_master where type = 'index' and tbl_name = 'AnotherIndexedEntity'", null);
        assertEquals(0, cursor.getCount());
        cursor.close();

        // not touched by cupboard
        cursor = db.rawQuery("select name, sql from sqlite_master where type = 'index' and tbl_name = 'mycustomtable'", null);
        assertEquals(1, cursor.getCount());
        cursor.close();
    }

    public void testUpgradeIndices() {
        TestDatabaseHelper helper = new TestDatabaseHelper(getContext(), 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("create index custom_unindexed on TestIndexedEntity(unindexedProperty)");
        db.close();
        // upgrade
        helper = new TestDatabaseHelper(getContext(), 2);
        db = helper.getWritableDatabase();

        // should still have 2 indices: custom + @Indexed
        Cursor cursor = db.rawQuery("select name, sql from sqlite_master where type = 'index' and tbl_name = 'TestIndexedEntity'", null);
        assertEquals(2, cursor.getCount());
        cursor.close();
    }

    public static class TestIndexedEntity {
        public Long _id;

        @Index
        public String indexedProperty;
        public String unindexedProperty;
    }

    public static class AnotherIndexedEntity {
        public Long _id;

        @Index
        public String indexedProperty;
        public String unindexedProperty;
    }

    private class TestDatabaseHelper extends SQLiteOpenHelper {
        public TestDatabaseHelper(Context context, int version) {
            super(context, "test_cupboard.db", null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mCupboard.withDatabase(db).createTables();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            mCupboard.withDatabase(db).upgradeTables();
        }
    }
}
