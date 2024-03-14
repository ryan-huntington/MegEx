package megex.serialization; /************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 0
 * Class: CSI 4321
 *
 ************************************************/

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Represents a Framer
 */
public class Framer {
    private static final int MAXMESSAGELENGTH = 16384;
    private static final int MINMESSAGELENGTH = 0;
    private DataOutputStream os;

    /**
     * Class creation of the Framer class. Builds frame to send through OutputStream
     *
     * @param out the OutputStream designated to send the frame through
     * @throws NullPointerException
     * if the OutputStream is null
     */
    public Framer(OutputStream out){
        this.os = new DataOutputStream(Objects.requireNonNull(out, "expected: not <null>"));

    }
    //like the books version of frameMsg
    /**
     * Creates a frame and writes it to the class' OutputStream
     *
     * @param message the header and the payload to be sent in the frame
     *
     * @throws IOException
     * if the payload is less than 1 byte
     * @throws IllegalArgumentException
     * if the message is too long
     * @throws NullPointerException
     * if the message is null
     */
    public void putFrame(byte [] message) throws IOException {
        Objects.requireNonNull(message);
        int byteMask = 0xff;
        int byteShift = 8;
        int payloadLength = message.length - 6;
        //payload should have a length, but maybe check to see if there is actually a header/
        //that could be io error

        if (payloadLength > MAXMESSAGELENGTH){
            throw new IllegalArgumentException("Message too long");
        }
        if (payloadLength < MINMESSAGELENGTH){
            throw new IllegalArgumentException("Message too short");
        }

        //write the length as 3 bytes (3 octets)
        os.write((payloadLength >> (2 * byteShift)) & byteMask);
        os.write((payloadLength >> byteShift) & byteMask);
        os.write(payloadLength & byteMask);

        //write the header
        //so the leader is actually included in the message, so i will not worry
        //about it yet

        //write the message
        os.write(message);

        os.flush();

    }

}
