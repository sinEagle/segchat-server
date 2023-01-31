package com.sineagle.service.impl;

import com.sineagle.mapper.UsersMapper;
import com.sineagle.pojo.Users;
import com.sineagle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UsersMapper usersMapper;
    @Override
    public boolean queryUsernameIsExist(String username) {
        Users user = new Users();
        user.setUsername(username);

        Users result = usersMapper.selectOne(user);


        return result != null ? true : false;
    }
}
