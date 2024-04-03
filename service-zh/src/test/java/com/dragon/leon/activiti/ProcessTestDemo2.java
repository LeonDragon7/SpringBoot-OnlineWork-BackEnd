package com.dragon.leon.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class ProcessTestDemo2 {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;
    //流程变量

    //1.部署流程定义
    @Test
    public void deployProcess(){
        //流程部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/jiaban.bpmn20.xml")
                .name("加班申请流程")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }

    //2.启动流程实例
    @Test
    public void startUpProcess(){
        Map<String, Object> map = new HashMap<>();
        //设置任务人
        map.put("assignee1","lucy02");
        map.put("assignee2","mary02");
        //创建流程实例,我们需要知道流程定义的key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban", map);
        //输出实例的相关信息
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
    }

    /**
     * 3.查询当前个人待执行的任务
     */
    @Test
    public void findPendingTaskList(){
        //任务负责人
        String assignee = "lucy02";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assignee)//只查询该任务负责人的任务
                .list();
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    //-------------------------------------

    // 在任务办理时设置流程变量


    //1.部署流程定义
    @Test
    public void deployProcess01(){
        //流程部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/jiaban.bpmn20.xml")
                .name("加班申请流程")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }

    //2.启动流程实例
    @Test
    public void startUpProcess02(){
        Map<String, Object> map = new HashMap<>();
        //设置任务人
        map.put("assignee1","lucy03");
        //创建流程实例,我们需要知道流程定义的key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban", map);
        //输出实例的相关信息
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
    }

    //3.在任务办理时设置流程变量
    @Test
    public void completeTask() {
        Task task = taskService.createTaskQuery()
                .taskAssignee("lucy03")  //要查询的负责人
                .singleResult();//返回一条

        Map<String, Object> variables = new HashMap<>();
        variables.put("assignee2", "zhao");
        //完成任务,参数：任务id
        taskService.complete(task.getId(), variables);
    }

    /**
     * 4.查询当前个人待执行的任务
     */
    @Test
    public void findPendingTaskList02(){
        //任务负责人
        String assignee = "lucy02";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assignee)//只查询该任务负责人的任务
                .list();
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    //--------------------------------------

    //通过当前流程实例设置
    @Test
    public void processInstanceIdSetVariables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("assignee2", "wang");
        runtimeService.setVariables("1c347a90-82c6-11ed-96ca-7c57581a7819", variables);
    }

    //--------------------------------------

    //设置Local变量

    @Test
    public void completLocalTask() {
        Task task = taskService.createTaskQuery()
                .taskAssignee("zhangsan")  //要查询的负责人
                .singleResult();//返回一条

        // 设置local变量，作用域为该任务
        taskService.setVariableLocal(task.getId(),"assignee2","li");
        // 查看local变量
        System.out.println(taskService.getVariableLocal(task.getId(), "assignee2"));
        //完成任务,参数：任务id
        taskService.complete(task.getId());
    }
}
