package com.atguigu.wechat.service.impl;

import com.atguigu.model.wechat.Menu;
import com.atguigu.vo.wechat.MenuVo;

import com.atguigu.wechat.mapper.MenuMapper;
import com.atguigu.wechat.service.MenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-03-12
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper,Menu> implements MenuService {
    //获取全部菜单
    @Override
    public List<MenuVo> findMenuInfo() {
        List<MenuVo> list = new ArrayList<>();
        //1：查询所有菜单list集合
        List<Menu> menuList = baseMapper.selectList(null);
        //2：查询所有一级菜单parent_id=0
        List<Menu> oneMenuList = menuList.stream().filter(menu -> menu.getParentId().longValue() == 0).collect(Collectors.toList());

        //3:一级菜单list集合遍历，得到每个一级菜单
        for(Menu oneMenu:oneMenuList){
            //一级菜单menu类型转换为menuVo
            MenuVo oneMenuVo = new MenuVo();
            BeanUtils.copyProperties(oneMenu,oneMenuVo);
            //4：获取每个一级菜单里面的二级菜单 id和parent_id比较
            List<Menu> twoMenuList = menuList.stream().filter(menu -> menu.getParentId().longValue() == oneMenu.getId()).collect(Collectors.toList());
            //5：把一级菜单下的二级菜单获取，封装一级菜单children集合里面
            List<MenuVo> children = new ArrayList<>();
            for(Menu twoMenu:twoMenuList){
                MenuVo twoMenuVo = new MenuVo();
                BeanUtils.copyProperties(twoMenu,twoMenuVo);
                children.add(twoMenuVo);
            }
            oneMenuVo.setChildren(children);
            list.add(oneMenuVo);
        }
        return list;
    }
}
