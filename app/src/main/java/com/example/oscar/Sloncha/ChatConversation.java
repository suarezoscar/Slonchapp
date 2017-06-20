package com.example.oscar.Sloncha;

/**
 * Created by oscar on 06/06/2017.
 */

public class ChatConversation {
    String Message;
    String Sender;
    String DateTime;

    public ChatConversation(String message, String sender, String dateTime) {
        Message = message;
        Sender = sender;
        DateTime = dateTime;
    }

    public ChatConversation() {

    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        Sender = sender;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }
}
