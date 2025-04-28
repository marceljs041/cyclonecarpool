package com.example.cyclonecarpool.chat;

public class ChatItem {

    private Integer otherId;
    private String otherName;
    private String otherPicUrl;
    private String lastMessage;

    public ChatItem(Integer otherId, String otherName, String otherPicUrl, String lastMessage) {
        this.otherId = otherId;
        this.otherName = otherName;
        this.otherPicUrl = otherPicUrl;
        this.lastMessage = lastMessage;
    }

    public Integer getOtherId() { return otherId; }

    public String getOtherName() { return otherName; }

    public String getOtherPicUrl() { return otherPicUrl; }

    public String getLastMessage() { return lastMessage; }
}
