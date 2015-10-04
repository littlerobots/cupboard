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

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;

import nl.qbusict.cupboard.convert.EntityConverter;

@SuppressWarnings("unchecked")
public class ProviderOperationsCompartment extends BaseCompartment {

    private final ArrayList<ContentProviderOperation> mOperations;
    private boolean mYieldAllowed = false;
    private int mYieldAfter = -1;

    protected ProviderOperationsCompartment(Cupboard cupboard, ArrayList<ContentProviderOperation> operations) {
        super(cupboard);
        mOperations = operations;
    }

    /**
     * Add an insert operation to the list of operations. If {@link #yield()} was called, {@link ContentProviderOperation#isYieldAllowed()}
     * will be set.
     *
     * @param uri    the uri to insert to
     * @param entity the entity. If the entity has it's id field set, then this id will be appended to the uri as per {@link ContentUris#appendId(android.net.Uri.Builder, long)}
     * @return {@link ProviderOperationsCompartment} for chaining
     */
    public <T> ProviderOperationsCompartment put(Uri uri, T entity) {
        EntityConverter<T> converter = (EntityConverter<T>) getConverter(entity.getClass());
        ContentValues values = new ContentValues(converter.getColumns().size());
        converter.toValues(entity, values);
        Long id = converter.getId(entity);
        if (id == null) {
            mOperations.add(ContentProviderOperation.newInsert(uri).
                    withValues(values).
                    withYieldAllowed(shouldYield()).
                    build());
        } else {
            mOperations.add(ContentProviderOperation.newInsert(ContentUris.withAppendedId(uri, id)).
                    withYieldAllowed(shouldYield()).
                    withValues(values).build());
        }
        mYieldAllowed = false;
        return this;
    }

    /**
     * Allow the content provider to yield after the next operation (when supported by the provider).
     *
     * @return the {@link ProviderOperationsCompartment} for chaining.
     * @see SQLiteDatabase#yieldIfContendedSafely()
     *
     */
    public ProviderOperationsCompartment yield() {
        mYieldAllowed = true;
        return this;
    }

    /**
     * Yield when the number of operations is reaches a multiple of the batch size set.
     *
     * @param operationCount the amount of operations allowed before yielding. Set to 1 to yield on every put or delete,
     *                       0 to control yielding using {@link #yield()}.
     * @return {@link ProviderOperationsCompartment} for chaining. Perform other operations using the returned instance
     * to ensure yields are set after <i>operationCount</i> operations.
     */
    public ProviderOperationsCompartment yieldAfter(int operationCount) {
        mYieldAfter = operationCount;
        return this;
    }

    /**
     * Add multiple put operations. If {@link #yield()} was called, {@link ContentProviderOperation#isYieldAllowed()}
     * is set on the last operation.
     *
     * @param uri         the uri to call
     * @param entityClass the type of the entities
     * @param entities    the entities
     * @return the {@link ProviderOperationsCompartment} for chaining
     */
    public <T> ProviderOperationsCompartment put(Uri uri, Class<T> entityClass, T... entities) {
        boolean mWasYieldAllowed = mYieldAllowed;
        mYieldAllowed = false;
        EntityConverter<T> converter = getConverter(entityClass);
        ContentValues[] values = new ContentValues[entities.length];
        int size = converter.getColumns().size();
        for (int i = 0; i < entities.length; i++) {
            values[i] = new ContentValues(size);
            converter.toValues(entities[i], values[i]);
        }
        for (int i = 0; i < entities.length; i++) {
            if (i == entities.length - 1) {
                mYieldAllowed = mWasYieldAllowed;
            }
            this.put(uri, entities[i]);
        }
        return this;
    }

    /**
     * Add a delete operation. If {@link #yield()} was called, {@link ContentProviderOperation#isYieldAllowed()} will
     * be set.
     *
     * @param uri    the uri to call. The object id will be appended to this uri as per {@link ContentUris#appendId(android.net.Uri.Builder, long)}. If no id is set no operation will be added
     * @param entity the entity to delete, must have an id
     * @return this {@link ProviderOperationsCompartment} for chaining
     */
    public <T> ProviderOperationsCompartment delete(Uri uri, T entity) {
        EntityConverter<T> converter = (EntityConverter<T>) getConverter(entity.getClass());
        Long id = converter.getId(entity);
        if (id == null) {
            return this;
        }
        mOperations.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(uri, id)).
                withYieldAllowed(mYieldAllowed).
                build());
        return this;
    }

    /**
     * Get the list of {@link ContentProviderOperation}s
     *
     * @return the list
     */
    public ArrayList<ContentProviderOperation> getOperations() {
        return mOperations;
    }

    private boolean shouldYield() {
        return mYieldAllowed || (mYieldAfter > 0 && mOperations.size() + 1 >= mYieldAfter && (
                (mOperations.size() + 1) % mYieldAfter == 0));
    }
}
