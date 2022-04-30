package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

public class Register extends Message {

    // Fields
    private final String name;
    private final String password;
    private final String birthday;

    // Constructor
    public Register(String name, String password, String birthday) {
        super((short) 1);
        this.name = name;
        this.password = password;
        this.birthday = birthday;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getBirthday() {
        return birthday;
    }

}
