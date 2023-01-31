package com.sineagle.mapper;

import java.util.List;

import com.sineagle.pojo.Users;
import com.sineagle.pojo.vo.FriendRequestVO;
import com.sineagle.pojo.vo.MyFriendsVO;
import com.sineagle.utils.MyMapper;

public interface UsersMapperCustom extends MyMapper<Users> {
	
	public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);
	
	public List<MyFriendsVO> queryMyFriends(String userId);
	
	public void batchUpdateMsgSigned(List<String> msgIdList);
	
}