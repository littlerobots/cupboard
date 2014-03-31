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
package nl.qbusict.cupboard.internal.convert;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.reflect.Type;

import nl.qbusict.cupboard.*;
import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.EntityConverter.ColumnType;
import nl.qbusict.cupboard.convert.FieldConverter;
import nl.qbusict.cupboard.convert.FieldConverterFactory;

public class EntityFieldConverterFactory implements FieldConverterFactory {

    private static class EntityFieldConverter implements FieldConverter<Object> {
        private final Class<Object> entityClass;
        private final EntityConverter<Object> mEntityConverter;

        public EntityFieldConverter(Class<Object> clz, EntityConverter<?> entityConverter) {
            this.mEntityConverter = (EntityConverter<Object>) entityConverter;
            this.entityClass = clz;
        }

        @Override
        public Object fromCursorValue(Cursor cursor, int columnIndex) {
            long id = cursor.getLong(columnIndex);
            Object entity;
            try {
                entity = entityClass.newInstance();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
            mEntityConverter.setId(id, entity);
            return entity;
        }

        @Override
        public void toContentValue(Object value, String key, ContentValues values) {
            values.put(key, mEntityConverter.getId(value));
        }

        @Override
        public ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }
    }

    @Override
    public FieldConverter<?> create(Cupboard cupboard, Type type) {
        if (!(type instanceof Class)) {
            return null;
        }
        if (cupboard.isRegisteredEntity((Class<?>) type)) {
            EntityConverter<?> converter = cupboard.getEntityConverter((Class<?>) type);
            return new EntityFieldConverter((Class<Object>) type, converter);
        }
        return null;
    }
}
