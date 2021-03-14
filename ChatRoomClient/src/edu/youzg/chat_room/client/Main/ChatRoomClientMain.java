package edu.youzg.chat_room.client.Main;

import edu.youzg.betty.action.ActionBeanFactory;
import edu.youzg.chat_room.client.view.connect.ConnectToServerView;
import edu.youzg.util.exceptions.FrameIsNullException;

import java.net.UnknownHostException;

public class ChatRoomClientMain {

	public static void main(String[] args) {
		ActionBeanFactory.scanPackage("edu.youzg.chat_room.client.view");
		try {
			ConnectToServerView connectToServer = new ConnectToServerView();
			connectToServer.initClient();
			connectToServer.initView();
			connectToServer.showView();
			connectToServer.startConnect();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (FrameIsNullException e) {
			e.printStackTrace();
		}
	}

}
