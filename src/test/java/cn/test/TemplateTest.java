package cn.test;

import cn.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author ckn
 * @date 2021/9/28
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TemplateTest {
    @Autowired
    private UserService userService;

    @Test
    public void test(){
        userService.selectByCondition();
    }
}
