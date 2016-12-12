package nl.qbusict.cupboard;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Test;

import java.util.List;

import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.EntityConverterFactory;
import nl.qbusict.cupboard.convert.ReflectiveEntityConverter;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class CupboardUnitTest {

    @Test
    public void resolveRegisteredEntitiesIncludesSuperClasses() {
        Cupboard cupboard = new Cupboard();
        cupboard.register(AbstractEntity.class);
        assertTrue(cupboard.isRegisteredEntity(MyEntity.class));
        cupboard = new Cupboard();
        cupboard.register(MyEntity.class);
        assertTrue(cupboard.isRegisteredEntity(MyEntity.class));
        assertFalse(cupboard.isRegisteredEntity(String.class));
    }

    @Test
    public void resolveBestMatchingEntityConverter() {
        Cupboard cupboard = new CupboardBuilder().registerEntityConverterFactory(new EntityConverterFactory() {
            @Override
            public <T> EntityConverter<T> create(Cupboard cupboard, Class<T> type) {
                if (type == MyEntity.class) {
                    return new EntityConverter<T>() {
                        @Override
                        public T fromCursor(Cursor cursor) {
                            return null;
                        }

                        @Override
                        public void toValues(T object, ContentValues values) {

                        }

                        @Override
                        public List<Column> getColumns() {
                            return null;
                        }

                        @Override
                        public void setId(Long id, T instance) {

                        }

                        @Override
                        public Long getId(T instance) {
                            return null;
                        }

                        @Override
                        public String getTable() {
                            return null;
                        }
                    };
                }
                return null;
            }
        }).build();

        cupboard.register(AbstractEntity.class);
        cupboard.register(MyEntity.class);
        assertTrue(cupboard.getEntityConverter(MySecondEntity.class) instanceof ReflectiveEntityConverter);
        assertFalse(cupboard.getEntityConverter(MyEntity.class) instanceof ReflectiveEntityConverter);
    }

    private static abstract class AbstractEntity {
    }

    ;

    private static class MyEntity extends AbstractEntity {
    }

    private static class MySecondEntity extends AbstractEntity {
    }
}
