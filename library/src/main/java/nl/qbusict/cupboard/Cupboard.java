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
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nl.qbusict.cupboard.annotation.Column;
import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.EntityConverterFactory;
import nl.qbusict.cupboard.convert.FieldConverter;
import nl.qbusict.cupboard.convert.FieldConverterFactory;
import nl.qbusict.cupboard.internal.convert.ConverterRegistry;

/**
 * <p>The entry point of Cupboard is this class. The typical way to get an instance of this class is to use {@link CupboardFactory} using a static import:</p>
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
 * <h2>Using annotations for column mapping</h2>
 *
 * When working with existing data, it might be convenient to use a different field name for a certain column.
 * For supporting that use case, the {@link Column} annotation can be used on an entity field. By default, these
 * annotations are not processed, processing needs to be enabled explicitly by creating a Cupboard instance using {@link nl.qbusict.cupboard.CupboardBuilder}
 * <pre>
 * Cupboard cupboard = new CupboardBuilder().useAnnotations().build();
 * cupboard.setUseAnnotations(true);
 * </pre>
 * Above would configure a local instance of Cupboard to use the {@link Column} annotation for mapping fields to columns.
 * If you would like to make this the global default, use {@link nl.qbusict.cupboard.CupboardBuilder} as well.
 *<pre>
 *static {
 *      new CupboardBuilder().useAnnotations().asGlobalInstance().build();
 *      cupboard().register(MyEntity.class);
 *}
 *</pre>
 *
 * @see DatabaseCompartment
 * @see ProviderCompartment
 * @see CursorCompartment
 * @see EntityCompartment
 * @see ProviderOperationsCompartment
 */
public class Cupboard {
    private boolean mUseAnnotations = false;
    private final ConverterRegistry mConverterRegistry;
    private Set<Class<?>> mEntities = new HashSet<Class<?>>(128);

    public Cupboard() {
        this.mConverterRegistry = new ConverterRegistry(this);
    }

    /**
     * Register an entity class. Every entity class has to be registered before use.
     * @param clz the entity class to register.
     */
    public <T> void register(Class<T> clz) {
        mEntities.add(clz);
    }

    /**
     * Operate on a {@link SQLiteDatabase}
     * @param db the database to wrap
     * @return a {@link DatabaseCompartment} wrapping the database for chaining.
     */
    public DatabaseCompartment withDatabase(SQLiteDatabase db) {
        return new DatabaseCompartment(this, db);
    }

    /**
     * Operate on a {@link Cursor}
     * @param cursor the cursor to wrap
     * @return a {@link CursorCompartment} wrapping the cursor for chaining.
     */
    public CursorCompartment withCursor(Cursor cursor) {
        return new CursorCompartment(this, cursor);
    }

    /**
     * Operate on a {@link ContentResolver}
     * @param context the {@link Context} to retrieve the {@link ContentResolver} from
     * @return a {@link ProviderCompartment} to interact with the {@link ContentResolver}
     */
    public ProviderCompartment withContext(Context context) {
        return new ProviderCompartment(this, context);
    }

    /**
     * Operate on a list of {@link ContentProviderOperation}s
     * @param operations the (empty) list of operations to append to
     * @return a {@link ProviderOperationsCompartment} for chaining
     */
    public ProviderOperationsCompartment withOperations(ArrayList<ContentProviderOperation> operations) {
        return new ProviderOperationsCompartment(this, operations);
    }

    /**
     * Operate on an entity
     * @param entityClass the entity class
     * @return an {@link EntityCompartment} to interact with the entity.
     */
    @SuppressWarnings("unchecked")
    public <T> EntityCompartment<T> withEntity(Class<T> entityClass) {
        return new EntityCompartment<T>(this, entityClass);
    }

    /**
     * Get the database table for an entity
     * @param clz the entity class
     * @return the database table name
     */
    public <T> String getTable(Class<T> clz) {
        return withEntity(clz).getTable();
    }

    /**
     * Get the classes that are registered with this instance
     * @return an unmodifiable collection of registered classes
     */
    public Collection<Class<?>> getRegisteredEntities() {
        return Collections.unmodifiableSet(mEntities);
    }

