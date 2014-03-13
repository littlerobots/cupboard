/*
 * Copyright (C) 2014 Qbus B.V.
 *
 * Portions Copyright (C) 2008 Google Inc.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.qbusict.cupboard.*;
import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.EntityConverter.ColumnType;
import nl.qbusict.cupboard.convert.EntityConverterFactory;
import nl.qbusict.cupboard.convert.FieldConverter;
import nl.qbusict.cupboard.convert.FieldConverterFactory;
import nl.qbusict.cupboard.convert.ReflectiveEntityConverter;

/*
 * Internal registry for converters, mostly inspired by Google Gson
 */
public class ConverterRegistry {

    private List<FieldConverterFactory> mFieldConverterFactories = new ArrayList<FieldConverterFactory>(256);
    private List<EntityConverterFactory> mEntityConverterFactories = new ArrayList<EntityConverterFactory>(64);

    private final ThreadLocal<Map<Type, FutureFieldConverter<?>>> mFieldConverterCalls = new ThreadLocal<Map<Type, FutureFieldConverter<?>>>();
    private final ThreadLocal<Map<Class<?>, EntityConverter<?>>> mEntityConverterCalls = new ThreadLocal<Map<Class<?>, EntityConverter<?>>>();
    private Map<Class<?>, EntityConverter<?>> mEntityConverterCache = new HashMap<Class<?>, EntityConverter<?>>(128);
    private Map<Type, FieldConverter<?>> mFieldConverterCache = new HashMap<Type, FieldConverter<?>>(128);
    private Cupboard mCupboard;

    public ConverterRegistry(Cupboard cupboard) {
        this.mCupboard = cupboard;
        addDefaultEntityConverterFactories();
        addDefaultFieldConverterFactories();
    }

    private static class FutureFieldConverter<T> implements FieldConverter<T> {
        private FieldConverter<T> mDelegate;

        @Override
        public T fromCursorValue(Cursor cursor, int columnIndex) {
            if (mDelegate == null) {
                throw new IllegalStateException();
            }
            return mDelegate.fromCursorValue(cursor, columnIndex);
        }

        @Override
        public void toContentValue(T value, String key, ContentValues values) {
            if (mDelegate == null) {
                throw new IllegalStateException();
            }
            mDelegate.toContentValue(value, key, values);
        }

        @Override
        public ColumnType getColumnType() {
            if (mDelegate == null) {
                throw new IllegalStateException();
            }
            return mDelegate.getColumnType();
        }

        void setDelegate(FieldConverter<T> delegate) {
            if (mDelegate != null) {
                throw new AssertionError();
            }
            mDelegate = delegate;
        }
    }

    private static class FutureEntityConverter<T> implements EntityConverter<T> {
        private EntityConverter<T> mDelegate;

        @Override
        public T fromCursor(Cursor cursor) {
            if (mDelegate == null) {
                throw new IllegalStateException();
            }
            return mDelegate.fromCursor(cursor);
        }

        @Override
        public void toValues(T object, ContentValues values) {
            if (mDelegate == null) {
                throw new IllegalStateException();
            }
            mDelegate.toValues(object, values);
        }

        @Override
        public List<Column> getColumns() {
            if (mDelegate == null) {
                throw new IllegalStateException();
            }
            return mDelegate.getColumns();
        }

        @Override
        public void setId(Long id, T instance) {
            if (mDelegate == null) {
                throw new IllegalStateException();
            }
            mDelegate.setId(id, instance);
        }

        @Override
        public Long getId(T instance) {
            if (mDelegate == null) {
                throw new IllegalStateException();
            }
            return mDelegate.getId(instance);
        }

        @Override
        public String getTable() {
            if (mDelegate == null) {
                throw new IllegalStateException();
            }
            return mDelegate.getTable();
        }

        void setDelegate(EntityConverter<T> delegate) {
            if (mDelegate != null) {
                throw new AssertionError();
            }
            mDelegate = delegate;
        }
    }


    private void addDefaultFieldConverterFactories() {
        mFieldConverterFactories.add(new DefaultFieldConverterFactory());
        mFieldConverterFactories.add(new EnumFieldConverterFactory());
        mFieldConverterFactories.add(new EntityFieldConverterFactory());
    }

    private void addDefaultEntityConverterFactories() {
        mEntityConverterFactories.add(new EntityConverterFactory() {
            @Override
            public <T> EntityConverter<T> create(Cupboard cupboard, Class<T> type) {
                return new ReflectiveEntityConverter<T>(cupboard, type);
            }
        });
    }

