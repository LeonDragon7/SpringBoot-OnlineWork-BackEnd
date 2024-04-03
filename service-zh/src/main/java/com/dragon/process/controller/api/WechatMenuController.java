package com.dragon.process.controller.api;


import com.dragon.common.result.Result;
import com.dragon.model.wechat.Menu;
import com.dragon.wechat.service.WechatMenuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 菜单 前端控制器
 * </p>
 *
 * @author fzt
 * @since 2024-04-02
 */
@RestController
@RequestMapping("/admin/wechat/menu")
public class WechatMenuController {

    @Autowired
    private WechatMenuService wechatMenuService;


    @ApiOperation("显示微信菜单")
    @GetMapping("findMenuInfo")
    public Result findMenuInfo(){
        return Result.ok(wechatMenuService.findMenuInfo());
    }

    @ApiOperation(value = "同步菜单")
    @GetMapping("syncMenu")
    public Result createMenu() {
        wechatMenuService.syncMenu();
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.menu.removeMenu')")
    @ApiOperation(value = "删除菜单")
    @DeleteMapping("removeMenu")
    public Result removeMenu() {
        wechatMenuService.removeMenu();
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.menu.add')")
    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody Menu menu) {
        wechatMenuService.save(menu);
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.menu.update')")
    @ApiOperation(value = "修改")
    @PutMapping("update")
    public Result updateById(@RequestBody Menu menu) {
        wechatMenuService.updateById(menu);
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.menu.remove')")
    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        wechatMenuService.removeById(id);
        return Result.ok();
    }
}

