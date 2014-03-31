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
package nl.qbusict.cupboard;

import nl.qbusict.cupboard.convert.EntityConverterFactory;
import nl.qbusict.cupboard.convert.FieldConverter;
import nl.qbusict.cupboard.convert.FieldConverterFactory;

/**
 * Aids in creating specialized {@link nl.qbusict.cupboard.Cupboard} instances
 */
public class CupboardBuilder {
    private Cupboard mCupboard;

    public CupboardBuilder() {
        mCupboard = new Cupboard();
    }

    /**
     * Register a {@link nl.qbusict.cupboard.convert.EntityConverterFactory}
     *
     * @param factory the factory
     * @return the builder for chaining
     */
    public CupboardBuilder registerEntityConverterFactory(EntityConverterFactory factory) {
        mCupboard.registerEntityConverterFactory(factory);
        return this;
    }

    /**
     * Register a {@link nl.qbusict.cupboard.convert.FieldConverterFactory}
     *
     * @param factory the factory
     * @return the builder for chaining
     */
    public CupboardBuilder registerFieldConverterFactory(FieldConverterFactory factory) {
        mCupboard.registerFieldConverterFactory(factory);
        return this;
    }

    /**
     * Register a field converter
     *
     * @param fieldClass the field class
     * @param converter  the converter
     * @return the builder for chaining
     */
    public <T> CupboardBuilder registerFieldConverter(Class<T> fieldClass, FieldConverter<T> converter) {
        mCupboard.registerFieldConverter(fieldClass, converter);
        return this;
    }

    /**
     * Enable the use of annotations
     *
     * @return the builder for chaining
     */
    public CupboardBuilder useAnnotations() {
        mCupboard.setUseAnnotations(true);
        return this;
    }


    /**
     * Create the {@link nl.qbusict.cupboard.Cupboard} instance.
     *
     * @return the Cupboard instance
     */
    public Cupboard build() {
        return mCupboard;
    }
}