    public <T> EntityConverter<T> getEntityConverter(Class<T> type) throws IllegalArgumentException {
        EntityConverter<?> cached = mEntityConverterCache.get(type);
        if (cached != null) {
            return (EntityConverter<T>) cached;
        }
        boolean requiresThreadLocalCleanup = false;
        Map<Class<?>, EntityConverter<?>> threadCalls = mEntityConverterCalls.get();
        if (threadCalls == null) {
            threadCalls = new HashMap<Class<?>, EntityConverter<?>>(16);
            mEntityConverterCalls.set(threadCalls);
            requiresThreadLocalCleanup = true;
        }
        // doesn't this leak a thread local in a race condition?
        FutureEntityConverter<T> ongoingCall = (FutureEntityConverter<T>) threadCalls.get(type);
        if (ongoingCall != null) {
            return ongoingCall;
        }

        try {
            FutureEntityConverter<T> call = new FutureEntityConverter<T>();
            threadCalls.put(type, call);

            for (EntityConverterFactory factory : mEntityConverterFactories) {
                EntityConverter<T> candidate = factory.create(mCupboard, type);
                if (candidate != null) {
                    call.setDelegate(candidate);
                    mEntityConverterCache.put(type, candidate);
                    return candidate;
                }
            }
            throw new IllegalArgumentException("Cannot convert entity of type " + type);
        } finally {
            threadCalls.remove(type);
            if (requiresThreadLocalCleanup) {
                mEntityConverterCalls.remove();
            }
        }
    }

    public <T> FieldConverter<T> getFieldConverter(Type type) throws IllegalArgumentException {
        FieldConverter<T> converter = (FieldConverter<T>) mFieldConverterCache.get(type);
        if (converter != null) {
            return converter;
        }
        boolean requiresThreadLocalCleanup = false;
        Map<Type, FutureFieldConverter<?>> threadCalls = mFieldConverterCalls.get();
        if (threadCalls == null) {
            threadCalls = new HashMap<Type, FutureFieldConverter<?>>(16);
            mFieldConverterCalls.set(threadCalls);
            requiresThreadLocalCleanup = true;
        }
        // doesn't this leak a thread local in a race condition?
        FutureFieldConverter<T> ongoingCall = (FutureFieldConverter<T>) threadCalls.get(type);
        if (ongoingCall != null) {
            return ongoingCall;
        }

        try {
            FutureFieldConverter<T> call = new FutureFieldConverter<T>();
            threadCalls.put(type, call);

            for (FieldConverterFactory factory : mFieldConverterFactories) {
                FieldConverter<T> candidate = (FieldConverter<T>) factory.create(mCupboard, type);
                if (candidate != null) {
                    call.setDelegate(candidate);
                    mFieldConverterCache.put(type, candidate);
                    return candidate;
                }
            }
            throw new IllegalArgumentException("Cannot convert field of type" + type);
        } finally {
            threadCalls.remove(type);
            if (requiresThreadLocalCleanup) {
                mFieldConverterCalls.remove();
            }
        }
    }

    public <T> EntityConverter<T> getDelegateEntityConverter(EntityConverterFactory skipPast, Class<T> entityClass) throws IllegalArgumentException {
        boolean factoryFound = false;
        for (EntityConverterFactory factory : mEntityConverterFactories) {
            if (!factoryFound) {
                if (factory == skipPast) {
                    factoryFound = true;
                }
                continue;
            }
            EntityConverter<T> candidate = factory.create(mCupboard, entityClass);
            if (candidate != null) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Cannot convert entity of type " + entityClass);
    }

    public FieldConverter getDelegateFieldConverter(FieldConverterFactory skipPast, Type fieldType) throws IllegalArgumentException {
        boolean factoryFound = false;
        for (FieldConverterFactory factory : mFieldConverterFactories) {
            if (!factoryFound) {
                if (factory == skipPast) {
                    factoryFound = true;
                }
                continue;
            }
            FieldConverter candidate = factory.create(mCupboard, fieldType);
            if (candidate != null) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Cannot convert field of type " + fieldType);
    }

    public void registerEntityConverterFactory(EntityConverterFactory factory) {
        mEntityConverterFactories.add(0, factory);
    }

    public void registerFieldConverterFactory(FieldConverterFactory factory) {
        mFieldConverterFactories.add(0, factory);
    }

    public <T> void registerFieldConverter(Class<T> clz, FieldConverter<T> converter) {
        mFieldConverterCache.put(clz, converter);
    }
}
