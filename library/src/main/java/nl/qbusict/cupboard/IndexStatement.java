package nl.qbusict.cupboard;

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

	
	public final boolean isUnique;
	public final String[] columnNames;
	public final boolean[] ascendings;
	public final String indexName;
	
	public IndexStatement(boolean isUnique, String[] columnNames, boolean[] ascendings, String indexName) {
		this.isUnique = isUnique;
		this.columnNames = columnNames;
		this.ascendings = ascendings;
		this.indexName = indexName;
	}
	
	public String getCreationSql(String table){
		return getCreationSql(table, true);
	}
	
	public String getCreationSql(String table, boolean includeIfNotExists) {
//		create *unique* index *if not exists* indexName on tableName ('col1' asc, 'col2' desc, 'col3' asc)
		StringBuilder sb = new StringBuilder("create ");
		if ( isUnique ){
			sb.append("unique ");
		}
		sb.append("index ");
		if ( includeIfNotExists ){
			sb.append("if not exists ");
		}
		sb.append(indexName).append(" on %s (");
		int size = columnNames.length;
		sb.append('\'').append(columnNames[0]).append("' ").append(ascendings[0]? "ASC" : "DESC");
		for(int i = 1; i < size; i++){
			sb.append(", '").append(columnNames[i]).append("' ").append(ascendings[i]? "ASC" : "DESC");
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
			if ( index.indexNames().length != 0 ){
				addCompositeIndexes(name, indexes, index.indexNames());
				added = true;
			}
			if ( index.uniqueNames().length != 0 ){
				addCompositeIndexes(name, uniqueIndexes, index.uniqueNames());
				added = true;
			}
			if ( !added ){
				boolean unique = index.unique();
				addCompositeIndex(name, unique ? uniqueIndexes : indexes, CompositeIndex.DEFAULT_ASCENDING, CompositeIndex.DEFAULT_ORDER, String.format(GENERATED_INDEX_NAME, table, name));
			}
		}

		private void addCompositeIndexes(String name, Map<String, Set<IndexColumnMetadata>> collectionToAdd, CompositeIndex[] composites) {
			for(CompositeIndex ci:composites){
				addCompositeIndex(name, collectionToAdd, ci.ascending(), ci.order(), ci.indexName());
			}
		}

		private void addCompositeIndex(String columnName, Map<String, Set<IndexColumnMetadata>> collectionToAdd, boolean ascending, int order, String indexName) {
			Set<IndexColumnMetadata> set = collectionToAdd.get(indexName);
			if ( set == null ){
				set = new HashSet<IndexStatement.Builder.IndexColumnMetadata>();
				collectionToAdd.put(indexName, set);
			}
			IndexColumnMetadata indexColumnMetadata = new IndexColumnMetadata(columnName, ascending, order);
			if ( ! set.add(indexColumnMetadata) ){
				throw new IllegalArgumentException(String.format("Column '%s' has two indexes with the same name", columnName, indexName));
			}
		}
		
		public List<IndexStatement> build(){
			List<IndexStatement> indexStatements = new ArrayList<IndexStatement>();
			Set<String> indexNames = new HashSet<String>();
			for(Entry<String, Set<IndexColumnMetadata>> indexEntry:indexes.entrySet()){
				String indexName = indexEntry.getKey();
				indexNames.add(indexName);
				addStatementToList(indexName, false, indexStatements, indexEntry.getValue());
			}
			for(Entry<String, Set<IndexColumnMetadata>> indexEntry:uniqueIndexes.entrySet()){
				String indexName = indexEntry.getKey();
				// Validate a column has not the same unique and non-unique index name.
				if ( !indexNames.add(indexName) ){
					throw new IllegalArgumentException(String.format("There are both unique and non-unique indexes with the same name : %s",indexName));
				}
				addStatementToList(indexName, true, indexStatements, indexEntry.getValue());
			}
			return indexStatements;
		}
		
		public Map<String,IndexStatement> buildAsMap(){
			Map<String,IndexStatement> map = new HashMap<String,IndexStatement>();
			for(IndexStatement is:build()){
				map.put(is.indexName, is);
			}
			return map;
		}
		
		public void addStatementToList(String indexName, boolean unique, List<IndexStatement> indexStatements, Set<IndexColumnMetadata> metadatas){
			List<IndexColumnMetadata> columnMetadatas = new ArrayList<IndexColumnMetadata>(metadatas);
			Collections.sort(columnMetadatas);
			int size = columnMetadatas.size();
			String[] columnNames = new String[size];
			boolean[] ascendingColumns = new boolean[size];
			for(int i=0; i < size; i++){
				IndexColumnMetadata indexColumnMetadata = columnMetadatas.get(i);
				columnNames[i] = indexColumnMetadata.columnName;
				ascendingColumns[i] = indexColumnMetadata.ascending;
			}
			indexStatements.add(new IndexStatement(unique, columnNames, ascendingColumns, indexName));
		}
		
		class IndexColumnMetadata implements Comparable<IndexColumnMetadata>{
			String columnName;
			boolean ascending;
			int order;
			
			public IndexColumnMetadata(String columnName, boolean ascending, int order) {
				this.columnName = columnName;
				this.ascending = ascending;
				this.order = order;
			}

			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((columnName == null) ? 0 : columnName.hashCode());
				return result;
			}

			public int compareTo(IndexColumnMetadata another) {
				if ( order < another.order ){
					return -1;
				}
				if ( order > another.order ){
					return 1;
				}
				throw new IllegalArgumentException(String.format("Columns '%s' and '%s' cannot have the same composite index order %d", columnName, another.columnName, order));
			}
			
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				IndexColumnMetadata other = (IndexColumnMetadata) obj;
				if (columnName == null) {
					if (other.columnName != null)
						return false;
				} else if (!columnName.equals(other.columnName))
					return false;
				return true;
			}
		}
	}

}
