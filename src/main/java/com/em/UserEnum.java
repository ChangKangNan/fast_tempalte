package com.em;

/**
 * @author ckn
 * @date 2021/9/28
 */
public enum UserEnum {
    id("id"),
    name("name"),
    email("email"),
    departmentId("department_id")
    ;
    public String info;
    UserEnum(String info) {
        this.info = info;
    }
}
