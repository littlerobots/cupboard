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

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Collection;
import java.util.List;

import nl.qbusict.cupboard.convert.EntityConverter;

@SuppressWarnings("unchecked")
public class ProviderCompartment extends BaseCompartment {
    private static final String QUERY_BY_ID = BaseColumns._ID + " = ?";

    private final ContentResolver mResolver;

    public static class QueryBuilder<T> {
        private final Class<T> mEntityClass;
        private final ProviderCompartment mCompartment;
        private final Uri mUri;
        private String mSelection;
        private String[] mSelectionArgs;
        private String mOrder;
        private String[] mProjection;

        public QueryBuilder(Uri uri, Class<T> entityClass, ProviderCompartment compartment) {
            this.mEntityClass = entityClass;
            this.mCompartment = compartment;
            this.mUri = uri;
        }

        public QueryBuilder<T> withSelection(String selection, String... args) {
            this.mSelection = selection;
            this.mSelectionArgs = args;
            return this;
        }

        public QueryBuilder<T> orderBy(String order) {
            this.mOrder = order;
            return this;
        }

        public QueryBuilder<T> withProjection(String... projection) {
            this.mProjection = projection;
            return this;
        }

        /**
         * Execute the query and return a {@link QueryResultIterable}
         *
         * @return the iterable
         */
        public QueryResultIterable<T> query() {
            return mCompartment.query(mUri, mEntityClass, mProjection, mSelection, mSelectionArgs, mOrder);
        }

        /**
         * Convenience for calling {@link #query()}.getCursor()
         *
         * @return the cursor
         */
        public Cursor getCursor() {
            return query().getCursor();
        }

        /**
         * Convenience for calling {@link #query()}.get()
         *
         * @return the entity or null if the query didn't return any results
         */
        public T get() {
            return query().get();
        }

        /**
         * Convenience for calling {@link #query()}.list()
         *
         * @return the result set as a list
         */
        public List<T> list() {
            return query().list();
        }
    }

    protected ProviderCompartment(Cupboard cupboard, Context context) {
        super(cupboard);
        mResolver = context.getContentResolver();
    }

    /**
     * Get an entity from a content provider
     *
     * @param uri         the uri to get from
     * @param entityClass the entity class
     * @return the first result (if any) returned by the content provider
     */
    public <T> T get(Uri uri, Class<T> entityClass) {
        return query(uri, entityClass).query().get();
    }

    /**
     * Put (insert) an object to a content uri.
     *
     * @param uri    the uri to insert to
     * @param entity the entity. If the entity has it's id field set, then this id will be appended to the uri as per {@link ContentUris#appendId(android.net.Uri.Builder, long)}
     * @return the uri as returned by the content provider
     */
    public <T> Uri put(Uri uri, T entity) {
        EntityConverter<T> converter = (EntityConverter<T>) getConverter(entity.getClass());
        ContentValues values = new ContentValues(converter.getColumns().size());
        converter.toValues(entity, values);
        Long id = converter.getId(entity);
        if (id == null) {
            return mResolver.insert(uri, values);
        } else {
            return mResolver.insert(ContentUris.withAppendedId(uri, id), values);
        }
    }

    /**
     * Put multiple entities using {@link ContentProvider#bulkInsert(Uri, ContentValues[])}
     *
     * @param uri         the uri to call
     * @param entityClass the type of the entities
     * @param entities    the entities
     * @return the result of {@link ContentProvider#bulkInsert(Uri, ContentValues[])}
     */
    public <T> int put(Uri uri, Class<T> entityClass, T... entities) {
        EntityConverter<T> converter = getConverter(entityClass);
        ContentValues[] values = new ContentValues[entities.length];
        int size = converter.getColumns().size();
        for (int i = 0; i < entities.length; i++) {
            values[i] = new ContentValues(size);
            converter.toValues(entities[i], values[i]);
        }
        return mResolver.bulkInsert(uri, values);
    }

    /**
     * Put multiple entities using {@link ContentProvider#bulkInsert(Uri, ContentValues[])}
     *
     * @param uri         the uri to call
     * @param entityClass the type of the entities
     * @param entities    the collection of entities
     * @return the result of {@link ContentProvider#bulkInsert(Uri, ContentValues[])}
     */
    public <T> int put(Uri uri, Class<T> entityClass, Collection<T> entities) {
        return put(uri, entityClass, (T[]) entities.toArray());
    }

