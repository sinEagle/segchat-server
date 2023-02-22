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

    /**
     * 搜索朋友的前置条件
     */
    public Integer preconditionSearchFriends(String myUserId, String friendUsername);

    /**
     *  根据用户名查询用户对象
     */
    public Users queryUserInfoByUsername(String username);

}
