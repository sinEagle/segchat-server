package com.sineagle.service.impl;

import com.sineagle.enums.SearchFriendsStatusEnum;
import com.sineagle.mapper.FriendsRequestMapper;
import com.sineagle.mapper.MyFriendsMapper;
import com.sineagle.mapper.UsersMapper;
import com.sineagle.pojo.FriendsRequest;
import com.sineagle.pojo.MyFriends;
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
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private MyFriendsMapper myFriendsMapper;

    @Autowired
    private FriendsRequestMapper friendsRequestMapper;

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

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUsername) {
        // 1. 搜索用户不存在，返回【无此用户】
        Users user = queryUserInfoByUsername(friendUsername);

        if (user == null) {
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        // 2. 搜索账号是你自己，返回【不能添加自己】
        if (user.getId().equals(myUserId)) {
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        // 3. 搜索的朋友已经是你的好友，返回【该用户已经是你的好友】
        Example mfe = new Example(MyFriends.class);
        Example.Criteria mfc = mfe.createCriteria();
        mfc.andEqualTo("myUserId", myUserId);
        mfc.andEqualTo("myFriendUserId", user.getId());
        MyFriends myFriendRel = myFriendsMapper.selectOneByExample(mfe);
        if (myFriendRel != null) {
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }

        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserInfoByUsername(String username) {
        Example ue = new Example(Users.class);
        Example.Criteria uc = ue.createCriteria();
        uc.andEqualTo("username", username);
        return usersMapper.selectOneByExample(ue);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {
        // 根据用户名把朋友信息查询出来
        Users friend = queryUserInfoByUsername(friendUsername);
        // 1. 查询发送好友请求记录表
        Example fre = new Example(FriendsRequest.class);
        Example.Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId", myUserId);
        frc.andEqualTo("acceptUserId", friend.getId());
        FriendsRequest friendsRequest = friendsRequestMapper.selectOneByExample(fre);
        if (friendsRequest == null) {
            // 2. 如果不是你的好友，并且好友记录没有添加，则新增好友请求记录
            String requestId = sid.nextShort();

            FriendsRequest request = new FriendsRequest();
            request.setId(requestId);
            request.setSendUserId(myUserId);
            request.setAcceptUserId(friend.getId());
            request.setRequestDateTime(new Date());
            friendsRequestMapper.insert(request);
        }

    }

}
