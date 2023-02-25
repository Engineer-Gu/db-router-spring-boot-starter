package com.lovegu.middleware.db.router.strategy.impl;

import com.lovegu.middleware.db.router.DBContextHolder;
import com.lovegu.middleware.db.router.DBRouterConfig;
import com.lovegu.middleware.db.router.strategy.IDBRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 老顾
 * @description 哈希路由
 * @date 2023/2/24
 */
public class DBRouterStrategyHashCode implements IDBRouterStrategy {

    private Logger logger = LoggerFactory.getLogger(DBRouterStrategyHashCode.class);

    private DBRouterConfig dbRouterConfig;

    public DBRouterStrategyHashCode(DBRouterConfig dbRouterConfig) {
        this.dbRouterConfig = dbRouterConfig;
    }


    @Override
    public void doRouter(String dbKeyAttr) {
        // 首先，算出库表数量
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();

        // 通过扰动函数计算库表的下标，库表数量 - 1 位运算库表字段的hashCode
        // 在 JDK 的 HashMap 中，对于一个元素的存放，需要进行哈希散列，为了让数据分布更加散列，所以添加了扰动函数
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));

        // 通过库表下标计算库表索引编号，相当于是把一个长条的桶，切割成段，对应分库分表的库编号和表编号
        int dbIdx = idx / dbRouterConfig.getTbCount() + 1;
        int tbIdx = idx - dbRouterConfig.getTbCount() * (dbIdx - 1);

        // 最后把库表编号存到 ThreadLocal 里
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
        DBContextHolder.setTBKey(String.format("%03d", tbIdx));
        logger.debug("数据库路由：dbIdx：{} tbIdx：{}", dbIdx, tbIdx);
    }

    @Override
    public void setDBKey(int dbIdx) {
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
    }

    @Override
    public void setTBKey(int tbIdx) {
        DBContextHolder.setTBKey(String.format("%03d", tbIdx));
    }

    @Override
    public int dbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int tbCount() {
        return dbRouterConfig.getTbCount();
    }

    @Override
    public void clear() {
        DBContextHolder.clearDBKey();
        DBContextHolder.clearTBKey();
    }
}
