package com.dragon.leon.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dragon.common.result.Result;
import com.dragon.leon.service.SysUserService;
import com.dragon.model.system.SysUser;
import com.dragon.vo.system.SysUserQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author fzt
 * @since 2024-03-13
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/admin/system/sysUser")
public class SysUserController {
    @Autowired
    private SysUserService service;

    /**
     *
     * @param page 当前页
     * @param limit 每页记录数
     * @param sysUserQueryVo 条件查询
     * @return
     */
    @PreAuthorize("hasAuthority('bnt.sysUser.list')")
    @ApiOperation("分页查询")
    @GetMapping("{page}/{limit}")
    public Result getPageList(@PathVariable Long page,
                           @PathVariable Long limit,
                           SysUserQueryVo sysUserQueryVo
    ){
        Page<SysUser> sysUserPage = new Page<>(page,limit);
        LambdaQueryWrapper<SysUser> sysUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String username = sysUserQueryVo.getKeyword();
        String createTimeBegin = sysUserQueryVo.getCreateTimeBegin();
        String createTimeEnd = sysUserQueryVo.getCreateTimeEnd();
        if(!StringUtils.isEmpty(username))
            sysUserLambdaQueryWrapper.like(SysUser::getUsername,username);

        if(!StringUtils.isEmpty(createTimeBegin))
            sysUserLambdaQueryWrapper.ge(SysUser::getCreateTime,createTimeBegin);

        if(!StringUtils.isEmpty(createTimeEnd))
            sysUserLambdaQueryWrapper.le(SysUser::getCreateTime,createTimeEnd);

        IPage<SysUser> userPage = service.page(sysUserPage, sysUserLambdaQueryWrapper);
        return Result.ok(userPage);
    }

    @PreAuthorize("hasAuthority('bnt.sysUser.add')")
    @ApiOperation("添加用户")
    @PostMapping("save")
    public Result save(@RequestBody SysUser sysUser){
        boolean isSave = service.save(sysUser);
        if(isSave)
            return Result.ok().message("添加成功！");
        else
            return Result.fail().message("添加失败！");
    }

    @PreAuthorize("hasAuthority('bnt.sysUser.list')")
    @ApiOperation("根据id查询用户")
    @GetMapping("get/{id}")
    public Result getById(@PathVariable Long id){
        SysUser user = service.getById(id);
        return Result.ok(user);
    }

    @PreAuthorize("hasAuthority('bnt.sysUser.update')")
    @ApiOperation("修改用户")
    @PutMapping("update")
    public Result update(@RequestBody SysUser sysUser){
        boolean isUpdate = service.updateById(sysUser);
        if(isUpdate)
            return Result.ok().message("修改成功！");
        else
            return Result.fail().message("修改失败！");
    }

    @PreAuthorize("hasAuthority('bnt.sysUser.remove')")
    @ApiOperation("删除用户")
    @DeleteMapping("delete/{id}")
    public Result deleteById(@PathVariable Long id){
        boolean isRemove = service.removeById(id);
        if(isRemove)
            return Result.ok().message("删除成功！");
        else
            return Result.fail().message("删除失败！");
    }

//    @ApiOperation("批量删除用户")
//    @DeleteMapping("batchDelete")
//    public Result deleteBatch(@RequestBody List<Long> listId){
//        boolean isBatchRemove = service.removeByIds(listId);
//        if(isBatchRemove)
//            return Result.ok().message("批量删除成功！");
//        else
//            return Result.fail().message("批量删除失败！");
//    }

    /**
     * 根据用户id更改状态
     * @param id 用户id
     * @param status 状态
     * @return
     */
    @ApiOperation("状态更新")
    @GetMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id,@PathVariable Integer status){
        service.updateStatus(id,status);
        return Result.ok();
    }

    @ApiOperation("显示当前用户信息")
    @GetMapping("getCurrentUser")
    public Result getCurrentUser(){
        return Result.ok(service.getCurrentUser());
    }
}

