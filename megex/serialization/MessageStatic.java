/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 1
 * Class: CSI 4321
 *
 ************************************************/
package megex.serialization;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * class used for static variables throughout messages
 */
public class MessageStatic {




    /**
     * value for byte mask
     */
    public static final int byteMask = 0xff;
    /**
     * value for byteshift
     */
    public static final int byteShift = 8;
    /**
     * max headers
     */
    public static final int MAXHEADERSZ = 1024;
    /**
     * max headers
     */
    public static final int MAXHEADERTBLSZ = 1024;
    /**
     * charset in use
     */
    public static final Charset CHARENC = StandardCharsets.US_ASCII;

    /**
     * max message length including the header ?
     */
    public static final int MAXMESSAGELENGTH = 16390;

    /**
     * Used to ensure that the 32nd bit of integers is ignored and automatically 0
     * @param value integer that is intended to be cleared
     * @return the integer that is ensured to be positive
     */
    public static int clear32bit(int value){
        int shift = 1;
        int bit = 31;
        return (value & (~(shift << bit)));
    }

    /**
     * constructor made private so nothing can call this class
     */
    private MessageStatic(){}


}
