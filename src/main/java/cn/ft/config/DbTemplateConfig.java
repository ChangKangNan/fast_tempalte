package cn.ft.config;

import cn.ft.pojo.User;
import cn.ft.orm.DbTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author ckn
 * @date 2021/9/28
 */
@Component
public class DbTemplateConfig {

    @Bean
    DbTemplate createDbTemplate(@Autowired JdbcTemplate jdbcTemplate) {
        return new DbTemplate(jdbcTemplate, User.class.getPackage().getName());
    }
}
