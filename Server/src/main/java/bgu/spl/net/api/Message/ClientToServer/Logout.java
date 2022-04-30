package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

public class Logout extends Message {

    // Constructor
    public Logout() {
        super((short) 3);
    }

}
