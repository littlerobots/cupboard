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

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

/**
 * An entity converter is responsible for converting an entity to {@link android.content.ContentValues} and from a {@link android.database.Cursor}
 *
 * @param <T> the entity type
 */
public interface EntityConverter<T> {
    /**
     * The SQLite column type
     */
    public enum ColumnType {
        TEXT,
        INTEGER,
        REAL,
        BLOB,
        /**
         * A surrogate type for columns that are only read, but never written.
         */
        JOIN
    }

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
     * Create an entity from the cursor. The cursor supplied is guaranteed to provide the columns in the order returned by {@link nl.qbusict.cupboard.convert.EntityConverter#getColumns()},
     * but the number of columns might be less if the result does not contain them.
     *
     * For example, if the converter has 10 columns and the cursor has only 7, the columns 0-6 from {@link nl.qbusict.cupboard.convert.EntityConverter#getColumns()} will be supplied, even
     * if the original cursor does not contain all of them. This allows a {@link nl.qbusict.cupboard.convert.EntityConverter} to iterate over the columns without checking for column name.
     *
     * Note the contract between @{link #getColumns} and this function: {@link #getColumns()} should always specify the required columns for conversion. Any unlisted columns will be dropped from
     * the cursor that is supplied here.
     *
     * @param cursor
     * @return the entity
     */
    public T fromCursor(Cursor cursor);

    /**
     * Convert an entity to content values
     * Generally speaking do not add content values for columns that aren't returned from {@link #getColumns()} and omit columns of value {@link nl.qbusict.cupboard.convert.EntityConverter.ColumnType#JOIN}
     *
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