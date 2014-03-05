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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import nl.qbusict.cupboard.*;
import nl.qbusict.cupboard.convert.EntityConverter.ColumnType;
import nl.qbusict.cupboard.convert.FieldConverter;
import nl.qbusict.cupboard.convert.FieldConverterFactory;

public class EnumFieldConverterFactory implements FieldConverterFactory {
    private static class EnumConverter<E extends Enum> implements FieldConverter<E> {

        private final Class<E> mEnumClass;

        public EnumConverter(Class<E> enumClass) {
            this.mEnumClass = enumClass;
        }

        @Override
        public E fromCursorValue(Cursor cursor, int columnIndex) {
            return (E) Enum.valueOf(mEnumClass, cursor.getString(columnIndex));
        }

        @Override
        public void toContentValue(E value, String key, ContentValues values) {
            values.put(key, value.toString());
        }

        @Override
        public ColumnType getColumnType() {
            return ColumnType.TEXT;
        }
    }

    @Override
    public FieldConverter<?> create(Cupboard cupboard, Type type) {
        // enum can also be declared as Enum<EnumType>
        if (type instanceof ParameterizedType) {
            if (((ParameterizedType) type).getRawType() == Enum.class) {
                type = ((ParameterizedType) type).getActualTypeArguments()[0];
            }
        }
        if (!(type instanceof Class)) {
            return null;
        }
        Class<?> clz = (Class<?>) type;
        if (clz.isEnum()) {
            return new EnumConverter<Enum>((Class<Enum>) clz);
        }
        return null;
    }
}
