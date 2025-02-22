package com.xxl.sso.server.controller;

import cn.hutool.core.date.DateUtil;
import com.xxl.sso.core.conf.Conf;
import com.xxl.sso.core.login.SsoWebLoginHelper;
import com.xxl.sso.core.store.SsoLoginStore;
import com.xxl.sso.core.user.XxlSsoUser;
import com.xxl.sso.core.store.SsoSessionIdHelper;
import com.xxl.sso.server.core.model.UserInfo;
import com.xxl.sso.server.core.result.ReturnT;
import com.xxl.sso.server.entity.SysUser;
import com.xxl.sso.server.service.SysUserService;
import com.xxl.sso.server.service.UserService;
import com.xxl.sso.server.utils.RandomKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * sso server (for web)
 *
 * @author xuxueli 2017-08-01 21:39:47
 */
@Controller
public class WebController {

    @Autowired
    private UserService userService;

    @Autowired
    private SysUserService sysUserService;

    @RequestMapping("/")
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {

        // login check
        XxlSsoUser xxlUser = SsoWebLoginHelper.loginCheck(request, response);

        if (xxlUser == null) {
            return "redirect:/login";
        } else {
            model.addAttribute("xxlUser", xxlUser);
            return "index";
        }
    }

    /**
     * Login page
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(Conf.SSO_LOGIN)
    public String login(Model model, HttpServletRequest request, HttpServletResponse response) {

        // login check
        XxlSsoUser xxlUser = SsoWebLoginHelper.loginCheck(request, response);

        if (xxlUser != null) {
            // success redirect
            String redirectUrl = request.getParameter(Conf.REDIRECT_URL);
            if (redirectUrl!=null && !redirectUrl.trim().isEmpty()) {
                String sessionId = SsoWebLoginHelper.getSessionIdByCookie(request);
                //String redirectUrlFinal = redirectUrl + "?" + Conf.SSO_SESSIONID + "=" + sessionId;;
                if (sessionId != null){
                    return "redirect:" + redirectUrl;
                }
            } else {
                return "redirect:http://app.deliver.com:8080";
            }
        }

        model.addAttribute("errorMsg", request.getParameter("errorMsg"));
        model.addAttribute(Conf.REDIRECT_URL, request.getParameter(Conf.REDIRECT_URL));
        return "login";
    }

    /**
     * Login
     *
     * @param request
     * @param redirectAttributes
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/doLogin")
    public String doLogin(HttpServletRequest request,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes,
                        String username,
                        String password,
                        String ifRemember) {

        boolean ifRem = (ifRemember!=null&&"on".equals(ifRemember))?true:false;

        // valid login
        ReturnT<UserInfo> result = userService.findUser(username, password);
        if (result.getCode() != ReturnT.SUCCESS_CODE) {
            redirectAttributes.addAttribute("errorMsg", result.getMsg());

            redirectAttributes.addAttribute(Conf.REDIRECT_URL, request.getParameter(Conf.REDIRECT_URL));
            return "redirect:/login";
        }

        // 1、make xxl-sso user
        XxlSsoUser xxlUser = new XxlSsoUser();
        xxlUser.setUserid(String.valueOf(result.getData().getUserid()));
        xxlUser.setUsername(result.getData().getUsername());
        xxlUser.setVersion(UUID.randomUUID().toString().replaceAll("-", ""));
        xxlUser.setExpireMinute(SsoLoginStore.getRedisExpireMinute());
        xxlUser.setExpireFreshTime(System.currentTimeMillis());


        // 2、make session id
        String sessionId = SsoSessionIdHelper.makeSessionId(xxlUser);

        // 3、login, store storeKey + cookie sessionId
        SsoWebLoginHelper.login(response, sessionId, xxlUser, ifRem);

        // 4、return, redirect sessionId
        String redirectUrl = request.getParameter(Conf.REDIRECT_URL);
        if (redirectUrl!=null && !redirectUrl.trim().isEmpty()) {
            String redirectUrlFinal = redirectUrl + "?" + Conf.SSO_SESSIONID + "=" + sessionId;
            return "redirect:" + redirectUrlFinal;
        } else {
            return "redirect:http://app.deliver.com:8080?xxl_sso_sessionid="+sessionId;
        }

    }

    /**
     * Logout
     *
     * @param request
     * @param redirectAttributes
     * @return
     */
    @RequestMapping(Conf.SSO_LOGOUT)
    public String logout(HttpServletRequest request,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {

        // logout
        SsoWebLoginHelper.logout(request, response);

        redirectAttributes.addAttribute(Conf.REDIRECT_URL, request.getParameter(Conf.REDIRECT_URL));
        return "redirect:/login";
    }

    @RequestMapping("/register")
    public String register(HttpServletRequest request, HttpServletResponse response,
                           RedirectAttributes redirectAttributes,
                           String usernam2re,
                           String password2r) {
        ReturnT<UserInfo> result = userService.findUser(usernam2re, password2r);
        if (result.getCode() == ReturnT.SUCCESS_CODE) { //已经存在该用户
            redirectAttributes.addAttribute("errorMsg", result.getMsg());
            redirectAttributes.addAttribute(Conf.REDIRECT_URL, request.getParameter(Conf.REDIRECT_URL));
            return "redirect:/login";
        }
        SysUser sysUser = new SysUser();
        sysUser.setId(RandomKit.uniqueStr());
        sysUser.setUsername(usernam2re);
        sysUser.setPassword(password2r);
        sysUser.setCreateTime(DateUtil.now());
        sysUserService.save(sysUser);
        return "redirect:www.deliver.com:9091/welcome";
    }


}
