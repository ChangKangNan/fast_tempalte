package cn.ft.service.impl;

import cn.ft.dto.PTO;
import cn.ft.pojo.Dep;
import cn.ft.pojo.User;
import cn.hutool.json.JSONUtil;
import cn.ft.orm.DbTemplate;
import cn.ft.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author kangnan.chang
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

    @Autowired
    DbTemplate db;

    @Override
    public User getUserById(long id) {
        return db.get(User.class, id);
    }

    @Override
    public User fetchUserByEmail(String email) {
        return db.from(User.class).where("email = ?", email).first();
    }

    @Override
    public User getUserByEmail(String email) {
        return db.from(User.class).where("email = ?", email).unique();
    }

    @Override
    public String getNameByEmail(String email) {
        User user = db.select("name").from(User.class).where("email = ?", email).unique();
        return user.getName();
    }

    @Override
    public List getUsers(int pageIndex) {
        int pageSize = 100;
        return db.from(User.class).orderBy("id").limit((pageIndex - 1) * pageSize, pageSize).list();
    }

    @Override
    public User login(String email, String password) {
        User user = fetchUserByEmail(email);
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        throw new RuntimeException("login failed.");
    }

    @Override
    public User register(String email, String password, String name, Long departmentId) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setDidP(departmentId);
        db.insert(user);
        return user;
    }

    @Override
    public void updateUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        db.update(user);
    }

    @Override
    public void deleteUser(Long id) {
        db.delete(User.class, id);
    }

    @Override
    public void selectByCondition() {
        List<PTO> list = db
                .select("name", "dept_name")
                .from(User.class)
                .leftJoin(Dep.class, "did_p", "dept_id")
                .where("test_dep.dept_name=?", "业务部")
                .list(PTO.class);
        for (PTO user : list) {
            System.out.println(JSONUtil.toJsonStr(user));
        }
    }
}
