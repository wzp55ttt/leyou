package com.leyou.user.service;

import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.util.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;


    /**
     * 检验数据是否可用
     * @param data 检测的数据
     * @param type 检测的类型
     * @return
     */
    public Boolean checkUserData(String data, Integer type) {

        User user = new User();
        if (type == 1) {
            user.setUsername(data);
        } else if (type == 2) {
            user.setPhone(data);
        } else {
            return null;
        }

        return this.userMapper.selectCount(user) == 0;
    }

    /**
     * 注册
     * @param user
     */
    public void userRegister(User user) {

        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));
        user.setId(null);
        user.setCreated(new Date());
        this.userMapper.insertSelective(user);
    }

    public User queryUser(String username, String password) {

        //根据用户名查询
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);

        //判断是否为空
        if (user == null) {
            return null;
        }
        //成为密码盐
        password = CodecUtils.md5Hex(password, user.getSalt());

        //检验密码
        if (StringUtils.equals(user.getPassword(),password)) {
            return user;
        }

        return null;
    }
}
