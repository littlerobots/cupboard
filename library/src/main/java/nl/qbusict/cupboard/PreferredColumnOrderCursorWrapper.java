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

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Arrays;
import java.util.List;

import nl.qbusict.cupboard.convert.EntityConverter.Column;

/**
 * A cursor that guarantees that it will return columns of the wrapped cursor in the requested order and with the requested casing.
 * This cursor is passed to {@link nl.qbusict.cupboard.convert.EntityConverter#fromCursor(Cursor)} so that the converter does not have to do any look up from column to translation:
 * it can just assume that the column at a certain index is the same as returned from {@link nl.qbusict.cupboard.convert.EntityConverter#getColumns()} at the same index.
 */
class PreferredColumnOrderCursorWrapper extends CursorWrapper {

    private String[] mColumns;
    private final int[] mColumnMap;

    public PreferredColumnOrderCursorWrapper(Cursor cursor, String[] columns) {
        super(cursor);
        this.mColumns = columns;
        this.mColumnMap = new int[columns.length];
        Arrays.fill(mColumnMap, -1);
        this.mColumns = remapColumns(cursor.getColumnNames(), columns);
    }

    public PreferredColumnOrderCursorWrapper(Cursor cursor, List<Column> columns) {
        this(cursor, toColumNames(columns));
    }

    private static String[] toColumNames(List<Column> columns) {
        String[] cols = new String[columns.size()];
        for (int i = cols.length - 1; i >= 0; i--) {
            cols[i] = columns.get(i).name;
        }
        return cols;
    }

    private String[] remapColumns(String[] cursorColumns, String[] columns) {
        int last = 0;
        for (int i = 0; i < columns.length; i++) {
            int index = getColumnIndex(columns[i]);
            mColumnMap[i] = index;
            if (index != -1) {
                last = i;
            }
        }
        if (last + 1 < columns.length) {
            String[] newCols = new String[last + 1];
            System.arraycopy(columns, 0, newCols, 0, last + 1);
            columns = newCols;
        }
        return columns;
    }

    @Override
    public String[] getColumnNames() {
        return mColumns;
    }

    @Override
    public short getShort(int columnIndex) {
        int index = mColumnMap[columnIndex];
        if (index == -1) {
            return 0;
        }
        return super.getShort(index);
    }

    @Override
    public byte[] getBlob(int columnIndex) {
        int index = mColumnMap[columnIndex];
        if (index == -1) {
            return null;
        }
        return super.getBlob(index);
    }

    @Override
    public double getDouble(int columnIndex) {
        int index = mColumnMap[columnIndex];
        if (index == -1) {
            return 0;
        }
        return super.getDouble(index);
    }

    @Override
    public float getFloat(int columnIndex) {
        int index = mColumnMap[columnIndex];
        if (index == -1) {
            return 0;
        }
        return super.getFloat(index);
    }

    @Override
    public int getInt(int columnIndex) {
        int index = mColumnMap[columnIndex];
        if (index == -1) {
            return 0;
        }
        return super.getInt(index);
    }

    @Override
    public long getLong(int columnIndex) {
        int index = mColumnMap[columnIndex];
        if (index == -1) {
            return 0;
        }
        return super.getLong(index);
    }

    @Override
    public String getString(int columnIndex) {
        int index = mColumnMap[columnIndex];
        if (index == -1) {
            return null;
        }
        return super.getString(index);
    }

    @Override
    public boolean isNull(int columnIndex) {
        int index = mColumnMap[columnIndex];
        if (index == -1) {
            return true;
        }
        return super.isNull(index);
    }

    @Override
    public int getColumnCount() {
        return mColumns.length;
    }
}
