package com.example.cyclonecarpool.chat;

public class MessageItem {

	private Long messageId;
	private String message;
	private String sentTime;
	private Integer senderId;

	public MessageItem(Long messageId, String message, String sentTime, Integer senderId) {
		this.messageId = messageId;
		this.message = message;
		this.sentTime = sentTime;
		this.senderId = senderId;
	}

//	public MessageItem() {
//		this("Lorem ipsum odor amet, consectetuer adipiscing elit.", "8:57 pm", -1);
//	}

	public String getMessage() {
		return message;
	}

	public String getSentTime() {
		return sentTime;
	}

	public Integer getSenderId() {
		return senderId;
	}

	public Long getMessageId() {
		return messageId;
	}
}
