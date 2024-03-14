/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 0
 * Class: CSI 4321
 *
 ************************************************/
package megex.serialization.test;


import megex.serialization.Deframer;
import megex.serialization.Framer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing class for Framer and Deframer
 */
public class MegExTest {
    //private constructor
    private MegExTest() {};

    /**
     * creates a message that should be sent to the framer with payload and 48 empty bits
     * @return a byte array for message for framer
     */
    public byte[] createMsgWithBytes(){
        byte[] payload = "hi".getBytes();
        byte[] header = new byte[6];
        byte[] msg = new byte[payload.length + header.length];
        System.arraycopy(header, 0, msg, 0, header.length);
        System.arraycopy(payload, 0, msg, header.length, payload.length);
        return msg;
    }

    /**
     * creates what should be the response for the deframer
     * @param b1 the first byte for the length
     * @param b2 the second byte for the length
     * @param b3 the third byte for the length
     * @return the expected result from the deframer
     */
    public byte[] createMsgWithLength(byte b1, byte b2, byte b3){
        byte[] length = new byte[3];
        byte[] payload = "hi".getBytes();
        byte[] header = new byte[6];
        byte[] msg = new byte[payload.length + header.length + length.length];


        length[0] = b1;
        length[1] = b2;
        length[2] = b3;

        System.arraycopy(length, 0, msg, 0, length.length);
        System.arraycopy(header, 0, msg, length.length, header.length);
        System.arraycopy(payload, 0, msg, length.length + header.length, payload.length);

        return msg;
    }

    /**
     * Framer tests
     */
    @Nested
    @DisplayName("framer tests")
    public class FramerTests {
        /**
         * private constructor for FramerTests
         */
        private FramerTests(){}

        /**
         * Valid Construction test of framer with working OutputStream
         */
        @Test
        @DisplayName("Test construct of frame")
        public void createFrame(){
            try{
                OutputStream out = new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {

                    }
                };
                Framer frame = new Framer(out);
            }catch(Exception e){
                fail(e.getMessage());
            }



        }

        /**
         * Tests the construction of the Framer if the OutputStream is null
         *
         */
        @Test
        @DisplayName("test create if outputstream null")
        public void constructNull(){

            assertThrows(NullPointerException.class, () -> {
                new Framer(null);
            });


            //show throw NullPointerException
        }

//        @Test
//        @DisplayName("test putframe (empty)")
//        public void testPutFrame(){
//
//        }

        /**
         * Tests the putFrame method to see if correct message is returned
         * @throws IOException
         * if the message is missing a header
         */
        @Test
        @DisplayName("put with normal parameters")
        public void putNormal() throws IOException {
            OutputStream out = new ByteArrayOutputStream();

            Framer frame = new Framer(out);
            byte[] msg = createMsgWithBytes();

            //need to create byte message with 48 empty bits and then the message
            frame.putFrame(msg);

            String output = out.toString();
            byte[] returnedFrame = output.getBytes();
            byte b1 = returnedFrame[0], b2 = returnedFrame[1], b3 = returnedFrame[2];
            int length = ((b1 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b3 & 0xFF);
            byte[] empty = {0, 0, 0, 0, 0, 0 };

            //tests to make sure that the length of the frame matches the length of the payload
            assertEquals(2, length);
            //tests that there are 48 empty bits
            assertArrayEquals(empty, Arrays.copyOfRange(returnedFrame, 3, 9));
            //asserts that message returned has empty bits and matching payload
            assertArrayEquals(msg, Arrays.copyOfRange(returnedFrame, 3, returnedFrame.length));


        }

        /**
         * Test that an exception is thrown with a null OutputStream
         *
         * @throws NullPointerException
         * if the OutputStream is null
         */
        @Test
        @DisplayName("put with null message")
        public void putNull(){
            //makes basic framer
            Framer frame = new Framer(new OutputStream() {
                @Override
                public void write(int b) throws IOException {

                }
            });
            //asserts that if null is passed, then an excpetion is thrown
            assertThrows(NullPointerException.class, () -> {
                frame.putFrame(null);
            });
            //show throw NullPointerException
        }

        /**
         * Test that an IOException is thrown
         *
         */
        @Test
        @DisplayName("put with IllegalArgument, too short")
        public void putIO(){
            Framer frame = new Framer(new OutputStream() {
                @Override
                public void write(int b) throws IOException {

                }
            });
            byte data[] = new byte[0];
            assertThrows(IllegalArgumentException.class, () -> {
                frame.putFrame(data);
            });
            //throw IOException
        }

        /**
         * Tests that IllegalArgumentException is thrown
         *
         * @throws IllegalArgumentException
         * if the message is too long
         */
        @Test
        @DisplayName("put with Illegal Argument Exception")
        public void putIllegal(){
            Framer frame = new Framer(new OutputStream() {
                @Override
                public void write(int b) throws IOException {

                }
            });
            byte data[] = new byte[17000];
            assertThrows(IllegalArgumentException.class, () -> {
                frame.putFrame(data);
            });
            //throw is invalid message like too long payload
        }

    }

