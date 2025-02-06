package com.xxl.sso.server.controller;

import cn.hutool.json.JSONObject;
import com.xxl.sso.core.login.SsoTokenLoginHelper;
import com.xxl.sso.core.store.SsoLoginStore;
import com.xxl.sso.core.user.XxlSsoUser;
import com.xxl.sso.core.store.SsoSessionIdHelper;
import com.xxl.sso.server.core.model.UserInfo;
import com.xxl.sso.server.core.result.ReturnT;
import com.xxl.sso.server.entity.SsoToken;
import com.xxl.sso.server.entity.SysUser;
import com.xxl.sso.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * sso server (for app)
 *
 * @author xuxueli 2018-04-08 21:02:54
 */
@Controller
@RequestMapping("/app")
public class AppController {

    @Autowired
    private UserService userService;


    /**
     * Login
     *
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/login")
    @ResponseBody
    public ReturnT<JSONObject> login(@RequestBody SysUser sysUser) {
        JSONObject json = new JSONObject();
        // valid login
        ReturnT<UserInfo> result = userService.findUser(sysUser.getUsername(), sysUser.getPassword());
        if (result.getCode() != ReturnT.SUCCESS_CODE) {
            json.put("code",result.getCode());
            json.put("msg",result.getMsg());
            return new ReturnT<JSONObject>(json);
        }

        // 1、make xxl-sso user
        XxlSsoUser xxlUser = new XxlSsoUser();
        xxlUser.setUserid(String.valueOf(result.getData().getUserid()));
        xxlUser.setUsername(result.getData().getUsername());
        xxlUser.setVersion(UUID.randomUUID().toString().replaceAll("-", ""));
        xxlUser.setExpireMinute(SsoLoginStore.getRedisExpireMinute());
        xxlUser.setExpireFreshTime(System.currentTimeMillis());


        // 2、generate sessionId + storeKey
        String sessionId = SsoSessionIdHelper.makeSessionId(xxlUser);

        // 3、login, store storeKey
        SsoTokenLoginHelper.login(sessionId, xxlUser);
        json.put("code", ReturnT.SUCCESS_CODE);
        json.put("sessionId", sessionId);
        json.put("redirectUrl", "http://app.deliver.com:8080?xxl_sso_sessionid="+sessionId);

        // 4、return sessionId
        return new ReturnT<JSONObject>(json);
    }


    /**
     * Logout
     *
     * @param sessionId
     * @return
     */
    @RequestMapping("/logout")
    @ResponseBody
    public ReturnT<JSONObject> logout(@RequestBody SsoToken ssoToken) {
        JSONObject json = new JSONObject();
        // logout, remove storeKey
        SsoTokenLoginHelper.logout(ssoToken.getSessionId());
        json.put("code", ReturnT.SUCCESS_CODE);
        json.put("redirectUrl", "http://sso.deliver.com:8087/xxl-sso-server/");
        return new ReturnT<JSONObject>(json) ;
    }

    /**
     * logincheck
     *
     * @param sessionId
     * @return
     */
    @RequestMapping("/logincheck")
    @ResponseBody
    public ReturnT<JSONObject> logincheck(@RequestBody SsoToken ssoToken) {
        JSONObject json = new JSONObject();
        // logout
        XxlSsoUser xxlUser = SsoTokenLoginHelper.loginCheck(ssoToken.getSessionId());
        if (xxlUser == null) {
            json.put("code", ReturnT.FAIL_CODE);
            json.put("msg", "sso not login.");
            return new ReturnT<JSONObject>(ReturnT.FAIL_CODE, "sso not login.");
        }
        json.put("code", ReturnT.SUCCESS_CODE);
        json.put("sessionId", ssoToken.getSessionId());
        json.put("redirectUrl", ssoToken.getRedirectUrl());
        json.put("username", xxlUser.getUsername());
        json.put("userid", xxlUser.getUserid());
        return new ReturnT<JSONObject>(json);
    }

}
