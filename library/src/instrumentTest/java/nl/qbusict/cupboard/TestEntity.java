package nl.qbusict.cupboard;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nl.qbusict.cupboard.convert.EntityConverter;

public class TestEntity implements EntityConverter<TestEntity> {
    public static enum TestEnum {TEST1, TEST2}

    public Long _id;
    public String stringProperty;
    public int intProperty;
    public Integer intObjectProperty;
    public long longProperty;
    public Long longObjectProperty;
    public short shortProperty;
    public Short shortObjectProperty;
    public double doubleProperty;
    public Double doubleObjectProperty;
    public float floatProperty;
    public Float floatObjectProperty;
    public byte[] byteArrayProperty;
    public boolean booleanProperty;
    public Boolean booleanObjectProperty;
    public Date dateProperty;
    public TestEnum enumProperty;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestEntity that = (TestEntity) o;

        if (booleanProperty != that.booleanProperty) return false;
        if (Double.compare(that.doubleProperty, doubleProperty) != 0) return false;
        if (Float.compare(that.floatProperty, floatProperty) != 0) return false;
        if (intProperty != that.intProperty) return false;
        if (longProperty != that.longProperty) return false;
        if (shortProperty != that.shortProperty) return false;
        if (_id != null ? !_id.equals(that._id) : that._id != null) return false;
        if (booleanObjectProperty != null ? !booleanObjectProperty.equals(that.booleanObjectProperty) : that.booleanObjectProperty != null)
            return false;
        if (!Arrays.equals(byteArrayProperty, that.byteArrayProperty)) return false;
        if (dateProperty != null ? !dateProperty.equals(that.dateProperty) : that.dateProperty != null)
            return false;
        if (doubleObjectProperty != null ? !doubleObjectProperty.equals(that.doubleObjectProperty) : that.doubleObjectProperty != null)
            return false;
        if (enumProperty != that.enumProperty) return false;
        if (floatObjectProperty != null ? !floatObjectProperty.equals(that.floatObjectProperty) : that.floatObjectProperty != null)
            return false;
        if (intObjectProperty != null ? !intObjectProperty.equals(that.intObjectProperty) : that.intObjectProperty != null)
            return false;
        if (longObjectProperty != null ? !longObjectProperty.equals(that.longObjectProperty) : that.longObjectProperty != null)
            return false;
        if (shortObjectProperty != null ? !shortObjectProperty.equals(that.shortObjectProperty) : that.shortObjectProperty != null)
            return false;
        if (stringProperty != null ? !stringProperty.equals(that.stringProperty) : that.stringProperty != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = _id != null ? _id.hashCode() : 0;
        result = 31 * result + (stringProperty != null ? stringProperty.hashCode() : 0);
        result = 31 * result + intProperty;
        result = 31 * result + (intObjectProperty != null ? intObjectProperty.hashCode() : 0);
        result = 31 * result + (int) (longProperty ^ (longProperty >>> 32));
        result = 31 * result + (longObjectProperty != null ? longObjectProperty.hashCode() : 0);
        result = 31 * result + (int) shortProperty;
        result = 31 * result + (shortObjectProperty != null ? shortObjectProperty.hashCode() : 0);
        temp = Double.doubleToLongBits(doubleProperty);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (doubleObjectProperty != null ? doubleObjectProperty.hashCode() : 0);
        result = 31 * result + (floatProperty != +0.0f ? Float.floatToIntBits(floatProperty) : 0);
        result = 31 * result + (floatObjectProperty != null ? floatObjectProperty.hashCode() : 0);
        result = 31 * result + (byteArrayProperty != null ? Arrays.hashCode(byteArrayProperty) : 0);
        result = 31 * result + (booleanProperty ? 1 : 0);
        result = 31 * result + (booleanObjectProperty != null ? booleanObjectProperty.hashCode() : 0);
        result = 31 * result + (dateProperty != null ? dateProperty.hashCode() : 0);
        result = 31 * result + (enumProperty != null ? enumProperty.hashCode() : 0);
        return result;
    }

    @Override
    public TestEntity fromCursor(Cursor cursor) {
        TestEntity te = new TestEntity();
        String[] cols = cursor.getColumnNames();
        int index = 0;
        for (String col : cols) {
            if ("_id".equals(col)) {
                te._id = cursor.getLong(index);
            }
            index++;
        }
        return te;
    }

    @Override
    public void toValues(TestEntity object, ContentValues values) {
        values.put("_id", _id);
        values.put("stringproperty", stringProperty);
    }

    @Override
    public List<Column> getColumns() {
        return Arrays.asList(new Column("_id", ColumnType.INTEGER), new Column("stringproperty", ColumnType.TEXT));
    }

    @Override
    public void setId(Long id, TestEntity instance) {
        instance._id = id;
    }

    @Override
    public Long getId(TestEntity instance) {
        return instance._id;
    }

    @Override
    public String getTable() {
        return "TestEntity";
    }
}