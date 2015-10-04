package nl.qbusict.cupboard;

import android.content.ContentProviderOperation;
import android.net.Uri;
import android.test.AndroidTestCase;

import java.util.ArrayList;

public class ProviderOperationsCompartmentTest extends AndroidTestCase {

    public void testPutWithYield() {
        Cupboard cupboard = new Cupboard();
        cupboard.register(TestEntity.class);
        ArrayList<ContentProviderOperation> operations = cupboard
                .withOperations(new ArrayList<ContentProviderOperation>(16))
                .put(Uri.parse("content://dummy"), new TestEntity()).getOperations();

        assertEquals(1, operations.size());
        assertFalse(operations.get(0).isYieldAllowed());
        operations = cupboard
                .withOperations(new ArrayList<ContentProviderOperation>(16))
                .yield()
                .put(Uri.parse("content://dummy"), new TestEntity()).getOperations();
        assertTrue(operations.get(0).isYieldAllowed());
        operations = cupboard
                .withOperations(new ArrayList<ContentProviderOperation>(16))
                .yield()
                .put(Uri.parse("content://dummy"), TestEntity.class, new TestEntity(), new TestEntity())
                .getOperations();
        assertEquals(2, operations.size());
        assertFalse(operations.get(0).isYieldAllowed());
        assertTrue(operations.get(1).isYieldAllowed());
    }

    public void testDeleteWithYield() {
        Cupboard cupboard = new Cupboard();
        cupboard.register(TestEntity.class);
        TestEntity te = new TestEntity();
        te._id = 1L;
        ArrayList<ContentProviderOperation> operations = cupboard
                .withOperations(new ArrayList<ContentProviderOperation>(16))
                .delete(Uri.parse("content://dummy"), te).getOperations();
        assertEquals(1, operations.size());
        assertFalse(operations.get(0).isYieldAllowed());
        operations = cupboard
                .withOperations(new ArrayList<ContentProviderOperation>(16))
                .yield()
                .delete(Uri.parse("content://dummy"), te).getOperations();
        assertEquals(1, operations.size());
        assertTrue(operations.get(0).isYieldAllowed());
    }

    public void testYieldAmount() {
        Cupboard cupboard = new Cupboard();
        cupboard.register(TestEntity.class);
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(16);
        ProviderOperationsCompartment compartment = cupboard.withOperations(operations)
                .yieldAfter(2)
                .put(Uri.parse("content://dummy"), new TestEntity());
        assertFalse(operations.get(0).isYieldAllowed());
        compartment.put(Uri.parse("content://dummy"), new TestEntity());
        assertTrue(operations.get(1).isYieldAllowed());
        compartment.put(Uri.parse("content://dummy"), new TestEntity());
        assertFalse(operations.get(2).isYieldAllowed());
        compartment.put(Uri.parse("content://dummy"), new TestEntity());
        assertTrue(operations.get(1).isYieldAllowed());
    }
}
