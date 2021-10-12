package com.orm;

import com.annotation.Column;
import com.annotation.Id;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Represent a bean property.
 *
 * @author ckn
 */
class AccessibleProperty {

    /**
     * field 字段
     */
    Field field;

    final Method getter;
    final Method setter;

    /**
     * java type
     */
    final Class<?> propertyType;

    /**
     * java bean property name
     */
    final String propertyName;

    /**
     * table column name
     */
    final String columnName;

    boolean isId() {
        Id id = this.field.getAnnotation(Id.class);
        return id != null;
    }

    boolean isNotId() {
        Id id = this.field.getAnnotation(Id.class);
        return id == null;
    }

    public AccessibleProperty(PropertyDescriptor pd) {
        this.getter = pd.getReadMethod();
        this.setter = pd.getWriteMethod();
        this.propertyType = pd.getReadMethod().getReturnType();
        this.propertyName = pd.getName();
        this.columnName = getColumnName(pd.getReadMethod(), propertyName);
    }

    private static String getColumnName(Method m, String defaultName) {
        Column col = m.getAnnotation(Column.class);
        if (col == null || col.name().isEmpty()) {
            return defaultName;
        }
        return col.name();
    }
}
