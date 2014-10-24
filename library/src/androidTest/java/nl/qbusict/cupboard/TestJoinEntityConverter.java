package nl.qbusict.cupboard;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.Arrays;
import java.util.List;

import nl.qbusict.cupboard.convert.EntityConverter;

public class TestJoinEntityConverter implements EntityConverter<TestJoinEntity> {
    @Override
    public TestJoinEntity fromCursor(Cursor cursor) {
        TestJoinEntity entity = new TestJoinEntity();
        entity._id = cursor.getLong(0);
        String names = cursor.getString(1);
        if (names != null) {
            entity.names = names.split(",");
        }
        return entity;
    }

    @Override
    public void toValues(TestJoinEntity object, ContentValues values) {
        if (object._id != null) {
            values.put(BaseColumns._ID, object._id);
        }
    }

    @Override
    public List<Column> getColumns() {
        return Arrays.asList(new Column("_id", ColumnType.INTEGER), new Column("names", ColumnType.JOIN));
    }

    @Override
    public void setId(Long id, TestJoinEntity instance) {
        instance._id = id;
    }

    @Override
    public Long getId(TestJoinEntity instance) {
        return instance._id;
    }

    @Override
    public String getTable() {
        return TestJoinEntity.class.getSimpleName();
    }
}
