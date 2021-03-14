package edu.youzg.chat_room.client.protocol;

public class ChatMessage {
	private EAppCommand command;
	private String message;

	public ChatMessage() {
	}

	public EAppCommand getCommand() {
		return command;
	}

	public void setCommand(EAppCommand command) {
		this.command = command;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}