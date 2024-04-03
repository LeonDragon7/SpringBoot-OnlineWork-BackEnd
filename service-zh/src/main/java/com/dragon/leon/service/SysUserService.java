package com.dragon.leon.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.dragon.model.system.SysUser;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author fzt
 * @since 2024-03-13
 */
public interface SysUserService extends IService<SysUser> {

    void updateStatus(Long id, Integer status);

    //根据用户名查询数据库，用户是否存在（信息是否正确）
    SysUser getByUserName(String username);

    //根据用户名获取登录信息
    Map<String, Object> getUserInfo(String username);

    //显示当前用户信息
    Map<String,Object> getCurrentUser();
}
