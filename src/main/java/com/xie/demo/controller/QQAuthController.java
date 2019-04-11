package com.xie.demo.controller;
import com.alibaba.fastjson.JSONObject;
import com.xie.demo.service.QQAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/qq")
public class QQAuthController {
    @Autowired
    private QQAuthService qqAuthService;

    //如果前端没处理可以使用
    // 访问登陆页面，然后会跳转到 QQ 的登陆页面
    @RequestMapping("/qqLoginPage")
    public String qqLoginPage() throws Exception {
        String uri = qqAuthService.getAuthorizationUrl();
        return "redirect:"+uri;
    }

    //qq授权后会回调此方法，并将code传过来
    @RequestMapping("/qqLogin")
    @ResponseBody
    public Object qqLogin(String code,String state, HttpServletResponse response) {
        //根据code获取token
        String accessToken = qqAuthService.getAccessToken(code);
        // 保存 accessToken 到 cookie，过期时间为 30 天，便于以后使用
        Cookie cookie = new Cookie("accessToken", accessToken);
        cookie.setMaxAge(60 * 24 * 30);
        response.addCookie(cookie);
        //通过qqAuthService类的getOpenId方法得到openId。
        String openId = qqAuthService.getOpenId(accessToken);
        //得到qq用户信息
        JSONObject jsonObject=qqAuthService.getUserInfo(accessToken,openId);
        return jsonObject;
    }

}

