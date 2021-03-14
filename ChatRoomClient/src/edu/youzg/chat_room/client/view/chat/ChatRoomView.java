package edu.youzg.chat_room.client.view.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.gson.Gson;
import edu.youzg.betty.core.Client;
import edu.youzg.betty.core.ClientActionAdapter;
import edu.youzg.chat_room.client.pojo.FriendPool;
import edu.youzg.chat_room.client.pojo.UserInfo;
import edu.youzg.chat_room.client.protocol.ChatMessage;
import edu.youzg.chat_room.client.protocol.EAppCommand;
import edu.youzg.util.ArgumentMaker;
import edu.youzg.util.ISwingHelper;
import edu.youzg.util.PromptBoxTool;
import edu.youzg.util.exceptions.FrameIsNullException;

public class ChatRoomView implements ISwingHelper {
	private Client client;
	
	private JFrame jfrmChatRoomView;
	private JTextArea jtatSystemMessage;
	private JTextArea jtatChat;
	private DefaultListModel<UserInfo> dlmFriendList;
	private JList<UserInfo> jlstFriendList;
	private JLabel jlblCurrentFriend;
	private JTextField jtxtSpeakContext;
	private JButton jbtnSend;
	
	private UserInfo me;
	private UserInfo all;
	
	private FriendPool friendPool;
	private Gson gson;
	
	private static final String allId = "00000000";

	public ChatRoomView(Client client,  UserInfo me) {
		this.gson = ArgumentMaker.gson;
		this.friendPool = new FriendPool();
		
		this.all = new UserInfo();
		this.all.setId(allId);
		this.all.setNick("所有人");
		
		this.client = client;
		this.client.setClientAction(new ChatRoomAction());
		this.me = me;
	}

	@Override
	public void reinit() {
		dlmFriendList.addElement(all);
		jlstFriendList.setSelectedIndex(0);
		jlblCurrentFriend.setText(all.getNick());
		// 向其他聊天室好友发送“我来了”的消息
		Gson gson = ArgumentMaker.gson;
		
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setCommand(EAppCommand.I_AM_COMMING);
		chatMessage.setMessage(gson.toJson(me));
		client.toOther(gson.toJson(chatMessage));
	}

	private void showSystemMessage(String message) {
		jtatSystemMessage.append(message + "\n");
		jtatSystemMessage.setCaretPosition(jtatSystemMessage.getText().length());
	}
	
	private void showChatMessage(String message) {
		jtatChat.append(message + "\n");
		jtatChat.setCaretPosition(jtatChat.getText().length());
	}

