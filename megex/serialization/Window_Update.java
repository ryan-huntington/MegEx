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
 * Window_Update message
 */
public class Window_Update extends Message{
    /**
     * value for window type
     */
    public static final int WINDOW_TYPE = 0x8;
    /**
     * minimum accepted stream id
     */
    private static final int MIN = 1;
    private int increment;


    /**
     * Creates Window_Update message from given values
     * @param streamID stream ID
     * @param increment increment value
     * @throws BadAttributeException
     * if attribute invalid (see protocol spec)
     */
    public Window_Update(int streamID, int increment) throws BadAttributeException {
        //calling the setters because I do not want to keep replicating the same badAttributeExceptions
        super(streamID);
        this.setIncrement(increment);
        //this.setStreamID(streamID);
    }

    /**
     * Get increment value
     * @return increment value
     */
    public int getIncrement() {
        return increment;
    }

    /**
     * Set increment value
     * @param increment increment value
     * @throws BadAttributeException
     * if invalid
     */
    public void setIncrement(int increment) throws BadAttributeException {
        if(increment < MIN){
            throw new BadAttributeException("increment must be at least 1", "increment", new IllegalArgumentException("increment"));
        }
        this.increment = increment;

    }

    /**
     * Returns string of the form
     * @return string format of window_update
     */
    @Override
    public String toString(){
        return "Window_Update: StreamID=" + this.getStreamID() +
                " increment=" + this.getIncrement();
    }

    /**
     * writes the increment value in 31 bits
     * @param out output stream writing to
     * @throws IOException
     * if IOError is encountered
     */
    private void writeIncrement(OutputStream out) throws IOException {

        out.write((this.getIncrement() >>> (3 * byteShift)) & byteMask);
        out.write((this.getIncrement() >>> (2 * byteShift)) & byteMask);
        out.write((this.getIncrement() >>> byteShift) & byteMask);
        out.write(this.getIncrement() & byteMask);
    }

    @Override
    public byte getCode() {
        return WINDOW_TYPE;
    }

    /**
     * Serialize the message specific to window_update specs
     * @param out output stream
     */
    protected void serialize(OutputStream out){
        int flags = 0x0;
        try {
            out.write(this.getCode());
            out.write(flags);
            this.writeID(out);
            this.writeIncrement(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Tests if equal
     * @param o other object
     * @return if t=objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Window_Update that = (Window_Update) o;
        return increment == that.increment && getStreamID() == that.getStreamID() && getCode() == that.getCode();
    }

    /**
     * sets hashcode
     * @return hacshcode
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(increment);
        result = 31 * result + Objects.hash(this.getCode(), streamID);
        return result;
    }
}
