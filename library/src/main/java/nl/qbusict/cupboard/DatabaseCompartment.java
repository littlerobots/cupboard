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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.EntityConverter.Column;
import nl.qbusict.cupboard.convert.EntityConverter.ColumnType;

/**
 * Operate on a {@link SQLiteDatabase}. A {@link DatabaseCompartment} is created from {@link Cupboard#withDatabase(SQLiteDatabase)}
 * <p/>
 * <h2>Example</h2>
 * <pre>
 * SQLiteDatabase db = ...
 * // get the book with id 1
 * Book book = cupboard().withDatabase(db).get(Book.class, 1L);
 * </pre>
 */
@SuppressLint("DefaultLocale")
public class DatabaseCompartment extends BaseCompartment {
    private static final String QUERY_BY_ID = BaseColumns._ID + " = ?";

    private final SQLiteDatabase mDatabase;

    protected DatabaseCompartment(Cupboard cupboard, SQLiteDatabase database) {
        super(cupboard);
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
        private String mLimit = null;
        private boolean mDistinct = false;

        QueryBuilder(Class<T> entityClass, DatabaseCompartment compartment) {
            this.mEntityClass = entityClass;
            this.mCompartment = compartment;
        }

        /**
         * Set the selection (where clause) and selection arguments. You can (and should) use a ? as a parameter placeholder for query
         * parameters. Each place holder will be replaced by an argument you pass in, in the specified order.
         *
         * @param selection The selection, optionally containing ? as parameter placeholders
         * @param args      The arguments matching the number of placeholders in the selection string.
         * @return this builder
         */
        public QueryBuilder<T> withSelection(String selection, String... args) {
            this.mSelection = selection;
            this.mSelectionArgs = args;
            return this;
        }

        /**
         * Set the order by clause. This is a SQL styled list of fields, optionally with "asc" or "desc" appended for specifying the order.
         * For example, to sort by the entity "name" field, in descending order pass the value <pre>name desc</pre>
         *
         * @param order the required order
         * @return this builder
         */
        public QueryBuilder<T> orderBy(String order) {
            this.mOrder = order;
            return this;
        }

        /**
         * Set the group by clause
         *
         * @param group the group by clause
         * @return this builder
         */
        public QueryBuilder<T> groupBy(String group) {
            this.mGroup = group;
            return this;
        }

        /**
         * Set the having clause
         *
         * @param having the having clause
         * @return this builder
         */
        public QueryBuilder<T> having(String having) {
            this.mHaving = having;
            return this;
        }

        /**
         * Set a projection, the columns returned, for this query. Setting a projection can be more performant, but will result in "incomplete"
         * objects.
         *
         * @param projection the columns (entity fields) to return
         * @return this builder
         */
        public QueryBuilder<T> withProjection(String... projection) {
            this.mProjection = projection;
            return this;
        }

        /**
         * Perform a query by id. This will also limit the number of results to 1 so that a query by id will return either zero or one results.
         *
         * @param id the id to query for
         * @return this builder
         */
        public QueryBuilder<T> byId(long id) {
            mSelection = "_id = ?";
            mSelectionArgs = new String[]{String.valueOf(id)};
            limit(1);
            return this;
        }

        /**
         * Set a limit on the number of rows returned. Must be greater or equal to 1.
         *
         * @param limit the maximum rows to return when the query is executed
         * @return the builder
         */
        public QueryBuilder<T> limit(int limit) {
            if (limit < 1) {
                throw new IllegalArgumentException("Limit must be greater or equal to 1");
            }
            mLimit = String.valueOf(limit);
            return this;
        }

        /**
         * Make this query distinct e.g. removing duplicate rows. This will most likely require that you pass in a projection as well.
         *
         * @return this builder.
         */
        public QueryBuilder<T> distinct() {
            mDistinct = true;
            return this;
        }

        /**
         * Execute the query
         *
         * @return The query result
         */
        public QueryResultIterable<T> query() {
            return mCompartment.query(mEntityClass, mProjection, mSelection, mSelectionArgs, mGroup, mHaving, mOrder, mLimit, mDistinct);
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
         * @return the result set as a list.
         */
        public List<T> list() {
            return query().list();
        }
    }

    /**
     * Create tables for the classes registered with {@link Cupboard#register(Class)}.
     * This is useful in {@link SQLiteOpenHelper#onCreate(SQLiteDatabase)}
     */
    public void createTables() {
        for (Class<?> entity : mCupboard.getRegisteredEntities()) {
            EntityConverter<?> converter = mCupboard.getEntityConverter(entity);
            createNewTable(mDatabase, converter.getTable(), converter.getColumns());
        }
    }

    /**
     * Upgrade and / or create tables for the classes registered with {@link Cupboard#register(Class)}
     * This is useful in {@link SQLiteOpenHelper#onUpgrade(SQLiteDatabase, int, int)}
     */
    public void upgradeTables() {
        for (Class<?> entity : mCupboard.getRegisteredEntities()) {
            EntityConverter<?> converter = mCupboard.getEntityConverter(entity);
            updateTable(mDatabase, converter.getTable(), converter.getColumns());
        }
    }

    /**
     * Drop all tables for the classes registered with {@link nl.qbusict.cupboard.Cupboard#register(Class)}
     */
    public void dropAllTables() {
        for (Class<?> entity : mCupboard.getRegisteredEntities()) {
            EntityConverter<?> converter = mCupboard.getEntityConverter(entity);
            mDatabase.execSQL("DROP TABLE IF EXISTS " + quoteTable(converter.getTable()));
        }
    }

    /**
     * Get an entity by id
     *
     * @param entityClass the entity class
     * @param id          the id of the entity
     * @return the entity or null if not found
     */
    public <T> T get(Class<T> entityClass, long id) {
        return query(entityClass).byId(id).get();
    }

    /**
     * Get an entity by the id set on an example entity
     *
     * @param object the entity
     * @return the entity, or null if not found
     * @throws IllegalArgumentException if the entity id is not set
     */
    @SuppressWarnings("unchecked")
    public <T> T get(T object) throws IllegalArgumentException {
        EntityConverter<T> converter = (EntityConverter<T>) getConverter(object.getClass());
        Long id = converter.getId(object);
        if (id != null) {
            return (T) get(object.getClass(), converter.getId(object));
        }
        throw new IllegalArgumentException("id of entity " + object.getClass() + " is not set");
    }

    /**
     * Query entities
     *
     * @param entityClass the entity to query for
     * @return a {@link QueryBuilder} for chaining
     */
    public <T> QueryBuilder<T> query(Class<T> entityClass) {
        return new QueryBuilder<T>(entityClass, this);
    }

    /**
     * Put multiple entities in a single transaction.
     *
     * @param entities the entities
     */
    public void put(Object... entities) {
        boolean mNestedTransaction = mDatabase.inTransaction();
        mDatabase.beginTransaction();
        try {
            for (Object entity : entities) {
                put(entity);
                if (!mNestedTransaction) {
                    mDatabase.yieldIfContendedSafely();
                }
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    /**
     * Put multiple entities in a single transaction.
     *
     * @param entities the entities
     */
    public void put(Collection<?> entities) {
        boolean mNestedTransaction = mDatabase.inTransaction();
        mDatabase.beginTransaction();
        try {
            for (Object entity : entities) {
                put(entity);
                if (!mNestedTransaction) {
                    mDatabase.yieldIfContendedSafely();
                }
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    /**
     * Put a single entity. If an entity of this type with this id already exists it will be replaced.
     *
     * @param entity the entity
     * @return the entity id (also set on the passed in entity)
     */
    @SuppressWarnings("unchecked")
    public <T> long put(T entity) {
        EntityConverter<T> converter = (EntityConverter<T>) getConverter(entity.getClass());
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
     * @param values      the content values
     * @return the id of the entity
     */
    public long put(Class<?> entityClass, ContentValues values) {
        EntityConverter<?> converter = getConverter(entityClass);
        Long id = values.getAsLong(BaseColumns._ID);
        if (id != null) {
            mDatabase.replaceOrThrow(quoteTable(converter.getTable()), "_id", values);
            return id;
        } else {
            id = mDatabase.insertOrThrow(quoteTable(converter.getTable()), "_id", values);
            return id;
        }
    }

    /**
     * Update entities
     *
     * @param entityClass the entity class
     * @param values      the content values. If it contains a {@link BaseColumns#_ID} then only the entity with the given id will be updated (if any)
     * @return the number of entities updated
     */
    public int update(Class<?> entityClass, ContentValues values) {
        EntityConverter<?> converter = getConverter(entityClass);
        if (values.containsKey(BaseColumns._ID)) {
            return mDatabase.update(quoteTable(converter.getTable()), values, QUERY_BY_ID, new String[]{values.getAsString(BaseColumns._ID)});
        } else {
            return mDatabase.update(quoteTable(converter.getTable()), values, null, null);
        }
    }

    /**
     * Update entities
     *
     * @param entityClass   entity class
     * @param values        content values
     * @param selection     where clause
     * @param selectionArgs selection arguments
     * @return the number of entities updated
     */
    public int update(Class<?> entityClass, ContentValues values, String selection, String... selectionArgs) {
        EntityConverter<?> converter = getConverter(entityClass);
        return mDatabase.update(quoteTable(converter.getTable()), values, selection, selectionArgs);
    }

    /**
     * Delete an entity
     *
     * @param entity the entity to delete.
     * @return true if the entity was deleted, false if no entity with the given id was found.
     */
    public <T> boolean delete(T entity) {
        @SuppressWarnings("unchecked")
        Class<T> clz = (Class<T>) entity.getClass();
        EntityConverter<T> converter = getConverter(clz);
        Long id = converter.getId(entity);
        if (id != null) {
            return delete(clz, QUERY_BY_ID, String.valueOf(id)) > 0;
        }
        return false;
    }

    /**
     * Delete an entity by id
     *
     * @param entityClass the entity class
     * @param id          the entity id
     * @return true if the entity was deleted, false if no entity with the given id was found
     */
    public boolean delete(Class<?> entityClass, long id) {
        EntityConverter<?> converter = getConverter(entityClass);
        return mDatabase.delete(quoteTable(converter.getTable()), QUERY_BY_ID, new String[]{String.valueOf(id)}) > 0;
    }

    /**
     * Delete entities
     *
     * @param entityClass   the entity class
     * @param selection     where clause
     * @param selectionArgs selection arguments
     * @return the number of deleted entities
     */
    public int delete(Class<?> entityClass, String selection, String... selectionArgs) {
        EntityConverter<?> converter = getConverter(entityClass);
        return mDatabase.delete(quoteTable(converter.getTable()), selection, selectionArgs);
    }

    boolean updateTable(SQLiteDatabase db, String table, List<Column> cols) {
        Cursor cursor = db.rawQuery("pragma table_info('" + table + "')", null);
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

    @SuppressWarnings("ConstantConditions")
    boolean updateTable(SQLiteDatabase db, String table, Cursor tableInfo, List<Column> cols) {
        Locale locale = Locale.US;
        Map<String, Column> columns = new HashMap<String, Column>(cols.size());
        for (Column col : cols) {
            if (col.type == ColumnType.JOIN) {
                continue;
            }
            columns.put(col.name.toLowerCase(locale), col);
        }

        int index = tableInfo.getColumnIndex("name");
        while (tableInfo.moveToNext()) {
            columns.remove(tableInfo.getString(index).toLowerCase(locale));
        }

        if (columns.isEmpty()) {
            return false;
        }
        for (Column column : columns.values()) {
            db.execSQL("alter table '" + table + "' add column '" + column.name + "' " + column.type.toString());
        }
        return true;
    }

    boolean createNewTable(SQLiteDatabase db, String table, List<Column> cols) {
        StringBuilder sql = new StringBuilder("create table '" + table + "' (_id integer primary key autoincrement");
        for (Column col : cols) {
            if (col.type == ColumnType.JOIN) {
                continue;
            }
            String name = col.name;
            if (!name.equals(BaseColumns._ID)) {
                sql.append(", '").append(name).append("'");
                sql.append(" ").append(col.type.toString());
            }
        }
        sql.append(");");
        db.execSQL(sql.toString());
        return true;
    }

    private <T> QueryResultIterable<T> query(Class<T> entityClass, String[] projection, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, boolean distinct) {
        EntityConverter<T> translator = getConverter(entityClass);
        Cursor cursor = mDatabase.query(distinct, quoteTable(translator.getTable()), projection, selection, selectionArgs, groupBy, having, orderBy, limit);
        return new QueryResultIterable<T>(cursor, translator);
    }

    private String quoteTable(String table) {
        return "'" + table + "'";
    }

}
