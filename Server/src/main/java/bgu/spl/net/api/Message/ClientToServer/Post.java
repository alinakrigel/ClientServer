package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

public class Post extends Message {

    // Fields
    private final String content;

    // Constructor
    public Post(String content) {
        super((short) 5);
        this.content = content;
    }

    // Getters
    public String getContent() {
        return content;
    }

}
