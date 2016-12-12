/*
 * Copyright (C) 2016 Little Robots
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package nl.qbusict.cupboard;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * This interface closely mimics the SQLiteDatabase interface, to allow alternative
 * implementations of SQLite to be used with Cupboard.
 */
public interface CupboardDatabase {

    long insertOrThrow(String table, String nullColumnHack, ContentValues values);

    long replaceOrThrow(String table, String nullColumnHack, ContentValues values);

    int update(String table, ContentValues values, String selection, String[] selectionArgs);

    Cursor query(boolean distinct, String table, String[] columns,
                 String selection, String[] selectionArgs, String groupBy,
                 String having, String orderBy, String limit);

    Cursor rawQuery(String sql, String[] selectionArgs);

    int delete(String table, String selection, String[] selectionArgs);

    boolean inTransaction();

    void beginTransaction();

    void yieldIfContendedSafely();

    void setTransactionSuccessful();

    void endTransaction();

    void execSQL(String sql);
}