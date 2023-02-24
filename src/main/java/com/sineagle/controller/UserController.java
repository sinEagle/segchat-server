package com.sineagle.controller;

import com.sineagle.enums.OperatorFriendRequestTypeEnum;
import com.sineagle.enums.SearchFriendsStatusEnum;
import com.sineagle.pojo.Users;
import com.sineagle.pojo.bo.UsersBO;
import com.sineagle.pojo.vo.FriendRequestVO;
import com.sineagle.pojo.vo.MyFriendsVO;
import com.sineagle.pojo.vo.UsersVO;
import com.sineagle.service.UserService;
import com.sineagle.utils.FastDFSClient;
import com.sineagle.utils.FileUtils;
import com.sineagle.utils.IMoocJSONResult;
import com.sineagle.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/u")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private FastDFSClient fastDFSClient;

    @PostMapping("/registerOrLogin")
    public IMoocJSONResult registerOrLogin(@RequestBody Users user) throws Exception {
        // 0. 判断用户名和密码不能为空
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
            return IMoocJSONResult.errorMsg("用户名或密码不能为空 ! ");
        }

        // 1. 判断用户名是否存在，如果存在则登录，否则进行注册
        boolean usernameIsExist = userService.queryUsernameIsExist(user.getUsername());
        Users userResult = null;
        if (usernameIsExist) {
            // 1.1 登录
            userResult = userService.queryUserForLogin(user.getUsername(),
                    MD5Utils.getMD5Str(user.getPassword()));
            if (userResult == null) {
                return IMoocJSONResult.errorMsg("用户名或密码不正确!");
            }
        } else {
            // 1.2 注册
            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));

            userResult = userService.saveUser(user);
        }

        UsersVO userVo = new UsersVO();
        BeanUtils.copyProperties(userResult, userVo);
        return IMoocJSONResult.ok(userVo);
    }
    @PostMapping("/uploadFaceBase64")
    public IMoocJSONResult uploadFaceBase64(@RequestBody UsersBO userBo) throws Exception {
        // 获取前端传过来的base64字符串，然后转化为文件对象再上传
        String base64Data = userBo.getFaceData();
        String userFacePath = "E:\\" + userBo.getUserId() + "userface64.png";
        FileUtils.base64ToFile(userFacePath, base64Data);
        // 上传文件到fastDFS
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(faceFile);
        System.out.println(url);
        // dsfsdlkfsdjlf.png
        // dsfsdlkfsdjlf_80x80.png
        // 获取缩略图的URL
        String thump = "_80x80.";
        String[] arr = url.split("\\.");
        String thumpImageUrl = arr[0] + thump + arr[1];
        // 更新用户头像
        Users user = new Users();
        user.setId(userBo.getUserId());
        user.setFaceImage(thumpImageUrl);
        user.setFaceImageBig(url);

        user = userService.updateUserInfo(user);

        return IMoocJSONResult.ok(user);
    }
    
    @PostMapping("/setNickname")
    public IMoocJSONResult setNickname(@RequestBody UsersBO userBO) throws Exception {
        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setNickname(userBO.getNickname());

        Users result = userService.updateUserInfo(user);

        return IMoocJSONResult.ok(result);
    }

    /**
     * 搜索好友接口, 根据账号做匹配查询而不是模糊查询
     */
    @PostMapping("/search")
    public IMoocJSONResult searchUser (String myUserId, String friendUsername) throws Exception {
        // 0. 判断myUserId和friendUsername不能为空
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)) {
            return IMoocJSONResult.errorMsg("");
        }
        // 前置条件：1. 搜索用户不存在，返回【无此用户】
        // 前置条件：2. 搜索账号是你自己，返回【不能添加自己】
        // 前置条件：3. 搜索的朋友已经是你的好友，返回【该用户已经是你的好友】
        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status == SearchFriendsStatusEnum.SUCCESS.status) {
            Users user = userService.queryUserInfoByUsername(friendUsername);
            UsersVO userVO = new UsersVO();
            BeanUtils.copyProperties(user, userVO);
            return IMoocJSONResult.ok(userVO);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IMoocJSONResult.errorMsg(errorMsg);
        }

    }

    @PostMapping("/addFriendRequest")
    public IMoocJSONResult addFriendRequest(String myUserId, String friendUsername) {
        // 0. 判断myUserId和friendUsername不能为空
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)) {
            return IMoocJSONResult.errorMsg("");
        }
        // 前置条件：1. 搜索用户不存在，返回【无此用户】
        // 前置条件：2. 搜索账号是你自己，返回【不能添加自己】
        // 前置条件：3. 搜索的朋友已经是你的好友，返回【该用户已经是你的好友】
        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status == SearchFriendsStatusEnum.SUCCESS.status) {
            userService.sendFriendRequest(myUserId, friendUsername);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IMoocJSONResult.errorMsg(errorMsg);
        }

        return IMoocJSONResult.ok();
    }

    @PostMapping("/queryFriendRequests")
    public IMoocJSONResult queryFriendRequests(String userId) {
        // 0. 判断是否为空
        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("");
        }

        // 1. 查询用户接受到的朋友申请
        List<FriendRequestVO> result = userService.queryFriendRequestList(userId);
        
        return IMoocJSONResult.ok(result);
    }

    /**
     * 接收方通过或忽略好友请求
     */
    @PostMapping("/operFriendRequest")
    public IMoocJSONResult operFriendRequest(String acceptUserId, String sendUserId, Integer operType) {
        // 0. acceptUserId sendUserId operType 判断不能为空
        if (StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(acceptUserId) || operType == null) {
            return IMoocJSONResult.errorMsg("");
        }
        // 1. 如果operType没有对应枚举值，则直接抛出空错误信息
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType)) ){
            return IMoocJSONResult.errorMsg("");
        }
        if (operType == OperatorFriendRequestTypeEnum.IGNORE.type) {
            // 2. 判断如果忽略好友请求，则直接删除好友请求的数据库表记录
            userService.deleteFriendRequest(sendUserId, acceptUserId);
        } else if (operType == OperatorFriendRequestTypeEnum.PASS.type) {
            // 3. 判断如果是通过好友请求，则互相增加好友记录到数据库对应的表
            // 然后删除好友请求的数据库表记录
            userService.passFriendRequest(sendUserId, acceptUserId);
        }
        return IMoocJSONResult.ok();
    }

    /**
     *  查询好友列表
     */
    @PostMapping("/myFriends")
    public IMoocJSONResult myFriends(String userId) {
        // 0. userId判断不能为空
        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("");
        }
        // 1. 数据库查询好友列表
        List<MyFriendsVO> myFriends = userService.queryMyFriends(userId);

        return IMoocJSONResult.ok(myFriends);
    }

}









