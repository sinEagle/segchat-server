package com.sineagle.service.impl;

import com.sineagle.mapper.UsersMapper;
import com.sineagle.pojo.Users;
import com.sineagle.service.UserService;
import com.sineagle.utils.FastDFSClient;
import com.sineagle.utils.FileUtils;
import com.sineagle.utils.QRCodeUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {
        Users user = new Users();
        user.setUsername(username);

        Users result = usersMapper.selectOne(user);


        return result != null ? true : false;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String pwd) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", pwd);

        Users result = usersMapper.selectOneByExample(userExample);

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users saveUser(Users user) throws IOException {
        String userId = sid.nextShort();
        // 为每个用户生成一个唯一的二维码
        String qrcodePath = "E://user/" + userId + "qrcode.png";
        // segchat_qrcode:[username]
        qrCodeUtils.createQRCode(qrcodePath, "setchat_qrcode:" + user.getUsername());
        MultipartFile qrCodeFile = FileUtils.fileToMultipart(qrcodePath);
        String qrCodeUrl =  fastDFSClient.uploadQRCode(qrCodeFile);

        user.setQrcode(qrCodeUrl);
        user.setId(userId);
        usersMapper.insert(user);

        return user;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users updateUserInfo(Users user) {
        usersMapper.updateByPrimaryKeySelective(user);
        return queryUsersById(user.getId());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Users queryUsersById(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }
}
