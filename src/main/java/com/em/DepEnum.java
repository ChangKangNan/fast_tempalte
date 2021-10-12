package com.em;

/**
 * @author ckn
 * @date 2021/9/28
 */
public enum DepEnum {
    deptId("dept_id"),
    deptName("dept_name");
    public String info;
    DepEnum(String info) {
        this.info = info;
    }
}
