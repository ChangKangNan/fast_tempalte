package cn.ft.bean;

import lombok.Data;

/**
 * @author kangnan.chang
 */
@Data
public class BeanInfo {
    /**
     * 属性名
     */
    private String propertyName;
    /**
     * 属性类型
     */
    private String propertyType;
    /**
     * 属性描述
     */
    private String propertyDesc;
}
