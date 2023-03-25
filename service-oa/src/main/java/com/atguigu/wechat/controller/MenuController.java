package com.atguigu.wechat.controller;


import com.atguigu.common.result.Result;
import com.atguigu.model.wechat.Menu;
import com.atguigu.vo.wechat.MenuVo;
import com.atguigu.wechat.service.MenuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜单 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2023-03-12
 */
@RestController
@RequestMapping("/admin/wechat/menu")
public class MenuController {
    @Autowired
    private MenuService menuService;
    //@PreAuthorize("hasAuthority('bnt.menu.list')")
    @ApiOperation(value = "获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        Menu menu = menuService.getById(id);
        return Result.ok(menu);
    }

    //@PreAuthorize("hasAuthority('bnt.menu.add')")
    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody Menu menu) {
        menuService.save(menu);
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.menu.update')")
    @ApiOperation(value = "修改")
    @PutMapping("update")
    public Result updateById(@RequestBody Menu menu) {
        menuService.updateById(menu);
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.menu.remove')")
    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        menuService.removeById(id);
        return Result.ok();
    }
    @ApiOperation(value = "获取全部菜单")
    @GetMapping("findMenuInfo")
    public Result findMenuInfo() {
        List<MenuVo> menuList = menuService.findMenuInfo();
        return Result.ok(menuList);
    }

}

