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

    private String key;

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
     * bean类导入的包
     */
    private Set<String> packages;

    /**
     * 以下为各个模板类型的文件路径和包信息
     */
    private String pojoPackagePath;
    private String pojoFilePath;
    private String pojoName;
}
