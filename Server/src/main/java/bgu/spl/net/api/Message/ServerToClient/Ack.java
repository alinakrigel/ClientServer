package bgu.spl.net.api.Message.ServerToClient;

import bgu.spl.net.api.User;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;

public class Ack extends ServerToClientMessage {

    // Fields
    private String userName = null; // for Follow/Unfollow
    private LinkedList<User> users = null; // for Stat/Logstat

    // Constructor
    public Ack(short messageOpcode) {
        super((short) 10, messageOpcode);
    }

    // Constructor for Follow/Unfollow message
    public Ack(short messageOpcode, String userName) {
        this(messageOpcode);
        this.userName = userName;
    }

    // Constructor for Logstat/Stat message
    public Ack(short messageOpcode, LinkedList<User> users) {
        this(messageOpcode);
        this.users = users;
    }

    // Returns the message encode to array of bytes
    public byte[] encodeMessage() {
        byte[] output = new byte[4];
        // Encode the two opcodes
        byte[] opcodeBytes = shortToBytes(getOpcode());
        byte[] msgOpcodeBytes = shortToBytes(getMessageOpcode());
        if (userName != null) { // Ack for Follow/Unfollow
            byte[] nameBytes = userName.getBytes(StandardCharsets.UTF_8);
            output = new byte[nameBytes.length + 5];
            int ind = 4;
            for (byte userNameByte : nameBytes) {
                output[ind] = userNameByte;
                ind++;
            }
            output[output.length - 1] = '\0';
        } else if (users != null) { // Ack for Logstat/Stat
            output = new byte[users.size() * 8 + 4];
            Iterator<User> userIterator = users.iterator();
            for (int i = 0; i < users.size(); i++) {
                User user = userIterator.next();
                output[i * 8 + 4] = shortToBytes((short)user.getAge())[0];
                output[i * 8 + 5] = shortToBytes((short)user.getAge())[1];
                output[i * 8 + 6] = shortToBytes((short)user.getNumOfPublishedPosts())[0];
                output[i * 8 + 7] = shortToBytes((short)user.getNumOfPublishedPosts())[1];
                output[i * 8 + 8] = shortToBytes((short)user.getNumOfFollowers())[0];
                output[i * 8 + 9] = shortToBytes((short)user.getNumOfFollowers())[1];
                output[i * 8 + 10] = shortToBytes((short)user.getNumOfFollowing())[0];
                output[i * 8 + 11] = shortToBytes((short)user.getNumOfFollowing())[1];
            }
        }
        output[0] = opcodeBytes[0];
        output[1] = opcodeBytes[1];
        output[2] = msgOpcodeBytes[0];
        output[3] = msgOpcodeBytes[1];
        return output;
    }
}
