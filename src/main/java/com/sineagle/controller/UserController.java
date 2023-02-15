package com.sineagle.controller;

import com.sineagle.pojo.Users;
import com.sineagle.pojo.bo.UsersBO;
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
}
