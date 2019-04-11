package com.xie.demo.service;

import java.io.UnsupportedEncodingException;
import com.alibaba.fastjson.JSONObject;
public interface AuthService {
    public abstract String getAccessToken(String code);
    public abstract String getOpenId(String accessToken);
    public abstract String refreshToken(String code);
    public abstract String getAuthorizationUrl() throws UnsupportedEncodingException;
    public abstract JSONObject getUserInfo(String accessToken, String openId);

}