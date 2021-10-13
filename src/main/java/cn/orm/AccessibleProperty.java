package cn.orm;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Represent a bean property.
 *
 * @author ckn
 */
class AccessibleProperty {

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
    String columnName;

    boolean isId;

    boolean isId() {
        return isId ==true;
    }

    boolean isNotId() {
       return isId ==false;
    }

    public AccessibleProperty(PropertyDescriptor pd) {
        this.getter = pd.getReadMethod();
        this.setter = pd.getWriteMethod();
        this.propertyType = pd.getReadMethod().getReturnType();
        this.propertyName = pd.getName();
    }
}
