package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

public class Stat extends Message {

    // Fields
    private final String listOfUsernames;

    // Constructor
    public Stat(String listOfUsernames) {
        super((short) 8);
        this.listOfUsernames = listOfUsernames;
    }

    // Getters
    public String getListOfUsernames() {
        return listOfUsernames;
    }

}
