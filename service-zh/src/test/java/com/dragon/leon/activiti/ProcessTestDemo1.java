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
import java.util.Objects;

@SpringBootTest
public class ProcessTestDemo1 {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    //uel-value方式
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
        map.put("assignee1","lucy");
        map.put("assignee2","mary");
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
        String assignee = "lucy";
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


    //------------------------

    //uel-method
    //1.部署流程定义
    @Test
    public void deployProcess01(){
        Deployment deploy = repositoryService.createDeployment().addClasspathResource("process/jiaban01.bpmn20.xml")
                .name("加班申请流程01").deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }

    //2.启动流程实例
    @Test
    public void startUpProcess01(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban01");
        //输出实例的相关信息
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
    }

    /**
     * 3.查询当前个人待执行的任务
     */
    @Test
    public void findPendingTaskList01(){
        //任务负责人
        String assignee = "lilei";
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

    //-----------------------------------------------------

    //监听器分配任务
    //1.部署流程定义
    @Test
    public void deployProcess02(){
        Deployment deploy = repositoryService.createDeployment().addClasspathResource("process/jiaban02.bpmn20.xml")
                .name("加班申请流程02").deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }

    //2.启动流程实例
    @Test
    public void startUpProcess02(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban02");
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
        String assignee = "jack";
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
}
