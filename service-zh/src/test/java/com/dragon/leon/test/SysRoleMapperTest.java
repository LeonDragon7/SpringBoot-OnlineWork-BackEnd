package com.dragon.leon.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dragon.leon.mapper.SysRoleMapper;
import com.dragon.model.system.SysRole;
import com.mysql.cj.Query;
import lombok.var;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class SysRoleMapperTest {
    @Autowired
    private SysRoleMapper sysRoleMapper;

    //查询所有的数据
    @Test
    public void testSelectList() {
        System.out.println(("----- selectAll method test ------"));
        //UserMapper 中的 selectList() 方法的参数为 MP 内置的条件封装器 Wrapper
        //所以不填写就是无任何条件
        List<SysRole> users = sysRoleMapper.selectList(null);
        users.forEach(System.out::println);
    }

    //添加角色管理
    @Test
    public void testInsert(){
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("角色管理员1");
        sysRole.setRoleCode("role");
        sysRole.setDescription("角色管理员");

        int result = sysRoleMapper.insert(sysRole);
        //影响的行数 System.out.println(result);
        assert result == 1 : "ok";
        System.out.println(sysRole); //id自动回填
    }

    //更新角色管理
    @Test
    public void testUpdateById(){
        SysRole sysRole = new SysRole();
        sysRole.setId(1L);
        sysRole.setRoleName("角色管理员1");

        int result = sysRoleMapper.updateById(sysRole);
        assert result == 1 : "ok";

    }

    //删除角色管理
    @Test
    public void testDeleteById(){
        int result = sysRoleMapper.deleteById(9L);
        assert result == 1 : "ok";
    }

    @Test
    public void testDeleteBatchIds() {
        int result = sysRoleMapper.deleteBatchIds(Arrays.asList(1, 2));
        assert result == 1 : "ok";
    }

    @Test
    //条件查询
    public void testSelect1(){
        //创建条件
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_name","角色管理员1");
        //查询
        var result = sysRoleMapper.selectList(queryWrapper);
        result.forEach(System.out::println);
    }

    @Test
    //条件查询
    public void testSelect2(){
        //创建条件
        LambdaQueryWrapper<SysRole> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(SysRole::getRoleName,"角色管理员1");
        //查询
        var result = sysRoleMapper.selectList(lambdaQueryWrapper);
        result.forEach(System.out::println);
    }
}

