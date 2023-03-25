package com.atguigu.auth.utils;

import com.atguigu.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

public class MenuHelper {
    //用递归方法构建菜单
    public static List<SysMenu> bulidTree(List<SysMenu> sysMenuList) {
        List<SysMenu> trees = new ArrayList<>();
        for(SysMenu sysMenu:sysMenuList){
            //递归入口进入
            //parent的值等于0 就是入口
            if(sysMenu.getParentId().longValue()==0){
                trees.add(getChildren(sysMenu,sysMenuList));
            }
        }
        return trees;
    }
    public static SysMenu getChildren(SysMenu sysMenu,List<SysMenu> sysMenuList){
        sysMenu.setChildren(new ArrayList<SysMenu>());
        //遍历所有菜单数据，判断id和parentId对应关系
        for(SysMenu it:sysMenuList){
            if(sysMenu.getId().longValue()==it.getParentId().longValue()){
                if (sysMenu.getChildren() == null) {
                    sysMenu.setChildren(new ArrayList<>());
                }
                sysMenu.getChildren().add(getChildren(it,sysMenuList));
            }
        }
        return sysMenu;
    }
}
