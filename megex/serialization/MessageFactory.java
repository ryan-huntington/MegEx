/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 1
 * Class: CSI 4321
 *
 ************************************************/
package megex.serialization;

import com.twitter.hpack.Decoder;

import javax.sql.rowset.serial.SerialException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static megex.serialization.Data.DATA_TYPE;
import static megex.serialization.Headers.HEADERS_TYPE;
import static megex.serialization.MessageStatic.*;
import static megex.serialization.Settings.SETTINGS_TYPE;
import static megex.serialization.Window_Update.WINDOW_TYPE;

/**
 * Factory for deserialization and serializing messages
 */
public class MessageFactory {
    /**
     * minimum length for the message to be
     */
    private static final int MIN_WIRE_LENGTH = 6;



    /**
     * Creates a message factory
     */
    public MessageFactory(){

    }

    //textbook fromWire
    /**
     * Deserializes message from given bytes
     * @param msgBytes message bytes
     * @return specific Message resulting from deserialization
     * @throws BadAttributeException
     * if validation failure
     * @throws NullPointerException
     * if msgBytes is null
     */
    public Message decode(byte[] msgBytes) throws BadAttributeException {
        Objects.requireNonNull(msgBytes, "msgBytes cannot be null");
        Message msg = null;

        //should I do a check to make sure the length is good? sure why not
        if(msgBytes.length < MIN_WIRE_LENGTH || msgBytes.length > MAXMESSAGELENGTH){
            throw new BadAttributeException("Invalid length", "length", new IllegalArgumentException("invalid length"));
        } //hey look I already did it, good for me
        ByteArrayInputStream bs = new ByteArrayInputStream(msgBytes);
        DataInputStream in = new DataInputStream(bs);
        try{
            int type = in.readByte();
            int flags = in.readByte();
            int sid = in.readInt();
            if(sid < 0){
                sid = clear32bit(sid);
            }
            switch (type) {
                case DATA_TYPE -> msg = createData(sid, flags, in);

                case SETTINGS_TYPE -> msg = createSettings(sid);

                case WINDOW_TYPE -> msg = createWindows(sid, in, msgBytes);

                case HEADERS_TYPE -> msg = createHeaders(sid, flags, in);
                default -> throw new BadAttributeException("Unrecognized type", String.valueOf(type), new IllegalArgumentException("unrecognized type"));
            }
            //Do I want to do try/catch, or something else?
        }catch(IOException e){
            throw new BadAttributeException("Error with deserialization", "unknown", e);
        }
        //so this would be like taking the header + payload and turning
        //it into a message object based on the bytes received

        return msg;
    }


    //textbook toWire
    //encode should never throw bad attribute anything
    /**
     * Serializes message
     * @param msg message to serialize
     * @return serialized message
     * 
     * @throws NullPointerException
     * if msg is null
     */
    public byte[] encode(Message msg){
        Objects.requireNonNull(msg, "Message cannot be null");
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream); //converts ints


        //at this point, my code can follow 3 (4?) different paths
        //I can make a Data, Settings, or window message, each looks slightly different

        msg.serialize(out);
        try {
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] data = byteStream.toByteArray();
        return data;

    }

    /**
     * creates data type of message
     * @param sid streamID
     * @param flags the flags read in
     * @param in the input stream in use
     * @return the message object
     * @throws BadAttributeException
     * if error on attribute
     * @throws IOException
     * if io error
     */
    private static Message createData(int sid, int flags, DataInputStream in) throws BadAttributeException, IOException {
        int badFlag = 0x8, endFlag = 0x1;
        if (flags == badFlag) {
            throw new BadAttributeException("BAD FLAGS", "Error reading flags");
        }
        boolean isEnd = flags == endFlag;
        byte[] data = in.readAllBytes();
        return new Data(sid, isEnd, data);
    }

    /**
     * creates data type of message
     * @param sid streamID
     * @return the message object
     * @throws BadAttributeException
     * if error on attribute
     * @throws IOException
     * if io error
     */
    private static Message createSettings(int sid) throws BadAttributeException, IOException {
        int stream = 0x0;
        if (sid != stream) {
            throw new BadAttributeException("Deserialization error", "StreamID invalid", new SerialException("StreamID invalid"));
        }
        return new Settings();

    }
    /**
     * creates data type of message
     * @param sid streamID
     * @param in the input stream in use
     * @param msgBytes msgBytes sent into decoder
     * @return the message object
     * @throws BadAttributeException
     * if error on attribute
     * @throws IOException
     * if io error
     */
    private static Message createWindows(int sid, DataInputStream in, byte[] msgBytes) throws BadAttributeException, IOException {
        int windowLength = 10;
        if(msgBytes.length != windowLength){
            throw new BadAttributeException("Length invalid", "error reading", new IllegalArgumentException("missing payload"));
        }
        int increment = in.readInt();
        increment = clear32bit(increment);
        return new Window_Update(sid, increment);
    }
    /**
     * creates data type of message
     * @param sid streamID
     * @param flags the flags read in
     * @param in the input stream in use
     * @return the message object
     * @throws BadAttributeException
     * if error on attribute
     * @throws IOException
     * if io error
     */
    private static Message createHeaders(int sid, int flags, DataInputStream in) throws BadAttributeException, IOException {
        int endFlag = 0x1, badFlags = 0x8, reqFlag = 0x4, skipFlag = 0x20; //0x20 |
        boolean isEnd = ((flags & endFlag) != 0 );
        if((flags & badFlags) != 0 || (flags & reqFlag) == 0){
            throw new BadAttributeException("Invalid flags", "flags", new IllegalArgumentException("bad flags"));
        }
        if((flags & skipFlag) != 0){
            in.skip(5);
        }
        Headers msg = new Headers(sid, isEnd);

        List<String> names = new ArrayList<>();
        List<String> values = new ArrayList<>();

        Decoder decoder = new Decoder(MAXHEADERSZ, MAXHEADERTBLSZ);
        decoder.decode(in, ((name, value, sensitive) -> {
            names.add(b2s(name));
            values.add(b2s(value));

        }));
        for(int i = 0; i < names.size(); i++){
            msg.addValue(names.get(i), values.get(i));
        }
        decoder.endHeaderBlock();
        return msg;
    }


    /**
     * turn byte array into string
     * @param b byte[]
     * @return the string version
     */
    private static String b2s(byte[] b) {
        return new String(b, CHARENC);
    }
}
