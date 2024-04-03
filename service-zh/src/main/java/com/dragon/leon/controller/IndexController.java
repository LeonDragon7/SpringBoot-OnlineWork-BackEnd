package com.dragon.leon.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dragon.common.execption.RuntimeExceptionBeen;
import com.dragon.common.jwt.JwtHelper;
import com.dragon.common.result.Result;
import com.dragon.common.utils.MD5;
import com.dragon.leon.service.SysMenuService;
import com.dragon.leon.service.SysUserService;
import com.dragon.model.system.SysUser;
import com.dragon.vo.system.LoginVo;
import com.dragon.vo.system.RouterVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "登录")
@RestController
@RequestMapping("/admin/system/index/")
public class IndexController {

    @Autowired
    private SysUserService userService;
    /**
     * 用户登录接口
     *  1 根据用户名查询数据库，用户是否存在（信息是否正确）
     *  2 如果用户存在（信息正确），判断用户是否被禁用 -> 条件：status
     *  3 登录成功之后保持登录状态
     *  （1） 基于token实现
     *  （2） 使用登录信息（用户id，用户名称等）生成唯一的字符串，对生成字符串进行编码加密处理（JWT工具）
     *  （3） 把唯一的字符串放到cookie里面，从cookie获取
     *  但是cookie有问题：不能跨域传递，比如前端项目9528的端口号，后端服务是8800端口号，不一样，产生跨域。
     *  解决：cookie跨域：每次发送请求的时候，把cookie值获取处理放到请求头，每次从请求头获取用户信息
     * @return
     */
    @ApiOperation("登录结果")
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo){
        //1 根据用户名查询数据库，用户是否存在（信息是否正确）
        SysUser sysUser = userService.getByUserName(loginVo.getUsername());
        if(sysUser == null)
            throw new RuntimeExceptionBeen(201,"用户名不存在");
        //2 如果用户存在（信息正确），判断用户是否被禁用 -> 条件：status
        if(sysUser.getStatus().longValue() == 0)
            throw new RuntimeExceptionBeen(201,"该用户被禁用");
        //3 判断密码是否正确 MD5加密
        if(!MD5.encrypt(loginVo.getPassword()).equals(sysUser.getPassword())) {
            throw new RuntimeExceptionBeen(201,"密码错误，请重新输入");
        }

        //4.将token返回
        String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
        Map<String, Object> map = new HashMap<>();
        map.put("token",token);
        return Result.ok(map);
    }

    /**
     * 登录之后，获取用户可以操作菜单（按钮）
     * 1 从请求头获取token字符串，从字符串获取用户id 或 用户名称
     * 2 根据用户id查询，返回用户可以操作菜单(routers)和操作按钮(buttons) -> 查询数据库动态构建路由结构，进行显示
     *  (1) 判断当前用户是否为管理员 userId = 1是管理员
     *      - 如果是管理员，查询所有菜单列表（状态，排序）
     *      - 如果不是管理员，根据userId查询可以操作菜单列表
     *  (2) 多表查询：用户角色关系表、角色菜单关系表、菜单表
     *  (3) 把查询出来数据列表构建成框架要求的路由数据结构
     *      - 首先使用菜单操作工具类构建成树形结构
     *      - 其次构建成框架要求的路由数据结构 -> 递归
     *          1. 创建List集合，存储最终的数据
     *          2. 遍历传过来的list集合进行set赋值
     *          3. 根据SysMenu的children进行下一层数据的遍历
     *          4. 加载隐藏路由 -> 根据type属性判断 将type为1下面的权限包含component 根据StreamAPI的方式进行过滤
     *          （如果当前是菜单【type == 1】，需将按钮对应的路由加载出来，如：“角色授权”按钮对应的路由在“系统管理”下面）
     *          5. 如果type不为1，则将显示的子菜单AlwaysShow设置为true
     * 3 根据用户id获取用户可以操作按钮列表  -> 判断当前用户是否可以操作该按钮
     *      - 判断是否是管理员，如果是管理员，查询所有按钮列表  -> 条件 status == 1
     *      - 不是管理员，根据userId查询可以操作按钮列表 多表查询：用户角色关系表、角色菜单关系表、菜单表 2 - (2)
     *             从查询出来的数据里面，获取可以操作按钮值的list集合，返回   StreamAPI方式
     * 4 返回相应的数据
     * @return
     */
    @ApiOperation("登录基本信息")
    @GetMapping("info")
    public Result info(HttpServletRequest request){
        //从请求头获取token字符串，从字符串获取用户名称
        String username = JwtHelper.getUsername(request.getHeader("token"));
        //根据用户名获取登录信息
        Map<String,Object> map = userService.getUserInfo(username);
        return Result.ok(map);
    }

    @ApiOperation("退出系统")
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }

}
