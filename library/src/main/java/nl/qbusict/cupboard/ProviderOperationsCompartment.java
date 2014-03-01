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
import android.net.Uri;

import java.util.ArrayList;

import nl.qbusict.cupboard.convert.EntityConverter;

@SuppressWarnings("unchecked")
public class ProviderOperationsCompartment extends BaseCompartment {

    private final ArrayList<ContentProviderOperation> mOperations;

    protected ProviderOperationsCompartment(Cupboard cupboard, ArrayList<ContentProviderOperation> operations) {
        super(cupboard);
        mOperations = operations;
    }

    /**
     * Add an insert operation to the list of operations
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
            mOperations.add(ContentProviderOperation.newInsert(uri).withValues(values).build());
        } else {
            mOperations.add(ContentProviderOperation.newInsert(ContentUris.withAppendedId(uri, id)).withValues(values).build());
        }
        return this;
    }

    /**
     * Add multiple put operations
     *
     * @param uri         the uri to call
     * @param entityClass the type of the entities
     * @param entities    the entities
     * @return the {@link ProviderOperationsCompartment} for chaining
     */
    public <T> ProviderOperationsCompartment put(Uri uri, Class<T> entityClass, T... entities) {
        EntityConverter<T> converter = getConverter(entityClass);
        ContentValues[] values = new ContentValues[entities.length];
        int size = converter.getColumns().size();
        for (int i = 0; i < entities.length; i++) {
            values[i] = new ContentValues(size);
            converter.toValues(entities[i], values[i]);
        }
        for (int i = 0; i < entities.length; i++) {
            this.put(uri, entities[i]);
        }
        return this;
    }

    /**
     * Add a delete operation
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
        mOperations.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(uri, id)).build());
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
}
