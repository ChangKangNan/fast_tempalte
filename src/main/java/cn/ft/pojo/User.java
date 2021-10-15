package cn.ft.pojo;

import cn.ft.annotation.Column;
import cn.ft.annotation.Id;
import cn.ft.annotation.Table;
import cn.ft.annotation.Entity;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author kangnan.chang
 */

@Accessors(chain=true)
@Table(value = "test_user")
@Entity
@Data
public class User{
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "email")
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "name")
    private String name;
    @Column(name = "did_p")
    private Long didP;
}
