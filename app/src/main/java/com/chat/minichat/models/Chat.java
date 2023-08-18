package com.chat.minichat.models;


import com.chat.minichat.enums.ChatType;

public class Chat {
    private String sender;
    private String target;
    private String data;
    private Long timeStamp = System.currentTimeMillis();

    private Boolean isValid;
    private ChatType type;

    public Chat(String sender,ChatType type, String target) {
        this.sender = sender;
        this.type = type;
        this.target = target;
    }

    public Chat(String sender, String target, String data) {
        this.sender = sender;
        this.target = target;
        this.data = data;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "sender='" + sender + '\'' +
                ", target='" + target + '\'' +
                ", data='" + data + '\'' +
                ", timeStamp=" + timeStamp +
                ", type=" + type +
                '}';
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public ChatType getType() {
        return type;
    }

    public void setType(ChatType type) {
        this.type = type;
    }
    public Boolean isValid() {
//        return System.currentTimeMillis() - this.timeStamp < 60000;
        return true;
    }
}