	@Override
	public void dealEvent() {
		jfrmChatRoomView.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				client.offline();
			}
		});
		
		jlstFriendList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					UserInfo curFriend = jlstFriendList.getSelectedValue();
					if (curFriend == null) {
						jlstFriendList.setSelectedIndex(0);
						curFriend = jlstFriendList.getSelectedValue();
					}
					jlblCurrentFriend.setText(curFriend.toString());
				}
			}
		});
		
		jbtnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String speakMessage = jtxtSpeakContext.getText();
				jtxtSpeakContext.setText("");
				if (speakMessage.length() <= 0) {
					PromptBoxTool.showError(jfrmChatRoomView, "不能发送空消息！");
					return;
				}
				
				UserInfo curFriend = jlstFriendList.getSelectedValue();
				String orgNick = jlblCurrentFriend.getText();
				if (!curFriend.getNick().equals(orgNick)) {
					PromptBoxTool.showError(jfrmChatRoomView, "好友[" + orgNick + "]已经不在聊天室！");
					return;
				}
				
				ChatMessage chatMessage = new ChatMessage();
				String message = new ArgumentMaker()
						.add("friend", me)
						.add("message", speakMessage)
						.toString();
				if (curFriend.getId().equals(allId)) {
					chatMessage.setCommand(EAppCommand.TO_OTHER);
					chatMessage.setMessage(message);
					client.toOther(gson.toJson(chatMessage));
					
					showChatMessage("你对大家喊道:" + speakMessage);
				} else {
					chatMessage.setCommand(EAppCommand.TO_ONE);
					chatMessage.setMessage(message);
					client.toOne(curFriend.getNetId(), gson.toJson(chatMessage));
					
					showChatMessage("你悄悄地对[" + curFriend + "]说道:" + speakMessage);
				}
			}
		});
	}

	@Override
	public JFrame getFrame() {
		return jfrmChatRoomView;
	}

	class ChatRoomAction extends ClientActionAdapter {
		
		public ChatRoomAction() {
		}

		@Override
		public boolean confirmOffline() {
			int choice = PromptBoxTool.getChoice(jfrmChatRoomView, "是否下线？", JOptionPane.YES_NO_OPTION);
			
			return choice == JOptionPane.YES_OPTION;
		}

		@Override
		public void beforeOffline() {
			client.sendMessageToServer("用户[" + me + "]离开聊天室");
			// 向其他好友发送“本尊下线”的消息
			ChatMessage leaveMessage = new ChatMessage();
			leaveMessage.setCommand(EAppCommand.I_AM_GONE);
			leaveMessage.setMessage(gson.toJson(me));
			
			client.toOther(gson.toJson(leaveMessage));
		}

		@Override
		public void afterOffline() {
			try {
				exitView();
			} catch (FrameIsNullException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void serverAbnormalDrop() {
			PromptBoxTool.showError(jfrmChatRoomView, "服务器异常宕机，服务停止！");
			try {
				exitView();
			} catch (FrameIsNullException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void dealToOne(String sourceId, String message) {
			ChatMessage chatMessage = gson.fromJson(message, ChatMessage.class);
			EAppCommand command = chatMessage.getCommand();
			String parameter = chatMessage.getMessage();
			
			switch (command) {
			case I_AM_HERE:
				UserInfo friend = gson.fromJson(parameter, UserInfo.class);
				friendPool.addFriend(friend);
				dlmFriendList.addElement(friend);
				break;
			case TO_ONE:
				ArgumentMaker argument = new ArgumentMaker(parameter);
				UserInfo speaker = (UserInfo) argument.getValue("friend", UserInfo.class);
				String speakContext = (String) argument.getValue("message", String.class);
				
				showChatMessage("[" + speaker + "]对你悄悄地说:" + speakContext);
				break;
			default:
				break;
			}
		}

		@Override
		public void dealToOther(String sourceId, String message) {
			ChatMessage chatMessage = gson.fromJson(message, ChatMessage.class);
			EAppCommand command = chatMessage.getCommand();
			String parameter = chatMessage.getMessage();
			
			switch (command) {
			case I_AM_COMMING:
				UserInfo newFriend = gson.fromJson(parameter, UserInfo.class);
				friendPool.addFriend(newFriend);
				dlmFriendList.addElement(newFriend);
				showSystemMessage("好友[" + newFriend + "]进入聊天室");
				
				ChatMessage echoMessage = new ChatMessage();
				echoMessage.setCommand(EAppCommand.I_AM_HERE);
				echoMessage.setMessage(gson.toJson(me));
				client.toOne(sourceId, gson.toJson(echoMessage));
				break;
			case I_AM_GONE:
				UserInfo leaveFriend = gson.fromJson(parameter, UserInfo.class);
				friendPool.removeFriend(leaveFriend);
				showSystemMessage("好友[" + leaveFriend + "]离开聊天室");
				
				UserInfo curFriend = jlstFriendList.getSelectedValue();
				dlmFriendList.removeElement(leaveFriend);
				if (curFriend.equals(leaveFriend)) {
					jlstFriendList.setSelectedIndex(0);
					jlblCurrentFriend.setText(all.toString());
					PromptBoxTool.showWarnning(jfrmChatRoomView, "好友[" + leaveFriend + "]已经离开");
				}
				break;
			case TO_OTHER:
				ArgumentMaker argument = new ArgumentMaker(parameter);
				UserInfo speaker = (UserInfo) argument.getValue("friend", UserInfo.class);
				String speakContext = (String) argument.getValue("message", String.class);
				
				showChatMessage("[" + speaker + "]对大家喊道:" + speakContext);
				break;
			default:
				break;
			}
		}
		
		@Override
		public void serverForcedown() {
			PromptBoxTool.showMessage(jfrmChatRoomView, "服务器强制宕机，服务停止！");
			try {
				exitView();
			} catch (FrameIsNullException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void init() {
		jfrmChatRoomView = new JFrame("右转哥粉丝聊天室");
		jfrmChatRoomView.setDefaultCloseOperation(
				JFrame.DO_NOTHING_ON_CLOSE);
		jfrmChatRoomView.setSize(800, 600);
		jfrmChatRoomView.setLocationRelativeTo(null);
		
		Container container = jfrmChatRoomView.getContentPane();
		container.setLayout(new BorderLayout());
		
		JLabel jlblTopic = new JLabel("右粉聊天室", JLabel.CENTER);
		jlblTopic.setFont(topicFont);
		jlblTopic.setForeground(topicColor);
		container.add(jlblTopic, BorderLayout.NORTH);
		
		jtatSystemMessage = new JTextArea(0, 15);
		jtatSystemMessage.setFont(normalFont);
		JScrollPane jscpSystemMessage = 
				new JScrollPane(jtatSystemMessage);
		jscpSystemMessage.setPreferredSize(new Dimension(200, 400));
		TitledBorder ttdbSystemMessage = new TitledBorder(" 系统信息 ");
		ttdbSystemMessage.setTitleFont(normalFont);
		ttdbSystemMessage.setTitlePosition(TitledBorder.ABOVE_TOP);
		ttdbSystemMessage.setTitleJustification(TitledBorder.CENTER);
		jscpSystemMessage.setBorder(ttdbSystemMessage);
		container.add(jscpSystemMessage, BorderLayout.EAST);
		
		jtatChat = new JTextArea();
		jtatChat.setFont(normalFont);
		JScrollPane jscpChat = new JScrollPane(jtatChat);
		TitledBorder ttdbChat = new TitledBorder(" 聊天信息 ");
		ttdbChat.setTitleFont(normalFont);
		ttdbChat.setTitleColor(Color.RED);
		ttdbChat.setTitlePosition(TitledBorder.ABOVE_TOP);
		ttdbChat.setTitleJustification(TitledBorder.CENTER);
		jscpChat.setBorder(ttdbChat);
		container.add(jscpChat, BorderLayout.CENTER);
		
		JPanel jpnlFriends = new JPanel(new BorderLayout());
		container.add(jpnlFriends, BorderLayout.WEST);
		
		JPanel jpnlWelcome = new JPanel(new GridLayout(0, 1));
		jpnlFriends.add(jpnlWelcome, BorderLayout.NORTH);
		
		JLabel jlblWelcome = new JLabel(" 欢迎 ", JLabel.CENTER);
		jlblWelcome.setFont(normalFont);
		jlblWelcome.setForeground(Color.RED);
		jpnlWelcome.add(jlblWelcome);
		
		JLabel jlblUserSelf = new JLabel(me.getNick(), JLabel.CENTER);
		jlblUserSelf.setFont(normalFont);
		jlblUserSelf.setForeground(topicColor);
		jpnlWelcome.add(jlblUserSelf);
		
		dlmFriendList = new DefaultListModel<>();
		jlstFriendList = new JList<>(dlmFriendList);
		jlstFriendList.setFont(normalFont);
		JScrollPane jscpFriendList = 
				new JScrollPane(jlstFriendList);
		jscpFriendList.setPreferredSize(new Dimension(200, 300));
		TitledBorder ttdbFriendList = 
				new TitledBorder(" 好友列表 ");
		ttdbFriendList.setTitleFont(normalFont);
		ttdbFriendList.setTitlePosition(TitledBorder.TOP);
		ttdbFriendList.setTitleJustification(
				TitledBorder.CENTER);
		jscpFriendList.setBorder(ttdbFriendList);
		jpnlFriends.add(jscpFriendList, BorderLayout.CENTER);
		
		JPanel jpnlCurFriend = 
				new JPanel(new GridLayout(0, 1));
		jpnlFriends.add(jpnlCurFriend, BorderLayout.SOUTH);
		
		JLabel jlblCurrentFriendCaption = 
				new JLabel("当前好友", 0);
		jlblCurrentFriendCaption.setFont(normalFont);
		jpnlCurFriend.add(jlblCurrentFriendCaption);
		
		jlblCurrentFriend = new JLabel("", JLabel.CENTER);
		jlblCurrentFriend.setFont(normalFont);
		jlblCurrentFriend.setForeground(Color.RED);
		jpnlCurFriend.add(jlblCurrentFriend);
		
		JPanel jpnlFooter = new JPanel();
		container.add(jpnlFooter, BorderLayout.SOUTH);
		
		JLabel jlblYousay = new JLabel("你说 ");
		jlblYousay.setFont(normalFont);
		jpnlFooter.add(jlblYousay);
		
		jtxtSpeakContext = new JTextField(40);
		jtxtSpeakContext.setFont(normalFont);
		jpnlFooter.add(jtxtSpeakContext);
		
		jbtnSend = new JButton("发送");
		jbtnSend.setFont(smallFont);
		jpnlFooter.add(jbtnSend);
	}
}