package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

    public class FollowUnfollow extends Message {

    // Fields
    private final int followUnfollow;
    private final String userName;

    // Constructor
    public FollowUnfollow(int followUnfollow, String name) {
        super((short) 4);
        this.followUnfollow = followUnfollow;
        this.userName = name;
    }

    // Getters
    public int getFollowUnfollow() {
        return followUnfollow;
    }

    public String getUserName() {
        return userName;
    }

}
