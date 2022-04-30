package bgu.spl.net.api.Message.ServerToClient;

import java.nio.charset.StandardCharsets;

public class Notification extends ServerToClientMessage {

    // Fields
    private final String notificationTypePMPublic;
    private final String postingUser;
    private final String content;

    // Constructor
    public Notification(String notificationTypePMPublic, String postingUser, String content) {
        super((short) 9, (short) -1); // The -1 is dummy
        this.notificationTypePMPublic = notificationTypePMPublic;
        this.postingUser = postingUser;
        this.content = content;
    }

    public byte[] encodeMessage() {
        byte[] opcodeBytes = shortToBytes(getOpcode());
        byte[] notifTypeBytes = notificationTypePMPublic.getBytes(StandardCharsets.UTF_8);
        byte[] postingUserBytes = postingUser.getBytes(StandardCharsets.UTF_8);
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] zeroBytes = "\0".getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[opcodeBytes.length + notifTypeBytes.length + postingUserBytes.length + contentBytes.length + 2];
        output[0] = opcodeBytes[0];
        output[1] = opcodeBytes[1];
        output[2] = notifTypeBytes[0];
        int ind = 3;
        for (byte currByte : postingUserBytes) {
            output[ind] = currByte;
            ind++;
        }
        output[ind] = zeroBytes[0];
        ind++;
        for (byte currByte : contentBytes) {
            output[ind] = currByte;
            ind++;
        }
        output[ind] = zeroBytes[0];
        return output;
    }
}
