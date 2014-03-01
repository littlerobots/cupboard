package nl.qbusict.cupboard;

import android.database.Cursor;

import java.util.List;

import nl.qbusict.cupboard.QueryResultIterable.QueryResultIterator;
import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.EntityConverter.Column;

public class TestHelper {
    public static <T> QueryResultIterator<T> createQueryResultIterator(Cursor cursor, EntityConverter<T> converter) {
        return new QueryResultIterator<T>(cursor, converter);
    }

    public static PreferredColumnOrderCursorWrapper newPreferredColumnOrderCursorWrapper(Cursor cursor, List<Column> columns) {
        return new PreferredColumnOrderCursorWrapper(cursor, columns);
    }
}
