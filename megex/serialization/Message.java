/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 1
 * Class: CSI 4321
 *
 ************************************************/
package megex.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import static megex.serialization.MessageStatic.*;

/**
 * Represents a message
 */
public abstract class Message {
    private static final int MAXID = 2147483647;
    private static final int MINID = 0x0;

    //Data = 0x0, Settings = 0x4, Window_Update = 0x8
    /**
     * determines the streamID
     */
    protected int streamID;

    /**
     * default contructor
     * @param streamID streamID
     * @throws BadAttributeException
     * if error setting streamID
     */
    protected Message(int streamID) throws BadAttributeException {
        setStreamID(streamID);
    }


    //the message is the header + payload
    /**
     * Returns type code for message
     * @return type code
     */
    public abstract byte getCode();

    /**
     * Returns the streamID
     * @return message stream ID
     */
    public int getStreamID(){
        return this.streamID;
    }

    /**
     * Sets the stream id in the frame. Stream ID validation depends on specific message type
     * @param streamID new stream id value
     * @throws BadAttributeException
     * if input stream id is invalid
     */
    public void setStreamID(int streamID) throws BadAttributeException {
        if(streamID < MINID || streamID > MAXID){
            throw new BadAttributeException("Invalid streamID", "StreamID only allowed 31 bits");
        }
        this.streamID = clear32bit(streamID);
    }

    /**
     * format the streamID for output
     * @param out outputstream in use
     * @throws IOException
     * if IO error
     */
    protected void writeID(OutputStream out) throws IOException {

        out.write((this.getStreamID() >>> (3 * byteShift)) & byteMask);
        out.write((this.getStreamID() >>> (2 * byteShift)) & byteMask);
        out.write((this.getStreamID() >>> byteShift) & byteMask);
        out.write(this.getStreamID() & byteMask);
    }

    /**
     * serialize method to be inherited and overridden
     * @param out outputstream in use
     */
    protected abstract void serialize(OutputStream out);

    /**
     * if objects are equal
     * @param o other object
     * @return if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return streamID == message.streamID;
    }

    /**
     * object unique hashcode
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(streamID);
    }
}
