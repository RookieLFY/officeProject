package com.atguigu.auth.controller;

import com.atguigu.auth.service.SysRoleService;
import com.atguigu.common.config.exception.GuiGuException;
import com.atguigu.common.result.Result;
import com.atguigu.model.system.SysRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.atguigu.vo.system.SysRoleQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "角色管理接口")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {
    //http://localhost:8800/admin/system/sysRole/findAll


    //注入service
    @Autowired
    private SysRoleService sysRoleService;

    //1查询所有角色和当前用户所属角色
    @ApiOperation("获取角色")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId){
       Map<String,Object> map = sysRoleService.findRoleDataByUserId(userId);
       return Result.ok(map);
    }
    //2为用户分配角色
    @ApiOperation("为用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginRoleVo assginRoleVo){
        sysRoleService.doAssign(assginRoleVo);
        return Result.ok();

    }

    //查询所有角色
/*    @GetMapping("/findAll")
    public List<SysRole> findAll(){
        List<SysRole> list = sysRoleService.list();
        return list;
    }*/

    //完成了统一返回数据的结果
    @ApiOperation("查询所有角色")
    @GetMapping("/findAll")
    public Result findAll(){
        List<SysRole> list = sysRoleService.list();
        try {
            int i = 1/0;
        }catch (Exception e){
            throw new GuiGuException(2001,"执行可自定义异常类...");
        }
        return Result.ok(list);
    }

    //条件分页查询
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("条件分页查询")
    //page：代表当前页；limit：代表每页显示记录数
    @GetMapping("{page}/{limit}")
    public Result pageQueryRole(@PathVariable Long page,
                                @PathVariable Long limit,
                                SysRoleQueryVo sysRoleQueryVo){
        //调用service里面的方法实现
        //1:创建一个page对象，传递分页相关参数
        Page<SysRole> pageParam = new Page<>(page,limit);

        //2：封装条件，判断条件值是否为空，如果不为空，则调用方法进行封装
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if(!StringUtils.isEmpty(roleName)){
            wrapper.like(SysRole::getRoleName,roleName);
        }
        IPage<SysRole> pageMode1 = sysRoleService.page(pageParam, wrapper);
        return Result.ok(pageMode1);
    }

    //添加角色
    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation("添加角色")
    @PostMapping("save")
    public Result save(@RequestBody SysRole sysRole){
        boolean is_success = sysRoleService.save(sysRole);
        if(is_success){
            return Result.ok();
        }else
            return Result.fail();
    }

    //修改角色-根据id查询
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("修改角色-根据id查询")
    @GetMapping("getUpdate/{id}")
    public Result getUpdate(@PathVariable Long id){
        SysRole sysRole = sysRoleService.getById(id);
        return Result.ok(sysRole);
    }

    //修改角色
    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation("修改角色-最终修改")
    @PutMapping("updateFinally")
    public Result updateFinally(@RequestBody SysRole sysRole){
        boolean is_success = sysRoleService.updateById(sysRole);
        if(is_success){
            return Result.ok();
        }else
            return Result.fail();
    }

    //根据id删除
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("根据id删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        boolean is_success = sysRoleService.removeById(id);
        if(is_success){
            return Result.ok();
        }else
            return Result.fail();
    }

    //批量删除
    //前端可传入json的数组【1、2、3】类似这种
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("批量删除")
    @DeleteMapping("delete")
    public Result delete(@RequestBody List<Long> idList){
        boolean is_success = sysRoleService.removeByIds(idList);
        if(is_success){
            return Result.ok();
        }else
            return Result.fail();
    }
}
