package com.dragon.leon.utils;

import com.dragon.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

public class MenuHelper {
    public static List<SysMenu> builderTrees(List<SysMenu> allLists) {
        // 1. 创建list集合，用于最终返回数据
        List<SysMenu> trees = new ArrayList<>();
        // 2. 对传入的所有菜单数据进行遍历
        for (SysMenu list : allLists) {
            // 3.递归入口进入
            if(list.getParentId().longValue() == 0)
                // 4. 对递归的方法传入的每个对象和所有菜单数据
                trees.add(getNodeTrees(list,allLists));
        }
        return trees;
    }

    private static SysMenu getNodeTrees(SysMenu list, List<SysMenu> allLists) {
        list.setChildren(new ArrayList<>());
        for (SysMenu tree : allLists) {
            // 1. 对传入的对象的id和所有菜单数据元素的parent_id进行比较 -> 遍历
            if(list.getId().longValue() == tree.getParentId().longValue()){
                // 2. 对传入的对象的children进行判空，如果为空设置children的值为list集合
                if(list.getChildren() == null)
                    list.setChildren(new ArrayList<>());
                // 3. 传入的对象的children属性进行递归调用，保存节点数据
                list.getChildren().add(getNodeTrees(tree,allLists));
            }
        }
        return list;
    }
}
