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

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.qbusict.cupboard.convert.Converter;
import nl.qbusict.cupboard.convert.Converter.Column;
import nl.qbusict.cupboard.convert.ConverterHolder;

/**
 * Operate on a {@link SQLiteDatabase}. A {@link DatabaseCompartment} is created from {@link Cupboard#withDatabase(SQLiteDatabase)}
 *
 * <h2>Example</h2>
 * <pre>
 * SQLiteDatabase db = ...
 * // get the book with id 1
 * Book book = cupboard().withDatabase(db).get(Book.class, 1L);
 * </pre>
 */
@SuppressLint("DefaultLocale")
public class DatabaseCompartment extends BaseCompartment {
    private static final String QUERY_BY_ID = BaseColumns._ID+" = ?";

    private final SQLiteDatabase mDatabase;

    protected DatabaseCompartment(Map<Class<?>, ConverterHolder<?>> converters, SQLiteDatabase database) {
        super(converters);
        this.mDatabase = database;
    }

    public static class QueryBuilder<T> {
        private final Class<T> mEntityClass;
        private final DatabaseCompartment mCompartment;
        private String mSelection;
        private String[] mSelectionArgs;
        private String mOrder;
        private String mGroup;
        private String mHaving;
        private String[] mProjection;

        QueryBuilder(Class<T> entityClass, DatabaseCompartment compartment) {
            this.mEntityClass = entityClass;
            this.mCompartment = compartment;
        }

        public QueryBuilder<T> withSelection(String selection, String...args) {
            this.mSelection = selection;
            this.mSelectionArgs = args;
            return this;
        }

        public QueryBuilder<T> orderBy(String order) {
            this.mOrder = order;
            return this;
        }

        public QueryBuilder<T> groupBy(String group) {
            this.mGroup = group;
            return this;
        }

        public QueryBuilder<T> having(String having) {
            this.mHaving = having;
            return this;
        }

        public QueryBuilder<T> withProjection(String... projection) {
            this.mProjection = projection;
            return this;
        }

        public QueryBuilder<T> byId(long id) {
            mSelection = "_id = ?";
            mSelectionArgs = new String[] { String.valueOf(id) };
            return this;
        }

        public QueryResultIterable<T> query() {
            return mCompartment.query(mEntityClass, mProjection, mSelection, mSelectionArgs, mGroup, mHaving, mOrder);
        }

        /**
         * Convenience for calling {@link #query()}.getCursor()
         * @return the cursor
         */
        public Cursor getCursor() {
            return query().getCursor();
        }

        /**
         * Convenience for calling {@link #query()}.get()
         * @return the entity or null if the query didn't return any results
         */
        public T get() {
            return query().get();
        }
    }

    /**
     * Create tables for the classes registered with {@link Cupboard#register(Class)}.
     * This is useful in {@link SQLiteOpenHelper#onCreate(SQLiteDatabase)}
     */
    public void createTables() {
        createAllConverters();
        for (Entry<Class<?>, ConverterHolder<?>> entry : mConverters.entrySet()) {
            Converter<?> converter = entry.getValue().get();
            createNewTable(mDatabase, converter.getTable(), converter.getColumns());
        }
    }

    /**
     * Upgrade and / or create tables for the classes registered with {@link Cupboard#register(Class)}
     * This is useful in {@link SQLiteOpenHelper#onUpgrade(SQLiteDatabase, int, int)}
     */
    public void upgradeTables() {
        createAllConverters();
        for (Entry<Class<?>, ConverterHolder<?>> entry : mConverters.entrySet()) {
            Converter<?> converter = entry.getValue().get();
            updateTable(mDatabase, converter.getTable(), converter.getColumns());
        }
    }

    /**
     * Get an entity by id
     * @param entityClass the entity class
     * @param id the id of the entity
     * @return the entity or null if not found
     */
    public <T> T get(Class<T> entityClass, long id) {
        return query(entityClass).byId(id).get();
    }

    /**
     * Get an entity by the id set on an example entity
     * @param object the entity
     * @return the entity, or null if not found
     * @throws IllegalArgumentException if the entity id is not set
     */
    @SuppressWarnings("unchecked")
    public <T> T get(T object) throws IllegalArgumentException {
        Converter<T> converter = (Converter<T>) getConverter(object.getClass());
        Long id = converter.getId(object);
        if (id != null) {
            return (T) get(object.getClass(), converter.getId(object));
        }
        throw new IllegalArgumentException("id of entity "+object.getClass()+" is not set");
    }

    /**
     * Query entities
     * @param entityClass the entity to query for
     * @return a {@link QueryBuilder} for chaining
     */
    public <T> QueryBuilder<T> query(Class<T> entityClass) {
        return new QueryBuilder<T>(entityClass, this);
    }

