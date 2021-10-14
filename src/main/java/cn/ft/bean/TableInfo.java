package cn.ft.bean;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * @author kangnan.chang
 */
@Data
public class TableInfo {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 前缀名
     */
    private String prefixName;

    /**
     * bean
     */
    private String beanName;

    /**
     * 表名
     */
    private String tableDesc;

    /**
     * 主键映射
     */
    private Map<String, String> primaryKey;
    /**
     * 字段类型映射
     */
    private List<ColumnInfo> columns;

    /**
     * 属性,属性类型
     */
    private Map<String, String> properties;

    /**
     * 属性,属性类型
     */
    private List<BeanInfo> properties2;
    /**
     * 属性,属性类型
     */
    private Map<String, String> propertiesAnColumns;

    /**
     * 属性,属性类型
     */
    private Map<String, String> insertPropertiesAnColumns;

    /**
     * bean类导入的包
     */
    private Set<String> packages;

    /**
     * 以下为各个模板类型的文件路径和包信息
     */
    private String pojoPackPath;
    private String pojoFilePath;
    private String pojoName;
    private String pojoClassPackPath;

    private String fastPojoPackPath;
    private String fastPojoFilePath;
    private String fastPojoName;
    private String fastPojoClassPackPath;

    private String pojoFastDaoPackPath;
    private String pojoFastDaoFilePath;
    private String pojoFastDaoName;
    private String pojoFastDaoClassPackPath;

    private String pojoFieldsPackPath;
    private String pojoFieldsFilePath;
    private String pojoFieldsName;
    private String pojoFieldsClassPackPath;

    private String dtoPackPath;
    private String dtoFilePath;
    private String dtoName;
    private String dtoClassPackPath;

    private String daoPackPath;
    private String daoFilePath;
    private String daoName;
    private String daoClassPackPath;

    private String iservicePackPath;
    private String iserviceFilePath;
    private String iserviceName;
    private String iserviceClassPackPath;

    private String servicePackPath;
    private String serviceFilePath;
    private String serviceName;
    private String serviceClassPackPath;

}