    /**
     * Delete an entity
     *
     * @param uri    the uri to call. The object id will be appended to this uri as per {@link ContentUris#appendId(android.net.Uri.Builder, long)}. Calling delete on an entity without an id set is a no op.
     * @param entity the entity to delete
     * @return the number of entities deleted
     */
    public <T> int delete(Uri uri, T entity) {
        EntityConverter<T> converter = (EntityConverter<T>) getConverter(entity.getClass());
        Long id = converter.getId(entity);
        if (id == null) {
            return 0;
        }
        return mResolver.delete(ContentUris.withAppendedId(uri, id), null, null);
    }

    /**
     * Delete by selection. This function is equivalent to {@link android.content.ContentResolver#delete(android.net.Uri, String, String[])}
     *
     * @param uri           the uri to call
     * @param selection     the selection for this delete
     * @param selectionArgs selection arguments
     * @return the number of entities deleted
     */
    public int delete(Uri uri, String selection, String... selectionArgs) {
        return mResolver.delete(uri, selection, selectionArgs);
    }

    /**
     * Update entities using the update method of {@link android.content.ContentResolver}. Useful for 'partial' updates to an entity, or
     * multiple entities.
     * For updating 'complete' entities in bulk, use {@link #put(android.net.Uri, Class, Object[])} or {@link #put(android.net.Uri, Class, java.util.Collection)}
     *
     * @param values the content values. If it contains a {@link BaseColumns#_ID} then the id will be appended to the URI and
     *               the selection "_id = &lt;entity id&gt;" will be passed to the underlying ContentProvider
     * @return the number of entities updated
     */
    public int update(Uri uri, ContentValues values) {
        if (values.containsKey(BaseColumns._ID)) {
            return mResolver.update(ContentUris.withAppendedId(uri, values.getAsLong(BaseColumns._ID)), values, QUERY_BY_ID, new String[]{values.getAsString(BaseColumns._ID)});
        } else {
            return mResolver.update(uri, values, null, null);
        }
    }

    /**
     * Update entities using the update method of {@link android.content.ContentResolver}. Useful for 'partial' updates to an entity, or
     * multiple entities.
     *
     * @param values        content values
     * @param selection     where clause
     * @param selectionArgs selection arguments
     * @return the number of entities updated
     */
    public int update(Uri uri, ContentValues values, String selection, String... selectionArgs) {
        return mResolver.update(uri, values, selection, selectionArgs);
    }

    /**
     * Query for entities
     *
     * @param uri         the uri to query
     * @param entityClass the entity class
     * @return a {@link QueryBuilder} for chaining
     */
    public <T> QueryBuilder<T> query(Uri uri, Class<T> entityClass) {
        return new QueryBuilder<T>(uri, entityClass, this);
    }

    /**
     * Get an entity by example, the id of the entity must be set.
     * This is useful when an entity is retrieved that has references to other entities. Those references will only have their id set.
     * This function can then be used to "swap out" the entity stubs with the real entities.
     *
     * @param uri    The base uri of the entity, the id will be appended
     * @param entity the entity to get
     * @return the entity or null if not found
     * @throws IllegalArgumentException if the entity id is not set.
     */
    public <T> T get(Uri uri, T entity) {
        EntityConverter<T> converter = (EntityConverter<T>) getConverter(entity.getClass());
        Long id = converter.getId(entity);
        if (id == null) {
            throw new IllegalArgumentException("entity does not have it's id set");
        }
        return (T) get(ContentUris.withAppendedId(uri, id), entity.getClass());
    }

    private <T> QueryResultIterable<T> query(Uri uri, Class<T> entityClass, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        EntityConverter<T> converter = getConverter(entityClass);
        Cursor cursor = mResolver.query(uri, projection, selection, selectionArgs, sortOrder);
        if (cursor == null) {
            cursor = new MatrixCursor(new String[]{BaseColumns._ID});
        }
        return new QueryResultIterable<T>(cursor, converter);
    }
}
