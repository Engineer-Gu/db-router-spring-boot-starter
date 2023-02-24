package com.lovegu.middleware.db.router.annotation;

import java.lang.annotation.*;

/**
 * @author 老顾
 * @description 路由注解
 * @date 2023/2/24
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouter {

    /**
     * 分库分表字段
     * @return 字段
     */
    String key() default "";
}
