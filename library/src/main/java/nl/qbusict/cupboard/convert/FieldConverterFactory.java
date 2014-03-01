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

import java.lang.reflect.Type;

import nl.qbusict.cupboard.*;

/**
 * An field converter factory instantiates {@link nl.qbusict.cupboard.convert.FieldConverter}s. A single factory may support
 * multiple field types.
 */
public interface FieldConverterFactory {

    /**
     * Create a new FieldConverter for the given type
     *
     * @param cupboard the cupboard instance requesting the FieldConverter
     * @param type     the type of the field.
     * @return a suitable FieldConverter or null if the type is not supported by this factory.
     */
    public FieldConverter<?> create(Cupboard cupboard, Type type);
}
