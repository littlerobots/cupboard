package nl.qbusict.cupboard.convert;

import android.test.AndroidTestCase;

import nl.qbusict.cupboard.annotation.Index;

public class IndexBuilderTest extends AndroidTestCase {

    public void testIndexBuilder() {
        Index index = new IndexBuilder().build();
        assertEquals(0, index.indexNames().length);
        assertEquals(0, index.uniqueNames().length);
        assertFalse(index.unique());

        index = new IndexBuilder().unique().build();

        assertEquals(0, index.indexNames().length);
        assertEquals(0, index.uniqueNames().length);
        assertTrue(index.unique());

        index = new IndexBuilder().named("my_index").build();

        assertEquals(1, index.indexNames().length);
        assertEquals(0, index.uniqueNames().length);
        assertFalse(index.unique());

        index = new IndexBuilder().named("my_index").unique().order(1).named("my_second_index").descending().build();

        assertEquals(1, index.indexNames().length);
        assertEquals(1, index.uniqueNames().length);
        assertEquals("my_second_index", index.indexNames()[0].indexName());
        assertFalse(index.indexNames()[0].ascending());
        assertEquals(0, index.indexNames()[0].order());

        assertEquals("my_index", index.uniqueNames()[0].indexName());
        assertEquals(1, index.uniqueNames()[0].order());
    }
}
