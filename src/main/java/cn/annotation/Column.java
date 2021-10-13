package cn.annotation;

import java.lang.annotation.*;

/**
 * 列名
 *
 * @author ckn
 *
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {
    /**
     * 数据库表名
     *
     * @return
     * @author ckn
     */
    String name() default "";
}
