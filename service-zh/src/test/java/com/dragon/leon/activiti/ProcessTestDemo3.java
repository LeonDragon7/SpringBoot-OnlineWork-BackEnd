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
public class ProcessTestDemo3 {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    //1.部署流程定义和启动流程实例
    @Test
    public void deployProcess(){
        //流程部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/jiaban03.bpmn20.xml")
                .name("加班申请流程03")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());

        //启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban03");
        System.out.println(processInstance.getId());
    }

    //2.查询组任务
    @Test
    public void findGroupTaskList(){

        //查询组任务
        List<Task> list = taskService.createTaskQuery()
                .taskCandidateUser("zhangsan01")//根据候选人查询
                .list();
        for (Task task : list) {
            System.out.println("----------------------------");
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    //3.拾取组任务
    @Test
    public void claimTask(){
        //拾取任务，即使用户不是候选人也能拾取（建议拾取时校验是否有资格）
        //校验用户有没有拾取任务的资格
        Task task = taskService.createTaskQuery()
                .taskCandidateUser("zhangsan01")//根据候选人查询
                .singleResult();
        if(task != null){
            //拾取任务
            taskService.claim(task.getId(), "zhangsan01");
            System.out.println("任务拾取成功");
        }
    }

    /**
     * 4.查询当前个人待执行的任务
     */
    @Test
    public void findPendingTaskList02(){
        //任务负责人
        String assignee = "zhangsan01";
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

    //5. 个人任务办理
    @Test
    public void completeGroupTask(){
        Task task = taskService.createTaskQuery()
                .taskAssignee("zhangsan01") //要查询的负责人
                .singleResult();//返回一条
        taskService.complete(task.getId());
    }

    //6. 归还组任务
    @Test
    public void assigneeToGroupTask(){
        String taskId = "";//zhangsan01
        //任务负责人
        String userId = "zhangsan01";
        // 校验userId是否是taskId的负责人，如果是负责人才可以归还组任务
        Task task = taskService
                .createTaskQuery()
                .taskId(taskId)
                .taskAssignee(userId)
                .singleResult();
        if(task != null)
        // 如果设置为null，归还组任务,该 任务没有负责人
            taskService.setAssignee(taskId,null);
    }

    //7.任务交接
    @Test
    public void assigneeToCandidateUser(){
        // 当前待办任务
        String taskId = "";//zhangsan01
        // 校验zhangsan01是否是taskId的负责人，如果是负责人才可以归还组任务
        Task task = taskService
                .createTaskQuery()
                .taskId(taskId)
                .taskAssignee("zhangsan01")
                .singleResult();
        if(task != null)
            // 将此任务交给其它候选人zhangsan02办理该 任务
            taskService.setAssignee(taskId,"zhangsan02");
    }
}
