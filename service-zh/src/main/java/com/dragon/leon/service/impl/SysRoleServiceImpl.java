package com.dragon.leon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragon.leon.mapper.SysRoleMapper;
import com.dragon.leon.service.SysRoleService;
import com.dragon.leon.service.SysUserRoleService;
import com.dragon.model.system.SysRole;
import com.dragon.model.system.SysUser;
import com.dragon.model.system.SysUserRole;
import com.dragon.vo.system.AssginRoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    //一、根据用户获取对应的角色信息
    @Override
    public Map<String, Object> getRoleByUserId(Long userId) {
        //1.显示所有角色  -> 查询角色表
        List<SysRole> sysRoles = baseMapper.selectList(null);
        /**
         * 2.显示当前用户所属角色
         * (1) 根据当前用户userId查询用户角色关系表，查询用户userId对应的所有角色id的集合，并且里面可能还有其他数据，用StreamAPI进行过滤角色id/for循环过滤角色id
         * (2) 根据当前用户所属角色id集合查询角色信息 -> 可以根据用户对应的所有角色id是否在查询的所有角色中存在来取得对象
         */
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId,userId);

        List<SysUserRole> list = sysUserRoleService.list(queryWrapper);

        //for循环
//        List<Long> existRoleList = new ArrayList<>();
//        for (SysUserRole userRole : list) {
//            roleList.add(userRole.getRoleId());
//        }

        //SteamAPI
        List<Long> existRoleList = list.stream().map(c -> c.getRoleId()).collect(Collectors.toList());


        List<SysRole> roleList = new ArrayList<>();
        for (SysRole sysRole : sysRoles) {
            if(existRoleList.contains(sysRole.getId()))
                roleList.add(sysRole);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("sysRoles",sysRoles);
        map.put("roleList",roleList);
        return map;
    }

    @Override
    public void removeRolesAndSave(AssginRoleVo assginRoleVo) {
        //(1) 把用户之前分配角色删除 -> 根据userId进行删除
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,assginRoleVo.getUserId());
        sysUserRoleService.remove(wrapper);
        //(2) 保存最新分配角色
        for (Long role : assginRoleVo.getRoleIdList()) {
            if(StringUtils.isEmpty(role)) continue;
            SysUserRole userRole = new SysUserRole();
            userRole.setRoleId(role);
            userRole.setUserId(assginRoleVo.getUserId());
            //(3) 用户角色关系表里面添加
            sysUserRoleService.save(userRole);
        }
    }
}
