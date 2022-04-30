package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

public class Login extends Message {

    // Fields
    private final String userName;
    private final String password;
    private final int captcha;

    // Constructor
    public Login(String userName, String password, int captcha) {
        super((short) 2);
        this.userName = userName;
        this.password = password;
        this.captcha = captcha;
    }

    // Getters
    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public int getCaptcha() {
        return captcha;
    }

}
