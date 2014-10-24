package nl.qbusict.cupboard;

import nl.qbusict.cupboard.annotation.CompositeIndex;
import nl.qbusict.cupboard.annotation.Index;

public class TestIndexAnnotatedEntity {
    public Long _id;

    @Index
    public String simpleIndex;

    @Index(unique = true)
    public String uniqueIndex;

    @Index(indexNames = {@CompositeIndex(ascending = true, indexName = "sharedIndex")})
    public String sharedIndexOne;

    @Index(indexNames = {@CompositeIndex(ascending = false, indexName = "sharedIndex", order = 2)})
    public String sharedIndexTwo;

    @Index(indexNames = {
            @CompositeIndex(ascending = true, indexName = "singleIndexThree"),
            @CompositeIndex(ascending = false, indexName = "sharedIndex", order = 1)})
    public String sharedIndexThree;

    @Index(uniqueNames = {@CompositeIndex(ascending = true, indexName = "sharedUniqueIndex")})
    public String sharedUniqueOne;

    @Index(uniqueNames = {@CompositeIndex(ascending = false, indexName = "sharedUniqueIndex", order = 1)})
    public String sharedUniqueTwo;

    @Index(uniqueNames = {
            @CompositeIndex(ascending = false, indexName = "sharedUniqueIndexTwo"),
            @CompositeIndex(ascending = false, indexName = "sharedUniqueIndex", order = 2)})
    public String sharedUniqueThree;

    @Index(uniqueNames = {@CompositeIndex(ascending = true, indexName = "sharedUniqueIndexTwo", order = 1)})
    public String sharedUniqueFour;
}
