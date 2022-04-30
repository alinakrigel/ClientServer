package bgu.spl.net.api.bidi;

import bgu.spl.net.api.DataBase;
import bgu.spl.net.api.Message.ClientToServer.*;
import bgu.spl.net.api.Message.Message;
import bgu.spl.net.api.Message.ServerToClient.Ack;
import bgu.spl.net.api.Message.ServerToClient.Error;
import bgu.spl.net.api.Message.ServerToClient.Notification;
import bgu.spl.net.api.User;

import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BGSProtocol<T> implements BidiMessagingProtocol<T> {

    private final DataBase dataBase = DataBase.getInstance();
    private ConnectionsImpl<Message> connections;
    private User user = null; // Connection handler isn't connect to any user
    private int connectionId;
    private boolean shouldTerminate = false;

    @Override
    public void start(int connectionId, Connections<T> connections) {
        this.connectionId = connectionId;
        this.connections = (ConnectionsImpl<Message>) connections;
    }

    // Process the message
    public void process(T message) {
        short opcode = ((Message) message).getOpcode();
        switch (opcode) {
            case 1: { // Register
                synchronized (dataBase.getLock()) {
                    Register register = (Register) message;
                    String userName = register.getName();
                    // ERROR if - is already register to the system
                    if (dataBase.isRegistered(userName))
                        connections.send(connectionId, new Error(opcode));
                    else { // Sign in to database and return Ack
                        User newUser = new User(userName, register.getPassword(), register.getBirthday(), connectionId);
                        dataBase.registerUser(userName, newUser);
                        connections.send(connectionId, new Ack(opcode));
                    }
                    break;
                }
            }
            case 2: { // Login
                Login login = (Login) message;
                String userName = login.getUserName();
                String password = login.getPassword();
                // ERROR if -
                // E1. Is not registered
                // E2. Already logged in
                // E3. Wrong password
                // E4. Captcha is zero
                if (!dataBase.isRegistered(userName) || isLoggedIn() || !dataBase.correctPassword(userName, password) || login.getCaptcha() != 1)
                    connections.send(connectionId, new Error(opcode));
                else {
                    User user = dataBase.getUserByName(userName); // Return the specific user by his name from db
                    // E5. User is already logged in - mean that he associate to another connection handler
                    if (user.isLoggedIn())
                        connections.send(connectionId, new Error(opcode));
                        // 1. Login the user and associate him to this connection id
                        // 2. Read all notifications of the user
                    else {
                        loginUser(user); // Set the user field and set connection id
                        connections.send(connectionId, new Ack(opcode));
                        // Read all notifications and remove them
                        ConcurrentLinkedQueue<Notification> notificationList = user.getNotificationList();
                        while (!notificationList.isEmpty()) {
                            Notification notification = notificationList.poll();
                            connections.send(connectionId, notification);
                        }
                    }
                }
                break;
            }
            case 3: { // Logout
                // ERROR if - User is already logout
                if (!isLoggedIn())
                    connections.send(connectionId, new Error(opcode));
                    // Logout the user
                else {
                    synchronized (user) {
                        logoutUser();
                        shouldTerminate = connections.send(connectionId, new Ack(opcode));
                        connections.disconnect(connectionId);
                    }
                }
                break;
            }
            case 4: { // Follow/Unfollow Message
                FollowUnfollow followUnfollow = (FollowUnfollow) message;
                String otherUserName = followUnfollow.getUserName();
                int followOrUnfollow = followUnfollow.getFollowUnfollow();
                User otherUser = dataBase.getUserByName(otherUserName);
                // ERROR if -
                // 1. User isn't logged in
                // 2. In case of Follow - a) User already follow on other user or vice versa. b) User is blocked other user or vice versa
                // 3. In case of Unfollow - User already unfollow on other user or vice versa
                // 4. The user is trying to follow/unfollow after himself
                if (!isLoggedIn() || user.getUserName().equals(otherUserName))
                    connections.send(connectionId, new Error(opcode));
                else if (followOrUnfollow == 0) { // Follow case
                    if (user.isFollowAfter(otherUserName) ||
                            otherUser.isFollowedBy(user.getUserName()) ||
                            user.isBlocked(otherUserName) ||
                            otherUser.isBlocked(user.getUserName()))
                        connections.send(connectionId, new Error(opcode));
                        // Add to followers/following list of both and send Ack
                    else {
                        user.addToFollowingList(otherUser);
                        otherUser.addToFollowersList(user);
                        connections.send(connectionId, new Ack(opcode, otherUserName));
                    }
                } else { // Unfollow case
                    if (!user.isFollowAfter(otherUserName) ||
                            !otherUser.isFollowedBy(user.getUserName()))
                        connections.send(connectionId, new Error(opcode));
                        // Remove from followers/following list of both and send Ack
                    else {
                        user.removeFromFollowingList(otherUser);
                        otherUser.removeFromFollowersList(user);
                        connections.send(connectionId, new Ack(opcode, otherUserName));
                    }
                }
                break;
            }
            case 5: { // Post
                Post post = (Post) message;
                String content = post.getContent();
                // Put all username who tag into a vector. Every cell of the vector is a string representing username
                Vector<String> userNamesWhoTag = splitByShtrudel(content);
                Vector<User> usersToSentPost = new Vector<>();
                // ERROR if -
                // E1. User isn't logged in
                if (!isLoggedIn()) {
                    connections.send(connectionId, new Error(opcode));
                    return;
                }
                // Adding all users from the tagging
                for (String userName : userNamesWhoTag) {
                    // E2. One of the user who tagged isn't register
                    // E3. I'm trying to follow myself
                    if (!dataBase.isRegistered(userName) || user.getUserName().equals(userName)) {
                        connections.send(connectionId, new Error(opcode));
                        return;
                    }
                    if (!usersToSentPost.contains(dataBase.getUserByName(userName)))
                        usersToSentPost.add(dataBase.getUserByName(userName));
                }
                ConcurrentLinkedQueue<User> followersList = user.getFollowersList();
                // Adding all users that follow after this
                for (User user : followersList) {
                    if (!usersToSentPost.contains(user)) // Preventing duplicates
                        usersToSentPost.add(user);
                }
                // Send Acc only if -
                // 1. One of the user that I tagged isn't registered, means that there is a null in the usersToSentPost vector
                // 2. One of the user that I tagged is blocking me
                // 3. I'm blocking one of the user that I tagged
                Notification notification = new Notification("1", user.getUserName(), content);
                for (User userToPost : usersToSentPost) {
                    if (userToPost != null && !user.isBlocked(userToPost.getUserName()) && !userToPost.isBlocked(user.getUserName())) {
                        // 1) Send the post to the user
                        // 2) Add the post to history
                        // 3) Send Acc
                        if (userToPost.isLoggedIn()) {
                            connections.send(userToPost.getConnectionId(), notification);
                            userToPost.addToHistory(post);
                        } else { // In case that the user that im trying to send isn't logged in just push a notification and send Acc
                            synchronized (userToPost) {
                                userToPost.addMessageToNotification(notification);
                            }
                        }
                    }
                }
                user.increaseNumOfPublishedPosts();
                connections.send(connectionId, new Ack(opcode));
                break;
            }
            case 6: { // PM
                PM pm = (PM) message;
                // ERROR if -
                // 1. The sender user isn't logged in
                // 2. The receiving user isn't registered
                // 3. The sender user isn't follows after the receiver user
                // 4. The sender user is blocked by receiver user or vice versa
                // 5. If im trying to send pm to myself
                String receiverUserName = pm.getUserName();
                User receiverUser = dataBase.getUserByName(receiverUserName);
                if (!isLoggedIn() || !dataBase.isRegistered(receiverUserName) || !user.isFollowAfter(receiverUserName) ||
                        user.isBlocked(receiverUserName) || receiverUser.isBlocked(user.getUserName()) || user.getUserName().equals(receiverUserName))
                    connections.send(connectionId, new Error(opcode));
                    // 1. Filter the message
                    // 2. If receiver user logged in- send acc and add the post to the receiver history . else, send notification
                else {
                    Notification notification = new Notification("0", user.getUserName(), pm.getContent());
                    // 1) Send the post to the user
                    // 2) Add the post to history
                    // 3) Send Acc
                    if (receiverUser.isLoggedIn()) {
                        connections.send(connectionId, new Ack(opcode));
                        connections.send(receiverUser.getConnectionId(), notification);
                        receiverUser.addToHistory(pm);
                    } else { // In case that the user that im trying to send isn't logged in just push a notification and send Acc
                        synchronized (receiverUser) {
                            receiverUser.addMessageToNotification(notification);
                            connections.send(connectionId, new Ack(opcode));
                        }
                    }
                }
                break;
            }
            case 7: { // Logstat
                // ERROR if - The user isn't logged in
                if (!isLoggedIn())
                    connections.send(connectionId, new Error(opcode));
                else { // Send information about all the logged-in user in the system
                    LinkedList<User> loggedInUsers = new LinkedList<>();
                    for (User currUser : dataBase.getUsersMap().values()) {
                        if (currUser.isLoggedIn() && !currUser.isBlocked(user.getUserName()) && !user.isBlocked(currUser.getUserName())) // Add to list only users who logged in and not block me
                            loggedInUsers.add(currUser);
                    }
                    connections.send(connectionId, new Ack(opcode, loggedInUsers));
                }
                break;
            }
            case 8: { // Stat
                Stat stat = (Stat) message;
                // ERROR if -
                // E1. The user isn't logged in
                if (!isLoggedIn())
                    connections.send(connectionId, new Error(opcode));
                else {
                    String userNamesStr = stat.getListOfUsernames();
                    String[] userNamesArr = userNamesStr.split("\\|");
                    // E2. One of the user in the stat list isn't registered
                    for (String userName : userNamesArr) {
                        if (!dataBase.isRegistered(userName)) {
                            connections.send(connectionId, new Error(opcode));
                            return;
                        }
                    }
                    LinkedList<User> loggedInUsers = new LinkedList<>();
                    for (String userName : userNamesArr) {
                        User currUser = dataBase.getUserByName(userName);
                        // E3. One of the user in the list is blocking me or im blocking him
                        if (user.isBlocked(userName) || currUser.isBlocked(user.getUserName())) {
                            connections.send(connectionId, new Error(opcode));
                            return;
                        }
                        if (currUser.isLoggedIn()) // Add to list only users who logged in and not block me
                            loggedInUsers.add(currUser);
                    }
                    connections.send(connectionId, new Ack(opcode, loggedInUsers));
                }
                break;
            }
            case 12: { // Block
                Block block = (Block) message;
                String userNameToBlock = block.getUsername();
                User userToBlock = dataBase.getUserByName(userNameToBlock);
                // ERROR if -
                // 1. User isn't logged in
                // 2. User that im trying to block isn't registered
                // 3. User already block other user or vice versa
                // 4. I'm trying to block myself
                if (!isLoggedIn() || !dataBase.isRegistered(userNameToBlock) || user.isBlocked(userNameToBlock) || userToBlock.isBlocked(user.getUserName()) || userNameToBlock.equals(user.getUserName()))
                    connections.send(connectionId, new Error(opcode));
                    // 1. send Acc
                    // 2. Remove him from my following/followers list and vice versa
                    // 3. Add to blocked list
                else {
                    user.removeFromFollowingList(userToBlock);
                    user.removeFromFollowersList(userToBlock);
                    userToBlock.removeFromFollowingList(user);
                    user.removeFromFollowersList(user);
                    user.addToBlockList(userToBlock);
                    connections.send(connectionId, new Ack(opcode));
                }
                break;
            }
        }
    }


    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    // Return true if user is logged in to the system
    private boolean isLoggedIn() {
        return user != null;
    }

    // Login the user to the system and associate him to the connection handler
    private void loginUser(User user) {
        this.user = user;
        user.setLoggedIn(true);
        user.setConnectionId(connectionId);
    }

    // Logout the user to the system
    private void logoutUser() {
        user.setLoggedIn(false);
        user.setConnectionId(-1);
        user = null;
    }

    // Split String by the char '@' into a vector
    private static Vector<String> splitByShtrudel(String s) {
        Vector<String> stringVector = new Vector<>();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '@') {
                int startIndForSubstring = i + 1;
                String acc = "";
                while (startIndForSubstring < s.length() && s.charAt(startIndForSubstring) != ' ') {
                    acc += s.charAt(startIndForSubstring);
                    startIndForSubstring++;
                }
                if (!acc.equals(""))
                    stringVector.add(acc);
            }
        }
        return stringVector;
    }

}
