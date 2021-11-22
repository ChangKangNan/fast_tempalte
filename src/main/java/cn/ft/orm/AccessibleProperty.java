package cn.ft.orm;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Represent a bean property.
 *
 * @author ckn
 */
public class AccessibleProperty {
    /**
     * getter方法
     */
    Method getter;
    /**
     * setter方法
     */
    Method setter;

    /**
     * java type
     */
    Class<?> propertyType;

    /**
     * java bean property name
     */
    String propertyName;

    /**
     * table column name
     */
    String columnName;

    boolean isId;

    boolean isId() {
        return isId == true;
    }

    boolean isNotId() {
        return isId == false;
    }

    public AccessibleProperty(PropertyDescriptor pd) {
        this.getter = pd.getReadMethod();
        this.setter = pd.getWriteMethod();
        this.propertyType = pd.getReadMethod().getReturnType();
        this.propertyName = pd.getName();
    }
}
