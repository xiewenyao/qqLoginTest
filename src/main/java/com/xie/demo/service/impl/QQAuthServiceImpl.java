package com.xie.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xie.demo.service.QQAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.ws.WebServiceException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
public class QQAuthServiceImpl extends DefaultAuthServiceImpl implements QQAuthService {

    private Logger logger = LoggerFactory.getLogger(QQAuthServiceImpl.class);
    //QQ 登陆页面的URL
    private final static String AUTHORIZATION_URL =
            "https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=%s";
    //获取token的URL
    private final static String ACCESS_TOKEN_URL =
            "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s";

    // 获取用户 openid 的 URL
    private static final String OPEN_ID_URL =
            "https://graph.qq.com/oauth2.0/me?access_token=%s";

    // 获取用户信息的 URL，oauth_consumer_key 为 apiKey
    private static final String USER_INFO_URL =
            "https://graph.qq.com/user/get_user_info?access_token=%s&oauth_consumer_key=%s&openid=%s";

    // 下面的属性可以通过配置读取
    @Value("${qqconf.callback-url}")
    private String CALLBACK_URL; // QQ 在登陆成功后回调的 URL，这个 URL 必须在 QQ 互联里填写过
    @Value("${qqconf.api-key}")
    private String API_KEY;         // QQ 互联应用管理中心的 APP ID
    @Value("${qqconf.api-secret}")
    private String API_SECRET;    // QQ 互联应用管理中心的 APP Key
    @Value("${qqconf.state}")
    private String STATE;             // QQ 互联的 API 接口，访问用户资料


    @Override
    public String getAccessToken(String code) {
        String url = String.format(ACCESS_TOKEN_URL, API_KEY, API_SECRET, code, CALLBACK_URL);
        String resp = getResp(url);
        if (resp.contains("access_token")) {
            Map<String, String> map = getParam(resp);
            String access_token = map.get("access_token");
            return access_token;
        } else {
            throw new WebServiceException();
        }
    }

    @Override
    public String getOpenId(String accessToken) {
        String url = String.format(OPEN_ID_URL, accessToken);
        String resp = getResp(url);
        if (resp.contains("openid")) {
            JSONObject jsonObject = ConvertToJson(resp);
            String openid = jsonObject.getString("openid");
            return openid;
        } else {
            throw new WebServiceException();
        }
    }

    @Override
    public String refreshToken(String code) {
        return null;
    }

    /**
     * @return QQ 登陆页面的 URL
     */
    @Override
    public String getAuthorizationUrl() {
        String url = String.format(AUTHORIZATION_URL, API_KEY, CALLBACK_URL, STATE);
        return url;
    }

    /**
     * @param accessToken 传入accessToken
     * @param openId      传入openId
     * @return 返回用户信息
     */
    @Override
    public JSONObject getUserInfo(String accessToken, String openId) {

        openId = getOpenId(accessToken);
        String url = String.format(USER_INFO_URL, accessToken, API_KEY, openId);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = builder.build().encode().toUri();

        String resp = getRestTemplate().getForObject(uri, String.class);
        JSONObject data = JSONObject.parseObject(resp);
        logger.error("resp = " + data);
        JSONObject result = new JSONObject();
        result.put("openId", openId);
        result.put("name", data.getString("nickname"));
        result.put("headurl", data.getString("figureurl_qq_1"));

        return result;
    }

    //由于QQ的几个接口返回类型不一样，此处是获取key-value类型的参数
    private Map<String, String> getParam(String string) {
        Map<String, String> map = new HashMap<>();
        String[] kvArray = string.split("&");
        for (int i = 0; i < kvArray.length; i++) {
            String[] kv = kvArray[i].split("=");
            map.put(kv[0], kv[1]);
        }
        return map;
    }

    //QQ接口返回类型是text/plain，此处将其转为json
    public JSONObject ConvertToJson(String string) {
        string = string.substring(string.indexOf("(") + 1);
        string = string.substring(0, string.indexOf(")"));
        logger.error("ConvertToJson s = " + string);
        JSONObject jsonObject = JSONObject.parseObject(string);
        return jsonObject;
    }

    /**
     * @param url 传入的url
     * @return 经过处理返回String 的 resp
     */
    public String getResp(String url) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = builder.build().encode().toUri();
        String resp = getRestTemplate().getForObject(uri, String.class);
        logger.error("getAccessToken resp = " + resp);
        return resp;
    }

    public String getCALLBACK_URL() {
        return CALLBACK_URL;
    }

    public void setCALLBACK_URL(String CALLBACK_URL) {
        this.CALLBACK_URL = CALLBACK_URL;
    }

    public String getAPI_KEY() {
        return API_KEY;
    }

    public void setAPI_KEY(String API_KEY) {
        this.API_KEY = API_KEY;
    }

    public String getAPI_SECRET() {
        return API_SECRET;
    }

    public void setAPI_SECRET(String API_SECRET) {
        this.API_SECRET = API_SECRET;
    }

    public String getSCOPE() {
        return STATE;
    }

    public void setSCOPE(String SCOPE) {
        this.STATE = SCOPE;
    }


}
