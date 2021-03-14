package edu.youzg.chat_room.server.service;

import edu.youzg.chat_room.server.dao.UserDao;
import edu.youzg.chat_room.server.pojo.UserInfo;
import edu.youzg.util.exceptions.XMLIsInexistentException;

public class UserService {
	private UserDao userDao;
	
	public UserService() throws XMLIsInexistentException {
		this.userDao = new UserDao();
	}

	public UserInfo getUserById(String id, String password) {
		UserInfo user = userDao.getUser(id);
		
		if (user == null || !password.equals(user.getPassword())) {
			return null;
		}
		
		return user;
	}
	
}