    /**
     * Put multiple entities in a single transaction.
     * @param entities the entities
     */
    public void put(Object...entities) {
        mDatabase.beginTransaction();
        try {
            for (Object entity : entities) {
                put(entity);
                mDatabase.yieldIfContendedSafely();
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    /**
     * Put a single entity. If an entity of this type with this id already exists it will be replaced.
     * @param entity the entity
     * @return the entity id (also set on the passed in entity)
     */
    @SuppressWarnings("unchecked")
    public <T> long put(T entity) {
        Converter<T> converter = (Converter<T>) getConverter(entity.getClass());
        ContentValues values = new ContentValues();
        converter.toValues(entity, values);
        Long id = values.getAsLong(BaseColumns._ID);
        long insertedId = put(entity.getClass(), values);
        if (id == null) {
            converter.setId(insertedId, entity);
        }
        return id == null ? insertedId : id;
    }

    /**
     * Put an entity from a {@link ContentValues} object.
     * If the content values contain a {@link BaseColumns#_ID} then this id will be used and an existing entity will be replaced.
     *
     * @param entityClass the entity class
     * @param values the content values
     * @return the id of the entity
     */
    public long put(Class<?> entityClass, ContentValues values) {
        Converter<?> converter = getConverter(entityClass);
        Long id = values.getAsLong(BaseColumns._ID);
        if (id != null) {
            mDatabase.replaceOrThrow(converter.getTable(), "_id", values);
            return id;
        } else {
            id = mDatabase.insertOrThrow(converter.getTable(), "_id", values);
            return id;
        }
    }

    /**
     * Update entities
     * @param entityClass the entity class
     * @param values the content values. If it contains a {@link BaseColumns#_ID} then only the entity with the given id will be updated (if any)
     * @return the number of entities updated
     */
    public int update(Class<?> entityClass, ContentValues values) {
        Converter<?> converter = getConverter(entityClass);
        if (values.containsKey(BaseColumns._ID)) {
            return mDatabase.update(converter.getTable(), values, QUERY_BY_ID, new String[] {values.getAsString(BaseColumns._ID)});
        } else {
            return mDatabase.update(converter.getTable(), values, null, null);
        }
    }

    /**
     * Update entities
     * @param entityClass entity class
     * @param values content values
     * @param selection where clause
     * @param selectionArgs selection arguments
     * @return the number of entities updated
     */
    public int update(Class<?> entityClass, ContentValues values, String selection, String... selectionArgs) {
        Converter<?> converter = getConverter(entityClass);
        return mDatabase.update(converter.getTable(), values, selection, selectionArgs);
    }

    /**
     * Delete an entity
     * @param entity the entity to delete.
     * @return true if the entity was deleted, false if no entity with the given id was found.
     */
    public <T> boolean delete(T entity) {
        @SuppressWarnings("unchecked")
        Class<T> clz = (Class<T>)entity.getClass();
        Converter<T> converter = getConverter(clz);
        Long id = converter.getId(entity);
        if (id != null) {
            return delete(clz, QUERY_BY_ID, String.valueOf(id)) > 0;
        }
        return false;
    }

    /**
     * Delete an entity by id
     * @param entityClass the entity class
     * @param id the entity id
     * @return true if the entity was deleted, false if no entity with the given id was found
     */
    public boolean delete(Class<?> entityClass, long id) {
        Converter<?> converter = getConverter(entityClass);
        return mDatabase.delete(converter.getTable(), QUERY_BY_ID, new String[] { String.valueOf(id) }) > 0;
    }

    /**
     * Delete entities
     * @param entityClass the entity class
     * @param selection where clause
     * @param selectionArgs selection arguments
     * @return the number of deleted entities
     */
    public int delete(Class<?> entityClass, String selection, String... selectionArgs) {
       Converter<?> converter = getConverter(entityClass);
       return mDatabase.delete(converter.getTable(), selection, selectionArgs);
    }

    boolean updateTable(SQLiteDatabase db, String table, List<Column> cols) {
        Cursor cursor = db.rawQuery("pragma table_info('"+table+"')", null);
        try {
            if (cursor.getCount() == 0) {
                return createNewTable(db, table, cols);
            } else {
                return updateTable(db, table, cursor, cols);
            }
        } finally {
            cursor.close();
        }
    }

    boolean updateTable(SQLiteDatabase db, String table, Cursor tableInfo, List<Column> cols) {
        Map<String, Column> columns = new HashMap<String, Converter.Column>(cols.size());
        for (Column col : cols) {
            columns.put(col.name.toLowerCase(), col);
        }

        int index = tableInfo.getColumnIndex("name");
        while (tableInfo.moveToNext()) {
            columns.remove(tableInfo.getString(index).toLowerCase());
        }

        if (columns.isEmpty()) {
            return false;
        }
        for (Column column : columns.values()) {
            db.execSQL("alter table '"+table+"' add column '"+column.name+"' "+column.type.toString());
        }
        return true;
    }

    boolean createNewTable(SQLiteDatabase db, String table, List<Column> cols) {
        StringBuilder sql = new StringBuilder("create table '"+table+"' (_id integer primary key autoincrement");
        for (Column col : cols) {
            String name = col.name;
            if (!name.equals(BaseColumns._ID)) {
                sql.append(", '").append(name).append("'");
                sql.append(" ").append(col.type.toString());
            }
        }
        sql.append(");");
        try {
            db.execSQL(sql.toString());
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }

    private <T> QueryResultIterable<T> query(Class<T> entityClass, String[] projection, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        Converter<T> translator = getConverter(entityClass);
        Cursor cursor = mDatabase.query(translator.getTable(), projection, selection, selectionArgs, groupBy, having, orderBy);
        return new QueryResultIterable<T>(cursor, translator);
    }

}
