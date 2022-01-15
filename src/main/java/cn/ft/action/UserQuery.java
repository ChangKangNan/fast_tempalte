package cn.ft.action;

import cn.ft.pojo.User;
import cn.hutool.core.util.ArrayUtil;

import java.util.HashMap;

/**
 * @author ckn
 * @date 2021/10/19
 */
public class UserQuery {
    public HashMap<String, Object> conditionSQL = new HashMap<>();
    User table = new User();
    private UserQuery() {
    }

    public static UserQuery create() {
        return new UserQuery();
    }

    public UserQuery id(Long... ids) {
        conditionSQL.put("id", "in (" + ArrayUtil.join(ids, ",") + ")");
        return this;
    }

    public UserQuery name(Long... names) {
        conditionSQL.put("name", "in (" + ArrayUtil.join(names, ",","'","'") + ")");
        return this;
    }

    public void list(){

    }
}
