package com.entity;

import com.annotation.Column;
import com.annotation.Entity;
import com.annotation.Id;
import com.annotation.Table;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author kangnan.chang
 */

@Accessors(chain=true)
@Table(value = "test_dep")
@Entity
@Data
public class Dep {
    @Id
    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "dept_name")
    private String deptName;

}
