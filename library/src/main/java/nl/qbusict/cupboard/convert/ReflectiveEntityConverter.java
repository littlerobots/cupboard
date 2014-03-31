/*
 * Copyright (C) 2014 Qbus B.V.
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

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.qbusict.cupboard.*;
import nl.qbusict.cupboard.annotation.Ignore;

/**
 * The default {@link nl.qbusict.cupboard.convert.EntityConverter}
 */
public class ReflectiveEntityConverter<T> implements EntityConverter<T> {

    private final ArrayList<Column> mColumns;
    private final Class<T> mClass;
    private final Property[] mProperties;
    private Property mIdProperty;

    private static class Property {
        Field field;
        String name;
        Class<?> type;
        FieldConverter<Object> fieldConverter;
        ColumnType columnType;
    }

    /**
     * The {@link nl.qbusict.cupboard.Cupboard} instance for this converter
     */
    protected final Cupboard mCupboard;
    private final boolean mUseAnnotations;

    public ReflectiveEntityConverter(Cupboard cupboard, Class<T> entityClass) {
        this(cupboard, entityClass, Collections.<String>emptyList(), Collections.<EntityConverter.Column>emptyList());
    }

    public ReflectiveEntityConverter(Cupboard cupboard, Class<T> entityClass, Collection<String> ignoredFieldsNames) {
        this(cupboard, entityClass, ignoredFieldsNames, Collections.<EntityConverter.Column>emptyList());
    }

    /**
     * Constructor suitable for {@link nl.qbusict.cupboard.convert.EntityConverterFactory}s that only need minor
     * changes to the default behavior of this converter, not requiring a sub class.
     *
     * @param cupboard          the cupboard instance
     * @param entityClass       the entity class
     * @param ignoredFieldNames a collection of field names that should be ignored as an alternative to implementing {@link #isIgnored(java.lang.reflect.Field)}
     * @param additionalColumns a collection of additional columns that will be requested from the cursor
     */
    public ReflectiveEntityConverter(Cupboard cupboard, Class<T> entityClass, Collection<String> ignoredFieldNames, Collection<Column> additionalColumns) {
        mCupboard = cupboard;
        mUseAnnotations = cupboard.isUseAnnotations();
        Field[] fields = getAllFields(entityClass);
        mColumns = new ArrayList<Column>(fields.length);
        this.mClass = entityClass;
        List<Property> properties = new ArrayList<Property>();
        for (Field field : fields) {
            if (ignoredFieldNames.contains(field.getName()) || isIgnored(field)) {
                continue;
            }
            Type type = field.getGenericType();
            FieldConverter<?> converter = getFieldConverter(field);
            if (converter == null) {
                throw new IllegalArgumentException("Do not know how to convert field " + field.getName() + " in entity " + entityClass.getName() + " of type " + type);
            }
            if (converter.getColumnType() == null) {
                continue;
            }
            Property prop = new Property();
            prop.field = field;
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            prop.name = getColumn(field);
            prop.type = field.getType();
            prop.fieldConverter = (FieldConverter<Object>) converter;
            prop.columnType = converter.getColumnType();
            properties.add(prop);
            if (BaseColumns._ID.equals(prop.name)) {
                mIdProperty = prop;
            }
            mColumns.add(new Column(prop.name, prop.columnType));
        }
        mColumns.addAll(additionalColumns);
        this.mProperties = properties.toArray(new Property[properties.size()]);
    }

    /**
     * Get a {@link nl.qbusict.cupboard.convert.FieldConverter} for the specified field. This allows for subclasses
     * to provide a specific {@link nl.qbusict.cupboard.convert.FieldConverter} for a property. The default implementation
     * simply calls {@link nl.qbusict.cupboard.Cupboard#getFieldConverter(java.lang.reflect.Type)}
     *
     * @param field the field
     * @return the field converter
     */
    protected FieldConverter<?> getFieldConverter(Field field) {
        return mCupboard.getFieldConverter(field.getGenericType());
    }

    /**
     * Get all fields for the given class, including inherited fields. Note that no
     * attempts are made to deal with duplicate field names.
     *
     * @param clz the class to get the fields for
     * @return the fields
     */
    private Field[] getAllFields(Class<?> clz) {
        // optimize for the case where an entity is not inheriting from a base class.
        if (clz.getSuperclass() == null) {
            return clz.getDeclaredFields();
        }
        List<Field> fields = new ArrayList<Field>(256);
        Class<?> c = clz;
        do {
            Field[] f = c.getDeclaredFields();
            fields.addAll(Arrays.asList(f));
            c = c.getSuperclass();
        } while (c != null);
        Field[] result = new Field[fields.size()];
        return fields.toArray(result);
    }

    /**
     * Check if a field should be ignored. This allows subclasses to ignore fields at their discretion.
     * <p/>
     * The default implementation ignores all static, final or transient fields and if
     * {@link nl.qbusict.cupboard.Cupboard#isUseAnnotations()} returns true also checks for the {@link nl.qbusict.cupboard.annotation.Ignore}
     * annotation.
     *
     * @param field the field
     * @return true if this field should be ignored, false otherwise
     */
    protected boolean isIgnored(Field field) {
        int modifiers = field.getModifiers();
        boolean ignored = Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers);
        if (mUseAnnotations) {
            ignored = ignored || field.getAnnotation(Ignore.class) != null;
        }
        return ignored;
    }

    @Override
    public T fromCursor(Cursor cursor) {
        try {
            T result = mClass.newInstance();
            String[] cols = cursor.getColumnNames();
            for (int index = 0; index < cols.length; index++) {
                Property prop = mProperties[index];
                Class<?> type = prop.type;
                if (cursor.isNull(index)) {
                    if (!type.isPrimitive()) {
                        prop.field.set(result, null);
                    }
                } else {
                    prop.field.set(result, prop.fieldConverter.fromCursorValue(cursor, index));
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
            if (prop.columnType == ColumnType.JOIN) {
                continue;
            }
            try {
                Object value = prop.field.get(object);
                if (value == null) {
                    if (!prop.name.equals(BaseColumns._ID)) {
                        values.putNull(prop.name);
                    }
                } else {
                    prop.fieldConverter.toContentValue(value, prop.name, values);
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
     * a {@link nl.qbusict.cupboard.annotation.Column} annotation, then
     * the column name is taken from the annotation. In all other cases the
     * column name is simply the name of the field.
     *
     * @param field the entity field
     * @return the database column name for this field
     */
    protected String getColumn(Field field) {
        if (mUseAnnotations) {
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
