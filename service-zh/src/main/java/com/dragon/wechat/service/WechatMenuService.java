package com.dragon.wechat.service;

import com.dragon.model.wechat.Menu;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dragon.vo.wechat.MenuVo;

import java.util.List;

/**
 * <p>
 * 菜单 服务类
 * </p>
 *
 * @author fzt
 * @since 2024-04-02
 */
public interface WechatMenuService extends IService<Menu> {
    //显示微信菜单
    List<MenuVo> findMenuInfo();

    //同步菜单
    void syncMenu();

    //删除菜单
    void removeMenu();
}
