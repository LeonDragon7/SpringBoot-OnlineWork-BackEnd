package com.dragon.leon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dragon.model.system.SysRole;
import com.dragon.vo.system.AssginRoleVo;
import org.springframework.stereotype.Service;

import java.util.Map;

public interface SysRoleService extends IService<SysRole> {

    Map<String, Object> getRoleByUserId(Long userId);

    void removeRolesAndSave(AssginRoleVo assginRoleVo);
}
