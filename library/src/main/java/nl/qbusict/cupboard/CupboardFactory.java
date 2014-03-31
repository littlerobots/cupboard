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

/**
 * Factory that provides the global {@link nl.qbusict.cupboard.Cupboard} instance
 */
public final class CupboardFactory {
    private static Cupboard INSTANCE = new Cupboard();

    /**
     * Replace the Cupboard instance
     *
     * @param cupboard the instance to use
     */
    public static void setCupboard(Cupboard cupboard) {
        INSTANCE = cupboard;
    }

    public static Cupboard getInstance() {
        return INSTANCE;
    }

    public static Cupboard cupboard() {
        return INSTANCE;
    }
}
