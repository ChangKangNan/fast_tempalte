package cn.ft.pojo;

import cn.ft.annotation.Column;
import cn.ft.annotation.Table;
import cn.ft.annotation.Entity;
import cn.ft.annotation.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;
import java.util.Date;

/**
* @author kangnan.chang
*/

@Accessors(chain=true)
@Table(value = "test_user")
@Entity
@Data
public class UserPojo {

    @Id
    @Column(name = "id")
    private Long id;


    @Column(name = "name")
    private String name;


    @Column(name = "password")
    private String password;


    @Column(name = "email")
    private String email;


    @Column(name = "department_id")
    private Long departmentId;


    @Column(name = "t_date")
    private Date tDate;


    @Column(name = "numbers")
    private BigDecimal numbers;


}
