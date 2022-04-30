package bgu.spl.net.api.Message;

public class Message {

    // Fields
    private final short opcode;

    // Constructors
    public Message(short opcode) {
        this.opcode = opcode;
    }

    // Getters
    public short getOpcode() {
        return opcode;
    }

}
