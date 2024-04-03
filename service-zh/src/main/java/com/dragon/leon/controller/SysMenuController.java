package com.dragon.leon.controller;


import com.dragon.common.result.Result;
import com.dragon.leon.service.SysMenuService;
import com.dragon.model.system.SysMenu;
import com.dragon.model.system.SysRoleMenu;
import com.dragon.vo.system.AssginMenuVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 菜单表 前端控制器
 * </p>
 *
 * @author fzt
 * @since 2024-03-15
 */
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/admin/system/sysMenu")
public class SysMenuController {
    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 菜单列表接口
     * 表关键字段 id parent_id
     * 实体类关键属性 List<SysMenu> children; -> 下级列表
     *
     * 1.查询所有菜单数据
     * 2.构建树形结构 -> 编写utils工具类 -> 实现递归
     * 递归方式：
     *      条件：
     *          (1)  找到递归入口 -> parent_id == 0
     *          (2)  结束条件 -> id != parent_id
     * 递归实现：
     *      (1) 创建list集合，用于最终返回数据
     *      (2) 对传入的所有菜单数据进行遍历
     *          递归入口进入
     *          对递归的方法传入的每个对象和所有菜单数据
     *      (3) 对传入的对象的id和所有菜单数据元素的parent_id进行比较 -> 遍历
     *      (4) 对传入的对象的children进行判空，如果为空设置children的值为list集合
     *      (5) 传入的对象的children属性进行递归调用，保存节点数据
     */
    @PreAuthorize("hasAuthority('bnt.sysMenu.list')")
    @ApiOperation("菜单列表接口")
    @GetMapping("findNodes")
    public Result findNodes(){
        List<SysMenu> trees = sysMenuService.findNodesTree();
        return Result.ok(trees);
    }

    @PreAuthorize("hasAuthority('bnt.sysMenu.add')")
    @ApiOperation(value = "新增菜单")
    @PostMapping("save")
    public Result save(@RequestBody SysMenu permission) {
        sysMenuService.save(permission);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.sysMenu.update')")
    @ApiOperation(value = "修改菜单")
    @PutMapping("update")
    public Result updateById(@RequestBody SysMenu permission) {
        sysMenuService.updateById(permission);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.sysMenu.remove')")
    @ApiOperation(value = "删除菜单")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        sysMenuService.removeNodesById(id);
        return Result.ok();
    }

    /**
     * 分配菜单
     * 一、根据角色获取对应的菜单信息
     * 1.查询所有菜单，按照树形结构封装 - 条件 status == 1
     * 2.查询当前角色用于菜单权限
     * (1) 根据当前角色roleId查询，角色菜单关系表，查询角色roleId对应的所有菜单id的集合，并且可能还有其他数据，用StreamAPI进行过滤菜单id。
     * (2) 根据当前角色所属菜单id集合查询菜单信息 -> 可以根据角色对应的所有菜单id是否在查询的所有菜单中存在来取得对象 -> 如果相同则将isSelect变为true，否则变为false
     * 3.返回树形结构的菜单列表
     */

    @ApiOperation("根据角色获取对应的菜单信息")
    @GetMapping("toAssign/{roleId}")
    public Result toAssign(@PathVariable Long roleId){
        List<SysMenu> sysMenuList = sysMenuService.getMenuByRoleId(roleId);
        return Result.ok(sysMenuList);
    }

    /**
     * 分配角色
     * 二、为角色分配菜单
     * 1.角色之前分配过菜单，角色之前分配菜单删除
     * (1) 把角色之前分配菜单删除 -> 根据roleId进行删除
     * (2) 保存最新分配菜单
     * (2) 角色菜单关系表里面添加
     */
    @PreAuthorize("hasAuthority('bnt.sysMenu.assignRole')")
    @ApiOperation("为角色分配菜单")
    @PostMapping("doAssign")
    public Result doAssign(@RequestBody AssginMenuVo assginMenuVo){
        sysMenuService.doAssign(assginMenuVo);
        return Result.ok();
    }
}

