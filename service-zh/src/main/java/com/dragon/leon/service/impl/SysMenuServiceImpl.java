package com.dragon.leon.service.impl;

import ch.qos.logback.core.boolex.EvaluationException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.dragon.common.execption.RuntimeExceptionBeen;
import com.dragon.leon.mapper.SysMenuMapper;
import com.dragon.leon.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragon.leon.service.SysRoleMenuService;
import com.dragon.leon.utils.MenuHelper;
import com.dragon.model.system.SysMenu;
import com.dragon.model.system.SysRoleMenu;
import com.dragon.vo.system.AssginMenuVo;
import com.dragon.vo.system.MetaVo;
import com.dragon.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author fzt
 * @since 2024-03-15
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    //菜单列表实现
    @Override
    public List<SysMenu> findNodesTree() {
        //1.查询所有菜单数据
        List<SysMenu> allLists = baseMapper.selectList(null);
        //2.构建树形结构
        List<SysMenu> trees = MenuHelper.builderTrees(allLists);
        return trees;
    }

    //实现是否有子节点进行删除
    @Override
    public void removeNodesById(Long id) {
        //判断当前菜单是否有下一层菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId,id);
        Integer count = baseMapper.selectCount(wrapper);
        if(count > 0)
            throw new RuntimeExceptionBeen(201,"菜单不能删除！");
        baseMapper.deleteById(id);
    }

    //根据角色获取对应的菜单信息
    @Override
    public List<SysMenu> getMenuByRoleId(Long roleId) {
        //1.查询所有菜单，按照树形结构封装 - 条件 status == 1
        LambdaQueryWrapper<SysMenu> sysMenuWrapper = new LambdaQueryWrapper<>();
        sysMenuWrapper.eq(SysMenu::getStatus,1);
        List<SysMenu> sysMenuListByStatus = baseMapper.selectList(sysMenuWrapper);

        //2.根据当前角色roleId查询，角色菜单关系表，查询角色roleId对应的所有菜单id的集合，并且可能还有其他数据，用StreamAPI进行过滤菜单id。
        LambdaQueryWrapper<SysRoleMenu> sysRoleMenuWrapper = new LambdaQueryWrapper<>();
        sysRoleMenuWrapper.eq(SysRoleMenu::getRoleId,roleId);
        List<SysRoleMenu> existList = sysRoleMenuService.list(sysRoleMenuWrapper);
        List<Long> menuIdLists = existList.stream().map(m -> m.getMenuId()).collect(Collectors.toList());

        //3.根据当前角色所属菜单id集合查询菜单信息 -> 可以根据角色对应的所有菜单id是否在查询的所有菜单中存在来取得对象 -> 如果相同则将isSelect变为true，否则变为false
        sysMenuListByStatus.stream().forEach(item -> {
            if(menuIdLists.contains(item.getId()))
                item.setSelect(true);
            else
                item.setSelect(false);
        });
        List<SysMenu> sysMenuList = MenuHelper.builderTrees(sysMenuListByStatus);
        return sysMenuList;
    }


    //为角色分配菜单
    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {
        //1. 把角色之前分配菜单删除 -> 根据roleId进行删除
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId,assginMenuVo.getRoleId());
        sysRoleMenuService.remove(wrapper);

        //2.保存最新分配菜单
        for (Long menuId : assginMenuVo.getMenuIdList()) {
            if(StringUtils.isEmpty(menuId)) continue;
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(assginMenuVo.getRoleId());
            sysRoleMenu.setMenuId(menuId);
            sysRoleMenuService.save(sysRoleMenu);
        }
    }

    //根据用户id获取菜单权限值
    @Override
    public List<RouterVo> findUserMenuList(Long userId) {
        List<SysMenu> menuList = null;
        //判断当前用户是否为管理员 userId = 1是管理员
        if(userId.longValue() == 1)
            //如果是管理员，查询所有菜单列表（状态，排序）
            menuList = list(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus,1).orderByAsc(SysMenu::getSortValue));
        else
            //如果不是管理员，根据userId查询可以操作菜单列表
            //多表查询：用户角色关系表、角色菜单关系表、菜单表
            menuList = baseMapper.findMenuListByUserId(userId);

        //使用菜单操作工具类构建成树形结构
        List<SysMenu> menuTrees = MenuHelper.builderTrees(menuList);
        //构建成框架要求的路由数据结构 -> 递归
        List<RouterVo> routerVoList = buildRouter(menuTrees);
        return routerVoList;
    }

    /**
     * 构建菜单路由
     * @param menus
     * @return
     */
    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        //创建List集合，存储最终的数据
        List<RouterVo> routerVos = new LinkedList<>();
        //遍历传过来的list集合进行set赋值
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            //根据SysMenu的children进行下一层数据的遍历
            List<SysMenu> children = menu.getChildren();
            //加载隐藏路由 -> 根据type属性判断 将type==1为菜单下面的权限包含component 根据StreamAPI的方式进行过滤
            if(menu.getType().intValue() == 1){
                List<SysMenu> hiddenMenuList = children.stream().filter(i -> !StringUtils.isEmpty(i.getComponent())).collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routerVos.add(hiddenRouter);
                }
            }else {
                if(!CollectionUtils.isEmpty(children)){
                    if(children.size() > 0)
                        router.setAlwaysShow(true);
                    router.setChildren(buildRouter(children));//进行递归，直到找到所有的菜单
                }
            }
            //返回菜单数据
            routerVos.add(router);
        }
        return routerVos;
    }

    //根据用户id获取用户按钮权限
    @Override
    public List<String> findUserPermsList(Long userId) {
        List<SysMenu> menuList = null;
        //判断是否是管理员，如果是管理员，查询所有按钮列表  -> 条件 status == 1
        if(userId.longValue() == 1)
            menuList = list(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus,1));
        else
            //如果不是管理员，根据userId查询可以操作菜单列表
            //多表查询：用户角色关系表、角色菜单关系表、菜单表
            menuList = baseMapper.findMenuListByUserId(userId);

        //从查询出来的数据里面，获取可以操作按钮值的list集合，返回
        List<String> permsList = menuList.stream().filter(i -> i.getType() == 2).map(i -> i.getPerms()).collect(Collectors.toList());
        return permsList;
    }


    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }

}
