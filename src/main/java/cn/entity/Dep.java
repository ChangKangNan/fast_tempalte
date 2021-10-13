package cn.entity;

import cn.annotation.Column;
import cn.annotation.Table;
import cn.annotation.Entity;
import cn.annotation.Id;
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
