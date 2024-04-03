package com.dragon.leon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dragon.model.system.SysMenu;
import com.dragon.model.system.SysRoleMenu;
import com.dragon.vo.system.AssginMenuVo;
import com.dragon.vo.system.RouterVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author fzt
 * @since 2024-03-15
 */
public interface SysMenuService extends IService<SysMenu> {

    //菜单列表接口
    List<SysMenu> findNodesTree();

    //删除接口 根据是否有子节点进行判断
    void removeNodesById(Long id);

    //根据角色获取对应的菜单信息
    List<SysMenu> getMenuByRoleId(Long roleId);

    //为角色分配菜单
    void doAssign(AssginMenuVo assginMenuVo);
    //根据用户id获取菜单权限值
    List<RouterVo> findUserMenuList(Long id);

    //根据用户id获取用户按钮权限
    List<String> findUserPermsList(Long id);
}
