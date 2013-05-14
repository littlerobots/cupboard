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

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

public interface Converter<T> {
    /**
     * The SQLite column type
     */
    public enum ColumnType { TEXT, INTEGER, REAL, BLOB }

    /**
     * Holds the column name and type
     */
    public static class Column {
        public final String name;
        public final ColumnType type;

        public Column(String name, ColumnType type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public int hashCode() {
            return 37 * name.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Column) {
                Column c = (Column) o;
                return c.name.equals(name) && c.type == type;
            } else if (o instanceof String) {
                return name.equals(o);
            } else {
                return super.equals(o);
            }
        }
    }

    /**
     * Create an entity from the cursor. The cursor supplied is guaranteed to provide the columns in the order returned by {@link Converter#getColumns()},
     * but the number of columns might be less if the result does not contain them.
     *
     * For example, if the converter has 10 columns and the cursor has only 7, the columns 0-6 from {@link Converter#getColumns()} will be supplied, even
     * if the original cursor does not contain all of them. This allows a {@link Converter} to iterate over the columns without checking for column name.
     *
     * @param cursor
     * @return the entity
     */
    public T fromCursor(Cursor cursor);

    /**
     * Convert an entity to content values
     * @param object the entity
     * @param values the content values to populate
     */
    public void toValues(T object, ContentValues values);

    /**
     * Get the database column names along with the colum types
     * @see ColumnType
     * @return the list of colums
     */
    public List<Column> getColumns();

    /**
     * Set the id value on an entity
     * @param id the id
     * @param instance the instance to set the id on
     */
    public void setId(Long id, T instance);

    /**
     * Get the id of an entity
     * @param instance the entity
     * @return the id
     */
    public Long getId(T instance);

    /**
     * Get the database table for the entity
     * @return the mapped table name
     */
    public String getTable();
}