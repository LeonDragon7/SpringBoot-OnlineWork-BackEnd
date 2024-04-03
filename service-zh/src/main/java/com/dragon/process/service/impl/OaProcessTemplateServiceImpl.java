package com.dragon.process.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dragon.process.mapper.OaProcessTemplateMapper;
import com.dragon.process.service.OaProcessService;
import com.dragon.process.service.OaProcessTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragon.process.service.OaProcessTypeService;
import com.dragon.model.process.ProcessTemplate;
import com.dragon.model.process.ProcessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author fzt
 * @since 2024-03-27
 */
@Service
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate> implements OaProcessTemplateService {

    @Autowired
    private OaProcessTypeService processTypeService;
    @Autowired
    private OaProcessService processService;
    @Override
    public IPage<ProcessTemplate> selectPage(Page<ProcessTemplate> pageParam) {
        //1.根据ProcessTemplate的id进行降序排序，创建条件
        LambdaQueryWrapper<ProcessTemplate> processTemplateLambdaQueryWrapper = new LambdaQueryWrapper<>();
        processTemplateLambdaQueryWrapper.orderByDesc(ProcessTemplate::getId);
        //2.根据传入的分页数据和排序条件进行查询审批模板数据
        IPage<ProcessTemplate> page = baseMapper.selectPage(pageParam, processTemplateLambdaQueryWrapper);
        //3.根据上面2查询的分页数据获取审批模板list数据
        List<ProcessTemplate> processTemplateList = page.getRecords();

        //4.根据StreamAPI处理获取id集合 -> 查询审批模板表的审批类型id的集合
        List<Long> processTypeIds = processTemplateList.stream().map(t -> t.getId()).collect(Collectors.toList());

        //5.判断4的id集合是否为空
        if(!StringUtils.isEmpty(processTypeIds)){
            /**
             * 6.根据模糊匹配查询processType的id是否在id集合的列表，在将查询出来的list进行StreamAPI的toMap进行map存储
             *   - key：id
             *   - value：processType对象
             */
            Map<Long, ProcessType> processTypeMap = processTypeService.list(new LambdaQueryWrapper<ProcessType>().in(ProcessType::getId, processTypeIds)).stream()
                    .collect(Collectors.toMap(ProcessType::getId, ProcessType -> ProcessType));
            /**
             * 7. 循环迭代上面的3获取的list集合，将每个对象ProcessTemplate的id传入上面的map获取value值，来获取对应的对象
             *  判断值是否为空，如果为空 continue，结束将审批类型名称封装到ProcessTemplate的属性审批类型名称
             */
            for (ProcessTemplate processTemplate : processTemplateList) {
                ProcessType processType = processTypeMap.get(processTemplate.getProcessTypeId());
                if(processType == null) continue;
                processTemplate.setProcessTypeName(processType.getName());
            }
        }
        return page;
    }

    //部署流程定义（发布）1：已发布，0：未发布
    @Override
    public void publish(Long id) {
        //1.发布
        ProcessTemplate processTemplate = baseMapper.selectById(id);
        processTemplate.setStatus(1);
        baseMapper.updateById(processTemplate);

        //TODO 流程定义部署 优先发布在线流程设计
        if(!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath()))
            processService.deployByZip(processTemplate.getProcessDefinitionPath());
    }
}
