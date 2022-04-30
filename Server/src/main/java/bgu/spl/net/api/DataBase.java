package bgu.spl.net.api;

import java.util.concurrent.ConcurrentHashMap;

public class DataBase {
    // For Singleton
    private static class DataBaseHolder {
        private static final DataBase instance = new DataBase();
    }

    // Fields
    private final ConcurrentHashMap<String, User> usersMap; // Map for all users in the system. The key is "userName"
    private final Object lock = new Object(); // Lock for synchronized in BGS Protocol

    // Constructor
    public DataBase() {
        usersMap = new ConcurrentHashMap<>();
    }

    // For Singleton
    public static DataBase getInstance() {
        return DataBaseHolder.instance;
    }

    // Getters
    public ConcurrentHashMap<String, User> getUsersMap() {
        return usersMap;
    }

    public Object getLock() {
        return lock;
    }

    // Check if user is registered to the system. userName is unique in the system
    public boolean isRegistered(String userName) {
        return usersMap.containsKey(userName);
    }

    // Register user to the system
    public void registerUser(String userName, User newUser) {
        usersMap.put(userName, newUser);
    }

    // Check if the password of user s correct
    public boolean correctPassword(String userName, String password) {
        return usersMap.get(userName).getPassword().equals(password);
    }

    // Return the specific user by his name
    public User getUserByName(String userName) {
        return usersMap.get(userName);
    }

}
