/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 1
 * Class: CSI 4321
 *
 ************************************************/
package megex.serialization;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Settings message
 */
public class Settings extends Message{
    /**
     * value for settings type
     */
    public static final int SETTINGS_TYPE = 0x4;
    private static final int STREAM_ID = 0x0;
    private static final int FLAGS = 0x1;
    //type = 0x4
    /**
     * Creates Settings message
     * @throws BadAttributeException
     * if attribute invalid (not thrown in this case)
     */
    public Settings() throws BadAttributeException {
        super(STREAM_ID);
    }

    /**
     * Returns string of the form
     * @return string format of settings object
     */
    @Override
    public String toString(){
        return "Settings: StreamID=0";
    }

    @Override
    public byte getCode() {
        return SETTINGS_TYPE;
    }

    /**
     * ensures streamID is valid
     * @param streamId new stream id value
     * @throws BadAttributeException
     * if invalid streamID
     */
    public void setStreamID(int streamId) throws BadAttributeException {
        if(streamId != STREAM_ID){
            throw new BadAttributeException("Cannot set streamID", "settings streamID", new IllegalArgumentException("invalid"));
        }
        this.streamID = streamId;
    }


    /**
     * serialize the message specific to settings spec
     * @param out output stream in use
     */
    protected void serialize(OutputStream out){
        try{
            out.write(this.getCode());
            out.write(FLAGS);
            this.writeID(out);
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }




}
