package nl.qbusict.cupboard;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import java.io.File;
import java.util.Date;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class MicroPerformanceTest extends AndroidTestCase {

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

    private TestSQLiteOpenHelper mHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "perf");
        context.deleteDatabase("test");
        mHelper = new TestSQLiteOpenHelper(context);
        CupboardFactory.setCupboard(mHelper.getCupboard());
        setupDatabase(mHelper.getWritableDatabase());
    }

    private void setupDatabase(SQLiteDatabase db) {
        TestEntity te = new TestEntity();
        te.booleanObjectProperty = false;
        te.booleanProperty = true;
        te.dateProperty = new Date();
        te.doubleObjectProperty = 100d;
        te.doubleProperty = 200.567d;
        te.floatObjectProperty = 2.5f;
        te.floatProperty = 6.5f;
        te.intObjectProperty = 100;
        te.intProperty = 10000;
        te.longObjectProperty = 20000L;
        te.longProperty = 30000L;
        te.shortProperty = 2;
        te.shortObjectProperty = 3;
        te.stringProperty = "test";

        db.beginTransaction();
        for (int i = 0; i < 15000; i++) {
            te._id = null;
            cupboard().withDatabase(db).put(te);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    public void testIterateCursor() {
        Cursor cursor = mHelper.getWritableDatabase().query(cupboard().withEntity(TestEntity.class).getTable(), null, null, null, null, null, null);
        int count = cursor.getColumnCount();
        //Debug.startMethodTracing("cupboard");
        while (cursor.moveToNext()) {
            for (int i = 0; i < count; i++) {
                if (!cursor.isNull(i)) {
                    cursor.getString(i);
                }
            }
        }
        //Debug.stopMethodTracing();
        cursor.close();
    }

    public void testIterateCupboard() {
        QueryResultIterable<TestEntity> itr = cupboard().withDatabase(mHelper.getWritableDatabase()).query(TestEntity.class).query();
        File dir = getContext().getExternalFilesDir(null);
        dir.mkdirs();

        //Debug.startMethodTracing(new File(dir, "cupboard").toString());
        for (TestEntity te : itr) {
            // noop
            if (te._id != null) {
            }
        }
        //Debug.stopMethodTracing();
        itr.close();
    }
}
