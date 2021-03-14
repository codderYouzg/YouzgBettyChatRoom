package edu.youzg.chat_room.server.dao;

import edu.youzg.util.XMLParser;
import edu.youzg.util.exceptions.XMLIsInexistentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.youzg.chat_room.server.pojo.UserInfo;

public class UserDao {
	private Document document;

	public UserDao() throws XMLIsInexistentException {
		this.document = XMLParser.getDocument("/userInfo.xml");
	}

	public UserInfo getUser(String id) {
		UserInfo user = new UserInfo();
		new XMLParser() {
			@Override
			public void dealElement(Element element, int index) {
				String strId = element.getAttribute("id");
				if (!strId.equals(id)) {
					return;
				}

				String nick = element.getAttribute("nick");
				String password = element.getAttribute("password");
				user.setId(strId);
				user.setNick(nick);
				user.setPassword(password);
			}
		}.parseTag(this.document, "user");
		
		if (user.getId() == null) {
			return null;
		}
		
		return user;
	}
	
}