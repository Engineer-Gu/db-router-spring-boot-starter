package com.lovegu.middleware.db.router.dynamic;

import com.lovegu.middleware.db.router.DBContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author 老顾
 * @description 动态数据源获取，每当切换数据源，都要从这个里面进行获取
 * @date 2023/2/24
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return "db" + DBContextHolder.getDBKey();
    }
}
