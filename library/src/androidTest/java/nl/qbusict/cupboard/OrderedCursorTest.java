package nl.qbusict.cupboard;

import android.database.MatrixCursor;
import android.test.AndroidTestCase;

import java.util.Arrays;

public class OrderedCursorTest extends AndroidTestCase {
    public void testCursorRemap() {
        MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
        cursor.addRow(new String[]{"a", "b", "c"});
        PreferredColumnOrderCursorWrapper wrapper = new PreferredColumnOrderCursorWrapper(cursor, new String[]{"c", "a", "b"});
        assertTrue(Arrays.equals(new String[]{"c", "a", "b"}, wrapper.getColumnNames()));
        wrapper.moveToNext();
        assertEquals("a", wrapper.getString(1));
    }

    public void testCursorRemapMissingColumns() {
        MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
        cursor.addRow(new String[]{"a", "b", "c"});
        PreferredColumnOrderCursorWrapper wrapper = new PreferredColumnOrderCursorWrapper(cursor, new String[]{"m1", "c", "a", "b", "m2", "m3"});
        assertTrue(Arrays.equals(new String[]{"m1", "c", "a", "b"}, wrapper.getColumnNames()));
        wrapper.moveToNext();
        assertEquals(4, wrapper.getColumnCount());
        assertEquals("a", wrapper.getString(2));
        assertTrue(wrapper.isNull(0));
    }

}
