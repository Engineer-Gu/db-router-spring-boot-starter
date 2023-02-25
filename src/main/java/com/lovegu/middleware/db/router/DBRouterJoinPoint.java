package com.lovegu.middleware.db.router;

import com.lovegu.middleware.db.router.annotation.DBRouter;
import com.lovegu.middleware.db.router.strategy.IDBRouterStrategy;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author 老顾
 * @description 数据路由切面，通过自定义注解的方式，拦截被切面的方法，进行数据库路由
 * @date 2023/2/24
 */
@Aspect
public class DBRouterJoinPoint {

    private final Logger logger = LoggerFactory.getLogger(DBRouterJoinPoint.class);

    private final DBRouterConfig dbRouterConfig;

    private final IDBRouterStrategy dbRouterStrategy;

    public DBRouterJoinPoint(DBRouterConfig dbRouterConfig, IDBRouterStrategy dbRouterStrategy) {
        this.dbRouterConfig = dbRouterConfig;
        this.dbRouterStrategy = dbRouterStrategy;
    }

    /**
     * AOP切面
     */
    @Pointcut("@annotation(com.lovegu.middleware.db.router.annotation.DBRouter)")
    public void aopPoint() {
    }

    /**
     * 引入 AOP 切面和 DBRouter 路由注解
     * 所有需要分库分表的操作，都需要使用自定义注解进行拦截，拦截后读取方法中的入参字段，根据字段进行路由操作
     * 1. dbRouter.key() 确定根据哪个字段进行路由
     * 2. getAttrValue 根据数据库路由字段，从入参中读取出对应的值，比如路由 key 是 uId，那么就从入参对象 obj 中获取到 uId 的值
     * 3. dbRouterStrategy.dbRouter(dbKeyAttr) 路由策略根据具体的路由进行处理
     * 4. 路由处理完成后，就放行，jp.proceed();
     * 5. 最后 dbRouterStrategy 需要执行 clear 因为这里用到了 threadLocal 需要手动清理
     * @param jp 切点
     * @param dbRouter 路由注解
     * @return 切面
     * @throws Throwable 运行时异常
     */
    @Around("aopPoint() && @annotation(dbRouter)")
    public Object dbRouter(ProceedingJoinPoint jp, DBRouter dbRouter) throws Throwable {
        String dbKey = dbRouter.key();
        if (StringUtils.isBlank(dbKey) && StringUtils.isBlank(dbRouterConfig.getRouterKey())) {
            throw new RuntimeException("");
        }
        dbKey = StringUtils.isNotBlank(dbKey) ? dbKey : dbRouterConfig.getRouterKey();
        // 路由属性
        String dbKeyAttr = getAttrValue(dbKey, jp.getArgs());
        // 路由策略
        dbRouterStrategy.doRouter(dbKeyAttr);
        // 返回结果
        try {
            return jp.proceed();
        }finally {
            dbRouterStrategy.clear();
        }
    }

    /**
     * 获取方法
     *
     * @param jp 切点
     * @return 方法
     * @throws NoSuchMethodException 没有如此方法异常
     */
    private Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        return jp.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }

    /**
     * 获取属性字段值
     *
     * @param attr 属性
     * @param args 对象数组
     * @return 字段值
     */
    public String getAttrValue(String attr, Object[] args) {
        if (1 == args.length) {
            Object arg = args[0];
            if (arg instanceof String) {
                return arg.toString();
            }
        }

        String filedValue = null;
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(filedValue)) {
                    break;
                }
                filedValue = BeanUtils.getProperty(arg, attr);
            } catch (Exception e) {
                logger.error("获取路由属性失败 attr：{}",attr, e);
            }
        }
        return filedValue;
    }
}
