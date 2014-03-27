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
import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.convert.EntityConverter.ColumnType;
import nl.qbusict.cupboard.convert.FieldConverter;
import nl.qbusict.cupboard.convert.FieldConverterFactory;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;

public class DefaultFieldConverterFactory implements FieldConverterFactory {
    
    private static HashMap<Type, FieldConverter<?>> sTypeConverters = new HashMap<Type, FieldConverter<?>>(25);
    
    static {
        sTypeConverters.put(BigDecimal.class, new BigDecimalConverter());
        sTypeConverters.put(BigIntegerConverter.class, new BigIntegerConverter());
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
    
    private static class BigDecimalConverter implements FieldConverter<BigDecimal> {
        @Override
        public BigDecimal fromCursorValue(Cursor cursor, int columnIndex) {
            return new BigDecimal(cursor.getString(columnIndex));
        }
        
        @Override
        public void toContentValue(BigDecimal value, String key, ContentValues values) {
            values.put(key, value.toPlainString());
        }
        
        @Override
        public ColumnType getColumnType() {
            return ColumnType.TEXT;
        }
    }
    
    private static class BigIntegerConverter implements FieldConverter<BigInteger> {
        @Override
        public BigInteger fromCursorValue(Cursor cursor, int columnIndex) {
            return new BigInteger(cursor.getString(columnIndex));
        }
        
        @Override
        public void toContentValue(BigInteger value, String key, ContentValues values) {
            values.put(key, value.toString());
        }
        
        @Override
        public ColumnType getColumnType() {
            return ColumnType.TEXT;
        }
    }
    
    private static class StringConverter implements FieldConverter<String> {
        @Override
        public String fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getString(columnIndex);
        }
        
        @Override
        public void toContentValue(String value, String key, ContentValues values) {
            values.put(key, value);
        }
        
        @Override
        public ColumnType getColumnType() {
            return ColumnType.TEXT;
        }
    }
    
    private static class IntegerConverter implements FieldConverter<Integer> {
        @Override
        public Integer fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getInt(columnIndex);
        }
        
        @Override
        public void toContentValue(Integer value, String key, ContentValues values) {
            values.put(key, value);
        }
        
        @Override
        public ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }
    }
    
    private static class FloatConverter implements FieldConverter<Float> {
        @Override
        public Float fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getFloat(columnIndex);
        }
        
        @Override
        public void toContentValue(Float value, String key, ContentValues values) {
            values.put(key, value);
        }
        
        @Override
        public ColumnType getColumnType() {
            return ColumnType.REAL;
        }
    }
    
    private static class ShortConverter implements FieldConverter<Short> {
        @Override
        public Short fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getShort(columnIndex);
        }
        
        @Override
        public void toContentValue(Short value, String key, ContentValues values) {
            values.put(key, value);
        }
        
        @Override
        public ColumnType getColumnType() {
            return ColumnType.REAL;
        }
    }
    
    private static class DoubleConverter implements FieldConverter<Double> {
        @Override
        public Double fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getDouble(columnIndex);
        }
        
        @Override
        public void toContentValue(Double value, String key, ContentValues values) {
            values.put(key, value);
        }
        
        @Override
        public ColumnType getColumnType() {
            return ColumnType.REAL;
        }
    }
    
    private static class LongConverter implements FieldConverter<Long> {
        @Override
        public Long fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getLong(columnIndex);
        }
        
        @Override
        public void toContentValue(Long value, String key, ContentValues values) {
            values.put(key, value);
        }
        
        @Override
        public ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }
    }
    
    private static class ByteConverter implements FieldConverter<Byte> {
        @Override
        public Byte fromCursorValue(Cursor cursor, int columnIndex) {
            return (byte) cursor.getInt(columnIndex);
        }
        
        @Override
        public void toContentValue(Byte value, String key, ContentValues values) {
            values.put(key, value);
        }
        
        @Override
        public ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }
    }
    
    private static class ByteArrayConverter implements FieldConverter<byte[]> {
        @Override
        public byte[] fromCursorValue(Cursor cursor, int columnIndex) {
            return cursor.getBlob(columnIndex);
        }
        
        @Override
        public void toContentValue(byte[] value, String key, ContentValues values) {
            values.put(key, value);
        }
        
        @Override
        public ColumnType getColumnType() {
            return ColumnType.BLOB;
        }
    }
    
    private static class BooleanConverter implements FieldConverter<Boolean> {
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
        public ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }
    }
    
    private static class DateConverter implements FieldConverter<Date> {
        @Override
        public Date fromCursorValue(Cursor cursor, int columnIndex) {
            return new Date(cursor.getLong(columnIndex));
        }
        
        @Override
        public void toContentValue(Date value, String key, ContentValues values) {
            values.put(key, value.getTime());
        }
        
        @Override
        public ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }
    }
    
    @Override
    public FieldConverter<?> create(Cupboard cupboard, Type type) {
        // we don't handle any generic types here
        if (!(type instanceof Class)) {
            return null;
        }
        return sTypeConverters.get(type);
    }
}