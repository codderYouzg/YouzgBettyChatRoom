package edu.youzg.chat_room.server.Main;

import edu.youzg.betty.action.ActionBeanFactory;
import edu.youzg.chat_room.server.view.ChatRoomServerView;
import edu.youzg.util.exceptions.FrameIsNullException;

public class ChatRoomServerMain {

	public static void main(String[] args) {
		try {
			ActionBeanFactory.scanPackage("edu.youzg.chat_room.server.controller");
			ChatRoomServerView serverView = new ChatRoomServerView()
					.initServer();
			serverView.showView();
		} catch (FrameIsNullException e) {
			e.printStackTrace();
		}
	}

}
