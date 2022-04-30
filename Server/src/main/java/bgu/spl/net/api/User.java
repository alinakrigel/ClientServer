package bgu.spl.net.api;

import bgu.spl.net.api.Message.Message;
import bgu.spl.net.api.Message.ServerToClient.Notification;

import java.util.concurrent.ConcurrentLinkedQueue;

public class User {

    // Fields
    private static final Integer YEAR = 2022;
    private final String userName;
    private final String password;
    private final String birthday;
    private int age;
    private int connectionId;
    private int numOfPublishedPosts = 0;
    private boolean isLoggedIn = false;
    private final ConcurrentLinkedQueue<User> followersList = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<User> followingList = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<User> blockedList = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Notification> notificationList = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Message> postAndPmHistory = new ConcurrentLinkedQueue<>();

    // Constructor
    public User(String userName, String password, String birthday, int connectionId) {
        this.userName = userName;
        this.password = password;
        this.birthday = birthday;
        this.connectionId = connectionId;
        computeAge();
    }

    // Getters
    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public ConcurrentLinkedQueue<User> getFollowersList() {
        return followersList;
    }

    public ConcurrentLinkedQueue<Notification> getNotificationList() {
        return notificationList;
    }

    public int getNumOfPublishedPosts() {
        return numOfPublishedPosts;
    }

    public int getAge() {
        return age;
    }

    public int getNumOfFollowers() {
        return followersList.size();
    }

    public int getNumOfFollowing() {
        return followingList.size();
    }

    public int getConnectionId() {
        return connectionId;
    }

    // Setters
    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    // Return true if this user is followed other user. userName is unique
    public boolean isFollowAfter(String otherUser) {
        for (User user : followingList) {
            if (user.getUserName().equals(otherUser))
                return true;
        }
        return false;
    }

    // Return true if other user is in followers list of this user. userName is unique
    public boolean isFollowedBy(String otherUser) {
        for (User user : followersList) {
            if (user.getUserName().equals(otherUser))
                return true;
        }
        return false;
    }

    // Return true if this user is blocking other user. userName is unique
    public boolean isBlocked(String otherUser) {
        for (User user : blockedList) {
            if (user.getUserName().equals(otherUser))
                return true;
        }
        return false;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    // Compute current age of the user
    public void computeAge() {
        int yearOfBirth = Integer.parseUnsignedInt(birthday.substring(6));
        age = YEAR - yearOfBirth;
    }

    // Adding user to this user following list
    public void addToFollowingList(User userToFollow) {
        followingList.add(userToFollow);
    }

    // Adding user to this user followers list
    public void addToFollowersList(User user) {
        followersList.add(user);
    }

    // Removing user from this user following list
    public void removeFromFollowingList(User userToRemove) {
        followingList.remove(userToRemove);
    }

    // Removing user from this user followers list
    public void removeFromFollowersList(User userToRemove) {
        followersList.remove(userToRemove);
    }

    // Adding message to the history of the user messages
    public void addToHistory(Message message) {
        postAndPmHistory.add(message);
    }

    // Add notification to a user that isn't logged in
    public void addMessageToNotification(Notification notification) {
        notificationList.add(notification);
    }

    // Increase number of published posts by one
    public void increaseNumOfPublishedPosts() {
        numOfPublishedPosts++;
    }

    // Adding user to this user blocked list
    public void addToBlockList(User userToBlock) {
        blockedList.add(userToBlock);
    }

}
