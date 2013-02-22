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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import nl.qbusict.cupboard.convert.ConverterFactory;
import nl.qbusict.cupboard.convert.ConverterHolder;
import nl.qbusict.cupboard.convert.DefaultConverterFactory;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * <p>The entrypoint of Cupboard is this class. The typical way to get an instance of this class is to use {@link CupboardFactory} using a static import:</p>
 * <pre>
 * import static nl.qbusict.cupboard.CupboardFactory.cupboard;
 *
 * public class Example {
 *  static {
 *   cupboard().register(MyEntity.class);
 *  }
 *
 *  public void storeMyEntity(SQLiteDatabase db, MyEntity entity) {
 *   cupboard().withDataBase(db).put(entity);
 *  }
 * }
 * </pre>
 *
 * <h2>Registering entities before use</h2>
 * <p>Entities that are used with Cupboard should be registered using the {@link #register(Class)} function. If an entity class isn't registered an {@link IllegalArgumentException} is thrown at runtime,
 * for example when attempting to call {@link DatabaseCompartment#createTables()}</p>
 *
 * <h2>Compartments</h2>
 * Storing an entity directly in a database is different from storing an entity through a {@link ContentResolver}. That's why there are different entry points:
 *
 * <ul>
 * <li>{@link Cupboard#withDatabase(SQLiteDatabase)} for interacting with a database</li>
 * <li>{@link Cupboard#withContext(Context)} for interacting with a {@link ContentProvider}</li>
 * <li>{@link Cupboard#withCursor(Cursor)} for getting results from {@link Cursor} objects</li>
 * <li>{@link Cupboard#withEntity(Class)} for converting entities to {@link ContentValues}</li>
 * <li>{@link Cupboard#withOperations(ArrayList)} for working on a list of {@link ContentProviderOperation}s</li>
 * </ul>
 *
 * @see DatabaseCompartment
 * @see ProviderCompartment
 * @see CursorCompartment
 * @see EntityCompartment
 * @see ProviderOperationsCompartment
 */
public class Cupboard {
    private final HashMap<Class<?>, ConverterHolder<?>> mEntities = new HashMap<Class<?>, ConverterHolder<?>>();
    private final ConverterFactory mTranslatorFactory;

    /**
     * Instantiate with the {@link DefaultConverterFactory}
     */
    public Cupboard() {
        this(new DefaultConverterFactory());
    }

    /**
     * Instantiate with a {@link ConverterFactory}
     * @param factory
     */
    public Cupboard(ConverterFactory factory) {
        this.mTranslatorFactory = factory;
    }

    /**
     * Register an entity class. Every entity class has to be registered before use.
     * @param clz the entity class to register.
     */
    public <T> void register(Class<T> clz) {
        mEntities.put(clz, new ConverterHolder<T>(clz, mTranslatorFactory, Collections.unmodifiableMap(mEntities)));
    }

    /**
     * Operate on a {@link SQLiteDatabase}
     * @param db the database to wrap
     * @return a {@link DatabaseCompartment} wrapping the database for chaining.
     */
    public DatabaseCompartment withDatabase(SQLiteDatabase db) {
        return new DatabaseCompartment(mEntities, db);
    }

    /**
     * Operate on a {@link Cursor}
     * @param cursor the cursor to wrap
     * @return a {@link CursorCompartment} wrapping the cursor for chaining.
     */
    public CursorCompartment withCursor(Cursor cursor) {
        return new CursorCompartment(mEntities, cursor);
    }

    /**
     * Operate on a {@link ContentResolver}
     * @param context the {@link Context} to retrieve the {@link ContentResolver} from
     * @return a {@link ProviderCompartment} to interact with the {@link ContentResolver}
     */
    public ProviderCompartment withContext(Context context) {
        return new ProviderCompartment(mEntities, context);
    }

    /**
     * Operate on a list of {@link ContentProviderOperation}s
     * @param operations the (empty) list of operations to append to
     * @return a {@link ProviderOperationsCompartment} for chaining
     */
    public ProviderOperationsCompartment withOperations(ArrayList<ContentProviderOperation> operations) {
        return new ProviderOperationsCompartment(mEntities, operations);
    }

    /**
     * Operate on an entity
     * @param entityClass the entity class
     * @return an {@link EntityCompartment} to interact with the entity.
     */
    @SuppressWarnings("unchecked")
    public <T> EntityCompartment<T> withEntity(Class<T> entityClass) {
        ConverterHolder<T> holder = (ConverterHolder<T>) mEntities.get(entityClass);
        if (holder == null) {
            throw new IllegalArgumentException("Class "+entityClass.toString()+" isn't registered.");
        }
        return new EntityCompartment<T>(holder);
    }

    /**
     * Get the database table for an entity
     * @param clz the entity class
     * @return the database table name
     */
    public <T> String getTable(Class<T> clz) {
        return withEntity(clz).getTable();
    }
}
