package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

public class Block extends Message {

    // Fields
    private final String username;

    // Constructor
    public Block(String username) {
        super((short) 12);
        this.username = username;
    }

    // Getters
    public String getUsername() {
        return username;
    }
}
