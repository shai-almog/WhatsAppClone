package com.codename1.whatsapp.model;

import java.util.List;

public interface ServerMessages {
    public void connected();
    public void disconnected();
    public void messageReceived(ChatMessage m);
    public void userTyping(String contactId);
    public void messageViewed(String messageId, List<String> userIds);
}
