package com.dragon.wechat.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dragon.common.jwt.JwtHelper;
import com.dragon.common.result.Result;
import com.dragon.leon.service.SysUserService;
import com.dragon.model.system.SysUser;
import com.dragon.vo.wechat.BindPhoneVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * <p>
 * 菜单 前端控制器
 * </p>
 *
 * @author fzt
 * @since 2024-04-02
 */
@Controller
@RequestMapping("/admin/wechat")
@CrossOrigin//跨域
public class WechatController {

    @Resource
    private SysUserService sysUserService;

    @Autowired
    private WxMpService wxMpService;

    @Value("${wechat.userInfoUrl}")
    private String userInfoUrl;

    @GetMapping("/authorize")
    public String authorize(@RequestParam("returnUrl") String returnUrl, HttpServletRequest request) {
        //由于授权回调成功后，要返回原地址路径，原地址路径带“#”号，当前returnUrl获取带“#”的url获取不全，因此前端把“#”号替换为“guiguoa”了，这里要还原一下
        String redirectURL = wxMpService.getOAuth2Service().buildAuthorizationUrl(userInfoUrl, WxConsts.OAuth2Scope.SNSAPI_USERINFO, URLEncoder.encode(returnUrl.replace("guiguoa", "#")));
        return "redirect:" + redirectURL;
    }

    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("code") String code,
                           @RequestParam("state") String returnUrl) throws Exception{
        WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
        String openId = accessToken.getOpenId();

        WxOAuth2UserInfo wxMpUser = wxMpService.getOAuth2Service().getUserInfo(accessToken, null);

        SysUser sysUser = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getOpenId, openId));
        String token = "";
        //null != sysUser 说明已经绑定，反之为建立账号绑定，去页面建立账号绑定
        if(null != sysUser)
            token = JwtHelper.createToken(sysUser.getId(),sysUser.getUsername());
        if(returnUrl.indexOf("?") == -1){
            return "redirect:" + returnUrl + "?token=" + token + "&openId=" + openId;
        }else {
            return "redirect:" + returnUrl + "&token=" + token + "&openId=" + openId;
        }
    }

    @ApiOperation(value = "微信账号绑定手机")
    @PostMapping("bindPhone")
    @ResponseBody
    public Result bindPhone(@RequestBody BindPhoneVo bindPhoneVo){
        SysUser sysUser = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, bindPhoneVo.getPhone()));
        if(null != sysUser){
            sysUser.setOpenId(bindPhoneVo.getOpenId());
            sysUserService.updateById(sysUser);

            String token = JwtHelper.createToken(sysUser.getId(),sysUser.getUsername());
            return Result.ok(token);
        }else{
            return Result.fail("手机号码不存在，绑定失败");
        }
    }
}

