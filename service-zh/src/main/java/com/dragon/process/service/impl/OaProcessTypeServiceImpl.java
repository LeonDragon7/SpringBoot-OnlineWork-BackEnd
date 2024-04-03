package com.dragon.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dragon.process.mapper.OaProcessTypeMapper;
import com.dragon.process.service.OaProcessTemplateService;
import com.dragon.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragon.model.process.ProcessTemplate;
import com.dragon.model.process.ProcessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author fzt
 * @since 2024-03-27
 */
@Service
public class OaProcessTypeServiceImpl extends ServiceImpl<OaProcessTypeMapper, ProcessType> implements OaProcessTypeService {

    @Autowired
    private OaProcessTemplateService processTemplateService;

    //获取审批分类
    @Override
    public List<ProcessType> findProcessType() {
        //1 查询所有审批分类，返回list集合
        List<ProcessType> processTypeList = baseMapper.selectList(null);
        //2 遍历返回所有审批分类list集合
        for (ProcessType processType : processTypeList) {
            //3 得到每个审批分类，根据审批分类id查询对应审批模板 -> 根据审批分类id查询对应审批模板
            //审批分类id
            Long typeId = processType.getId();
            LambdaQueryWrapper<ProcessTemplate> processTemplateLambdaQueryWrapper = new LambdaQueryWrapper<>();
            processTemplateLambdaQueryWrapper.eq(ProcessTemplate::getProcessTypeId,typeId);
            List<ProcessTemplate> processTemplateList = processTemplateService.list(processTemplateLambdaQueryWrapper);
            //4 根据审批分类id查询对应审批模板数据（List）封装到每个审批分类对象里面
            processType.setProcessTemplateList(processTemplateList);
        }
        return processTypeList;
    }
}
