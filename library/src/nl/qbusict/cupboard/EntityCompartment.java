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
package nl.qbusict.cupboard;

import nl.qbusict.cupboard.convert.ConverterHolder;
import android.content.ContentValues;

public class EntityCompartment<T> {
    private final ConverterHolder<T> mConverter;

    protected EntityCompartment(ConverterHolder<T> converter) {
        this.mConverter = converter;
    }

    /**
     * Get the table name for this entity class
     * @return the table name
     */
    public String getTable() {
        return mConverter.get().getTable();
    }

    /**
     * Convert an entity to {@link ContentValues}
     * @param entity the entity
     * @return the values
     */
    public ContentValues toContentValues(T entity) {
        return toContentValues(entity, null);
    }

    /**
     * Convert an entity to {@link ContentValues}
     * @param entity the entity
     * @param values the content values, may be null
     * @return the values
     */
    public ContentValues toContentValues(T entity, ContentValues values) {
        if (values == null) {
            values = new ContentValues(mConverter.get().getColumns().size());
        }
        mConverter.get().toValues(entity, values);
        return values;
    }
}
