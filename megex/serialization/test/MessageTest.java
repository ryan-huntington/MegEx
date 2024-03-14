/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 1
 * Class: CSI 4321
 *
 ************************************************/
package megex.serialization.test;


import com.twitter.hpack.Encoder;
import megex.serialization.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static megex.serialization.Data.DATA_TYPE;
import static megex.serialization.MessageStatic.*;
import static megex.serialization.Settings.SETTINGS_TYPE;
import static megex.serialization.Window_Update.WINDOW_TYPE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * class that holds all tests for messages
 */
public class MessageTest {
    /**
     * private constructor
     */
    private MessageTest(){}

    MessageFactory factory = new MessageFactory();

    /**
     * tests specifically for Data
     */
    @Nested
    @DisplayName("Data message tests")
    public class DataTests {
        byte[] data = {0x0, 0x1, 0x2, 0x3};
        Message msg = new Data(0x12345678, false, data);
        byte[] expected = {0x0, 0x0, 0x12, 0x34, 0x56, 0x78, 0x0, 0x1, 0x2, 0x3};

        /**
         * Constructor in order to have objects span all tests
         * @throws BadAttributeException
         * if message is created incorrectly
         */
        private DataTests() throws BadAttributeException {
        }

        /**
         * Tests that creating objects has the correct code
         */
        @Test
        @DisplayName("Test that type is 0x0")
        public void testTypeCorrect(){
            try {
                Message msg = new Data(1, true, new byte[]{0x0});
                Message msg2 = new Data(1, true, new byte[]{0x0});
                Message msg3 = new Data(1, false, new byte[]{0x0});
                BadAttributeException bad = new BadAttributeException("hello", "helo", new IllegalArgumentException("hel"));

                assertEquals(DATA_TYPE, msg.getCode());
                assertEquals(DATA_TYPE, msg2.getCode());
                assertEquals(DATA_TYPE, msg3.getCode());
            } catch (BadAttributeException e) {
                throw new RuntimeException(e);
            }

        }


        //THESE ARE BOTH FLAGS

