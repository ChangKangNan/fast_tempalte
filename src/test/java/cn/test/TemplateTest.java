package cn.test;

import cn.ft.dto.PTO;
import cn.hutool.json.JSONUtil;
import cn.ft.orm.DbTemplate;
import cn.ft.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ckn
 * @date 2021/9/28
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TemplateTest {
    @Autowired
    private UserService userService;
    @Autowired
    DbTemplate dbTemplate;

    @Test
    public void test() {
        Map map=new HashMap();
        map.put("depName","业务部");
        List<PTO> objects = dbTemplate.selectByFile("/sql/y.sql", map,PTO.class);
        System.out.println(JSONUtil.toJsonStr(objects));
    }
}
