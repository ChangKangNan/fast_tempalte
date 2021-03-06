package cn.ft.annotation;

import java.lang.annotation.*;

/**
 * 表名
 *
 * @author ckn
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Entity {
    /**
     * 数据库表名
     *
     * @return
     * @author ckn
     */
    String value() default "";
}
