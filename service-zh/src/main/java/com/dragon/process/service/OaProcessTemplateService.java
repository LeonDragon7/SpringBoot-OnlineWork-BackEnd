package com.dragon.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dragon.model.process.ProcessTemplate;

/**
 * <p>
 * 审批模板 服务类
 * </p>
 *
 * @author fzt
 * @since 2024-03-27
 */
public interface OaProcessTemplateService extends IService<ProcessTemplate> {

    IPage<ProcessTemplate> selectPage(Page<ProcessTemplate> pageParam);

    //部署流程定义（发布）1：已发布，0：未发布
    void publish(Long id);

}
