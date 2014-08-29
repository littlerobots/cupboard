package nl.qbusict.cupboard;

import android.test.AndroidTestCase;

import nl.qbusict.cupboard.convert.EntityConverter;

public class EmbeddedEntityTest extends AndroidTestCase {
    public void testResolveNestedConverter() {
        Cupboard cupboard = new Cupboard();
        cupboard.register(MyEntity.class);
        cupboard.register(MySecondEntity.class);
        EntityConverter<MyEntity> converter = cupboard.getEntityConverter(MyEntity.class);
        converter.getTable();
    }

    public void testWithEntity() {
        Cupboard cupboard = new Cupboard();
        cupboard.register(MyEntity.class);
        cupboard.register(MySecondEntity.class);

        // request converter for MySecondEntity
        // EntityConverter<MySecondEntity> will request FieldConverter<Long> and FieldConverter<MySecondEntity>
        // FieldConverter<MySecondEntity> will request EntityConverter<MySecondEntity> (in flight)
        // This works because the actual EntityConverter<MySecondEntity> isn't touched, the ColumnType is static.
        EntityConverter<MySecondEntity> converter = cupboard.getEntityConverter(MySecondEntity.class);


        cupboard = new Cupboard();
        cupboard.register(MyEntity.class);
        cupboard.register(MySecondEntity.class);

        // will request a converter for MyEntity
        // EntityConverter<MyEntity> will request FieldConverter<Long> and FieldConverter<MySecondEntity>
        // EntityConverter<MySecondEntity> will request FieldConverter<Long> (resolved) and FieldConverter<MySecondEntity> (in flight)
        // EntityConverter<MySecondEntity> will resolve ColumnType on FieldConverter<MySecondEntity> but since this is a stub, blows up

        EntityConverter<MyEntity> converter2 = cupboard.getEntityConverter(MyEntity.class);
    }

    public static class MySecondEntity {
        public Long _id;
        public MySecondEntity looped;
    }

    public static class MyEntity {
        public Long _id;
        public MySecondEntity mySecondEntity;
    }
}
