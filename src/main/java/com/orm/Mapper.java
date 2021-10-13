package com.orm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.annotation.Column;
import com.annotation.Id;
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

    final RowMapper<T> rowMapper;

    String selectSQL;
    String insertSQL;
    String updateSQL;
    String deleteSQL;

    public Mapper(Class<T> clazz) throws Exception {

        List<AccessibleProperty> all = getProperties(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        if (ArrayUtil.isNotEmpty(declaredFields)) {
            for (Field field : declaredFields) {
                String fieldName = field.getName();
                for (AccessibleProperty property : all) {
                    String fName = property.propertyName;
                    if (fieldName.equals(fName)) {
                        Column fieldAnnotation = field.getAnnotation(Column.class);
                        if (fieldAnnotation == null || fieldAnnotation.name().isEmpty()) {
                            property.columnName = property.propertyName;
                        }else {
                            property.columnName = fieldAnnotation.name();
                        }
                        Id id = field.getAnnotation(Id.class);
                        if (id != null) {
                            property.isId=true;
                        }
                    }
                }
            }
        }

        //校验ID是否存在
        AccessibleProperty[] ids = all.stream().filter(AccessibleProperty::isId).toArray(AccessibleProperty[]::new);
        if (ids.length != 1) {
            throw new RuntimeException("Require one @Id");
        }

        this.id = ids[0];
        this.allProperties = all;
        this.allPropertiesMap = buildPropertiesMap(this.allProperties);
        this.insertableProperties = all.stream().filter(AccessibleProperty::isNotId).collect(Collectors.toList());
        this.updatableProperties = all.stream().collect(Collectors.toList());
        this.entityClass = clazz;

        //初始化SQL
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

        //jdbcTemplate 返回rowMapper
        this.rowMapper = new BeanPropertyRowMapper<>(this.entityClass);
    }

    /**
     * 获取ID值
     * @param bean
     * @return
     * @throws ReflectiveOperationException
     */
    Object getIdValue(Object bean) throws ReflectiveOperationException {
        return this.id.getter.invoke(bean);
    }

    /**
     * 产生所有column列的map
     * @param props
     * @return
     */
    Map<String, AccessibleProperty> buildPropertiesMap(List<AccessibleProperty> props) {
        Map<String, AccessibleProperty> map = new HashMap<>();
        for (AccessibleProperty prop : props) {
            map.put(underscoreName(prop.propertyName), prop);
        }
        return map;
    }

    /**
     *  将驼峰式命名的字符串转换为下划线大写方式。如果转换前的驼峰式命名的字符串为空，则返回空字符串。
     *  例如：HelloWorld->HELLO_WORLD->hello_world
     *  @param name 转换前的驼峰式命名的字符串
     *  @return 转换后下划线大写方式命名的字符串
     */
    public static String underscoreName(String name) {
        StringBuilder result = new StringBuilder();
        if (name != null && name.length() > 0) {
            // 将第一个字符处理成大写
            result.append(name.substring(0, 1).toUpperCase());
            // 循环处理其余字符
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                // 在大写字母前添加下划线
                if (s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
                    result.append("_");
                }
                // 其他字符直接转成大写
                result.append(s.toUpperCase());
            }
        }
        return result.toString().toLowerCase();
    }

    /**
     * ?包装返回值
     * @param n
     * @return
     */
    private String numOfQuestions(int n) {
        String[] qs = new String[n];
        return String.join(", ", Arrays.stream(qs).map((s) -> {
            return "?";
        }).toArray(String[]::new));
    }

    /**
     * 获取表名
     * @param clazz
     * @return
     */
    private String getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table != null) {
            return table.value();
        }
        return clazz.getSimpleName();
    }

    /**
     * 获取lombok所有属性
     * @param clazz
     * @return
     * @throws Exception
     */
    private List<AccessibleProperty> getProperties(Class<?> clazz) throws Exception {
        List<AccessibleProperty> properties = new ArrayList<>();
        BeanInfo info = Introspector.getBeanInfo(clazz);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            // 排除getClass():
            if (pd.getName().equals("class")) {
                continue;
            }
            String fieldName = pd.getDisplayName();
            Method getter = null;
            Method setter = null;
            Method[] declaredMethods = clazz.getDeclaredMethods();
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getterName = "get" + firstLetter + fieldName.substring(1);
            String setterName = "set" + firstLetter + fieldName.substring(1);
            for (Method method : declaredMethods) {
                String name = method.getName();
                if (name.equals(getterName)) {
                    getter = method;
                }
                if (name.equals(setterName)) {
                    setter = method;
                }
            }
            pd.setReadMethod(getter);
            pd.setWriteMethod(setter);
            if (getter != null && setter != null) {
                properties.add(new AccessibleProperty(pd));
            } else {
                throw new RuntimeException("Property " + pd.getName() + " is not read/write.");
            }
        }
        return properties;
    }
}
