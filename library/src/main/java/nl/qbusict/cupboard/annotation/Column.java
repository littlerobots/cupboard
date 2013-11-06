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

import android.provider.ContactsContract;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nl.qbusict.cupboard.convert.DefaultConverterFactory;

/**
 * Annotation interface that allows one to decouple a field name from a column
 * name, by specifying the column name in an Annotation. This is particularly
 * useful when working with existing data like the {@link ContactsContract} ContentProvider as it utilises
 * generic column names (e.g. data1, data2,...,data15) which map to various
 * aliases depending on the mime type in use for a given row.
 *
 * Note that annotations are not processed by default. To enable processing of annotations construct a
 * configure a Cupboard instance with{@link DefaultConverterFactory} that has annotation processing enabled.
 * <br/>
 * <code>
 * Cupboard cupboard = new Cupboard(new DefaultConverterFactory(true));
 * </code>
 **/
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Column {
    String value();
}
