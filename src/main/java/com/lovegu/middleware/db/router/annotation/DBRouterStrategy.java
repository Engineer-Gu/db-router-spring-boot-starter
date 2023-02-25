package com.lovegu.middleware.db.router.annotation;

import java.lang.annotation.*;

/**
 * @author 老顾
 * @description 路由策略，分表标记
 * @date 2023/2/25
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouterStrategy {

    boolean splitTable() default false;
}
