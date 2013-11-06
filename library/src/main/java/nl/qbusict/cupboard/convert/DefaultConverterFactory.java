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

import nl.qbusict.cupboard.annotation.Column;

public class DefaultConverterFactory implements ConverterFactory {
    private final boolean mUseAnnotations;

    /**
     * Construct the factory, not processing annotations on entities
     */
    public DefaultConverterFactory() {
        this(false);
    }

    /**
     * Construct the factory, optionally processing annotations on entities.
     *
     * @param useAnnotations
     *            true if this factory should create {@link DefaultConverter}s
     *            that honour {@link Column} annotations
     */
    public DefaultConverterFactory(boolean useAnnotations) {
        this.mUseAnnotations = useAnnotations;
    }

    @Override
    public <T> Converter<T> newConverter(Class<T> clz, Map<Class<?>, ConverterHolder<?>> entities) {
        return new DefaultConverter<T>(clz, entities, mUseAnnotations);
    }
}