    /**
     * Tests the deframer
     */
    @Nested
    @DisplayName("deframer tests")
    public class DeframerTests {
        /**
         * private constructor
         */
        private DeframerTests(){}
        /**
         * Tests construction of the deframer
         *
         * @throws NullPointerException
         * if the InputStream is null
         */
        @Test
        @DisplayName("Test construct of deframe")
        public void createDeframer(){
            try{
                InputStream in = new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return 0;
                    }
                };
                Deframer deframe = new Deframer(in);
            }catch(Exception e){
                fail(e.getMessage());
            }
        }

        /**
         * Tests construction of the deframer with null InputStream
         *
         * @throws NullPointerException
         * if the InputStream is null
         */
        @Test
        @DisplayName("test create if inputstream null")
        public void constructNull(){
            assertThrows(NullPointerException.class, () -> {
                new Deframer(null);
            });
            //show throw NullPointerException
        }

//        @Test
//        @DisplayName("test get frame (empty)")
//        public void testGetFrame(){
//
//        }

        /**
         * Tests the getFrame method with a valid message
         *
         * @throws IOException
         * if the payload encounters an IO error
         */
        @Test
        @DisplayName("get with normal parameters")
        public void getNormal() throws IOException {
            int byteShift = 8;
            int byteMask = 0xFF;
            byte[] length = new byte[3];
            byte[] payload = "hi".getBytes();
            byte[] header = new byte[6];
            byte[] msg = new byte[payload.length + header.length + length.length];
            byte[] msg2 = new byte[payload.length + header.length];



            length[0] = (byte) ((payload.length >> (2 * byteShift)) & byteMask);
            length[1] = (byte) ((payload.length >> byteShift) & byteMask);
            length[2] = (byte) (payload.length & byteMask);

            System.arraycopy(length, 0, msg, 0, length.length);
            System.arraycopy(header, 0, msg, length.length, header.length);
            System.arraycopy(payload, 0, msg, length.length + header.length, payload.length);

            System.arraycopy(header, 0, msg2, 0, header.length);
            System.arraycopy(payload, 0, msg2, header.length, payload.length);

            InputStream in = new ByteArrayInputStream(msg);
            Deframer deframer = new Deframer(in);

            byte[] recMsg = deframer.getFrame();

            assertArrayEquals(msg2, recMsg);


        }

        /**
         * Tests that a proper EOFException is thrown with empty framer
         *
         */
        @Test
        @DisplayName("get EOF premature")
        public void getEOF(){
            InputStream stub = new ByteArrayInputStream("".getBytes());
            Deframer deframer = new Deframer(stub);
            assertThrows(EOFException.class, () -> {
                deframer.getFrame();
            });

            //show throw EOFException
        }

        /**
         * Tests that an IOException is thrown
         *
         */
        @Test
        @DisplayName("get with IOException")
        public void getIO(){
            byte[] msg = new byte[10];
            msg[0] = 0x00;
            msg[1] = 0x00;
            msg[2] = 0x07;
            InputStream in = new ByteArrayInputStream(msg);
            Deframer deframer = new Deframer(in);
            assertThrows(IOException.class, () -> {
                deframer.getFrame();
            });

            //throw IOException
        }

        /**
         * Tests the getFrame to see if an illegal length with throw Illegal Argument
         * @throws IOException
         * if IO error
         * @throws IllegalArgumentException
         * if the length is too large
         */
        @Test
        @DisplayName("get with Illegal Argument Exception")
        public void getIllegal() throws IOException {
            //feed in length as part of the frame that is over 00 40 00


            byte[] msg = createMsgWithLength((byte) 0x00, (byte) 0x50, (byte) 0x00);

            InputStream in = new ByteArrayInputStream(msg);
            Deframer deframer = new Deframer(in);



            assertThrows(IllegalArgumentException.class, () -> {
                deframer.getFrame();
            });
            //throw is invalid message like too long payload
        }

        /**
         * Tests that an EOF is thrown if the length is longer than the payload
         *
         */
        @Test
        @DisplayName("length does not match payload(EOF)")
        public void incorrectLength(){


            byte[] msg = createMsgWithLength((byte) 0x00, (byte) 0x00, (byte) 0x60);

            InputStream in = new ByteArrayInputStream(msg);
            Deframer deframer = new Deframer(in);

            assertThrows(EOFException.class, () -> {
                deframer.getFrame();
            });

        }
    }

    /**
     * Tests that the Framing followed by Deframing works
     */
    @Nested
    @DisplayName("Frame then deframe")
    public class FrameDeframe {
        /**
         * private constructor
         */
        private FrameDeframe(){
        }

        /**
         * Test that Frame followed by Deframe works
         * @throws IOException
         * if the framer or deframer encounters an IO error
         */
        @Test
        @DisplayName("Valid Frame then Valid Deframe")
        public void normalTest() throws IOException {
            OutputStream out = new ByteArrayOutputStream();
            Framer frame = new Framer(out);
            //trying to format the header + payload
            byte[] msg = createMsgWithBytes();
            //should be 00 00 00 00 00 00 68 69 in bytes

            //writes the frame
            frame.putFrame(msg);

            //get the output back (just for testing)
            String output = out.toString();
            byte[] returnedFrame = output.getBytes();


            //now pass it into input stream and deframe it
            InputStream in = new ByteArrayInputStream(returnedFrame);
            Deframer deframer = new Deframer(in);

            byte[] recMsg = deframer.getFrame();

            //check that the payload matches the message received
            assertArrayEquals(msg, recMsg);


        }
    }
}
