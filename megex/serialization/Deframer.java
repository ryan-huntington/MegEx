/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 0
 * Class: CSI 4321
 *
 ************************************************/
package megex.serialization;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Represents a Deframer
 */
public class Deframer {
    private static final int MAXMESSAGELENGTH = 16384;
    private static final int MINMESSAGELENGTH = 0;
    private static final int HEADERLENGTH = 6;
    private static final int BYTEMASK = 0xFF;
    private final DataInputStream in;

    /**
     * Class creation of a Deframer class. Used to get message from InputStream
     *
     * @param in the InputStream that is being used for the client
     * @throws NullPointerException
     * if the InputStream is null
     */
    public Deframer(InputStream in){
        this.in = new DataInputStream(Objects.requireNonNull(in, "expected: not <null>"));


    }
    //like the books version of nextMsg

    /**
     * Gets the payload from a frame through the InputStream
     *
     * @return a byte array for payload
     * @throws IOException
     * if there is an error reading the payload
     * @throws EOFException
     * if there is a premature end to reading the frame
     * @throws IllegalArgumentException
     * if the length given is larger or smaller than what a frame could handle
     */
    public byte[] getFrame() throws IOException {
        byte b1, b2, b3;
        int length = -1;
        int byteSize = 8;
        //try eofexception and illegalargumentexc
        try{
            b1 = in.readByte();
            b2 = in.readByte();
            b3 = in.readByte();
        }catch (EOFException e){ //read less than 3 bytes
            throw new EOFException("Reached end too fast");
        }
        length = ((b1 & BYTEMASK) << (2 * byteSize)) | ((b2 & BYTEMASK) << byteSize) | (b3 & BYTEMASK);
        if(length < MINMESSAGELENGTH || length > MAXMESSAGELENGTH){
            throw new IllegalArgumentException("Length too large");
        }
        byte[] msg = new byte[length + HEADERLENGTH];

        //reads for the entirity of data.length
        in.readFully(msg);
        //throws eof if premature finish
        //IO is io error

        if(msg.length != length + HEADERLENGTH){
            throw new IOException("length given is too short for message");
        }

        return msg;

    }

}
