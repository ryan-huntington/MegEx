/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 3
 * Class: CSI 4321
 *
 ************************************************/
package megex.app.server;

import megex.serialization.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Protocol handles the client and specific implementation
 */
public class Protocol implements Runnable{
    private static final String CONNECTION_PREFACE =
            "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";
    private static final int PREFACE_LENGTH = 24;
    private static final int TIMEOUT = 40000;
    private Socket socket;
    private Logger logger;
    private File directory;

    /**
     * constructor for the protocol
     * @param socket the client
     * @param logger the logger
     * @param directory root directory used
     */
    public Protocol(Socket socket, Logger logger, File directory){
        this.socket = socket;
        this.logger = logger;
        this.directory = directory;
    }

    /**
     * handles the client deals with the info being sent
     * @param clntSock the client socket
     * @param logger the logger
     * @param directory the root directory
     */
    public static void handleClient(Socket clntSock, Logger logger, File directory) {
        try (clntSock) {
            logger.log(Level.INFO, "Client connected");
            clntSock.setSoTimeout(Server.MINDATAINTERVAL);
            // Get the input and output I/O streams from socket
            InputStream in = clntSock.getInputStream();
            OutputStream out = clntSock.getOutputStream();
            Framer framer = new Framer(out);
            Deframer deframer = new Deframer(in);
            MessageFactory factory = new MessageFactory();

            Set<Headers> headers = new HashSet<>();
            Map<Headers, FileInputStream> validHeaders = new HashMap<>();

            byte[] buffer = new byte[PREFACE_LENGTH]; // create a byte array to hold the data
            int bytesRead = 0;
            int bytesToRead = buffer.length;

            while (bytesRead < bytesToRead) {
                int result = in.read(buffer, bytesRead, bytesToRead - bytesRead);
                if (result == -1) {
                    // end of stream reached before all bytes could be read
                    logger.log(Level.WARNING, "Error reading preface");
                    clntSock.close();
                }
                bytesRead += result;
            }
            Message settings = get(factory, deframer);
            if(!Arrays.equals(buffer, CONNECTION_PREFACE.getBytes())
                    || ! (settings instanceof Settings)){
                if(settings != null) {
                    logger.log(Level.WARNING, "Bad Preface: " + (settings instanceof Settings ? settings : ""));
                    clntSock.close();
                }
            }
            logger.log(Level.INFO, "Received message: " + settings);
            Message preface = new Settings();
            send(preface, framer, factory);

            Map<Headers, Long> timers = new HashMap<>();


            long timeLastActed = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();
            //will need to add another check to this while loop i think
            //its technically working, but I am upset :/
            while(!clntSock.isClosed() && currentTime - timeLastActed < TIMEOUT){
                currentTime = System.currentTimeMillis();
                try{
                    Message msg = get(factory, deframer);
                    //SocketTimeoutException thrown
                    timeLastActed = System.currentTimeMillis();
                    switch (msg.getCode()) {
                        case Data.DATA_TYPE -> logger.log(Level.WARNING, "Unexpected message: " + msg);
                        case Headers.HEADERS_TYPE -> {
                            String path = ((Headers) msg).getValue(":path");
                            File f = new File(directory.getAbsolutePath() + path);
                            int id = msg.getStreamID();
                            //Duplicate stream id
                            if (checkHeaders(headers, id)) {
                                logger.log(Level.WARNING, "Duplicate request: " + msg);
                            }
                            //Illegal stream id
                            else if (id % 2 != 1 || id < 1 || (headers.size() > 0 && !(id > lastID(headers)))) {
                                logger.log(Level.WARNING, "Illegal stream ID: " + msg);
                            }
                            else{
                                String warning;
                                Headers header;
                                //no or bad path
                                if (path.isBlank()) {
                                    warning = "No or bad path";
                                    logger.log(Level.WARNING, warning);
                                    header = createHeaders(id, "400");
                                    //terminate stream
                                }
                                //Directory
                                //I have no idea what i am checking for here
                                else if (f.isDirectory()) {
                                    warning = "Cannot request directory";
                                    logger.log(Level.WARNING, warning);
                                    header = createHeaders(id, "403");

                                }
                                //Non-existent/No permission file
                                else if (!f.exists() || !f.canRead()) {
                                    warning = "File not found";
                                    logger.log(Level.WARNING, warning);
                                    header = createHeaders(id, "404");
                                    //terminate stream
                                }
                                //valid
                                else {
                                    header = createHeaders(id, "200");
                                    FileInputStream fd = new FileInputStream(f);
                                    validHeaders.put(header, fd);
                                    timers.put(header, System.currentTimeMillis());
                                    //send the data later
                                }
                                headers.add(header);
                                send(header, framer, factory);
                            }

                        }
                        case Window_Update.WINDOW_TYPE, Settings.SETTINGS_TYPE -> logger.log(Level.INFO, "Received message: " + msg);
                        default -> logger.log(Level.WARNING, "Unknown type");
                    }
                }catch(SocketTimeoutException ignored){

                }catch(BadAttributeException e){
                    logger.log(Level.WARNING, "Unable to parse: ", e.getMessage());

                }


                //could refactor and say while i have data to send, send it
                if(validHeaders.size() > 0){
                    //loop through each of the headers that i currently have and send the data one packet at a time?
                    List<Headers> keys = new ArrayList<>(validHeaders.keySet());
                    for (Headers key : keys) {
                        Long curTime = System.currentTimeMillis();
                        if(curTime - timers.get(key) > Server.MINDATAINTERVAL){
                            Data data = sendData(key, validHeaders.get(key));
                            timers.put(key, curTime);
                            send(data, framer, factory);
                            if(data.isEnd()){
                                validHeaders.remove(key);
                            }
                        }
                    }
                }
            }

        } catch (IOException ex) {
            logger.log(Level.WARNING, "Exception in GET protocol", ex);

        } catch (BadAttributeException e) {
            logger.log(Level.WARNING, "Unable to parse: " + e.getMessage());
        }finally{
            try {
                clntSock.close();
                logger.log(Level.INFO, "Closed client");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing socket");
            }
        }
    }

