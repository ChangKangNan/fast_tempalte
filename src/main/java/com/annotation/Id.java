package com.annotation;

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
public @interface Id {
}
