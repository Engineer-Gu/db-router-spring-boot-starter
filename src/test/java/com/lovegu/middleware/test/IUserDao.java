package com.lovegu.middleware.test;

import com.lovegu.middleware.db.router.annotation.DBRouter;

/**
 * @author 老顾
 * @description 用户接口
 * @date 2023/2/24
 */
public interface IUserDao {

    @DBRouter(key = "userId")
    void insertUser(String req);
}
