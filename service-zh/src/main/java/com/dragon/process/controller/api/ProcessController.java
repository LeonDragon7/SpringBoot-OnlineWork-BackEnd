package com.dragon.process.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dragon.common.result.Result;
import com.dragon.model.process.Process;
import com.dragon.model.process.ProcessTemplate;
import com.dragon.process.service.OaProcessService;
import com.dragon.process.service.OaProcessTemplateService;
import com.dragon.process.service.OaProcessTypeService;
import com.dragon.vo.process.ApprovalVo;
import com.dragon.vo.process.ProcessFormVo;
import com.dragon.vo.process.ProcessVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "审批流管理")
@RestController
@RequestMapping(value="/admin/process")
@CrossOrigin  //跨域
public class ProcessController {
    @Autowired
    private OaProcessService processService;
    @Autowired
    private OaProcessTypeService processTypeService;

    @Autowired
    private OaProcessTemplateService processTemplateService;


    @ApiOperation("获取所有审批分类和模板")
    @GetMapping("findProcessType")
    public Result findProcessType(){
        return Result.ok(processTypeService.findProcessType());
    }

    @ApiOperation("获取审批模板")
    @GetMapping("getProcessTemplate/{id}")
    public Result getId(@PathVariable Long id){
        ProcessTemplate processTemplate = processTemplateService.getById(id);
        return Result.ok(processTemplate);
    }

    @ApiOperation("启动流程")
    @PostMapping("/startUp")
    public Result startUp(@RequestBody ProcessFormVo processFormVo){
        processService.startUp(processFormVo);
        return Result.ok();
    }

    @ApiOperation("待处理")
    @GetMapping("findPending/{page}/{limit}")
    public Result findPending(
            @ApiParam(name = "page",value = "当前页码",required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit",value = "每页显示记录数",required = true)
            @PathVariable Long limit){
        Page<Process> processPage = new Page<>(page, limit);
        return Result.ok(processService.findPending(processPage));
    }

    @ApiOperation("获取审批详情")
    @GetMapping("show/{id}")
    public Result show(@PathVariable Long id){
        return Result.ok(processService.show(id));
    }


    @ApiOperation("审批")
    @PostMapping("approve")
    public Result approve(@RequestBody ApprovalVo approvalVo){
        processService.approve(approvalVo);
        return Result.ok();
    }

    @ApiOperation("已处理")
    @GetMapping("findProcessed/{page}/{limit}")
    public Result findProcessed(
            @ApiParam(name = "page",value = "当前页码",required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit",value = "每页显示记录数",required = true)
            @PathVariable Long limit
            ){
        Page<Process> processParam = new Page<>(page, limit);
        return Result.ok(processService.findProcessed(processParam));
    }

    @ApiOperation("已发起")
    @GetMapping("findStarted/{page}/{limit}")
    public Result findStarted(
            @ApiParam(name = "page",value = "当前页码",required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit",value = "每页显示记录数",required = true)
            @PathVariable Long limit
    ){
        Page<ProcessVo> processParam = new Page<>(page, limit);
        return Result.ok(processService.findStarted(processParam));
    }


}
