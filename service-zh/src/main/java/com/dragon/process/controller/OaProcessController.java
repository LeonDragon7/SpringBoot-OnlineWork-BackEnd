package com.dragon.process.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dragon.common.result.Result;
import com.dragon.process.service.OaProcessService;
import com.dragon.vo.process.ProcessQueryVo;
import com.dragon.vo.process.ProcessVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 审批类型 前端控制器
 * </p>
 *
 * @author fzt
 * @since 2024-03-28
 */
@Api(tags = "审批流管理")
@RestController
@RequestMapping(value = "/admin/process")
@SuppressWarnings({"unchecked", "rawtypes"})
public class OaProcessController {
    @Autowired
    private OaProcessService processService;

    @PreAuthorize("hasAuthority('bnt.process.list')")
    @ApiOperation("获取分页数据")
    @GetMapping("{page}/{limit}")
    public Result index(
            @ApiParam(name = "page",value = "当前页码数",required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit",value = "每页显示记录数",required = true)
            @PathVariable Long limit,
            @ApiParam(name = "processVo",value = "审批查询条件")
            @RequestBody ProcessQueryVo processQueryVo){
        Page<ProcessVo> pageParam = new Page<>(page, limit);
        IPage<ProcessVo> pageModel =  processService.selectPage(pageParam,processQueryVo);
        return Result.ok(pageModel);
    }
}

