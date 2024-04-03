package com.dragon.process.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dragon.model.process.ProcessType;

import java.util.List;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author fzt
 * @since 2024-03-27
 */
public interface
OaProcessTypeService extends IService<ProcessType> {

    //获取审批分类
    List<ProcessType> findProcessType();
}
