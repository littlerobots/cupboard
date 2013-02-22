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

import java.util.Map;

/**
 * Class to lazily instantiate a {@link Converter} for a particular entity type.
 * The converter isn't instantiated until the first time {@link #get()} is called.
 */
public class ConverterHolder<T> {
    private final Class<T> mEntityClass;
    private final ConverterFactory mFactory;
    private Converter<T> mTranslator;
    private final Map<Class<?>, ConverterHolder<?>> mEntities;

    public ConverterHolder(Class<T> clz, ConverterFactory factory, Map<Class<?>, ConverterHolder<?>> entities) {
        this.mEntityClass = clz;
        this.mFactory = factory;
        this.mEntities = entities;
    }

    public Converter<T> get() {
        if (mTranslator == null) {
            mTranslator = mFactory.newConverter(mEntityClass, mEntities);
        }
        return mTranslator;
    }
}