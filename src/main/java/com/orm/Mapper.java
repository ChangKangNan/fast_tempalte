package com.orm;

import com.annotation.Table;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kangnan.chang
 */
public class Mapper<T> {

    final Class<T> entityClass;
    final String tableName;

    /**
     * @Id property
     */
    final AccessibleProperty id;

    /**
     * all properties including @Id, key is property name (NOT column name)
     */
    final List<AccessibleProperty> allProperties;

    /**
     * lower-case property name -> AccessibleProperty
     */
    final Map<String, AccessibleProperty> allPropertiesMap;

    final List<AccessibleProperty> insertableProperties;
    final List<AccessibleProperty> updatableProperties;

    /**
     * lower-case property name -> AccessibleProperty
     */
    final Map<String, AccessibleProperty> updatablePropertiesMap;

    final RowMapper<T> rowMapper;

    String selectSQL;
    String insertSQL;
    String updateSQL;
    final String deleteSQL;

    public Mapper(Class<T> clazz) throws Exception {
        List<AccessibleProperty> all = getProperties(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();

        if (declaredFields != null && declaredFields.length > 0) {
            for (Field field : declaredFields) {
                String fieldName = field.getName();
                for (AccessibleProperty property : all) {
                    String fName = property.propertyName;
                    if (fieldName.equals(fName)) {
                        property.field = field;
                    }
                }
            }
        }

        AccessibleProperty[] ids = all.stream().filter(AccessibleProperty::isId).toArray(AccessibleProperty[]::new);
        if (ids.length != 1) {
            throw new RuntimeException("Require exact one @Id.");
        }
        this.id = ids[0];
        this.allProperties = all;
        this.allPropertiesMap = buildPropertiesMap(this.allProperties);
        this.insertableProperties = all.stream().filter(AccessibleProperty::isNotId).collect(Collectors.toList());
        this.updatableProperties = all.stream().collect(Collectors.toList());
        this.updatablePropertiesMap = buildPropertiesMap(this.updatableProperties);
        this.entityClass = clazz;
        this.tableName = getTableName(clazz);

        this.selectSQL = "SELECT * FROM " + this.tableName + " WHERE " + this.id.columnName + " = ?";
        this.insertSQL = "INSERT INTO " + this.tableName + " ("
                + String.join(", ", this.insertableProperties.stream().map(p -> p.columnName).toArray(String[]::new))
                + ") VALUES (" + numOfQuestions(this.insertableProperties.size()) + ")";
        this.updateSQL = "UPDATE " + this.tableName + " SET "
                + String.join(", ",
                this.updatableProperties.stream().filter(AccessibleProperty::isNotId).map(p -> p.columnName + " = ?").toArray(String[]::new))
                + " WHERE " + this.id.columnName + " = ?";
        this.deleteSQL = "DELETE FROM " + this.tableName + " WHERE " + this.id.columnName + " = ?";
        this.rowMapper = new BeanPropertyRowMapper<>(this.entityClass);
    }

    Object getIdValue(Object bean) throws ReflectiveOperationException {
        return this.id.getter.invoke(bean);
    }

    Map<String, AccessibleProperty> buildPropertiesMap(List<AccessibleProperty> props) {
        Map<String, AccessibleProperty> map = new HashMap<>();
        for (AccessibleProperty prop : props) {
            map.put(prop.propertyName.toLowerCase(), prop);
        }
        return map;
    }

    private String numOfQuestions(int n) {
        String[] qs = new String[n];
        return String.join(", ", Arrays.stream(qs).map((s) -> {
            return "?";
        }).toArray(String[]::new));
    }

    private String getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table != null) {
            return table.value();
        }
        return clazz.getSimpleName();
    }

    private List<AccessibleProperty> getProperties(Class<?> clazz) throws Exception {
        List<AccessibleProperty> properties = new ArrayList<>();
        BeanInfo info = Introspector.getBeanInfo(clazz);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            // 排除getClass():
            if (pd.getName().equals("class")) {
                continue;
            }
            Method getter = pd.getReadMethod();
            Method setter = pd.getWriteMethod();

            if (getter != null && setter != null) {
                properties.add(new AccessibleProperty(pd));
            } else {
                throw new RuntimeException("Property " + pd.getName() + " is not read/write.");
            }
        }
        return properties;
    }
}
