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

import nl.qbusict.cupboard.*;

/**
 * An entity converter factory instantiates {@link nl.qbusict.cupboard.convert.EntityConverter}s. A single factory may support
 * multiple entity types.
 */
public interface EntityConverterFactory {
    /**
     * Create a converter for the requested type
     *
     * @param cupboard the cupboard instance
     * @param type     the type
     * @return a {@link nl.qbusict.cupboard.convert.EntityConverter} for the supplied type, or null if the type is not supported by this
     * factory.
     */
    public <T> EntityConverter<T> create(Cupboard cupboard, Class<T> type);
}
