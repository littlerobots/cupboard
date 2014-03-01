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

import nl.qbusict.cupboard.convert.EntityConverter.ColumnType;

/**
 * Converts a field of type T to a column representation and vice versa
 *
 * @param <T> the type
 */
public interface FieldConverter<T> {
    /**
     * Convert a cursor value at the specified index to an instance of T
     *
     * @param cursor      the cursor
     * @param columnIndex the index of the requested value in the cursor
     * @return the value or null
     */
    public T fromCursorValue(Cursor cursor, int columnIndex);

    /**
     * Convert an instance of T to a value that can be stored in a ContentValues object
     *
     * @param value  the value
     * @param key    the key to store the value under
     * @param values the content values to store the value
     */
    public void toContentValue(T value, String key, ContentValues values);

    /**
     * Return the column type
     *
     * @return the column type or null if this field type should be ignored.
     */
    public ColumnType getColumnType();
}