    /**
     * Return if annotations are enabled
     *
     * @return true if annotations are enabled, false otherwise
     */
    public boolean isUseAnnotations() {
        return mUseAnnotations;
    }

    /**
     * Enable or disable the use of annotations. This works as a hint that an {@link nl.qbusict.cupboard.convert.EntityConverter} or {@link nl.qbusict.cupboard.convert.FieldConverter}
     * may use through {@link #isUseAnnotations()}.
     *
     * @param useAnnotations true to enable annotations, false otherwise.
     */
    void setUseAnnotations(boolean useAnnotations) {
        mUseAnnotations = useAnnotations;
    }

    /**
     * Get a field converter for the specified {@link java.lang.reflect.Type}
     *
     * @param type the type
     * @return the field converter
     * @throws java.lang.IllegalArgumentException if a field of this type cannot be converted by this instance
     */
    public FieldConverter<?> getFieldConverter(Type type) throws IllegalArgumentException {
        return mConverterRegistry.getFieldConverter(type);
    }

    /**
     * Get an entity converter for an entity class
     *
     * @param entityClass the entity class, must have been previous registered using {@link #register(Class)}
     * @param <T>         the entity type
     * @return a converter for the given type
     * @throws java.lang.IllegalArgumentException if an entity of this type cannot be converted by this instance
     */
    public <T> EntityConverter<T> getEntityConverter(Class<T> entityClass) throws IllegalArgumentException {
        if (!isRegisteredEntity(entityClass)) {
            throw new IllegalArgumentException("Entity is not registered: " + entityClass);
        }
        return mConverterRegistry.getEntityConverter(entityClass);
    }

    /**
     * Get a field converter to be used as a delegate. The converter can be used in a {@link nl.qbusict.cupboard.convert.FieldConverter} to delegate
     * to the default converter for example.
     *
     * @param skipPast the FieldConverterFactory to skip when searching for the delegate converter
     * @param type     the type for the field
     * @return the field converter
     * @throws java.lang.IllegalArgumentException if a field of this type cannot be converted by this instance
     */
    public FieldConverter<?> getDelegateFieldConverter(FieldConverterFactory skipPast, Type type) throws IllegalArgumentException {
        return mConverterRegistry.getDelegateFieldConverter(skipPast, type);
    }

    /**
     * Get an entity converter to be used as a delegate. The converter can be used in a {@link nl.qbusict.cupboard.convert.EntityConverter} to delegate
     * to the default converter for example.
     *
     * @param skipPast    The EntityConverterFactory to skip when searching for the delegate converter
     * @param entityClass the entity class
     * @param <T>         the entity type
     * @return the converter
     * @throws java.lang.IllegalArgumentException if an entity of this type cannot be converted by this instance
     */
    public <T> EntityConverter<T> getDelegateEntityConverter(EntityConverterFactory skipPast, Class<T> entityClass) throws IllegalArgumentException {
        return mConverterRegistry.getDelegateEntityConverter(skipPast, entityClass);
    }

    /**
     * Register a {@link nl.qbusict.cupboard.convert.EntityConverterFactory}
     *
     * @param factory the factory
     */
    void registerEntityConverterFactory(EntityConverterFactory factory) {
        mConverterRegistry.registerEntityConverterFactory(factory);
    }

    /**
     * Register a {@link nl.qbusict.cupboard.convert.FieldConverterFactory}
     *
     * @param factory the factory
     */
    void registerFieldConverterFactory(FieldConverterFactory factory) {
        mConverterRegistry.registerFieldConverterFactory(factory);
    }

    /**
     * Register a field converter
     *
     * @param fieldClass the field class
     * @param converter  the converter
     * @param <T>        the type of field
     */
    <T> void registerFieldConverter(Class<T> fieldClass, FieldConverter<T> converter) {
        mConverterRegistry.registerFieldConverter(fieldClass, converter);
    }

    /**
     * Check if an entity is registered. This is primarily for use in an entity converter
     *
     * @param entityClass the entity class
     * @return true if registered with this instance, false otherwise
     */
    public boolean isRegisteredEntity(Class<?> entityClass) {
        return mEntities.contains(entityClass);
    }
}
