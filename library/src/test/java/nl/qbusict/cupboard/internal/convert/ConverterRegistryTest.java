package nl.qbusict.cupboard.internal.convert;

import org.junit.Test;

import java.lang.reflect.Type;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.EntityConverterFactory;
import nl.qbusict.cupboard.convert.FieldConverter;
import nl.qbusict.cupboard.convert.FieldConverterFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class ConverterRegistryTest {

    @Test
    public void entityConverterIsAddedBeforeDefaultConverter() {
        ConverterRegistry registry = new ConverterRegistry(new Cupboard());
        EntityConverterFactory myEntityFactory = new EntityConverterFactory() {
            @Override
            public <T> EntityConverter<T> create(Cupboard cupboard, Class<T> type) {
                return null;
            }
        };
        assertTrue(!registry.mEntityConverterFactories.isEmpty());
        registry.registerEntityConverterFactory(myEntityFactory);
        assertEquals(registry.mEntityConverterFactories.get(0), myEntityFactory);
    }

    @Test
    public void fieldConverterFactoryIsAddedBeforeDefaultConverterFactories() {
        ConverterRegistry registry = new ConverterRegistry(new Cupboard());
        FieldConverterFactory myFieldConverterFactory = new FieldConverterFactory() {
            @Override
            public FieldConverter<?> create(Cupboard cupboard, Type type) {
                return null;
            }
        };
        assertTrue(!registry.mFieldConverterFactories.isEmpty());
        registry.registerFieldConverterFactory(myFieldConverterFactory);
        assertEquals(registry.mFieldConverterFactories.get(0), myFieldConverterFactory);
    }

    @Test
    public void entityConverterFactoryIsAddedInOrder() {
        ConverterRegistry registry = new ConverterRegistry(new Cupboard());
        EntityConverterFactory myEntityFactory = new EntityConverterFactory() {
            @Override
            public <T> EntityConverter<T> create(Cupboard cupboard, Class<T> type) {
                return null;
            }
        };
        EntityConverterFactory mySecondEntityFactory = new EntityConverterFactory() {
            @Override
            public <T> EntityConverter<T> create(Cupboard cupboard, Class<T> type) {
                return null;
            }
        };
        registry.registerEntityConverterFactory(myEntityFactory);
        registry.registerEntityConverterFactory(mySecondEntityFactory);
        assertEquals(registry.mEntityConverterFactories.get(0), myEntityFactory);
        assertEquals(registry.mEntityConverterFactories.get(1), mySecondEntityFactory);
    }

    @Test
    public void fieldConverterFactoryIsAddedInOrder() {
        ConverterRegistry registry = new ConverterRegistry(new Cupboard());
        FieldConverterFactory myFieldConverterFactory = new FieldConverterFactory() {
            @Override
            public FieldConverter<?> create(Cupboard cupboard, Type type) {
                return null;
            }
        };
        FieldConverterFactory mySecondFieldConverterFactory = new FieldConverterFactory() {
            @Override
            public FieldConverter<?> create(Cupboard cupboard, Type type) {
                return null;
            }
        };
        registry.registerFieldConverterFactory(myFieldConverterFactory);
        registry.registerFieldConverterFactory(mySecondFieldConverterFactory);
        assertEquals(registry.mFieldConverterFactories.get(0), myFieldConverterFactory);
        assertEquals(registry.mFieldConverterFactories.get(1), mySecondFieldConverterFactory);
    }
}
