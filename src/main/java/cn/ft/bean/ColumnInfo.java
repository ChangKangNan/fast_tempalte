package cn.ft.bean;

import lombok.Data;

/**
 * 表的所有字段
 * @author kangnan.chang
 */
@Data
public class ColumnInfo {
    /**
     * 字段名
     */
    private String columnName;
    /**
     * 字段类型
     */
    private String columnType;
    /**
     * 字段注释
     */
    private String columnRemarks;
    /**
     * 是否是主键
     */
    private boolean isKey;
}
