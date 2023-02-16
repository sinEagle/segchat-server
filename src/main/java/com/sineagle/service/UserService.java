package com.sineagle.service;

import com.sineagle.pojo.Users;

import java.io.IOException;

public interface UserService {
    /**
     * 判断用户名是否存在
     */
    public boolean queryUsernameIsExist(String username);

    /**
     * 查询用户是否存在
     */
    public Users queryUserForLogin(String username, String pwd);


    /**
     * 用户注册
     */
    public Users saveUser(Users user) throws IOException;

    /**
     *  修改用户记录
     */
    public Users updateUserInfo(Users user);

}
