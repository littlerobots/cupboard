package nl.qbusict.cupboard;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.List;

import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.EntityConverterFactory;

public class CupboardBuilderTest extends AndroidTestCase {

    public void testEnableAnnotations() {
        Cupboard cupboard = new CupboardBuilder().useAnnotations().build();
        assertEquals(true, cupboard.isUseAnnotations());
    }

    public void testRegisterEntityConverterFactory() {
        final EntityConverter<Object> dummyConverter = new EntityConverter<Object>() {

            @Override
            public Object fromCursor(Cursor cursor) {
                return null;
            }

            @Override
            public void toValues(Object object, ContentValues values) {

            }

            @Override
            public List<Column> getColumns() {
                return null;
            }

            @Override
            public void setId(Long id, Object instance) {

            }

            @Override
            public Long getId(Object instance) {
                return 1L;
            }

            @Override
            public String getTable() {
                return "Dummy";
            }
        };
        Cupboard cupboard = new CupboardBuilder().registerEntityConverterFactory(new EntityConverterFactory() {
            @Override
            public <T> EntityConverter<T> create(Cupboard cupboard, Class<T> type) {
                return (EntityConverter<T>) dummyConverter;
            }
        }).build();

        cupboard.register(TestEntity.class);

        EntityConverter<TestEntity> converter = cupboard.getEntityConverter(TestEntity.class);
        assertEquals(dummyConverter, converter);
    }
}
