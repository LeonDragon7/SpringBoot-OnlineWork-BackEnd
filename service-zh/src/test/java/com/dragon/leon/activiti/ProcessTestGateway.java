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
public class ProcessTestGateway {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    //排他网关

    //1.部署流程定义
    @Test
    public void deployProcess(){
        //流程部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/qingjia02.bpmn20.xml")
                .name("加班申请流程02")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }

    //2.启动流程实例
    @Test
    public void startUpProcess(){
        Map<String, Object> map = new HashMap<>();
        //设置请假天数
        map.put("day","2");
        //创建流程实例,我们需要知道流程定义的key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjia02", map);
        //输出实例的相关信息
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
    }

    /**
     * 3.查询当前个人待执行的任务
     */
    @Test
    public void findPendingTaskList02(){
        //任务负责人
        String assignee = "zhao6";
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

    //4.在任务办理时设置流程变量
    @Test
    public void completeTask() {
        Task task = taskService.createTaskQuery()
                .taskAssignee("zhao6")  //要查询的负责人
                .singleResult();//返回一条
        //完成任务,参数：任务id
        taskService.complete(task.getId());
    }

    //并行网关

    //1.部署流程定义
    @Test
    public void deployProcess1(){
        //流程部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/qingjia03.bpmn20.xml")
                .name("请假申请03")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }

    //2.启动流程实例
    @Test
    public void startUpProcess2(){
        Map<String, Object> map = new HashMap<>();
        //设置请假天数
        map.put("day","2");
        //创建流程实例,我们需要知道流程定义的key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjia03");
        //输出实例的相关信息
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
    }

    /**
     * 3.查询当前个人待执行的任务
     */
    @Test
    public void findPendingTaskList03(){
        //任务负责人
        String assignee = "wang5";
        //String assignee = "gouwa";
        //String assignee = "xiaoli";
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

    //4.在任务办理时设置流程变量
    @Test
    public void completeTask4() {
        Task task = taskService.createTaskQuery()
                .taskAssignee("wang5")  //要查询的负责人
                //.taskAssignee("gouwa")  //要查询的负责人
                //.taskAssignee("xiaoli")  //要查询的负责人
                .singleResult();//返回一条
        //完成任务,参数：任务id
        taskService.complete(task.getId());
    }
}
