/*
 * Copyright (C) 2013 Qbus B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.qbusict.cupboard.convert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
/**
 * Converter translates object instances to {@link ContentValues} and {@link Cursor}s to objects.
 * One can specify whether to check for Annotations or not when instantiating or ad-hoc via setter method.
 * @param <T> The object type
 */
public class DefaultConverter<T> implements Converter<T> {

    private boolean mUsingAnnotations = false;

    private static class Property {
        Field field;
        String name;
        Class<?> type;
        TypeConverter<Object> typeConverter;
        ColumnType columnType;
    }

    private static interface TypeConverter<F> {
        public F fromCursorValue(Cursor cursor, int columnIndex);
        public void toContentValue(F value, String key, ContentValues values);
        public ColumnType getColumnType();
    }

    private static class StringConverter implements TypeConverter<String> {
        @Override
        public String fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getString(columnIndex);
        }

        @Override
        public void toContentValue(String value, String key, ContentValues values) {
            values.put(key, value);
        }

        @Override
        public nl.qbusict.cupboard.convert.Converter.ColumnType getColumnType() {
            return ColumnType.TEXT;
        }
    }

    private static class IntegerConverter implements TypeConverter<Integer> {
        @Override
        public Integer fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getInt(columnIndex);
        }

        @Override
        public void toContentValue(Integer value, String key, ContentValues values) {
            values.put(key, value);
        }

        @Override
        public nl.qbusict.cupboard.convert.Converter.ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }
    }

    private static class FloatConverter implements TypeConverter<Float> {
        @Override
        public Float fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getFloat(columnIndex);
        }

        @Override
        public void toContentValue(Float value, String key, ContentValues values) {
            values.put(key, value);
        }

        @Override
        public nl.qbusict.cupboard.convert.Converter.ColumnType getColumnType() {
            return ColumnType.REAL;
        }
    }

    private static class ShortConverter implements TypeConverter<Short> {
        @Override
        public Short fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getShort(columnIndex);
        }

        @Override
        public void toContentValue(Short value, String key, ContentValues values) {
            values.put(key, value);
        }

        @Override
        public nl.qbusict.cupboard.convert.Converter.ColumnType getColumnType() {
            return ColumnType.REAL;
        }
    }

    private static class DoubleConverter implements TypeConverter<Double> {
        @Override
        public Double fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getDouble(columnIndex);
        }

        @Override
        public void toContentValue(Double value, String key, ContentValues values) {
            values.put(key, value);
        }

        @Override
        public nl.qbusict.cupboard.convert.Converter.ColumnType getColumnType() {
            return ColumnType.REAL;
        }
    }

    private static class LongConverter implements TypeConverter<Long> {
        @Override
        public Long fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getLong(columnIndex);
        }

        @Override
        public void toContentValue(Long value, String key, ContentValues values) {
            values.put(key, value);
        }

        @Override
        public nl.qbusict.cupboard.convert.Converter.ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }
    }

    private static class ByteConverter implements TypeConverter<Byte> {
        @Override
        public Byte fromCursorValue(Cursor cursor, int columnIndex) {
            return (byte) cursor.getInt(columnIndex);
        }

        @Override
        public void toContentValue(Byte value, String key, ContentValues values) {
            values.put(key, value);
        }

        @Override
        public nl.qbusict.cupboard.convert.Converter.ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }
    }

    private static class ByteArrayConverter implements TypeConverter<byte[]> {
        @Override
        public byte[] fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getBlob(columnIndex);
        }

        @Override
        public void toContentValue(byte[] value, String key, ContentValues values) {
            values.put(key, value);
        }

        @Override
        public nl.qbusict.cupboard.convert.Converter.ColumnType getColumnType() {
            return ColumnType.BLOB;
        }
    }

    private static class BooleanConverter implements TypeConverter<Boolean> {
        @Override
        public Boolean fromCursorValue(Cursor cursor, int columnIndex) {
            try {
                return cursor.getInt(columnIndex) == 1;
            } catch (NumberFormatException ex) {
                return "true".equals(cursor.getString(columnIndex));
            }
        }

        @Override
        public void toContentValue(Boolean value, String key, ContentValues values) {
            values.put(key, value);
        }

        @Override
        public nl.qbusict.cupboard.convert.Converter.ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }
    }

    private static class DateConverter implements TypeConverter<Date> {
        @Override
        public Date fromCursorValue(Cursor cursor, int columnIndex) {
            return new Date(cursor.getLong(columnIndex));
        }

        @Override
        public void toContentValue(Date value, String key, ContentValues values) {
            values.put(key, value.getTime());
        }

        @Override
        public nl.qbusict.cupboard.convert.Converter.ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }
    }

    private static class EntityConverter<T> implements TypeConverter<T> {
        private final ConverterHolder<T> mTranslatorHolder;
        private final Class<T> entityClass;
        private final Converter<T> mTranslator;

        public EntityConverter(Class<T> clz, ConverterHolder<T> translator) {
            this.mTranslatorHolder = translator;
            this.entityClass = clz;
            this.mTranslator = null;
        }

        public EntityConverter(Class<T> clz, Converter<T> translator) {
            this.mTranslatorHolder = null;
            this.entityClass = clz;
            this.mTranslator = translator;
        }

        @Override
        public T fromCursorValue(Cursor cursor, int columnIndex) {
            long id = cursor.getLong(columnIndex);
            T entity;
            try {
                entity = entityClass.newInstance();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
            getTranslator().setId(id, entity);
            return entity;
        }

        private Converter<T> getTranslator() {
            if (mTranslator != null) {
                return mTranslator;
            }
            return mTranslatorHolder.get();
        }

        @Override
        public void toContentValue(T value, String key, ContentValues values) {
            values.put(key, getTranslator().getId(value));
        }

        @Override
        public nl.qbusict.cupboard.convert.Converter.ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }

    }

    private final Property[] mProperties;
    private Property mIdProperty = null;
    private final Class<T> mClass;
    private final List<Column> mColumns;
    private static HashMap<Class<?>, TypeConverter<?>> sTypeConverters = new HashMap<Class<?>, DefaultConverter.TypeConverter<?>>(10);

    static {
        sTypeConverters.put(String.class, new StringConverter());
        sTypeConverters.put(int.class, new IntegerConverter());
        sTypeConverters.put(Integer.class, new IntegerConverter());
        sTypeConverters.put(float.class, new FloatConverter());
        sTypeConverters.put(Float.class, new FloatConverter());
        sTypeConverters.put(short.class, new ShortConverter());
        sTypeConverters.put(Short.class, new ShortConverter());
        sTypeConverters.put(double.class, new DoubleConverter());
        sTypeConverters.put(Double.class, new DoubleConverter());
        sTypeConverters.put(long.class, new LongConverter());
        sTypeConverters.put(Long.class, new LongConverter());
        sTypeConverters.put(byte.class, new ByteConverter());
        sTypeConverters.put(Byte.class, new ByteConverter());
        sTypeConverters.put(byte[].class, new ByteArrayConverter());
        sTypeConverters.put(boolean.class, new BooleanConverter());
        sTypeConverters.put(Boolean.class, new BooleanConverter());
        sTypeConverters.put(Date.class, new DateConverter());
    }

    /**
     * Constructs the converter
     * @param clz the entity class
     * @param entities other registered entities
     * @param useAnnotations true if this converter should inspect clz for {@link Column} annotations, false otherwise
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public DefaultConverter(Class<T> clz, Map<Class<?>, ConverterHolder<?>> entities, boolean useAnnotations) {
        this.mUsingAnnotations = useAnnotations;
        Field[] fields = clz.getDeclaredFields();
        mColumns = new ArrayList<Converter.Column>(fields.length);
        this.mClass = clz;
        List<Property> properties = new ArrayList<DefaultConverter.Property>();
        for (Field field : fields) {
            if (!Modifier.isTransient(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                Class<?> type = field.getType();
                TypeConverter<?> converter = sTypeConverters.get(type);
                if (converter == null) {
                    if (type == clz) {
                        converter = new EntityConverter(type, this);
                    } else {
                        ConverterHolder<?> holder = entities.get(type);
                        if (holder == null) {
                            throw new IllegalArgumentException("Field "+field+" cannot be persisted and should be marked as transient");
                        }
                        converter = new EntityConverter(type, holder);
                        /* do not add it to sTypes */
                    }
                }
                Property prop = new Property();
                prop.field = field;
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                prop.name = getColumn(field);
                prop.type = field.getType();
                prop.typeConverter = (TypeConverter<Object>) converter;
                prop.columnType = converter.getColumnType();
                properties.add(prop);
                if (BaseColumns._ID.equals(prop.name)) {
                    mIdProperty = prop;
                }
                mColumns.add(new Column(prop.name, prop.columnType));
            }
        }
        this.mProperties = properties.toArray(new Property[properties.size()]);
    }

    @Override
    public T fromCursor(Cursor cursor) {
        try {
            T result = mClass.newInstance();
            String[] cols = cursor.getColumnNames();
            for (int index=0; index < cols.length; index++) {
                Property prop = mProperties[index];
                Class<?> type = prop.type;
                if (cursor.isNull(index)) {
                    if (!type.isPrimitive()) {
                        prop.field.set(result, null);
                    }
                } else {
                    prop.field.set(result, prop.typeConverter.fromCursorValue(cursor, index));
                }
            }
            return result;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toValues(T object, ContentValues values) {
        for (Property prop : mProperties) {
            try {
                Object value = prop.field.get(object);
                if (value == null) {
                    if (!prop.name.equals(BaseColumns._ID)) {
                        values.putNull(prop.name);
                    }
                } else {
                    prop.typeConverter.toContentValue(value, prop.name, values);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<Column> getColumns() {
        return mColumns;
    }

    @Override
    public void setId(Long id, T instance) {
        if (mIdProperty != null) {
            try {
                mIdProperty.field.set(instance, id);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Long getId(T instance) {
        if (mIdProperty != null) {
            try {
                return (Long) mIdProperty.field.get(instance);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Return the column name based on the field supplied. If annotation
     * processing is enabled for this converter and the field is annotated with
     * a {@link nl.qbusict.cupboard.convert.annotation.Column} annotation, then
     * the column name is taken from the annotation. In all other cases the
     * column name is simply the name of the field.
     *
     * @param field
     *            the entity field
     * @return the database column name for this field
     */
    protected String getColumn(Field field) {
        if (mUsingAnnotations) {
            nl.qbusict.cupboard.annotation.Column column = field
                    .getAnnotation(nl.qbusict.cupboard.annotation.Column.class);
            if (column != null) {
                return column.value();
            }
        }
        return field.getName();
    }

    private static String getTable(Class<?> clz) {
        return clz.getSimpleName();
    }

    @Override
    public String getTable() {
        return getTable(mClass);
    }
}
