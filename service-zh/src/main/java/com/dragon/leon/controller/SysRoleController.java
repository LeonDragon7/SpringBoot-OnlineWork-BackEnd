package com.dragon.leon.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dragon.common.result.Result;
import com.dragon.leon.service.SysRoleService;
import com.dragon.model.system.SysRole;
import com.dragon.vo.system.AssginRoleVo;
import com.dragon.vo.system.SysRoleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "角色管理")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation(value = "角色列表")
    @GetMapping("/findAll")
    public Result findAll(){
        List<SysRole> list = sysRoleService.list();
        return Result.ok(list);
    }


    /**
     *
     * @param page 当前页
     * @param limit 每页显示记录数
     * @param sysRoleQueryVo 条件对象
     * @return
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("分页查询")
    @GetMapping("{page}/{limit}")
    public Result pageQueryRole(@PathVariable Long page,
                                @PathVariable Long limit,
                                SysRoleQueryVo sysRoleQueryVo){
        //1.创建Page对象，传递分页相关参数
        Page<SysRole> sysRolePage = new Page<>(page, limit);

        //2.封装条件，判断条件是否为空，不为空进行封装
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if(!StringUtils.isEmpty(roleName)){
            queryWrapper.like(SysRole::getRoleName,roleName);
        }
        //3.方法实现
        IPage<SysRole> pageModel = sysRoleService.page(sysRolePage, queryWrapper);
        return Result.ok(pageModel);
    }

    /**
     *
     * @param id 角色id
     * @return
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("根据id查询")
    @GetMapping("get/{id}")
    public Result getListById(@PathVariable Long id){
        SysRole role = sysRoleService.getById(id);
        return Result.ok(role);
    }

    /**
     *
     * @param sysRole 角色信息 -> @RequestBody -> JSON
     * @return
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation("添加角色")
    @PostMapping("save")
    public Result save(@RequestBody SysRole sysRole){
        boolean isSave = sysRoleService.save(sysRole);
        if(isSave)
            return Result.ok().message("添加成功！");
        else
            return Result.fail().message("添加失败！");
    }

    /**
     *
     * @param sysRole 角色信息 -> @RequestBody -> JSON
     * @return
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation("修改角色")
    @PutMapping("update")
    public Result updateById(@RequestBody SysRole sysRole){
        boolean isUpdate = sysRoleService.updateById(sysRole);
        if(isUpdate)
            return Result.ok().message("修改成功！");
        else
            return Result.fail().message("修改失败！");
    }

    /**
     *
     * @param id 角色id
     * @return
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("根据id删除角色")
    @DeleteMapping("delete/{id}")
    public Result deleteById(@PathVariable Long id){
        boolean isDeleteById = sysRoleService.removeById(id);
        if(isDeleteById)
            return Result.ok();
        else
            return Result.fail();
    }

    /**
     *
     * @param listId 角色们的id -> @RequestBody -> JSON
     *               ids -> 数组 ->  List
     *               JSON -> 对象 -> JavaBean
     * @return
     */
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("批量删除")
    @DeleteMapping("batchDelete")
    public Result batchDelete(@RequestBody List<Long> listId){
        boolean isBatchDelete = sysRoleService.removeByIds(listId);
        if(isBatchDelete)
            return Result.ok().message("批量删除成功！");
        else
            return Result.fail().message("批量删除失败！");
    }

    /**
     * 分配角色
     * 一、根据用户获取对应的角色信息
     * 1.显示所有角色  -> 查询角色表
     * 2.显示当前用户所属角色
     * (1) 根据当前用户userId查询，用户角色关系表，查询用户userId对应的所有角色id的集合，并且里面可能还有其他数据，用StreamAPI进行过滤角色id/for循环过滤角色id
     * (2) 根据当前用户所属角色id集合查询角色信息 -> 可以根据用户对应的所有角色id是否在查询的所有角色中存在来取得对象
     * 3.将1和2返回的数据用map<String,Object>保存
     * 1.key : sysRoles
     * 2.key : roleList
     */
    @ApiOperation("根据用户id获取角色")
    @GetMapping("toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId){
        Map<String,Object> map = sysRoleService.getRoleByUserId(userId);
        return Result.ok(map);
    }


    /**
     * 分配角色
     * 二、为用户分配角色
     * 1.把用户最终分配角色到数据库
     * (1) 把用户之前分配角色删除 -> 根据userId进行删除
     * (2) 保存最新分配角色
     * (3) 用户角色关系表里面添加
     */
    @ApiOperation("保存最新分配的角色")
    @PostMapping("doAssign")
    public Result doAssign(@RequestBody AssginRoleVo assginRoleVo){
        sysRoleService.removeRolesAndSave(assginRoleVo);
        return Result.ok();
    }

}
