package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.pojo.UserInfo;
import com.leyou.user.pojo.User;
import com.leyou.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 效验返回token
     * @param username
     * @param password
     * @return
     */
    public String accredit(String username, String password) {

        //查询用户
        User user = userClient.queryUser(username, password);

        //判断是否存在
        if (user == null) {
            return null;
        }

        //返回token
        try {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            return JwtUtils.generateToken(userInfo, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
