package com.entity;

import com.annotation.Column;
import com.annotation.Entity;
import com.annotation.Id;
import com.annotation.Table;
import lombok.experimental.Accessors;


/**
 * @author kangnan.chang
 */

@Accessors(chain=true)
@Table(value = "test_dep")
@Entity
public class Dep {
    @Id
    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "dept_name")
    private String deptName;


    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}
