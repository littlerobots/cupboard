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

import java.util.Map;

import nl.qbusict.cupboard.convert.Converter;
import nl.qbusict.cupboard.convert.ConverterHolder;
import android.database.Cursor;

/**
 * {@link CursorCompartment} is used to get or iterate results from a {@link Cursor}.
 * A {@link CursorCompartment} is created from {@link Cupboard#withCursor(Cursor)}.
 *
 *  <h2>Example</h2>
 *  <pre>
 *  Cursor cursor = ...
 *  // get the first Book from this cursor
 *  Book book = cupboard().withCursor(cursor).get(Book.class);
 *  // iterate all books
 *  Iterable<Book> itr = cupboard().withCursor(cursor).iterate();
 *  for (Book book : itr) {
 *    // access book
 *  }
 *  </pre>
 */
public class CursorCompartment extends BaseCompartment {

    private final Cursor mCursor;

    protected CursorCompartment(Map<Class<?>, ConverterHolder<?>> converters, Cursor cursor) {
        super(converters);
        this.mCursor = cursor;
    }

    /**
     * Create a {@link Iterable} of objects.
     * @param clz the entity type
     * @return the iterable
     */
    public <T> QueryResultIterable<T> iterate(Class<T> clz) {
        Converter<T> converter = getConverter(clz);
        return new QueryResultIterable<T>(mCursor, converter);
    }

    /**
     * Get the first entity from the cursor
     * @param clz the entity type
     * @return the object or null if the cursor has no results.
     */
    public <T> T get(Class<T> clz) {
        return iterate(clz).get(false);
    }
}
