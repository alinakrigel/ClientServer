package bgu.spl.net.api.Message.ServerToClient;


import bgu.spl.net.api.Message.Message;

public abstract class ServerToClientMessage extends Message {

    private final short messageOpcode;

    public ServerToClientMessage(short opcode, short messageOpcode) {
        super(opcode);
        this.messageOpcode = messageOpcode;
    }

    // Getters
    public short getMessageOpcode() {
        return messageOpcode;
    }

    public abstract byte[] encodeMessage();

    // Convert short to array of bytes
    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }
}
