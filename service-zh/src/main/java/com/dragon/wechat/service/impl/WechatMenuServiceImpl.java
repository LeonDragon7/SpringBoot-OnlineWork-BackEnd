package com.dragon.wechat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.dragon.model.wechat.Menu;
import com.dragon.vo.wechat.MenuVo;
import com.dragon.wechat.mapper.WechatMenuMapper;
import com.dragon.wechat.service.WechatMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单 服务实现类
 * </p>
 *
 * @author fzt
 * @since 2024-04-02
 */
@Service
public class WechatMenuServiceImpl extends ServiceImpl<WechatMenuMapper, Menu> implements WechatMenuService {

    @Autowired
    private WxMpService wxMpService;
    //显示微信菜单
    @Override
    public List<MenuVo> findMenuInfo() {
        List<MenuVo> list = new ArrayList<>();
        //1. 查询所有微信菜单
        List<Menu> menusList = baseMapper.selectList(null);
        //2. 根据StreamAPI获取parent_id为0的集合
        List<Menu> parentList = menusList.stream().
                filter(m -> m.getParentId().intValue() == 0).collect(Collectors.toList());
        //3. 遍历集合2，将数据转换为List<MenuVo>
        for (Menu menu : parentList) {
            MenuVo menuVo = new MenuVo();
            BeanUtils.copyProperties(menu,menuVo);

            //4. 根据StreamAPI过滤出parent_id和id相等的集合，根据sort排序
            List<Menu> idsList = menusList.stream().filter(m -> m.getParentId().intValue() == menu.getId())
                    .sorted(Comparator.comparing(Menu::getSort))
                    .collect(Collectors.toList());

            //5. 将4进行遍历，数据转换为List<MenuVo>，保存children
            List<MenuVo> children = new ArrayList<>();
            for (Menu childrenMenu : idsList) {
                MenuVo menuVo2 = new MenuVo();
                BeanUtils.copyProperties(childrenMenu,menuVo2);
                children.add(menuVo2);
            }
            menuVo.setChildren(children);
            list.add(menuVo);
        }
        //6. 将上面的数据封装，数据返回
        return list;
    }

    //同步菜单
    @Override
    public void syncMenu() {
        //查询菜单数据，封装微信要求菜单格式
        List<MenuVo> menuVoList = this.findMenuInfo();
        JSONArray buttonList = new JSONArray();
        for(MenuVo oneMenuVo : menuVoList) {
            JSONObject one = new JSONObject();
            one.put("name", oneMenuVo.getName());
            if(CollectionUtils.isEmpty(oneMenuVo.getChildren())) {
                one.put("type", oneMenuVo.getType());
                one.put("url", "http://oa.atguigu.cn/#"+oneMenuVo.getUrl());
            } else {
                JSONArray subButton = new JSONArray();
                for(MenuVo twoMenuVo : oneMenuVo.getChildren()) {
                    JSONObject view = new JSONObject();
                    view.put("type", twoMenuVo.getType());
                    if(twoMenuVo.getType().equals("view")) {
                        view.put("name", twoMenuVo.getName());
                        //H5页面地址
                        view.put("url", "http://oa.atguigu.cn#"+twoMenuVo.getUrl());
                    } else {
                        view.put("name", twoMenuVo.getName());
                        view.put("key", twoMenuVo.getMeunKey());
                    }
                    subButton.add(view);
                }
                one.put("sub_button", subButton);
            }
            buttonList.add(one);
        }
        JSONObject button = new JSONObject();
        button.put("button", buttonList);
        try {
            wxMpService.getMenuService().menuCreate(button.toJSONString());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    //删除菜单
    @SneakyThrows
    @Override
    public void removeMenu() {
        wxMpService.getMenuService().menuDelete();
    }
}
