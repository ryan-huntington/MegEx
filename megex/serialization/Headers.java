/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/
package megex.serialization;

import com.twitter.hpack.Encoder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static megex.serialization.MessageStatic.*;

/**
 * Headers message
 *
 * Note: The order of name/value pairs must be the insertion order. Duplicate
 * names are not allowed; adding a duplicate name results in a replacement
 * name/value at the end of the list. Given the insertion order a=dog, b=cat,
 * a=bird, the name/value pair order will be b=cat, a=bird. Any output involving
 * names much preserve this order (getNames(), encoding, etc.)
 */
public class Headers extends Message{
    /**
     * value for header type
     */
    public static final int HEADERS_TYPE = 0x1;
    private static final int MIN = 0x0;
    //can have duplicate values, but not duplicate names?
    private boolean isEnd;

    private Map<String,String> map;

    //

    /**
     * Creates Headers message from given values
     * @param streamID stream ID
     * @param isEnd true if last header
     * @throws BadAttributeException
     * if attribute invalid (see protocol spec)
     */
    public Headers(int streamID, boolean isEnd) throws BadAttributeException{
        super(streamID);
        this.setEnd(isEnd);
        this.map = new LinkedHashMap<>();

    }

    @Override
    public byte getCode() {
        return HEADERS_TYPE;
    }

    /**
     * Specific to the Headers class, ensures that the streamID cannot be 0x0
     * @param streamID new stream id value
     * @throws BadAttributeException
     * if the streamID is 0x0
     */
    public void setStreamID(int streamID) throws BadAttributeException {
        if(streamID <= MIN){
            throw new BadAttributeException("Invalid streamID", "cannot use that value for streamID", new IllegalArgumentException("invalid streamID"));
        }
        this.streamID = clear32bit(streamID);
    }

    /**
     * Return end value
     * @return end value
     */
    public boolean isEnd(){
        return this.isEnd;
    }

    /**
     * Set end value
     * @param end end value
     */
    public void setEnd(boolean end){
        this.isEnd = end;
    }

    /**
     * Get the Headers value associated with the given name
     * @param name  the name for which to find the associated value
     * @return the value associated with the name or null if the association
     * cannot be found (e.g., no such name, invalid name, etc.)
     */
    public String getValue(String name){
        //given name, get the value associated
        return map.get(name);

    }

    /**
     * Get (potentially empty) set of names in Headers
     * @return (non-null) set of names in sort order
     */
    public Set<String> getNames(){
        //just get all the names
        return map.keySet();
    }

    /**
     * create the String representation of Headers
     * @return the sting for headers
     */
    @Override
    public String toString() {
        return "Headers: StreamID=" + getStreamID() + " isEnd=" + this.isEnd + " (" +
                map.entrySet().stream()
                        .map(entry -> "[" + entry.getKey() + "=" + entry.getValue() + "]")
                        .collect(Collectors.joining()) + ")";
    }

    /**
     * Add name/value pair to header. If the name is already contained in the header,
     * the corresponding value is replaced by the new value.
     * @param name name to add
     * @param value value to add/replace
     * @throws BadAttributeException
     * if invalid name or value
     */
    public void addValue(String name, String value) throws BadAttributeException{
        if(name == null || value == null){
            throw new BadAttributeException("Name/value cannot be null", "name/value", new NullPointerException("null name/value"));
        }


        if(nameCheck(name) && valueCheck(value)){
            //instead of replacing old value, should delete and put at end of map
            map.remove(name);
            //if map.put does not keep insertion order, remove from map first, then insert.
            map.put(name, value);
        }
        else{
            throw new BadAttributeException("invalid name/value pair", "name/value", new IllegalArgumentException("name/value"));
        }

    }


    /**
     * Format the byte array for the Data message
     * @param out outputstream in use
     */
    protected void serialize(OutputStream out) {
        int endFlag = (this.isEnd) ? 0x1 : 0x0;
        int reqFlag = 0x4;
        Encoder encoder = new Encoder(MAXHEADERTBLSZ);

        try {
            out.write(this.getCode());
            out.write(endFlag | reqFlag);
            this.writeID(out);

            //now I need to write the name/value pairs
            for (String s : this.getNames()){
                encoder.encodeHeader(out, s2b(s), s2b(this.getValue(s)), false);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        if (!super.equals(o)) return false;
        Headers headers = (Headers) o;
        return isEnd == headers.isEnd && Objects.equals(map, headers.map);
    }

    /**
     * generates the hashcode for object
     * @return unique hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isEnd, map);
    }

    /**
     * checks if valid
     * @param value string value
     * @return
     * if it passes the checks
     */
    public boolean nameCheck(String value){
        return (!Objects.equals(value, "")) && (ncharCheck(value) && value.chars().noneMatch(Character::isUpperCase));
    }

    /**
     * checks if valid
     * @param value string value
     * @return
     * if it passes the checks
     */
    public boolean valueCheck(String value){
        //if not, just add the same thing that namecheck has
        return (!Objects.equals(value, "")) &&(vcharCheck(value));
    }
    /**
     * checks if valid
     * @param value string value
     * @return
     * if it passes the checks
     */
    public boolean ncharCheck(String value){
        return (vischarCheck(value) && !delimCheck(value));
    }
    /**
     * checks if valid
     * @param value string value
     * @return
     * if it passes the checks
     */
    public boolean vischarCheck(String value){
        return value.chars().allMatch(c -> c >= 0x21 && c <= 0x7E);
    }
    /**
     * checks if valid
     * @param value string value
     * @return
     * if it passes the checks
     */
    public boolean delimCheck(String value){
        Character[] validChar = {'(', ')', ',', '/', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '{', '}'};
        Set<Character> allowed = new HashSet<>(Arrays.asList(validChar));
        return value.chars().anyMatch(c -> allowed.contains((char) c));

    }

    /**
     * checks if valid
     * @param value string value
     * @return
     * if it passes the checks
     */
    public boolean vcharCheck(String value){
        int sp = 0x20, htab = 0x9;
        return (value.chars().allMatch(c -> c == sp || c == htab || ( c >= 0x21 && c <= 0x7E )));
    }





    /**
     * turn string into byteArray (might delete this function)
     * @param v string to be converted
     * @return
     * the byte[] value
     */
    private static byte[] s2b(String v) {
        return v.getBytes(CHARENC);
    }




}
