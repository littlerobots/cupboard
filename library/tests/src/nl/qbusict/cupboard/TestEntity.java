package nl.qbusict.cupboard;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nl.qbusict.cupboard.convert.Converter;
import android.content.ContentValues;
import android.database.Cursor;

public class TestEntity implements Converter<TestEntity>{
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_id == null) ? 0 : _id.hashCode());
        result = prime * result + ((booleanObjectProperty == null) ? 0 : booleanObjectProperty.hashCode());
        result = prime * result + (booleanProperty ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(byteArrayProperty);
        result = prime * result + ((dateProperty == null) ? 0 : dateProperty.hashCode());
        result = prime * result + ((doubleObjectProperty == null) ? 0 : doubleObjectProperty.hashCode());
        long temp;
        temp = Double.doubleToLongBits(doubleProperty);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((floatObjectProperty == null) ? 0 : floatObjectProperty.hashCode());
        result = prime * result + Float.floatToIntBits(floatProperty);
        result = prime * result + ((intObjectProperty == null) ? 0 : intObjectProperty.hashCode());
        result = prime * result + intProperty;
        result = prime * result + ((longObjectProperty == null) ? 0 : longObjectProperty.hashCode());
        result = prime * result + (int) (longProperty ^ (longProperty >>> 32));
        result = prime * result + ((shortObjectProperty == null) ? 0 : shortObjectProperty.hashCode());
        result = prime * result + shortProperty;
        result = prime * result + ((stringProperty == null) ? 0 : stringProperty.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestEntity other = (TestEntity) obj;
        if (_id == null) {
            if (other._id != null)
                return false;
        } else if (!_id.equals(other._id))
            return false;
        if (booleanObjectProperty == null) {
            if (other.booleanObjectProperty != null)
                return false;
        } else if (!booleanObjectProperty.equals(other.booleanObjectProperty))
            return false;
        if (booleanProperty != other.booleanProperty)
            return false;
        if (!Arrays.equals(byteArrayProperty, other.byteArrayProperty))
            return false;
        if (dateProperty == null) {
            if (other.dateProperty != null)
                return false;
        } else if (!dateProperty.equals(other.dateProperty))
            return false;
        if (doubleObjectProperty == null) {
            if (other.doubleObjectProperty != null)
                return false;
        } else if (!doubleObjectProperty.equals(other.doubleObjectProperty))
            return false;
        if (Double.doubleToLongBits(doubleProperty) != Double.doubleToLongBits(other.doubleProperty))
            return false;
        if (floatObjectProperty == null) {
            if (other.floatObjectProperty != null)
                return false;
        } else if (!floatObjectProperty.equals(other.floatObjectProperty))
            return false;
        if (Float.floatToIntBits(floatProperty) != Float.floatToIntBits(other.floatProperty))
            return false;
        if (intObjectProperty == null) {
            if (other.intObjectProperty != null)
                return false;
        } else if (!intObjectProperty.equals(other.intObjectProperty))
            return false;
        if (intProperty != other.intProperty)
            return false;
        if (longObjectProperty == null) {
            if (other.longObjectProperty != null)
                return false;
        } else if (!longObjectProperty.equals(other.longObjectProperty))
            return false;
        if (longProperty != other.longProperty)
            return false;
        if (shortObjectProperty == null) {
            if (other.shortObjectProperty != null)
                return false;
        } else if (!shortObjectProperty.equals(other.shortObjectProperty))
            return false;
        if (shortProperty != other.shortProperty)
            return false;
        if (stringProperty == null) {
            if (other.stringProperty != null)
                return false;
        } else if (!stringProperty.equals(other.stringProperty))
            return false;
        return true;
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
    public List<Converter.Column> getColumns() {
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