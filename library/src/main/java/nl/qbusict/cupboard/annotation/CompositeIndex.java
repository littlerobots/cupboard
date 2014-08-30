/*
 * Copyright (C) 2013 Qbus B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.qbusict.cupboard.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation interface that allows one to order columns for a composite index (if another column in the same table shares
 * the same index name in {@link nl.qbusict.cupboard.annotation.Index})
 * For more information see
 * <a href="http://stackoverflow.com/questions/2292662/how-important-is-the-order-of-columns-in-indexes}"><b>Why order matters?</b></a>
 * <p/>
 * Note that annotations are not processed by default. To enable processing of annotations construct an instance of Cupboard using {@link nl.qbusict.cupboard.CupboardBuilder} and call {@link nl.qbusict.cupboard.CupboardBuilder#useAnnotations()} <br/>
 */
@Retention(value = RetentionPolicy.RUNTIME)
public @interface CompositeIndex {
    public static final boolean DEFAULT_ASCENDING = true;
    public static final int DEFAULT_ORDER = 0;
    public static final String DEFAULT_INDEX_NAME = "";

    /**
     * @return whether a ascending index should be created on this column. By default it is true.
     */
    boolean ascending() default DEFAULT_ASCENDING;

    /**
     * @return order of the column in the composite index
     */
    int order() default DEFAULT_ORDER;

    /**
     * @return name of the composite index if .
     */
    String indexName();
}
