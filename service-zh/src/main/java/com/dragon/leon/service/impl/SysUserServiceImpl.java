package com.dragon.leon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dragon.leon.mapper.SysUserMapper;
import com.dragon.leon.service.SysMenuService;
import com.dragon.leon.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragon.model.system.SysUser;
import com.dragon.security.custom.LoginUserInfoHelper;
import com.dragon.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author fzt
 * @since 2024-03-13
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysMenuService menuService;
    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        //根据传入用户id获取用户信息
        SysUser sysUser = baseMapper.selectById(id);
        //判断status的值
        if(status == 0 || status == 1)
            sysUser.setStatus(status);
        else
            log.error("数值不合法！");
        //修改状态
       baseMapper.updateById(sysUser);
    }

    //根据用户名查询数据库，用户是否存在（信息是否正确）
    @Override
    public SysUser getByUserName(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    //根据用户名获取登录信息
    @Override
    public Map<String, Object> getUserInfo(String username) {
        Map<String, Object> result = new HashMap<>();
        //根据用户名获取用户信息
        SysUser sysUser = getByUserName(username);

        //根据用户id获取菜单权限值
        List<RouterVo> routerVoList = menuService.findUserMenuList(sysUser.getId());
        //根据用户id获取用户按钮权限
        List<String> permsList = menuService.findUserPermsList(sysUser.getId());

        result.put("name", sysUser.getName());
        result.put("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        //当前权限控制使用不到，我们暂时忽略
        result.put("roles",  new HashSet<>());
        result.put("buttons", permsList);
        result.put("routers", routerVoList);
        return result;
    }

    //显示当前用户信息
    @Override
    public Map<String, Object> getCurrentUser() {
        Map<String, Object> map = new HashMap<>();
        SysUser sysUser = baseMapper.selectById(LoginUserInfoHelper.getUserId());
        map.put("name",sysUser.getName());
        map.put("phone",sysUser.getPhone());
        return map;
    }
}
