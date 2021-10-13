package cn.ft.entity;

import cn.ft.annotation.Column;
import cn.ft.annotation.Table;
import cn.ft.annotation.Entity;
import cn.ft.annotation.Id;
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
