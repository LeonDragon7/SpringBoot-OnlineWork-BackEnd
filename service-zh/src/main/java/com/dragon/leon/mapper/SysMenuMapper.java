package com.dragon.leon.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dragon.model.system.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author fzt
 * @since 2024-03-15
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    //根据userId查询可以操作菜单列表
    //多表查询：用户角色关系表、角色菜单关系表、菜单表
    List<SysMenu> findMenuListByUserId(@Param("userId") Long userId);
}
