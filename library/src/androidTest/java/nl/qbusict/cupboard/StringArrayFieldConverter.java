package nl.qbusict.cupboard;

import android.content.ContentValues;
import android.database.Cursor;

import nl.qbusict.cupboard.convert.EntityConverter.ColumnType;
import nl.qbusict.cupboard.convert.FieldConverter;

public class StringArrayFieldConverter implements FieldConverter<String[]> {
    @Override
    public String[] fromCursorValue(Cursor cursor, int columnIndex) {
        return cursor.getString(columnIndex).split(",");
    }

    @Override
    public void toContentValue(String[] value, String key, ContentValues values) {
    }

    @Override
    public ColumnType getColumnType() {
        return ColumnType.JOIN;
    }
}
