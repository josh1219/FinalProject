package com.DogProject.dto;

public interface ChatDTO {
    Integer getCIdx();
    String getContent();
    String getSendTime();
    Integer getSender();
    Integer getReceiver();
    Boolean getIsRead();
    Integer getUnreadCount();
}
