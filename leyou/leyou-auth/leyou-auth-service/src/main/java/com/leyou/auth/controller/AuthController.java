package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.utils.CookieUtils;
import com.leyou.pojo.UserInfo;
import com.leyou.utils.JwtUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录认证
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */
    @PostMapping("accredit")
    public ResponseEntity<Void> accredit(@RequestParam("username") String username,
                                         @RequestParam("password") String password,
                                         HttpServletRequest request, HttpServletResponse response) {

        //获取token
        String token = this.authService.accredit(username, password);

        //判断
        if (StringUtils.isBlank(token)) {
            return ResponseEntity.badRequest().build();
        }

        CookieUtils.setCookie(request, response, this.jwtProperties.getCookieName(), token, this.jwtProperties.getExpire() * 60);

        return ResponseEntity.ok(null);
    }

    /**
     * 解析cookie返回用户信息
     * @param token
     * @param request
     * @param response
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> queryUser(@CookieValue("LY_TOKEN") String token,
                                              HttpServletRequest request, HttpServletResponse response) {

        try {
            //解析token
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());

            if (userInfo == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 解析成功要重新刷新token
            token = JwtUtils.generateToken(userInfo, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExpire());
            // 更新cookie中的token
            CookieUtils.setCookie(request, response, this.jwtProperties.getCookieName(), token, this.jwtProperties.getExpire() * 60);

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
