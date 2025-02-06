package com.xxl.sso.core.filter;

import com.xxl.sso.core.conf.Conf;
import com.xxl.sso.core.entity.ReturnT;
import com.xxl.sso.core.login.SsoTokenLoginHelper;
import com.xxl.sso.core.path.impl.AntPathMatcher;
import com.xxl.sso.core.user.XxlSsoUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * app sso filter
 *
 * @author xuxueli 2018-04-08 21:30:54
 */
public class XxlSsoTokenFilter extends HttpServlet implements Filter {
    private static Logger logger = LoggerFactory.getLogger(XxlSsoTokenFilter.class);

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private String ssoServer;
    private String logoutPath;
    private String excludedPaths;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //三个配置项
        //认证服务中心路径
        ssoServer = filterConfig.getInitParameter(Conf.SSO_SERVER);
        //登出路径
        logoutPath = filterConfig.getInitParameter(Conf.SSO_LOGOUT_PATH);
        //排除路径: 地址以逗号分隔
        excludedPaths = filterConfig.getInitParameter(Conf.SSO_EXCLUDED_PATHS);

        logger.info("XxlSsoTokenFilter init.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // make url 获取请求路径
        String servletPath = req.getServletPath();

        // 是否在需要排除的路径内
        if (excludedPaths!=null && !excludedPaths.trim().isEmpty()) {
            for (String excludedPath:excludedPaths.split(",")) {
                String uriPattern = excludedPath.trim();

                // 支持ANT表达式,例:
                if (antPathMatcher.match(uriPattern, servletPath)) {
                    // 在需要排除的路径内，直接放行
                    chain.doFilter(request, response);
                    return;
                }

            }
        }

        // 登出路径
        if (logoutPath!=null
                && !logoutPath.trim().isEmpty()
                && logoutPath.equals(servletPath)) {

            // 匹配登出路径
            SsoTokenLoginHelper.logout(req);

            //-- 可定制登出逻辑,可以是重定向到登录页---
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().println("{\"code\":"+ReturnT.SUCCESS_CODE+", \"msg\":\"\"}");
            //--------
            return;
        }

        // 不在 排除路径 和 登出路径 内
        // 登录校验
        XxlSsoUser xxlUser = SsoTokenLoginHelper.loginCheck(req);
        if (xxlUser == null) {

            // 登录失败逻辑(可定制)
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().println("{\"code\":"+Conf.SSO_LOGIN_FAIL_RESULT.getCode()+", \"msg\":\""+ Conf.SSO_LOGIN_FAIL_RESULT.getMsg() +"\"}");
            return;
        }

        // 将用户信息放入request
        request.setAttribute(Conf.SSO_USER, xxlUser);


        // already login, allow
        chain.doFilter(request, response);
        return;
    }


}
