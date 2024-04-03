package com.dragon.process.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dragon.common.result.Result;
import com.dragon.process.service.OaProcessTemplateService;
import com.dragon.model.process.ProcessTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 * <p>
 * 审批模板 前端控制器
 * </p>
 *
 * @author fzt
 * @since 2024-03-27
 */
@Api(value = "审批模板管理", tags = "审批模板管理")
@RestController
@RequestMapping(value = "/admin/process/processTemplate")
@SuppressWarnings({"unchecked", "rawtypes"})
public class OaProcessTemplateController {
    @Autowired
    private OaProcessTemplateService processTemplateService;

    @ApiOperation(value = "获取分页数据")
    @GetMapping("{page}/{limit}")
    public Result index(
            @ApiParam(name = "page",value = "当前页码",required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit",value = "每页显示记录数",required = true)
            @PathVariable Long limit
    ){
        Page<ProcessTemplate> pageParam = new Page<>(page, limit);
        IPage<ProcessTemplate> pageModel = processTemplateService.selectPage(pageParam);
        return Result.ok(pageModel);
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.list')")
    @ApiOperation(value = "获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        ProcessTemplate processTemplate = processTemplateService.getById(id);
        return Result.ok(processTemplate);
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.templateSet')")
    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody ProcessTemplate processTemplate) {
        processTemplateService.save(processTemplate);
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.templateSet')")
    @ApiOperation(value = "修改")
    @PutMapping("update")
    public Result updateById(@RequestBody ProcessTemplate processTemplate) {
        processTemplateService.updateById(processTemplate);
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.remove')")
    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        processTemplateService.removeById(id);
        return Result.ok();
    }


    /**
     * @param file 接收客户端的文件
     * @return
     */
    //@PreAuthorize("hasAuthority('bnt.processTemplate.templateSet')")
    @ApiOperation(value = "上传流程定义")
    @PostMapping("/uploadProcessDefinition")
    public Result uploadProcessDefinition(MultipartFile file) throws FileNotFoundException {
        //1.获取URL为"classpath:"的绝对路径
        String path = new File(ResourceUtils.getURL("classpath:").getPath()).getAbsolutePath();
        //2.创建从客户端传过来的文件名
        String fileName = file.getOriginalFilename();
        //3.创建上传目录
        File tempFile = new File(path + "/processes/");
        //4.判断目录是否存在 - 如果不存在进行创建
        if(!tempFile.exists())
            tempFile.mkdir();
        //5.创建空文件用于写入文件 -> 加入2
        File imgFile = new File(path + "/processes/" + fileName);
        //6.保存文件流到本地
        try {
            file.transferTo(imgFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //7.根据上传地址后续部署流程定义，文件名称为流程定义的默认key
        HashMap<String, Object> map = new HashMap<>();
        map.put("processDefinitionPath","processes/" + fileName);
        map.put("processDefinitionKey",fileName.substring(0,fileName.lastIndexOf(".")));
        return Result.ok(map);
    }

    //部署流程定义（发布）1：已发布，0：未发布
    //@PreAuthorize("hasAuthority('bnt.processTemplate.publish')")
    @ApiOperation(value = "发布")
    @GetMapping("/publish/{id}")
    public Result publish(@PathVariable Long id){
        processTemplateService.publish(id);
        return Result.ok();
    }
}

