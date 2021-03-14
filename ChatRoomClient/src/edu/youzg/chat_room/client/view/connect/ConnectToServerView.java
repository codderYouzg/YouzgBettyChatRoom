package edu.youzg.chat_room.client.view.connect;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import edu.youzg.betty.core.Client;
import edu.youzg.betty.core.ClientActionAdapter;
import edu.youzg.betty.exception.ClientActionNotSetException;
import edu.youzg.chat_room.client.view.login.LoginView;
import edu.youzg.util.ISwingHelper;
import edu.youzg.util.PromptBoxTool;
import edu.youzg.util.exceptions.FrameIsNullException;

public class ConnectToServerView implements ISwingHelper {
	private JFrame jfrmConnectView;
	private JLabel jlblMessage;	
	private int count;
	
	private Client client;
	
	public ConnectToServerView() {
		this.count = 1;
	}

	public void initClient() throws UnknownHostException {
		this.client = new Client();
		this.client.initClient("/client.cfg.properties");
		this.client.setClientAction(new ConnectAction());
	}
	
	@Override
	public void init() {
		jfrmConnectView = new JFrame("连接服务器");
		jfrmConnectView.setLayout(new BorderLayout());
		jfrmConnectView.setMinimumSize(new Dimension(300, 150));
		jfrmConnectView.setLocationRelativeTo(null);
		jfrmConnectView.setResizable(false);
		jfrmConnectView.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JLabel jlblTopic = new JLabel("连接服务器", 0);
		jlblTopic.setFont(topicFont);
		jlblTopic.setForeground(topicColor);
		jfrmConnectView.add(jlblTopic, BorderLayout.NORTH);
		
		jlblMessage = new JLabel("", 0);
		jlblMessage.setFont(normalFont);
		jfrmConnectView.add(jlblMessage);
	}

	public void startConnect() {
		jlblMessage.setText("正在进行第" + count++ + "次连接……");
		try {
			while (!client.connectToServer()) {
				int choice = PromptBoxTool.getChoice(jfrmConnectView, "连接服务器失败，是否继续尝试连接?",
						JOptionPane.YES_NO_OPTION);
				if (choice != JOptionPane.YES_OPTION) {
					exitView();
					return;
				}
				jlblMessage.setText("正在进行第" + count++ + "次连接……");
			}
		} catch (ClientActionNotSetException e) {
			e.printStackTrace();
		} catch (FrameIsNullException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void reinit() {}

	@Override
	public void dealEvent() {
		jfrmConnectView.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					exitView();
				} catch (FrameIsNullException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	@Override
	public JFrame getFrame() {
		return jfrmConnectView;
	}

	class ConnectAction extends ClientActionAdapter {
		
		public ConnectAction() {
		}

		@Override
		public void afterConnectToServer() {
			// 连接成功，开始“登录”界面
			LoginView loginView = new LoginView(client);
			loginView.initView();
			try {
				loginView.showView();
			} catch (FrameIsNullException e) {
				e.printStackTrace();
			}
			try {
				exitView();
			} catch (FrameIsNullException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void serverAbnormalDrop() {
			PromptBoxTool.showMessage(jfrmConnectView, "服务器拒绝连接，请稍后尝试……");
			try {
				exitView();
			} catch (FrameIsNullException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void serverOutOfRoom() {
			PromptBoxTool.showMessage(jfrmConnectView, "服务器已满，请稍后尝试连接……");
			try {
				exitView();
			} catch (FrameIsNullException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}