package nl.qbusict.cupboard;

import java.util.List;

import nl.qbusict.cupboard.QueryResultIterable.QueryResultIterator;
import nl.qbusict.cupboard.convert.Converter;
import nl.qbusict.cupboard.convert.Converter.Column;
import android.database.Cursor;

public class TestHelper {
    public static <T> QueryResultIterator<T> createQueryResultIterator(Cursor cursor, Converter<T> translator) {
        return new QueryResultIterator<T>(cursor, translator);
    }

    public static PreferredColumnOrderCursorWrapper newPreferredColumnOrderCursorWrapper(Cursor cursor, List<Column> columns) {
        return new PreferredColumnOrderCursorWrapper(cursor, columns);
    }
}
