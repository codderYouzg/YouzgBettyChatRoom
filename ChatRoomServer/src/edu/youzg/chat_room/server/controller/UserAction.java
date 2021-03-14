package edu.youzg.chat_room.server.controller;

import edu.youzg.betty.action.Actioner;
import edu.youzg.betty.action.Argument;
import edu.youzg.betty.action.Mapping;
import edu.youzg.chat_room.server.pojo.UserInfo;
import edu.youzg.chat_room.server.service.UserService;
import edu.youzg.util.exceptions.XMLIsInexistentException;

@Actioner
public class UserAction {
	private UserService userService;

	public UserAction() throws XMLIsInexistentException {
		this.userService = new UserService();
	}
	
	@Mapping("userLogin")
	public UserInfo getUserById(
			@Argument("id") String id,
			@Argument("password") String password) {
		UserInfo user = userService.getUserById(id, password);
		
		if (user == null) {
			user = new UserInfo();
			user.setId("ERROR");
		} else {
			user.setPassword(null);
		}
		
		return user;
	}

}
