package edu.youzg.chat_room.client.pojo;

import java.util.HashMap;
import java.util.Map;

public class FriendPool {
	private Map<String, UserInfo> userPool;
	
	public FriendPool() {
		this.userPool = new HashMap<>();
	}

	public void addFriend(UserInfo user) {
		userPool.put(user.getId(), user);
	}
	
	public void removeFriend(UserInfo user) {
		userPool.remove(user.getId());
	}
	
	public UserInfo getUserById(String userId) {
		return userPool.get(userId);
	}
	
}