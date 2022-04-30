package bgu.spl.net.api;

import bgu.spl.net.api.Message.ClientToServer.*;
import bgu.spl.net.api.Message.Message;
import bgu.spl.net.api.Message.ClientToServer.Block;
import bgu.spl.net.api.Message.ServerToClient.ServerToClientMessage;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Message> {

    // Fields
    private byte[] bytes = new byte[1 << 10]; // Start with 1k
    private int len = 0;
    private String[] messageSplit; // Splitting the message by '/0'
    private short opcode;

    public Message decodeNextByte(byte nextByte) {
        // Notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        // this allows us to do the following comparison
        if (len == 2)
            opcode = bytesToShort(bytes);
        if (nextByte == ';')
            return popMessage();

        pushByte(nextByte);
        return null; // Not a message it
    }

    // Returns encode message into array of bytes
    public byte[] encode(Message message) {
        byte[] currEncodedMsg = ((ServerToClientMessage) message).encodeMessage();
        // Create new array and add ';' at the of the encoded message
        byte[] newEncodeMessage = new byte[currEncodedMsg.length + 1];
        System.arraycopy(currEncodedMsg, 0, newEncodeMessage, 0, currEncodedMsg.length); // Copy the array
        byte[] lastChar = ";".getBytes(StandardCharsets.UTF_8);
        newEncodeMessage[newEncodeMessage.length - 1] = lastChar[0];
        return newEncodeMessage;
    }

    // Add new byte to bytes array
    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    private Message popMessage() {
        // Notice that we're explicitly requesting that the string will be decoded from UTF-8
        // this is not actually required as it is the default encoding in java.
        String recivedMessage = new String(bytes, 2, len - 2, StandardCharsets.UTF_8);
        messageSplit = recivedMessage.split("\0"); // Split the message to array of strings and separate by the char '\0'\
        switch (opcode) {
            case 1: // Register
                return createRegisterMessage();// Returns Register Message
            case 2: // Login
                return createLoginMessage();// Returns Login Message
            case 3: // Logout
                return createLogoutMessage();// Returns Logout Message
            case 4: // FollowUnfollow
                return createFollowUnfollowMessage();// Returns FollowUnfollow Message
            case 5: // Post
                return createPostMessage();// Returns Post Message
            case 6: // PM
                return createPMMessage();// Returns PM Message
            case 7: // Logstat
                return createLogstatMessage();// Returns Logstat Message
            case 8: // Stat
                return createStatMessage();// Returns Stat Message
            case 12: // Block
                return createBlockMessage();// Returns Block Message
        }
        len = 0;
        return null; // It will never happen
    }

    // Returns Register Message
    private Register createRegisterMessage() {
        String userName = messageSplit[0];
        String password = messageSplit[1];
        String birthday = messageSplit[2];
        messageSplit = new String[0];
        clearFields();
        return new Register(userName, password, birthday);
    }

    // Returns Login Message
    private Login createLoginMessage() {
        String userName = messageSplit[0];
        String password = messageSplit[1];
        int captcha = Integer.parseInt(messageSplit[2]);
        clearFields();
        return new Login(userName, password, captcha);
    }

    // Returns Logout Message
    private Logout createLogoutMessage() {
        clearFields();
        return new Logout();
    }

    // Returns FollowUnfollow Message
    private FollowUnfollow createFollowUnfollowMessage() {
        int followUnfollow = Integer.parseInt(messageSplit[0].substring(0, 1));
        String userName = messageSplit[0].substring(1);
        clearFields();
        return new FollowUnfollow(followUnfollow, userName);
    }

    // Returns Post Message
    private Post createPostMessage() {
        String content = messageSplit[0];
        clearFields();
        return new Post(content);
    }

    // Returns PM Message
    private PM createPMMessage() {
        String userName = messageSplit[0];
        String content = messageSplit[1];
        clearFields();
        return new PM(userName, content);
    }

    // Returns Logstat Message
    private Logstat createLogstatMessage() {
        clearFields();
        return new Logstat();
    }

    // Returns Stat Message
    private Stat createStatMessage() {
        String listOfUsernames = messageSplit[0];
        clearFields();
        return new Stat(listOfUsernames);
    }

    // Returns Block Message
    private Block createBlockMessage() {
        String userName = messageSplit[0];
        clearFields();
        return new Block(userName);
    }

    // Recover akk fields to default
    private void clearFields() {
        len = 0;
        bytes = new byte[1 << 10];
    }

    // Convert byte to short
    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

}