    /**
     * This gets a message when expecting from the client
     * @param factory decides what message was sent
     * @param deframer gets the bytes of the message
     * @return
     * return the type of message received, null if unrecognized
     */
    private static Message get(MessageFactory factory, Deframer deframer) throws SocketTimeoutException, IOException, BadAttributeException {
        byte[] buffer = deframer.getFrame();
        return factory.decode(buffer);

    }

    /**
     * sends a message to the client
     * @param message message to be sent
     * @param framer frames the message
     * @param factory factory to encode the message
     * @throws IOException
     * if error writing to client
     */
    private static void send(Message message, Framer framer, MessageFactory factory) throws IOException {
        framer.putFrame(factory.encode(message));
    }

    /**
     * checks the headers
     * @param headers the list of headers ive received
     * @param id id to check for duplicate
     * @return if the set has the id
     */
    private static boolean checkHeaders(Set<Headers> headers, int id){
        for(Headers h : headers){
            if(h.getStreamID() == id){
                return true;
            }
        }
        return false;
    }

    /**
     * gets the last id that was received
     * @param headers the set of received headers
     * @return highest received headerID
     */
    private static int lastID(Set<Headers> headers){
        int max = 0;
        for(Headers h : headers){
            if (h.getStreamID() > max){
                max = h.getStreamID();
            }
        }
        return max;

    }

    /**
     * creates a header to send to the client
     *
     * @param nextID streamID
     * @param status status code
     * @return the Header message
     * @throws BadAttributeException if error creating Header
     */
    private static Headers createHeaders(int nextID, String status) throws BadAttributeException {
        Headers header = new Headers(nextID, false);
        header.addValue(":status", status);
        return header;
    }

    /**
     * creates a data object from the file it has to read
     * @param h the headers it is in response to
     * @param in the fileInputStream
     * @return the Data message
     * @throws IOException if IO error
     * @throws BadAttributeException
     * if error creating Data message
     */
    private static Data sendData(Headers h, FileInputStream in) throws IOException, BadAttributeException {
        long fileSize = in.available();
        if(in.available() == 0){
            return new Data(h.getStreamID(), true, new byte[0]);
        }
        else{
            int bytesRead = 0;
            byte[] chunk = new byte[Server.MAXDATASIZE];
            int numBytes = in.read(chunk);

            byte[] newArray = new byte[numBytes];
            System.arraycopy(chunk, 0, newArray, 0, numBytes);

            boolean isEnd = (bytesRead + numBytes) == fileSize;
            return new Data(h.getStreamID(), isEnd, newArray);
        }

    }

    /**
     * Runs the code over the thread
     */
    @Override
    public void run() {
        handleClient(socket, logger, directory);
    }
}
