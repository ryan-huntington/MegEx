/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 1
 * Class: CSI 4321
 *
 ************************************************/
package megex.serialization;
import java.io.Serial;
import java.util.Objects;

/**
 * Thrown if problem with attribute
 */
public class BadAttributeException extends Exception {
    /**
     * creates serialID
     */
    @Serial
    private static final long serialVersionUID = 121L;

    /**
     * attribute
     */
    private final String attribute;


    /**
     * Constructs a BadAttributeException with given message and attribute with no given cause
     * @param message detail message
     * @param attribute attribute related to problem
     *
     * @throws NullPointerException
     * if message or attribute is null
     */
    public BadAttributeException(String message, String attribute){
        super(message, null);
        Objects.requireNonNull(message, "message cannot be null");
        this.attribute = Objects.requireNonNull(attribute, "attribute cannot be null");
        //do I need to set cause equal to something?


    }

    /**
     * Constructs a BadAttributeException with given message, attribute, and cause
     * @param message detail message
     * @param attribute attribute related to problem
     * @param cause underlying cause (null is permitted and indicates no or unknown cause)
     *
     * @throws NullPointerException
     * if message or attribute is null
     */
    public BadAttributeException(String message, String attribute, Throwable cause){
        super(message, cause);
        Objects.requireNonNull(message, "message cannot be null");
        this.attribute = Objects.requireNonNull(attribute, "attribute cannot be null");

    }

    /**
     * Return attribute related to problem
     * @return attribute name
     */
    public String getAttribute(){
        return this.attribute;
    }


}
