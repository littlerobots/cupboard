package nl.qbusict.cupboard;

import android.database.Cursor;

import java.util.List;

import nl.qbusict.cupboard.QueryResultIterable.QueryResultIterator;
import nl.qbusict.cupboard.convert.Converter;
import nl.qbusict.cupboard.convert.Converter.Column;

public class TestHelper {
    public static <T> QueryResultIterator<T> createQueryResultIterator(Cursor cursor, Converter<T> translator) {
        return new QueryResultIterator<T>(cursor, translator);
    }

    public static PreferredColumnOrderCursorWrapper newPreferredColumnOrderCursorWrapper(Cursor cursor, List<Column> columns) {
        return new PreferredColumnOrderCursorWrapper(cursor, columns);
    }
}
