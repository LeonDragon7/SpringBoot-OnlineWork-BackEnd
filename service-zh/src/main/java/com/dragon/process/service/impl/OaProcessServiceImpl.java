package com.dragon.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dragon.leon.service.SysUserService;
import com.dragon.model.process.ProcessRecord;
import com.dragon.model.process.ProcessTemplate;
import com.dragon.model.system.SysUser;
import com.dragon.process.mapper.OaProcessMapper;
import com.dragon.process.service.OaProcessRecordService;
import com.dragon.process.service.OaProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dragon.model.process.Process;
import com.dragon.process.service.OaProcessTemplateService;
import com.dragon.security.custom.LoginUserInfoHelper;
import com.dragon.vo.process.ApprovalVo;
import com.dragon.vo.process.ProcessFormVo;
import com.dragon.vo.process.ProcessQueryVo;
import com.dragon.vo.process.ProcessVo;
import com.dragon.wechat.service.MessageService;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author fzt
 * @since 2024-03-28
 */
@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, Process> implements OaProcessService {

    @Autowired
    private SysUserService userService;
    @Autowired
    private OaProcessTemplateService processTemplateService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private OaProcessRecordService processRecordService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private MessageService messageService;
    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> page = baseMapper.selectPage(pageParam,processQueryVo);
        return page;
    }

    //流程部署定义
    @Override
    public void deployByZip(String deployPath) {
        //定义zip输入流
        InputStream inputStream = this
                .getClass()
                .getClassLoader()
                .getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        //流程部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .deploy();

        System.out.println("deployment.getId() = " + deployment.getId());
        System.out.println("deployment.getName() = " + deployment.getName());
    }

    //启动流程
    @Override
    public void startUp(ProcessFormVo processFormVo) {
        //1.根据当前的用户id查询用户
        SysUser sysUser = userService.getById(LoginUserInfoHelper.getUserId());
        //2.根据传入的审批申请属性的审批模板id获取审批模板对象
        ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());
        //3.保存提交审批信息到业务表   ->     创建process对象，将数据封装
        Process process = new Process();
        BeanUtils.copyProperties(processFormVo,process);
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        process.setStatus(1);
        //保存封装的数据
        baseMapper.insert(process);

        //4.根据process获取业务id
        String businessKey = String.valueOf(process.getId());

        //5.将表单数据放入到流程实例中，用Map存储  -> 循环转换
        //formData{F1t562enucybf: "09:38:42"}
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());//将前端传过来的表单JSON数据转换为Java对象
        JSONObject formData = jsonObject.getJSONObject("formData");//获取JSON格式的key
        Map<String, Object> map = new HashMap<>();
        //循环转换
        for (Map.Entry<String, Object> entry : formData.entrySet()){
                map.put(entry.getKey(),entry.getValue());
        }
        //流程参数
        Map<String, Object> variable = new HashMap<>();
        variable.put("data",map);

        //6.启动流程实例 -> 根据流程实例对象获取实例id，封装到process
        //6.1 流程定义key   ->    processTemplate.getProcessDefinitionKey()
        //6.2 业务key  ->   4
        //6.3 流程参数 form表单json数据，转换map集合
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processTemplate.getProcessDefinitionKey(),businessKey, variable);
        //业务表关联当前流程实例id
        process.setProcessInstanceId(processInstance.getId());
        //7.查询下一个审批人，可能有多个（并行审批），传入实例id，获取审批人集合
        List<Task> taskList = this.getCurrentTaskList(processInstance.getId());
        if(!StringUtils.isEmpty(taskList)){
            List<String> list = new ArrayList<>();
            //将审批人真实姓名保存在list集合
            for (Task task : taskList) {
                SysUser user = userService.getByUserName(task.getAssignee());
                list.add(user.getName());
                //TODO 推送消息给下一个审批人
                messageService.pushPendingMessage(process.getId(),user.getId(), task.getId());
            }
        }
        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待" + org.apache.commons.lang3.StringUtils.join(list().toArray(), ",") + "审批");

        //8.业务和流程关联,更新封装到process的数据
        baseMapper.updateById(process);

        //记录操作审批信息记录
        processRecordService.record(process.getId(),1,"发起申请");
    }

    //查询待处理任务列表
    @Override
    public IPage<ProcessVo> findPending(Page<Process> processPage) {
        //1 封装查询条件，根据当前登录的用户名称
        TaskQuery query = taskService.createTaskQuery().taskAssignee(LoginUserInfoHelper.getUsername()).orderByTaskCreateTime().desc();
        long total = query.count();
        //2 调用方法分页条件查询，返回list集合，待办任务集合
        int startPage = (int)((processPage.getCurrent() - 1) * processPage.getSize());
        int size = (int)processPage.getSize();
        List<Task> taskList = query.listPage(startPage, size);
        //3 封装返回list集合数据到List<ProcessVo>里面
        List<ProcessVo> processVoList = new ArrayList<>();
        for (Task task : taskList) {
            // 3.1 获取task里面的实例id
            String processInstanceId = task.getProcessInstanceId();
            // 3.2 根据实例id获取实例对象
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if(processInstance == null) continue;
            // 3.3 根据实例对象获取业务id -> process的id
            Long businessKey = Long.parseLong(processInstance.getBusinessKey());
            if(businessKey == null) continue;
            // 3.4 根据业务id获取process对象
            Process process = baseMapper.selectById(businessKey);
            // 3.5 将process对象拷贝到processVo
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            // 3.6 封装其他条件
            processVo.setTaskId(task.getId());
            // 3.7 添加到processVo集合中
            processVoList.add(processVo);
        }
        //4 封装返回Page对象 并将转换的集合封装到page对象中
        IPage<ProcessVo> pageModel = new Page<>(processPage.getCurrent(),processPage.getSize(),total);
        pageModel.setRecords(processVoList);
        return pageModel;
    }

    //显示审批详情
    @Override
    public Map<String, Object> show(Long id) {
        //1 根据流程id获取流程信息Process
        Process process = this.getById(id);
        //2 根据流程id获取流程记录信息
        LambdaQueryWrapper<ProcessRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProcessRecord::getProcessId,id);
        List<ProcessRecord> processRecordList = processRecordService.list(queryWrapper);
        //3 根据模板id查询模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        //4 判断当前用户是否可以审批
        //可以看到信息不一定能审批，不能重复审批
        boolean isApprove = false;
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if(!StringUtils.isEmpty(taskList)){
            for (Task task : taskList) {
                if(task.getAssignee().equals(LoginUserInfoHelper.getUsername()))
                    isApprove = true;
            }
        }
        //查询封装数据到map集合，返回
        Map<String, Object> map = new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);
        map.put("isApprove",isApprove);
        return map;
    }

    //审批
    @Override
    public void approve(ApprovalVo approvalVo) {
        //1. 从approvalVo获取任务id，根据任务id获取流程变量
        Map<String, Object> variables = taskService.getVariables(approvalVo.getTaskId());
        //2. 遍历流程变量Map,输出key和value
        for (Map.Entry<String,Object> entry :variables.entrySet()){
            System.out.println("key = " + entry.getKey());
            System.out.println("value = " + entry.getValue());
        }

        //3. 判断审批状态值是否为1
        //3.1 状态值 = 1 审批通过
        if(approvalVo.getStatus().intValue() == 1){
            Map<String, Object> variable = new HashMap<>();
            taskService.complete(approvalVo.getTaskId(),variable);
        }else{
            //3.2 -1(未通过)：驳回，流程直接结束
            this.endTasked(approvalVo.getTaskId());
        }
        //4.记录审批相关过程信息 oa_process_record
        String description = approvalVo.getStatus().intValue() == 1 ? "已通过" : "驳回";
        processRecordService.record(approvalVo.getProcessId(),approvalVo.getStatus(),description);

        //5.查询下一个审批人，更新流程表记录 process表记录
        Process process = this.getById(approvalVo.getProcessId());
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if(!CollectionUtils.isEmpty(taskList)){
            for (Task task : taskList) {
                List<String> list = new ArrayList<>();
                SysUser user = userService.getByUserName(task.getAssignee());
                list.add(user.getName());

                //TODO 微信公众号推送信息
                messageService.pushPendingMessage(process.getId(), user.getId(), task.getId());
            }
            process.setDescription("等待" + org.apache.commons.lang3.StringUtils.join(list().toArray(), ",") + "审批");
            process.setStatus(1);
        }else{
            if(approvalVo.getStatus().intValue() == 1){
                process.setDescription("审批完成（同意）");
                process.setStatus(2);
            }else {
                process.setDescription("审批完成（拒绝）");
                process.setStatus(-1);
            }
        }
        //更新process表的数据
        baseMapper.updateById(process);
    }

    //已处理
    @Override
    public IPage<ProcessVo> findProcessed(Page<Process> processParam) {
        //1. 通过当前用户创建查询条件
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery().taskAssignee(LoginUserInfoHelper.getUsername()).finished().orderByTaskCreateTime()
                .desc();
        //2. 封装分页查询，获取总数
        //开始位置
        int start = (int)((processParam.getCurrent() - 1) * processParam.getSize());
        //每页显示记录数
        int size = (int)processParam.getSize();
        long total = query.count();
        List<HistoricTaskInstance> listPage = query.listPage(start, size);
        //3. 封装返回list集合数据到List<ProcessVo>里面
        List<ProcessVo> processVoList = new ArrayList<>();
        for (HistoricTaskInstance historicTaskInstance : listPage) {
            //获取条件实例id
            String processInstanceId = historicTaskInstance.getProcessInstanceId();
            //根据实例id获取process
            LambdaQueryWrapper<Process> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Process::getProcessInstanceId,processInstanceId);
            Process process = this.getOne(wrapper);
            //将process复制到processVo
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            //其他条件
            processVo.setTaskId("0");
            processVoList.add(processVo);
        }
        //4. 封装分页，并将数据返回
        IPage page = new Page(processParam.getCurrent(),processParam.getSize(),total);
        page.setRecords(processVoList);
        return page;
    }

    //已发起
    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> processParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> page = baseMapper.selectPage(processParam, processQueryVo);
        return null;
    }




    private void endTasked(String taskId) {
        // 1. 根据传入的taskId获取任务对象id
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 2. 根据流程定义id获取BpmnModel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        // 2.1 获取所有下个审批人节点事件
        List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        if(CollectionUtils.isEmpty(endEventList)) return;
        // 3. 获取结束流向节点
        FlowNode endFlowNode = endEventList.get(0);
        // 4. 当前流向节点
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getProcessDefinitionId());
        // 5. 临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        // 6. 清理当前流动方向
        currentFlowNode.getOutgoingFlows().clear();
        // 7. 创建新流向
        SequenceFlow sequenceFlow = new SequenceFlow();
        sequenceFlow.setId("sequenceFlowId");
        sequenceFlow.setSourceFlowElement(currentFlowNode);
        sequenceFlow.setTargetFlowElement(endFlowNode);
        List newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(sequenceFlow);
        // 8. 当前节点指向新方向
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);
        // 9. 完成当前任务
        taskService.complete(task.getId());
    }

    /**
     * 获取当前任务列表
     * @param processInstanceId
     * @return
     */
    private List<Task> getCurrentTaskList(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }
}
