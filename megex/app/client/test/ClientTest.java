/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/
package megex.app.client.test;

import megex.app.client.Client;
import megex.serialization.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import java.io.*;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static megex.app.client.Client.*;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.*;

/**
 * testing the client package
 */
public class ClientTest {
    /**
     * basic constructor
     */
    private ClientTest(){

    }

    /**
     * tests the client class
     */
    @Nested
    @DisplayName("Client tests")
    public class ClientTests {
        /**
         * basic constructor
         */
        private ClientTests(){

        }
        OutputStream out = new ByteArrayOutputStream();
        InputStream in = new ByteArrayInputStream("".getBytes());
        MessageFactory factory = new MessageFactory();
        Framer framer = new Framer(out);
        Deframer deframer = new Deframer(in);
        Client client = new Client(factory, deframer, framer);

        /**
         * creates a client with one message being sent in
         * @param serverData the data sent to the client
         * @return the client
         * @throws IOException if error
         */
        public Client createClient(byte[] serverData) throws IOException {
            InputStream in = new ByteArrayInputStream(serverData);
            OutputStream out = new ByteArrayOutputStream();
            MessageFactory factory = new MessageFactory();
            Framer framer = new Framer(out);
            Deframer deframer = new Deframer(in);
            return new Client(factory, deframer, framer);

        }

        /**
         * creates a more open ended client
         * @param in inputstream
         * @param out outputstream
         * @return new client
         */
        public Client createBasic(InputStream in, OutputStream out){
            MessageFactory factory = new MessageFactory();
            Framer framer = new Framer(out);
            Deframer deframer = new Deframer(in);
            return new Client(factory, deframer, framer);
        }



        //can test with a live website or with my own personal server

        /**
         * test a basic run to make sure no errors are thrown
         */
        @Test
        @DisplayName("run with duckduckgo and valid file")
        public void testRun(){
            List<String> paths = new ArrayList<>();
            paths.add("/tl5.js");
            assertDoesNotThrow(() -> run("duckduckgo.com", 443, paths));


        }

        /**
         * test that I get a settings frame from get
         * @throws BadAttributeException if baddattribute
         * @throws IOException if error
         */
        @Test
        @DisplayName("get a settings frame")
        public void getSettings() throws BadAttributeException, IOException {
            Client client = createClient(new byte[] {
                    0x00, 0x00, 0x04, // Frame length
                    0x04, // Frame type (SETTINGS)
                    0x00, // Flags
                    0x00, 0x00, 0x00, 0x00, // Stream ID
                    0x00, 0x00, 0x00, 0x00, // Payload (empty)
            });

            Message settings = client.get();
            Message expected = new Settings();

            assertEquals(expected, settings);


        }


        /**
         * tests that the preface was sent correctly
         * @throws IOException if error
         * @throws BadAttributeException problem creating preface
         */
        @Test
        @DisplayName("Test Preface")
        public void testPreface() throws IOException, BadAttributeException {
            client.sendPreface(out);
            byte[] expected = {0x50, 0x52, 0x49, 0x20, 0x2A, 0x20, 0x48, 0x54, 0x54, 0x50, 0x2F, 0x32, 0x2E, 0x30, 0x0D, 0x0A, 0x0D, 0x0A, 0x53, 0x4D, 0x0D, 0x0A, 0x0D, 0x0A, 0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00};
            byte[] returned = out.toString().getBytes();

            assertArrayEquals(expected, returned);

        }

        /**
         * tests that client receives headers correctly (if status is valid)
         * @throws BadAttributeException if error with headers
         * @throws IOException if error
         */
        @Test
        @DisplayName("Headers are received correctly")
        public void headersReceived() throws BadAttributeException, IOException {
            Client testHeaders = createClient(new byte[] {0x0, 0x0, 0x7, 0x01, 0x04, 0x00, 0x00, 0x00, 0x01, 0x48, (byte) 0x85, 0x10, 0x00, (byte) 0xA6, (byte) 0xAC, (byte) 0xDF});
            Headers valid = new Headers(1, false);
            Headers notValid = new Headers(3, false);

            valid.addValue(":status", "200 OK");
            notValid.addValue(":status", "404 ERROR");

            List<String> head = new ArrayList<>();
            head.add("1");
            head.add("2");
            Map<Integer, String> testMap = new HashMap<>();
            testHeaders.sendHeaders(head, "localhost", testMap);
            Message validHeader = testHeaders.get();
            testHeaders = createClient(new byte[] {0x0, 0x0, 0x0A, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, 0x48, (byte) 0x88, 0x68, 0x0D, 0x29, (byte) 0x83, 0x6E, (byte) 0xDD, 0x5B, 0x7F });
            Message invalidHeader = testHeaders.get();

            assertEquals(valid, validHeader);
            assertNull(invalidHeader);


        }

        /**
         * tests that the headers received are valid
         * @throws IOException if error
         * @throws BadAttributeException if error with headers
         */
        @Test
        @DisplayName("Server response are valid")
        public void testStatus() throws IOException, BadAttributeException {
            InputStream in = new ByteArrayInputStream(new byte[] {0x0, 0x0, 0x7, 0x01, 0x04, 0x00, 0x00, 0x00, 0x01, 0x48, (byte) 0x85, 0x10, 0x00, (byte) 0xA6, (byte) 0xAC, (byte) 0xDF});
            OutputStream out = new ByteArrayOutputStream();
            Client testHeaders = createBasic(in, out);
            Headers valid = new Headers(1, false);
            Headers notValid = new Headers(3, false);

            valid.addValue(":status", "200 OK");
            notValid.addValue(":status", "404 ERROR");

            List<String> head = new ArrayList<>();
            head.add("1");
            //head.add("2");
            Map<Integer, String> testMap = new HashMap<>();
            testHeaders.sendHeaders(head, "localhost", testMap);
            byte[] header1 = {0x00, 0x00, 0x0D, 0x01, 0x04, 0x00, 0x00, 0x00,
                    0x01, (byte) 0xEF, (byte) 0xBF, (byte) 0xBD, 0x44, 0x01, 0x31, 0x41,
                    (byte) 0xEF, (byte) 0xBF, (byte) 0xBD, (byte) 0xEF, (byte) 0xBF, (byte) 0xBD,
                    (byte) 0xEF, (byte) 0xBF, (byte) 0xBD, 0x1D, 0x13, (byte) 0xEF, (byte) 0xBF, (byte) 0xBD,
                    0x09, (byte) 0xEF, (byte) 0xBF, (byte) 0xBD};

            byte[] returned = out.toString().getBytes();
            assertArrayEquals(header1, returned);

        }

        /**
         * Test basic running with files
         */
        @Test
        @DisplayName("Test that the files are received")
        public void testBasicFunctionality()  {
            String server = "duckduckgo.com";
            int port = 443;
            List<String> files = new ArrayList<>();
            files.add("/tl5.js");
            files.add("/ti5.js");
            files.add("/b159.js");
            files.add("/xxxx");

            run(server, port, files);

            for (String file : files) {
                file = file.replace('/', '-');
                File f = new File(file);
                assertTrue(f.exists());
                assertTrue(f.length() > 0);
                f.delete();
            }
        }


        /**
         * test that the port is valid
         */
        @Test
        @DisplayName("Test without a valid port")
        public void testInvalidPort() {
            String server = "duckduckgo.com";
            int port = 1234;
            List<String> files = new ArrayList<>();
            files.add("/file");

            //we shall see
            assertDoesNotThrow( () -> run(server, port, files));
        }

    }
}
