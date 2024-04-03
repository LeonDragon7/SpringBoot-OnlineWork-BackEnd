package com.dragon.process.service.impl;


import com.dragon.leon.service.SysUserService;
import com.dragon.model.system.SysUser;
import com.dragon.process.mapper.OaProcessRecordMapper;
import com.dragon.process.service.OaProcessRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragon.model.process.ProcessRecord;
import com.dragon.process.service.OaProcessService;
import com.dragon.security.custom.LoginUserInfoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 审批记录 服务实现类
 * </p>
 *
 * @author fzt
 * @since 2024-03-28
 */
@Service
public class OaProcessRecordServiceImpl extends ServiceImpl<OaProcessRecordMapper, ProcessRecord> implements OaProcessRecordService {

    @Autowired
    private SysUserService userService;
    @Override
    public void record(Long processId, Integer status, String description) {
        SysUser sysUser = userService.getById(LoginUserInfoHelper.getUserId());
        ProcessRecord processRecord = new ProcessRecord();
        processRecord.setProcessId(processId);
        processRecord.setStatus(status);
        processRecord.setDescription(description);
        processRecord.setOperateUserId(sysUser.getId());
        processRecord.setOperateUser(sysUser.getName());
        baseMapper.insert(processRecord);
    }
}