        /**
         * Tests that the constructor sets the end flag correctly
         */
        @Test
        @DisplayName("Test for endStream")
        public void testEndStream(){
            //end stream is a type of flag that can be set (0x1)
            try {
                Message msg = new Data(1, true, new byte[]{0x0});
                Message msg2 = new Data(1, true, new byte[]{0x0});
                Message msg3 = new Data(1, false, new byte[]{0x0});

                assertTrue(((Data) msg).isEnd());
                assertTrue(((Data) msg2).isEnd());
                assertFalse(((Data) msg3).isEnd());
            } catch (BadAttributeException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * decodes a message and makes sure its value for endstream is expected
         * @throws BadAttributeException
         * if error decoding
         */
        @Test
        @DisplayName("Test endstream from encoder")
        public void testOtherEnd() throws BadAttributeException {
            byte[] exampleEndFlag = {0x0, 0x1, 0x12, 0x0, 0x0, 0x0, 0x1};
            Message returned = factory.decode(exampleEndFlag);

            assertTrue(((Data)returned).isEnd());

        }

        /**
         * Ensure that bad flags does not throw an error incorrectly
         */
        @Test
        @DisplayName("Test no badflags thrown")
        public void testGoodFlags(){
            //create facotry
            //create handcrafted byte message (no bad flags)
            byte[] exampleGoodFlags = {0x0, 0x0, 0x1, 0x0, 0x0, 0x0, 0x1};
            //decode it
            assertDoesNotThrow(() -> {
                factory.decode(exampleGoodFlags);
            });
            //assert no errors were thrown with the decoding

        }

        /**
         * if there are bad flags, an error is thrown
         */
        @Test
        @DisplayName("Test bad flags")
        public void testBadFlags(){
            //create facotry
            //create handcrafted byte message (bad flags)
            byte[] exampleBadFlags = {0x0, 0x8, 0x12, 0x3, 0x0, 0x0, 0x1};
            //decode it
            //assert errors were thrown with the decoding
            assertThrows(BadAttributeException.class, () -> {
                factory.decode(exampleBadFlags);
            });
        }

        /**
         * tests that an invalid streamID throws error
         */
        @Test
        @DisplayName("ensure desrial error on streamID = 0x0")
        public void serialError(){
            byte[] exampleBadStreamID = {0x0, 0x8, 0x0, 0x0, 0x0, 0x0, 0x1};
            assertThrows(BadAttributeException.class, () -> {
                Message returned = factory.decode(exampleBadStreamID);
            });

        }

        /**
         * tests passing in a null value to encode
         */
        @Test
        @DisplayName("Test null encode")
        public void testNullEncode(){
            assertThrows(NullPointerException.class, () -> {
                factory.encode(null);
            });
        }

        /**
         * tests passing in null value to decode
         */
        @Test
        @DisplayName("Test null decode")
        public void testNullDecode(){
            assertThrows(NullPointerException.class, () -> {
                factory.decode(null);
            });
        }

        /**
         * tests that encoding a message gives what is expected
         */
        @Test
        @DisplayName("Test encoding data type")
        public void normalEncode(){
            //create data message
            //create message factory
            //encode it
            byte[] returned = factory.encode(msg);
            //assert that the bytes match a handcrafted example
            assertArrayEquals(expected, returned);

        }

        /**
         * tests that decoding gives objects with same value
         * @throws BadAttributeException
         * if error when decoding
         */
        @Test
        @DisplayName("Test normal decoding data type")
        public void normalDecode() throws BadAttributeException {
            //create message
            //create message factory
            //create handcrafted message
            //decode message
            Message returned = factory.decode(expected);
            //assert that the values of the objects are equal
            assertEquals(msg.toString(), returned.toString());

        }

        /**
         * test given by donahoo
         * @throws BadAttributeException
         * if error
         */
        @Test
        public void DonTest() throws BadAttributeException {
            byte[] dat = {0, 1, (byte) 0x80, 0, 0, 1, 1, 2, 3, 4, 5};
            Message returned = factory.decode(dat);
        }



    }

    /**
     * tests for the settings object
     */
    @Nested
    @DisplayName("Settings tests")
    public class SettingsTests {
        Message msg = new Settings();


        /**
         * creates test with the values needed
         * @throws BadAttributeException
         * if error creating settings object
         */
        private SettingsTests() throws BadAttributeException {
        }

        /**
         * Tests that the constructor correctly sets the type
         */
        @Test
        @DisplayName("Test that type is 0x4")
        public void testTypeCorrect(){
            try {
                Message msg = new Settings();
                Message msg2 = new Settings();
                Message msg3 = new Settings();

                assertEquals(SETTINGS_TYPE, msg.getCode());
                assertEquals(SETTINGS_TYPE, msg2.getCode());
                assertEquals(SETTINGS_TYPE, msg3.getCode());
            } catch (BadAttributeException e) {
                throw new RuntimeException(e);
            }

        }

        /**
         * ensures that the encode sets the settings type correctly
         */
        @Test
        @DisplayName("Type must be 0x4")
        public void testEncodeType() {
            //create the settings object
            //Message msg = new Settings();
            //create the message factory
            //MessageFactory factory = new MessageFactory();
            //call the encode function
            byte[] data = factory.encode(msg);
            //assert that the flags bit = 0x1
            assertEquals(SETTINGS_TYPE, data[0]);
        }

        /**
         * tests that flags are 0x1 on encode
         *
         */
        @Test
        @DisplayName("Flags must be 0x1")
        public void testFlags()  {
//            //create the settings object
//            Message msg = new Settings();
//            //create the message factory
//            MessageFactory factory = new MessageFactory();
            //call the encode function
            byte[] data = factory.encode(msg);
            //assert that the flags bit = 0x1
            assertEquals(0x1, data[1]);
        }

        /**
         * tests that the flags are ignored
         * @throws BadAttributeException
         * if error on decode
         */
        @Test
        @DisplayName("Flags not set to 0x1")
        public void testIncorrectFlags() throws BadAttributeException {
            //create the settings object
            byte[] exampleSettingBadFlags = {0x4, 0x2, 0x0, 0x0, 0x0, 0x0};
            //create the message factory
            //MessageFactory factory = new MessageFactory();
            //call the decode function on hand built byte array with incorrect flags
            assertDoesNotThrow(() -> {
                Message msg = factory.decode(exampleSettingBadFlags);

            });
            //assert that it was ignored

        }

        /**
         * ensures that they payload length is 0 on encode
         */
        @Test
        @DisplayName("Payload bytes = 0")
        public void payloadIsZero() {
            //create the settings object
            //Message msg = new Settings();
            //create the message factory
            //MessageFactory factory = new MessageFactory();
            //call the encode function
            byte[] data = factory.encode(msg);
            //assert that the payload size is 0
            assertEquals(6, data.length);

        }

        /**
         * if the payload length is not 0, the decode ignores it
         */
        @Test
        @DisplayName("Error for payload not zero")
        public void payloadNotZero(){
            //create the settings object
            byte[] exampleSettingUnintentionalPayload = {0x4, 0x1, 0x0, 0x0, 0x0, 0x0, 0x55, 0x00};
            //create the message factory
            //MessageFactory factory = new MessageFactory();
            //call the decode function on hand built byte array with payload
            assertDoesNotThrow(() -> {
                Message msg = factory.decode(exampleSettingUnintentionalPayload);
            });
            //assert that it was ignored
        }

        /**
         * encoding the stream makes streamID = 0x0
         */
        @Test
        @DisplayName("streamID must be 0")
        public void testStreamID()  {
            //create the settings object
            //Message msg = new Settings();
            //create the message factory
            //MessageFactory factory = new MessageFactory();
            //call the encode function
            byte[] data = factory.encode(msg);
            //assert that the streamID is 0
            byte[] exampleStreamID = {0x0, 0x0, 0x0, 0x0};
            assertArrayEquals(exampleStreamID, Arrays.copyOfRange(data, 2, 6));


            //or
            //just call constructor, assert that the object has ID of 0
            //no, test the actual encoder
        }

        /**
         * if the streamID is not 0x0, then an error is thrown
         */
        @Test
        @DisplayName("StreamID not 0")
        public void testInvalidStreamId(){
            byte[] exampleSettingBadStreamID = {0x4, 0x1, 0x0, 0x0, 0x0, 0x01};
            //create the message factory
            //MessageFactory factory = new MessageFactory();
            //call the decode function on hand built byte array streamID not 0
            assertThrows(BadAttributeException.class, () -> {
                factory.decode(exampleSettingBadStreamID);
            });
            //assert that deserialization error is thrown
        }

        /**
         * Normal test to encode
         */
        @Test
        @DisplayName("Test encoding setting type")
        public void normalEncode(){
            //create setting message
            //create message factory
            //encode it
            byte[] data = factory.encode(msg);
            //assert that the bytes match a handcrafted example
            byte[] example = {0x4, 0x1, 0x0, 0x0, 0x0, 0x0};
            assertArrayEquals(example, data);

        }

        /**
         * normal test to decode
         * @throws BadAttributeException
         * if error on decode
         */
        @Test
        @DisplayName("Test normal decoding setting type")
        public void normalDecode() throws BadAttributeException {
            //create message
            //create message factory
            //create handcrafted message
            byte[] example = {0x4, 0x1, 0x0, 0x0, 0x0, 0x0};
            //decode message
            Message returned = factory.decode(example);
            //assert that the values of the objects are equal
            assertEquals(msg.getCode(), returned.getCode());
            assertEquals(msg.getStreamID(), returned.getStreamID());

        }


    }

    /**
     * tests for window_update class
     */
    @Nested
    @DisplayName("Window_Update Tests")
    public class Window_UpdateTests{
        Message msg = new Window_Update(0x12345678, 1);
        byte[] expected = {0x8, 0x0, 0x12, 0x34, 0x56, 0x78, 0x0, 0x0, 0x0, 0x1};


        /**
         * private constructor
         * @throws BadAttributeException
         * if error creating objects
         */
        private Window_UpdateTests() throws BadAttributeException {
        }

        /**
         * Tests constructor to correctly set the type
         */
        @Test
        @DisplayName("Test that type is 0x8")
        public void testTypeCorrect(){
            try {
                Message msg = new Window_Update(1, 1);
                Message msg2 = new Window_Update(2, 36);
                Message msg3 = new Window_Update(4, 10000);

                assertEquals(WINDOW_TYPE, msg.getCode());
                assertEquals(WINDOW_TYPE, msg2.getCode());
                assertEquals(WINDOW_TYPE, msg3.getCode());
            } catch (BadAttributeException e) {
                throw new RuntimeException(e);
            }

        }

        /**
         * encode correctly sets type as well
         */
        @Test
        @DisplayName("Encoding correctly uses type")
        public void codeCorrectEncode(){
            //encode
            byte[] data = factory.encode(msg);
            //assert that type is 0x8
            assertEquals(WINDOW_TYPE, data[0]);
        }

        /**
         * makes sure r bit is unset on normal value
         * @throws BadAttributeException
         * if error on encoding
         */
        @Test
        @DisplayName("R bit is unset")
        public void RUnset() throws BadAttributeException {
            int BYTEMASK = 0xFF;
            assertDoesNotThrow(() -> {
                factory.encode(msg);
            });
            byte[] returned = factory.encode(msg);
            int increment = ((returned[6] & BYTEMASK) << 24) | ((returned[7] & BYTEMASK) << 16) | ((returned[8] & BYTEMASK) << 8) | (returned[9] & BYTEMASK);
            assertEquals(1, increment);



        }

        /**
         * if the rbit is set, tests to make sure it is ignored for the increment value
         * @throws BadAttributeException
         * if error on creating window
         */
        @Test
        @DisplayName("R invalid")
        public void RSet() throws BadAttributeException {
            int BYTEMASK = 0xFF;

            //encode

            //assert that the rbit is unset on the integer (meaning it cannot be negative)
            //6789 are the increment size
            //int increment = ((returned[6] & BYTEMASK) << 24) | ((returned[7] & BYTEMASK) << 16) | ((returned[8] & BYTEMASK) << 8) | (returned[9] & BYTEMASK);
            BadAttributeException exception = assertThrows(BadAttributeException.class, () -> {
                Message window = new Window_Update(0x12345678, 0x80000000);
                byte[] returned = factory.encode(window);
            });


        }

        /**
         * asserts that encoding the flags are not set
         */
        @Test
        @DisplayName("Flags are not set")
        public void flagsNotSet(){
            //encode
            byte[] data = factory.encode(msg);
            //assert that flags are zero?
            assertEquals(0x0, data[1]);
        }

        /**
         * if the flags were improperly set, do not throw error
         * @throws BadAttributeException
         * if error on decode
         */
        @Test
        @DisplayName("flags improperly set")
        public void improperFlags() throws BadAttributeException {
            byte[] exampleBadFlags = {0x8, 0x1, 0x12, 0x34, 0x56, 0x78, 0x0, 0x0, 0x0, 0x1};
            //decode
            assertDoesNotThrow(() -> {
                Message returned = factory.decode(exampleBadFlags);

            });
            //assert no errors thrown

        }

        /**
         * if the increment size is invalid (not enough bytes), throws error
         */
        @Test
        @DisplayName("Test with invalid increment size")
        public void testIncrement(){
            //missing 2 bytes of the int
            byte[] exampleBadIncrement = {0x8, 0x1, 0x12, 0x34, 0x56, 0x78, 0x0, 0x0};
            //use factory to decode
            //do I want it to throw exception, or do I want to fill it in with 0?
            //well I want to have exactly 32 bits in the payload, so anything less would be a bad attribute, so error
            assertThrows(BadAttributeException.class, () -> {
                factory.decode(exampleBadIncrement);
            });

            //should I throw an error or no?
        }

        /**
         * a normal encoding on the window_update object
         */
        @Test
        @DisplayName("Test encoding window type")
        public void normalEncode(){
            //create window message
            //create message factory
            //encode it
            byte[] returned = factory.encode(msg);
            //assert that the bytes match a handcrafted example
            assertArrayEquals(expected, returned);

        }

        /**
         * normal decoding of byte[] of window object
         * @throws BadAttributeException
         * if error on decoding
         */
        @Test
        @DisplayName("Test normal decoding window type")
        public void normalDecode() throws BadAttributeException {
            //create message
            //create message factory
            //create handcrafted message
            //decode message
            Message returned = factory.decode(expected);
            //assert that the values of the objects are equal
            assertEquals(msg.toString(), returned.toString());

        }

        /**
         * testing decode
         * @throws IOException is ioerror
         * @throws BadAttributeException if badattribute
         */
        @DisplayName("decode")
        @Test
        void testDecode() throws IOException, BadAttributeException {
            MessageFactory f = new MessageFactory();
            Window_Update w = (Window_Update) f.decode(new byte[] { 8, 0, 0, 0, 0, 5, 0, 0, 0, 10 });
            assertAll(() -> assertEquals(5, w.getStreamID()), () -> assertEquals(10, w.getIncrement()),
                    () -> assertEquals(8, w.getCode()));
        }

        /**
         * test BadAttribute
         */
        @Test
        @DisplayName("assert not null message")
        public void notNull(){
            assertNotNull(new BadAttributeException("message", "yes", new Exception()).getMessage());
            assertThrows(NullPointerException.class, () -> new BadAttributeException(null, "type", null));
        }

    }


    /**
     * headers test
     */

    @Nested
    @DisplayName("Headers tests")
    public class HeadersTests {

        Message msg = new Headers(0x12345678, false);
        byte[] msgBytes = {0x1, 0x4, 0x12, 0x34, 0x56, 0x78};
        Headers example = new Headers(0x12, true);
        byte[] exampleBytes = {0x1, 0x5, 0x0, 0x0, 0x0, 0x12};

        /**
         * basic private constructor
         * @throws BadAttributeException
         * if error
         */
        private HeadersTests() throws BadAttributeException {
        }

        /**
         * tests normal encode
         * @throws BadAttributeException
         * if error
         */
        @Test
        @DisplayName("normal encode")
        public void normalEncode() throws BadAttributeException {


            //encode the test
            //ensure that it matches the expected result

        }

        /**
         * tests normal decode
         * @throws BadAttributeException
         * if error
         */
        @DisplayName("decode")
        @Test
        void testDecode() throws BadAttributeException {
            byte[] msgBytes = {0x01, 0x04, 0x12, 0x34, 0x56, 0x78, 0x42, (byte) 0x84, (byte) 0xE2, 0x3A, 0x0D, 0x27};
            Headers head = new Headers(0x12345678, false);
            head.addValue(":method", "Valid");
            //decode the handcrafted example
            Message result = factory.decode(msgBytes);
            //assert that the result equals message
            assertEquals(head, result);
            //also test that the hash and equals works through this

        }

        /**
         * tests endsstream
         * @throws BadAttributeException
         * if error
         */
        @Test
        @DisplayName("test endstream correctly set")
        public void testEndStream() throws BadAttributeException {
            //decode examples with their endflag set respectively
            byte[] noEndFlag = {0x1, 0x4, 0x12, 0x34, 0x56, 0x78};
            byte[] endFlag = {0x1, 0x5, 0x12, 0x34, 0x56, 0x78};
            //factory.decode()
            Message noEnd = factory.decode(noEndFlag);
            Message end = factory.decode(endFlag);
            //assert that the constructor correctly set them
            assertFalse(((Headers)noEnd).isEnd());
            assertTrue(((Headers)end).isEnd());

        }

        /**
         * tests the required flags
         */
        @Test
        @DisplayName("test required flag set")
        public void requiredFlag(){
            byte[] reqFlag = {0x1, 0x4, 0x12, 0x34, 0x56, 0x78};
            byte[] noReq = {0x1, 0x3, 0x12, 0x34, 0x56, 0x78};
            //decode example
            assertDoesNotThrow(() -> factory.decode(reqFlag));
            assertThrows(BadAttributeException.class, () -> factory.decode(noReq));
            //test what the required bit is set
            //decode example without bit set
            //assert that error is thrown if not set

        }

        /**
         * tests invalid flags
         */
        @Test
        @DisplayName("Error on invalid flags")
        public void invalidFlags(){
            byte[] goodFlags = {0x1, 0x4, 0x12, 0x34, 0x56, 0x78};
            byte[] badFlags1 = {0x1, 0xC, 0x12, 0x34, 0x56, 0x78};
            byte[] badFlags3 = {0x1, 0x2C, 0x12, 0x34, 0x56, 0x78};
            //create examples with invalid flags
            //assert that the invalid flags throw an error
            assertDoesNotThrow(() -> factory.decode(goodFlags));
            assertThrows(BadAttributeException.class, () -> factory.decode(badFlags1));
            assertThrows(BadAttributeException.class, () -> factory.decode(badFlags3));

        }

        /**
         * tests the streamID
         * @throws BadAttributeException
         * if error
         */
        @Test
        @DisplayName("ensure streamID valid check")
        public void checkStreamID() throws BadAttributeException {
            byte[] validStreamID = {0x1, 0x4, 0x12, 0x34, 0x56, 0x78};
            byte[] invalidStreamID = {0x1, 0x4, 0x0, 0x0, 0x0, 0x0};
            assertDoesNotThrow(() -> factory.decode(validStreamID));
            assertThrows(BadAttributeException.class, () -> factory.decode(invalidStreamID));

            assertThrows(BadAttributeException.class, () -> new Headers(0x0, false));
            Headers head = new Headers(0x12, false);
            assertThrows(BadAttributeException.class, () -> head.setStreamID(0x0));
            assertThrows(BadAttributeException.class, () -> head.setStreamID(-1));
            //check multiple streamID, assert all are valid

        }


        /**
         * tests the headers block
         * @throws BadAttributeException
         * if error
         */
        @Test
        @DisplayName("get headers in order")
        public void headersBlockTest() throws BadAttributeException {
            Headers head = new Headers(0x12345678, false);
            head.addValue(":method", "valid");
            head.addValue("stillvalid", "Visible? yes it is");
            head.addValue(":method", "replace");

            Map<String, String> expected = new LinkedHashMap<>();
            expected.put("stillvalid", "Visible? yes it is");
            expected.put(":method", "replace");
            //make sure encoding is correct order
            //get names should be correct order

            //this makes sure the first element is the same between both
            assertEquals(expected.keySet().stream().findFirst(), head.getNames().stream().findFirst());



        }

        /**
         * testing the getValue function
         * @throws BadAttributeException
         * if error
         */
        @Test
        @DisplayName("getting value back")
        public void testHeaderValue() throws BadAttributeException {
            Headers head = new Headers(0x12345678, false);
            head.addValue(":method", "valid");
            assertEquals("valid", head.getValue(":method"));
            head.addValue(":method", "newvalid");
            assertEquals("newvalid", head.getValue(":method"));

        }


        /**
         * tests teh delimiterCheck function
         * @throws BadAttributeException
         * if the headers is invalid
         */
        @Test
        @DisplayName("testing the header checks")
        public void checkDelim() throws BadAttributeException {
            Headers ms = new Headers(8, false);
            String yes = "(),////<<]{";
            String nonValid = "((()) ..";
            String alsoNonValid = "yello :))";
            String wink = ";)";

            assertTrue(ms.delimCheck(nonValid));
            assertTrue(ms.delimCheck(alsoNonValid));
            assertTrue(ms.delimCheck(yes));
            assertTrue(ms.delimCheck(wink));

        }
        /**
         * tests teh delimiterCheck function
         * @throws BadAttributeException
         * if the headers is invalid
         */
        @Test
        @DisplayName("testing the name header checks")
        public void checkName() throws BadAttributeException {
            String name = "nouppercase";
            String uppercase = "UppercaseInvalid";
            String inclusive = "!stillvalid~";
            String space = "no space allowed";
            String withDelim = "thereisdelim/uhoh";

            String additional = "";

            assertTrue(example.nameCheck(name));
            assertFalse(example.nameCheck(uppercase));
            assertTrue(example.nameCheck(inclusive));
            assertFalse(example.nameCheck(space));
            assertFalse(example.nameCheck(withDelim));


        }

        /**
         * tests teh delimiterCheck function
         * @throws BadAttributeException
         * if the headers is invalid
         */
        @Test
        @DisplayName("testing the value header checks")
        public void checkValue() throws BadAttributeException {
            String name = "nouppercase";
            String uppercase = "UppercaseInvalid";
            String inclusive = "!stillvalid~";
            String space = "no space allowed";
            String withDelim = "thereisdelim/uhoh";

            assertTrue(example.valueCheck(name));
            assertTrue(example.valueCheck(uppercase));
            assertTrue(example.valueCheck(inclusive));
            assertTrue(example.valueCheck(space));
            assertTrue(example.valueCheck(withDelim));

        }

        /**
         * tests teh NCHARCheck function
         * @throws BadAttributeException
         * if the headers is invalid
         */
        @Test
        @DisplayName("testing the nchar header checks")
        public void checkNchar() throws BadAttributeException {
            String name = "nouppercase";
            String uppercase = "UppercaseValid";
            String inclusive = "!stillvalid~";
            String space = "no space allowed";
            String withDelim = "thereisdelim/uhoh";
            String onlyDelim = "///";

            assertTrue(example.ncharCheck(name));
            assertTrue(example.ncharCheck(uppercase));
            assertTrue(example.ncharCheck(inclusive));
            assertFalse(example.ncharCheck(space));
            assertFalse(example.ncharCheck(withDelim));
            assertFalse(example.ncharCheck(onlyDelim));


        }

        /**
         * tests teh delimiterCheck function
         * @throws BadAttributeException
         * if the headers is invalid
         */
        @Test
        @DisplayName("testing the vischar header checks")
        public void checkVischar() throws BadAttributeException {
            String name = "nouppercase";
            String uppercase = "UppercaseInvalid";
            String inclusive = "!stillvalid~";
            String space = "no space allowed";
            String withDelim = "thereisdelim/uhoh";

            assertTrue(example.vischarCheck(name));
            assertTrue(example.vischarCheck(uppercase));
            assertTrue(example.vischarCheck(inclusive));
            assertFalse(example.vischarCheck(space));
            assertTrue(example.vischarCheck(withDelim));


        }

        /**
         * tests teh vcharCheck function
         * @throws BadAttributeException
         * if the headers is invalid
         */
        @Test
        @DisplayName("testing the vchar header checks")
        public void checkVchar() throws BadAttributeException {
            String name = "nouppercase";
            String uppercase = "UppercaseInvalid";
            String inclusive = "!stillvalid~";
            String space = "no space allowed";
            String withDelim = "thereisdelim/uhoh";
            String sp = " ";
            String htab = String.valueOf(0x9);

            assertTrue(example.vcharCheck(name));
            assertTrue(example.vcharCheck(uppercase));
            assertTrue(example.vcharCheck(inclusive));
            assertTrue(example.vcharCheck(space));
            assertTrue(example.vcharCheck(withDelim));
            assertTrue(example.vcharCheck(sp));
            assertTrue(example.vcharCheck(htab));


        }

        /**
         * Donahoo test
         * @throws IOException if io error
         * @throws BadAttributeException if problem creating header
         */
        @Test
        @DisplayName("Don test")
        public void donTest() throws IOException, BadAttributeException {
            // Expected
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(new byte[] { 1, 0x4, 0, 0, 0, 1 });
            new Encoder(1024).encodeHeader(out, s2b("x"), s2b("1"), false);

            // Actual
            Headers h = new Headers(1, false);
            h.addValue("x", "1");
            assertArrayEquals(out.toByteArray(), new MessageFactory().encode(h));
        }

        /**
         * Tests for the name again
         */
        @Test
        @DisplayName("more name test")
        public void testNameMore(){
            String uppercase = "CANTHAVEUPPERCASE";
            String uppercase2 = "Nouppercase";
            String noSpaces = "cant have spaces";
            String noDelim = "no/delim";
            String noDelim2 = "///////;;()/\\<-::";
            String onlyVis = "\tno";

            assertFalse(example.nameCheck(uppercase));
            assertFalse(example.nameCheck(uppercase2));
            assertFalse(example.nameCheck(noSpaces));
            assertFalse(example.nameCheck(noDelim));
            assertFalse(example.nameCheck(noDelim2));
            assertFalse(example.nameCheck(onlyVis));
            assertTrue(example.nameCheck(":validname"));
            assertFalse(example.nameCheck(""));


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

        /**
         * another test from donahoo, should fail
         */
        @Test
        public void donTest2(){
            byte[] data = {1, 36, 0, 0, 0, 4, -126, 68, -125, 98, 83, -97, 64, -122, -71, -36, -74, 32, -57, -85, -121, -57, -65, 126, -74, 2, -72, 127};
            assertThrows(BadAttributeException.class, () -> factory.decode(data));
        }

        /**
         * testing two specific name paris
         * @throws BadAttributeException
         * if error creating headers
         */
        @Test
        public void donTest3() throws BadAttributeException {
            Headers head = new Headers(1, false);
            Headers head2 = new Headers(3, false);
            head.addValue(":status" , "404");
            head.addValue("x-guploader-uploadid" , "ADPycdv2P45AQ-ntHnIYWqTsXHbW9BUImgqzanRwNnBXUzn7XC-7M3E-mHTkR1_VcWi2a3zxibZ72jAE7EDgVAWWdSJ4xw");
            head2.addValue(":status" , "200");
            head2.addValue("x-guploader-uploadid" , "ADPycdtbQLbntFabZ6u2NPVYdn6dSqMEwJBKRkS4kw8biYh-nxP_DNUV8YuUj9n1pkyWTJgVKjsdbhOqzFZ2C1IwST4c1g");

            byte[] data1 = factory.encode(head);
            byte[] data2 = factory.encode(head2);

            assertDoesNotThrow(() -> factory.decode(data1));
            assertDoesNotThrow(() -> factory.decode(data2));
        }



    }

}
