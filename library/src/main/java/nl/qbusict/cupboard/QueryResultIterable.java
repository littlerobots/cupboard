package nl.qbusict.cupboard;

import android.database.Cursor;

import java.util.Iterator;
import java.util.NoSuchElementException;

import nl.qbusict.cupboard.convert.EntityConverter;

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
public class QueryResultIterable<T> implements Iterable<T> {

    private final Cursor mCursor;
    private final EntityConverter<T> mTranslator;
    private final int mPosition;

    static class QueryResultIterator<E> implements Iterator<E> {
        private final Cursor mCursor;
        private final EntityConverter<E> mTranslator;
        private boolean mHasNext;

        public QueryResultIterator(Cursor cursor, EntityConverter<E> translator) {
            this.mCursor = new PreferredColumnOrderCursorWrapper(cursor, translator.getColumns());
            this.mTranslator = translator;
            this.mHasNext = cursor.moveToNext();
        }

        @Override
        public boolean hasNext() {
            return mHasNext;
        }

        @Override
        public E next() {
            if (!mHasNext) {
                throw new NoSuchElementException();
            }
            E elem = mTranslator.fromCursor(mCursor);
            mHasNext = mCursor.moveToNext();
            return elem;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    QueryResultIterable(Cursor cursor, EntityConverter<T> translator) {
        if (cursor.getPosition() > -1) {
            this.mPosition = cursor.getPosition() - 1;
        } else {
            this.mPosition = -1;
        }
        this.mCursor = cursor;
        this.mTranslator = translator;
    }

    @Override
    public Iterator<T> iterator() {
        mCursor.moveToPosition(mPosition);
        return new QueryResultIterator<T>(mCursor, mTranslator);
    }

    public void close() {
        if (!mCursor.isClosed()) {
            mCursor.close();
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public T get() {
        return get(true);
    }

    public T get(boolean close) {
        try {
            Iterator<T> itr = iterator();
            if (itr.hasNext()) {
                return itr.next();
            } else {
                return null;
            }
        } finally {
            if (close) {
                close();
            }
        }
    }

}
