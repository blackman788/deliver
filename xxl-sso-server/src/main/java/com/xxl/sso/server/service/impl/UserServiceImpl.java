package com.xxl.sso.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xxl.sso.server.core.model.UserInfo;
import com.xxl.sso.server.core.result.ReturnT;
import com.xxl.sso.server.entity.SysUser;
import com.xxl.sso.server.service.SysUserService;
import com.xxl.sso.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    SysUserService sysUserService;


    @Override
    public ReturnT<UserInfo> findUser(String username, String password) {

        if (username==null || username.trim().isEmpty()) {
            return new ReturnT<UserInfo>(ReturnT.FAIL_CODE, "请输入用户名");
        }
        if (password==null || password.trim().isEmpty()) {
            return new ReturnT<UserInfo>(ReturnT.FAIL_CODE, "请输入密码");
        }

        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().eq("username",username).eq("password",password);
        SysUser sysUser = sysUserService.getOne(queryWrapper);
        if (sysUser!=null) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserid(sysUser.getId());
            userInfo.setUsername(sysUser.getUsername());
            userInfo.setPassword(sysUser.getPassword());
            return new ReturnT<UserInfo>(userInfo);
        }

        return new ReturnT<UserInfo>(ReturnT.FAIL_CODE, "请输入正确的用户名和密码");
    }


}
