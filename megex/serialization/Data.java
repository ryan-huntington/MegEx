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
import java.util.Arrays;
import java.util.Objects;

import static megex.serialization.MessageStatic.*;

/**
 * Data message
 */
public class Data extends Message{
    /**
     * value for data type
     */
    public static final int DATA_TYPE = 0x0;
    private static final int INVALID_ID = 0x0;


    private boolean end;
    private byte[] data;

    /**
     * Creates Data message from given values
     * @param streamID stream ID
     * @param isEnd true if last data message
     * @param data bytes of application data
     * @throws BadAttributeException
     * if attribute invalid (see protocol spec)
     */
    public Data(int streamID, boolean isEnd, byte[] data) throws BadAttributeException{
        super(streamID);

        //he said that calling something that can be overridden means I am playing with fire
        //so should I really be calling it here? otherwise I am writing the same code multiple times

        //calling the setters because I do not want to keep replicating the same badAttributeExceptions
        this.setData(data);
        this.end = isEnd;


        //data = payload - fieldsPresent
    }

    @Override
    public byte getCode() {
        return DATA_TYPE;
    }

    /**
     * Specific to the data class, ensures that the streamID cannot be 0x0
     * @param streamID new stream id value
     * @throws BadAttributeException
     * if the streamID is 0x0
     */
    public void setStreamID(int streamID) throws BadAttributeException {
        if(streamID <= INVALID_ID){
            throw new BadAttributeException("Invalid streamID", "cannot use that value for streamID", new IllegalArgumentException("invalid streamID"));
        }
        this.streamID = clear32bit(streamID);
    }

    /**
     * Return end value
     * @return end value
     */
    public boolean isEnd() {
        return end;
    }

    /**
     * Set end value
     * @param end end value
     */
    public void setEnd(boolean end) {
        this.end = end;
    }

    /**
     * Returns Data's data
     * @return data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Set data
     * @param data data to set
     *
     * @throws BadAttributeException
     * if invalid
     */
    public void setData(byte[] data) throws BadAttributeException {
        //what error tho?
        int headerLength = 6;
        if(data == null){
            throw new BadAttributeException("Data cannot be null", "data", new NullPointerException("data is null"));
        }

        if(data.length > MAXMESSAGELENGTH - headerLength){
            throw new BadAttributeException("Bad length", "data", new IllegalArgumentException("bad length"));
        }
        this.data = data;
    }

    /**
     * Returns string of the form
     * @return returns object in string form
     */
    @Override
    public String toString() {
        return "Data: StreamID=" + this.getStreamID() +
                " isEnd=" + this.isEnd() +
                " data=" + this.getData().length;

    }



    /**
     * Sets if objects equal one another
     * @param o other object
     * @return if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data1 = (Data) o;
        return end == data1.end && Arrays.equals(data, data1.data) && getStreamID() == data1.getStreamID() && getCode() == data1.getCode();
    }

    /**
     * generates the hashcode for object
     * @return unique hashcode
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(end);
        result = 31 * result + Arrays.hashCode(data) + Objects.hash(this.getCode(), streamID);
        return result;
    }

    /**
     * Format the byte array for the Data message
     * @param out outputstream in use
     */
    protected void serialize(OutputStream out) {

        try {
            out.write(this.getCode());
            if (this.isEnd()) {
                out.write(0x1);
            } else {
                out.write(0x0);
            }
            //shift the stream id down?
            this.writeID(out);
            out.write(this.getData());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
