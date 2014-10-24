package nl.qbusict.cupboard.convert;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import nl.qbusict.cupboard.annotation.CompositeIndex;
import nl.qbusict.cupboard.annotation.Index;

/**
 * Builder that can be used to build {@link nl.qbusict.cupboard.annotation.Index} annotations. This is useful for custom {@link nl.qbusict.cupboard.convert.EntityConverter}s
 * to specify their column indices in a {@link nl.qbusict.cupboard.convert.EntityConverter.Column}. For example {@link nl.qbusict.cupboard.convert.ReflectiveEntityConverter} will only process annotations when
 * {@link nl.qbusict.cupboard.Cupboard#isUseAnnotations()} is <code>true</code>. By overriding {@link nl.qbusict.cupboard.convert.ReflectiveEntityConverter#getIndexes(java.lang.reflect.Field)}
 * the indices can be constructed in code using this builder, without the need for reflection.
 */
public class IndexBuilder {

    private CompositeIndexBuilder mCompositeIndexBuilder = new CompositeIndexBuilder();
    private boolean mUnique = false;

    /**
     * Set the index name to use
     *
     * @param name the index name
     * @return a {@link nl.qbusict.cupboard.convert.IndexBuilder.CompositeIndexBuilder} for chaining
     */
    public CompositeIndexBuilder named(String name) {
        mCompositeIndexBuilder.named(name);
        if (mUnique) {
            mCompositeIndexBuilder.unique();
        }
        return mCompositeIndexBuilder;
    }

    /**
     * Make this index unique
     *
     * @return the builder for chaining
     */
    public IndexBuilder unique() {
        mUnique = true;
        return this;
    }

    /**
     * Build the index
     *
     * @return the index
     */
    public Index build() {
        return new IndexImpl(mUnique, mCompositeIndexBuilder.mIndices, mCompositeIndexBuilder.mUniqueIndices);
    }

    public class CompositeIndexBuilder {
        private List<CompositeIndex> mIndices = new ArrayList<CompositeIndex>(10);
        private List<CompositeIndex> mUniqueIndices = new ArrayList<CompositeIndex>(10);

        private CompositeIndexImpl mCurrentIndex;

        private CompositeIndexBuilder() {
        }

        /**
         * Add a new name, creating a composite index
         *
         * @param name the index name to use
         * @return the builder for chaining
         */
        public CompositeIndexBuilder named(String name) {
            mCurrentIndex = new CompositeIndexImpl(name);
            mIndices.add(mCurrentIndex);
            return this;
        }

        /**
         * Make the index unique
         *
         * @return the builder for chaining
         */
        public CompositeIndexBuilder unique() {
            if (mIndices.remove(mCurrentIndex)) {
                mUniqueIndices.add(mCurrentIndex);
            }
            return this;
        }

        /**
         * Set the order for the composite index
         *
         * @param order the order
         * @return the builder for chaining
         */
        public CompositeIndexBuilder order(int order) {
            mCurrentIndex.setOrder(order);
            return this;
        }

        /**
         * Set the index to be in ascending order
         *
         * @return the builder for chaining
         */
        public CompositeIndexBuilder ascending() {
            mCurrentIndex.setAscending(true);
            return this;
        }

        /**
         * Set the index to be in descending order
         *
         * @return the builder for chaining
         */
        public CompositeIndexBuilder descending() {
            mCurrentIndex.setAscending(false);
            return this;
        }

        /**
         * Build the index
         *
         * @return the index
         */
        public Index build() {
            return new IndexImpl(false, mIndices, mUniqueIndices);
        }
    }

    private class IndexImpl implements Index {

        private final boolean mUnique;
        private final CompositeIndex[] mNames;
        private final CompositeIndex[] mUniqueNames;

        public IndexImpl(boolean unique, List<CompositeIndex> names, List<CompositeIndex> uniqueNames) {
            this.mUnique = unique;
            this.mNames = names.toArray(new CompositeIndex[names.size()]);
            this.mUniqueNames = uniqueNames.toArray(new CompositeIndex[uniqueNames.size()]);
        }

        @Override
        public CompositeIndex[] indexNames() {
            return mNames;
        }

        @Override
        public boolean unique() {
            return mUnique;
        }

        @Override
        public CompositeIndex[] uniqueNames() {
            return mUniqueNames;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Index.class;
        }
    }

    private class CompositeIndexImpl implements CompositeIndex {

        private final String mName;
        private boolean mAscending;
        private int mOrder;

        public CompositeIndexImpl(String name) {
            this.mName = name;
        }

        void setAscending(boolean ascending) {
            mAscending = ascending;
        }

        void setOrder(int order) {
            mOrder = order;
        }

        @Override
        public boolean ascending() {
            return mAscending;
        }

        @Override
        public int order() {
            return mOrder;
        }

        @Override
        public String indexName() {
            return mName;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return CompositeIndex.class;
        }
    }
}
