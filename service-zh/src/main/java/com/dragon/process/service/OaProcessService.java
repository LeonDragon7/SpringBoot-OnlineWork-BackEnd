package com.dragon.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dragon.model.process.Process;
import com.dragon.vo.process.ApprovalVo;
import com.dragon.vo.process.ProcessFormVo;
import com.dragon.vo.process.ProcessQueryVo;
import com.dragon.vo.process.ProcessVo;

import java.util.Map;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author fzt
 * @since 2024-03-28
 */
public interface OaProcessService extends IService<Process> {

    //获取分页数据
    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo);

    //流程部署定义
    void deployByZip(String deployPath);

    //启动流程
    void startUp(ProcessFormVo processFormVo);

    //查询待处理任务列表
    IPage<ProcessVo> findPending(Page<Process> processPage);

    //显示审批详情
    Map<String,Object> show(Long id);

    //审批
    void approve(ApprovalVo approvalVo);

    //已处理
    IPage<ProcessVo> findProcessed(Page<Process> processParam);

    //已发起
    IPage<ProcessVo> findStarted(Page<ProcessVo> processParam);


}
