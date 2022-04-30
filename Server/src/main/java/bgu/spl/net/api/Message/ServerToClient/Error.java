package bgu.spl.net.api.Message.ServerToClient;

public class Error extends ServerToClientMessage {

    // Constructor
    public Error(short messageOpcode) {
        super((short) 11, messageOpcode);
    }

    // Returns the encoded message into array of bytes
    public byte[] encodeMessage() {
        byte[] opcode = shortToBytes(getOpcode());
        byte[] messageOpcode = shortToBytes(getMessageOpcode());
        byte[] encodeMessage = new byte[4];
        encodeMessage[0] = opcode[0];
        encodeMessage[1] = opcode[1];
        encodeMessage[2] = messageOpcode[0];
        encodeMessage[3] = messageOpcode[1];
        return encodeMessage;
    }
}
