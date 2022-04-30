package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.ConnectionHandler;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {
    // For Singleton
    private static class ConnectionsImplHolder {
        private static final ConnectionsImpl instance = new ConnectionsImpl<>();
    }

    private final ConcurrentHashMap<Integer, ConnectionHandler<T>> handlersMap;

    private ConnectionsImpl() {
        handlersMap = new ConcurrentHashMap<>();
    }

    // For Singleton
    public static ConnectionsImpl getInstance() {
        return ConnectionsImpl.ConnectionsImplHolder.instance;
    }

    // Return true if the id is in the map and succeed to send
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> ch = handlersMap.get(connectionId);
        if (ch != null) {
            ch.send(msg);
            return true;
        }
        return false;
    }

    // Not in use
    public void broadcast(T msg) {
    }

    // Remove from hash map
    public void disconnect(int connectionId) {
        handlersMap.remove(connectionId);
    }

    // Add to hash map
    public void register(int connectionId, ConnectionHandler<T> ch){
        handlersMap.putIfAbsent(connectionId, ch);
    }

}
