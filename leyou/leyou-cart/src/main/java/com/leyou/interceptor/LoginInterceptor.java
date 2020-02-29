package com.leyou.interceptor;

import com.leyou.common.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import com.leyou.pojo.UserInfo;
import com.leyou.utils.JwtUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtProperties jwtProperties;

    //定义一个线程域，存放登录对象
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //获取token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        if (StringUtils.isBlank(token)) {
            //未登录，返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        //解析token，获取用户
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

        //放入线程域
        THREAD_LOCAL.set(userInfo);

        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //关闭线程域
        THREAD_LOCAL.remove();
        super.afterCompletion(request, response, handler, ex);
    }
}
