package com.oszero.deliver.admin.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.oszero.deliver.admin.model.sso.ReturnT;
import com.oszero.deliver.admin.model.sso.XxlSsoUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;

/**
 * @author: black788
 * @date: 2025/1/17 19:58
 */
@Controller
@RequestMapping("/sso")
public class SsoContorller {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 拿到token,去http的post请求 "http://xxlssoserver.com:8087/xxl-sso-server/logincheck"
     * 请求体参数只有一个：sessionId 和 redirectUrl
     *
     * 转换回来的响应体内容为 ReturnT<XxlSsoUser>
     *     检查 ReturnT的code 是否为200,如果是,重定向到redirectUrl
     *                       如果是500,重定向到登录页 http://xxlssoserver.com:8087/xxl-sso-server/
     */
    @PostMapping("/login")
    @ResponseBody
    public JSONObject login(@RequestParam String sessionId, @RequestParam String redirectUrl) {
        JSONObject json = new JSONObject();
        json.append("redirectUrl","http://xxlssoserver.com:8087/xxl-sso-server/login");
        try {
            String url = "http://xxlssoserver.com:8087/xxl-sso-server/app/logincheck?sessionId="+sessionId;
            HashMap<String,Object> params = new HashMap<>();
            params.put("sessionId", sessionId);

            ReturnT<XxlSsoUser> response = restTemplate.getForObject(url, ReturnT.class,params);

            // 假设返回的响应体是一个JSON字符串，包含code字段
            // 这里假设使用了一个简单的JSON解析方式，实际项目中可能需要使用Jackson或其他库
            if (response != null){
                if (response.getCode() == 200) {
                    json.append("redirectUrl",redirectUrl);
                    return json;
                } else if (response.getCode() == 500) {
                    return json;
                }
            }

            // 默认情况下重定向到登录页
            return json;
        } catch (Exception e) {
            return json;
        }
    }
}
