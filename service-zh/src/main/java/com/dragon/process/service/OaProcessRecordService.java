package com.dragon.process.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dragon.model.process.ProcessRecord;

/**
 * <p>
 * 审批记录 服务类
 * </p>
 *
 * @author fzt
 * @since 2024-03-28
 */
public interface OaProcessRecordService extends IService<ProcessRecord> {
    //记录提交记录
    void record(Long processId, Integer status, String description);
}
