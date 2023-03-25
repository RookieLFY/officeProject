package com.atguigu.auth;

import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.model.system.SysRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestMpDemo1 {
    @Autowired
    private SysRoleMapper mapper;
    //查询所有记录
    @Test
    public void getAll(){
        List<SysRole> list = mapper.selectList(null);
        System.out.println(list);
    }

    //添加操作
    @Test
    public void getAdd(){
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("角色管理员");
        sysRole.setRoleCode("role");
        sysRole.setDescription("角色管理员");
        int rows = mapper.insert(sysRole);
        System.out.println(sysRole);
        System.out.println(rows);
    }

    //修改操作
    @Test
    public void getUpdate(){
        //根据id查询
        SysRole sysRole = mapper.selectById(10);
        //设置修改值
        sysRole.setRoleName("atlfy角色管理员");
        //调用方法实现最终修改
        int rows = mapper.updateById(sysRole);
        System.out.println(sysRole);
        System.out.println(rows);
    }

    //删除操作
    @Test
    public void getDelete(){
        int rows = mapper.deleteById(10);
        System.out.println(rows);
    }

    //条件查询
    @Test
    public void getSelectQuery(){
        QueryWrapper<SysRole>  queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_name","总经理");
        List<SysRole> list = mapper.selectList(queryWrapper);
        System.out.println(list);
    }
    @Test
    public void getLambdaQuery(){
        LambdaQueryWrapper<SysRole> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysRole::getRoleName,"总经理");
        List<SysRole> list = mapper.selectList(lambdaQueryWrapper);
        System.out.println(list);
    }
}
