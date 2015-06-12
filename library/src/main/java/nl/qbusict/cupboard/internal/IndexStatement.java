package nl.qbusict.cupboard.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.qbusict.cupboard.annotation.CompositeIndex;
import nl.qbusict.cupboard.annotation.Index;

public class IndexStatement {
    public static final String INDEX_PREFIX = "_cb";
    public final boolean mIsUnique;
    public final String[] mColumnNames;
    public final boolean[] mAscendings;
    public final String mIndexName;

    public IndexStatement(boolean isUnique, String[] columnNames, boolean[] ascendings, String indexName) {
        this.mIsUnique = isUnique;
        this.mColumnNames = columnNames;
        this.mAscendings = ascendings;
        this.mIndexName = indexName;
    }

    public String getCreationSql(String table) {
        return getCreationSql(table, true);
    }

    public String getCreationSql(String table, boolean includeIfNotExists) {
//		create *unique* index *if not exists* indexName on tableName ('col1' asc, 'col2' desc, 'col3' asc)
        StringBuilder sb = new StringBuilder("create ");
        if (mIsUnique) {
            sb.append("unique ");
        }
        sb.append("index ");
        if (includeIfNotExists) {
            sb.append("if not exists ");
        }
        sb.append(INDEX_PREFIX).append(mIndexName).append(" on %s (");
        int size = mColumnNames.length;
        sb.append('\'').append(mColumnNames[0]).append("' ").append(mAscendings[0] ? "ASC" : "DESC");
        for (int i = 1; i < size; i++) {
            sb.append(", '").append(mColumnNames[i]).append("' ").append(mAscendings[i] ? "ASC" : "DESC");
        }
        sb.append(')');
        return String.format(sb.toString(), table, includeIfNotExists);
    }

    public static class Builder {
        public static final String GENERATED_INDEX_NAME = "%s_%s";

        Map<String, Set<IndexColumnMetadata>> indexes = new HashMap<String, Set<IndexColumnMetadata>>();
        Map<String, Set<IndexColumnMetadata>> uniqueIndexes = new HashMap<String, Set<IndexColumnMetadata>>();

        public void addIndexedColumn(String table, String name, Index index) {
            boolean added = false;
            if (index.indexNames().length != 0) {
                addCompositeIndexes(name, indexes, index.indexNames());
                added = true;
            }
            if (index.uniqueNames().length != 0) {
                addCompositeIndexes(name, uniqueIndexes, index.uniqueNames());
                added = true;
            }
            if (!added) {
                boolean unique = index.unique();
                addCompositeIndex(name, unique ? uniqueIndexes : indexes, CompositeIndex.DEFAULT_ASCENDING, CompositeIndex.DEFAULT_ORDER, String.format(GENERATED_INDEX_NAME, table, name));
            }
        }

        private void addCompositeIndexes(String name, Map<String, Set<IndexColumnMetadata>> collectionToAdd, CompositeIndex[] composites) {
            for (CompositeIndex ci : composites) {
                addCompositeIndex(name, collectionToAdd, ci.ascending(), ci.order(), ci.indexName());
            }
        }

        private void addCompositeIndex(String columnName, Map<String, Set<IndexColumnMetadata>> collectionToAdd, boolean ascending, int order, String indexName) {
            Set<IndexColumnMetadata> set = collectionToAdd.get(indexName);
            if (set == null) {
                set = new HashSet<IndexStatement.Builder.IndexColumnMetadata>();
                collectionToAdd.put(indexName, set);
            }
            IndexColumnMetadata indexColumnMetadata = new IndexColumnMetadata(columnName, ascending, order);
            if (!set.add(indexColumnMetadata)) {
                throw new IllegalArgumentException(String.format("Column '%s' has two indexes with the same name %s", columnName, indexName));
            }
        }

        public List<IndexStatement> build() {
            List<IndexStatement> indexStatements = new ArrayList<IndexStatement>();
            Set<String> indexNames = new HashSet<String>();
            for (Entry<String, Set<IndexColumnMetadata>> indexEntry : indexes.entrySet()) {
                String indexName = indexEntry.getKey();
                indexNames.add(indexName);
                addStatementToList(indexName, false, indexStatements, indexEntry.getValue());
            }
            for (Entry<String, Set<IndexColumnMetadata>> indexEntry : uniqueIndexes.entrySet()) {
                String indexName = indexEntry.getKey();
                // Validate a column has not the same unique and non-unique index name.
                if (!indexNames.add(indexName)) {
                    throw new IllegalArgumentException(String.format("There are both unique and non-unique indexes with the same name : %s", indexName));
                }
                addStatementToList(indexName, true, indexStatements, indexEntry.getValue());
            }
            return indexStatements;
        }

        public Map<String, IndexStatement> buildAsMap() {
            Map<String, IndexStatement> map = new HashMap<String, IndexStatement>();
            for (IndexStatement is : build()) {
                map.put(is.mIndexName, is);
            }
            return map;
        }

        public void addStatementToList(String indexName, boolean unique, List<IndexStatement> indexStatements, Set<IndexColumnMetadata> metadatas) {
            List<IndexColumnMetadata> columnMetadatas = new ArrayList<IndexColumnMetadata>(metadatas);
            Collections.sort(columnMetadatas);
            int size = columnMetadatas.size();
            String[] columnNames = new String[size];
            boolean[] ascendingColumns = new boolean[size];
            for (int i = 0; i < size; i++) {
                IndexColumnMetadata indexColumnMetadata = columnMetadatas.get(i);
                columnNames[i] = indexColumnMetadata.mColumnName;
                ascendingColumns[i] = indexColumnMetadata.mAscending;
            }
            indexStatements.add(new IndexStatement(unique, columnNames, ascendingColumns, indexName));
        }

        class IndexColumnMetadata implements Comparable<IndexColumnMetadata> {
            String mColumnName;
            boolean mAscending;
            int mOrder;

            public IndexColumnMetadata(String columnName, boolean ascending, int order) {
                this.mColumnName = columnName;
                this.mAscending = ascending;
                this.mOrder = order;
            }

            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result
                        + ((mColumnName == null) ? 0 : mColumnName.hashCode());
                return result;
            }

            @Override
            public int compareTo(IndexColumnMetadata another) {
                if (mOrder < another.mOrder) {
                    return -1;
                }
                if (mOrder > another.mOrder) {
                    return 1;
                }
                throw new IllegalArgumentException(String.format("Columns '%s' and '%s' cannot have the same composite index order %d", mColumnName, another.mColumnName, mOrder));
            }

            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (((Object) this).getClass() != obj.getClass())
                    return false;
                IndexColumnMetadata other = (IndexColumnMetadata) obj;
                if (mColumnName == null) {
                    if (other.mColumnName != null)
                        return false;
                } else if (!mColumnName.equals(other.mColumnName))
                    return false;
                return true;
            }
        }
    }

}
