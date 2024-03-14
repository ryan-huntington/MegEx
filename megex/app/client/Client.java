/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/
package megex.app.client;

import megex.serialization.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static tls.TLSFactory.getClientSocket;

/**
 * client
 */
public class Client {
    private static final int TIMEOUT = 10000;
    private static final Charset CHARENC =
            StandardCharsets.US_ASCII;
    private static final String CONNECTION_PREFACE =
            "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";

    private final MessageFactory factory;
    private final Deframer deframer;
    private final Framer framer;
    //true = valid, false = not valid
    private final Map<Integer, Boolean> streamIDs;
    private final List<Integer> receivedHeaders = new ArrayList<>();


    /**
     * constructor for the client class
     * @param factory determines the message type
     * @param deframer deframes messages from server
     * @param framer frames messages for server
     */
    public Client(MessageFactory factory, Deframer deframer, Framer framer){
        this.factory = factory;
        this.deframer = deframer;
        this.framer = framer;
        this.streamIDs = new HashMap<>();


    }

    /**
     * gets set of all streams currently open
     * @return open streams
     */
    private Set<Integer> getValidStreams(){
        return this.streamIDs.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

    }

    /**
     * main client running method
     * @param args command line arguments
     */
    public static void main(String[] args)  {
        // Test for correct # of args
        if (args.length < 2){
            System.err.println("Parameter(s): <Server> <Port> [<list of paths>]");
        }

        String server = args[0];
        int serverPort = Integer.parseInt(args[1]);
        List<String> paths = Arrays.asList(args).subList(2, args.length);
        run(server, serverPort, paths);



    }

    /**
     * runs the main client program
     * @param server request from server
     * @param serverPort the port we are connecting to
     * @param paths files to be downloaded
     */
    public static void run(String server, int serverPort, List<String> paths){

        Map<Integer, String> pathID = new HashMap<>();

        MessageFactory factory = new MessageFactory();
        // Create socket that is connected to server on specified port
        try (Socket socket = getClientSocket(server, serverPort)) {
            socket.setSoTimeout(2000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            Framer framer = new Framer(out);
            Deframer deframer = new Deframer(in);

            Client client = new Client(factory, deframer, framer);

            client.sendPreface(out);

            client.sendHeaders(paths, server, pathID);
            out.flush();
            Message firstFrame = client.get();
            if (!(firstFrame instanceof Settings)) {
                System.err.println("Expected Settings frame first");
                System.exit(1);
            }

            //I could keep looping until all streams are closed or I get the reset stream (reset is type 0x3)
            //keep looping until i get all files, so really just until the streams are closed

            //since it should still be accepting from headers
            long start = System.currentTimeMillis(), cur = System.currentTimeMillis();
            while (client.getValidStreams().size() > 0 && !socket.isClosed() && start - cur < TIMEOUT) {
                start = System.currentTimeMillis();
                Message msg = client.get();
                if (msg == null) {
                    continue;
                }
                cur = System.currentTimeMillis();
                //if I got a valid headers back, then I delete the file that correlates with it
                if (msg instanceof Headers) {
                    client.receivedHeaders.add(msg.getStreamID());
                    File f = new File(pathID.get(msg.getStreamID()));
                    if( f.exists() && f.canWrite() ){
                        client.deleteFiles(f);
                    }
                    else{
                        System.err.println("Cannot edit file");
                    }

                }
                //I should be sending out a windows update after each time
                if (msg instanceof Data && ((Data) msg).getData().length > 0) {
                    Message window = new Window_Update(msg.getStreamID(), ((Data) msg).getData().length);
                    Message window2 = new Window_Update(0, ((Data) msg).getData().length);
                    client.send(window2);
                    client.send(window);
                    //also write out the info to a file
                    client.writeToFile(pathID.get(msg.getStreamID()), ((Data) msg).getData());
                }

            }
        } catch (SocketTimeoutException e) {
            System.err.println("Error: Socket timed out");
        } catch (Exception e) {
            System.err.println("Error communicating with server");
            System.exit(1);
        }


    }

    /**
     * sends the first messages to establish connection with server
     * @param out output stream
     * @throws IOException is IOError
     * @throws BadAttributeException if error creating settings object
     */
    public void sendPreface(OutputStream out) throws IOException, BadAttributeException {
        Objects.requireNonNull(out);
        //client sends connection preface as 24 octets first
        out.write(CONNECTION_PREFACE.getBytes(CHARENC));

        //then client sends a settings frame through the framer
        Message settings = new Settings();
        this.send(settings);
    }

    /**
     * makes a GET header request
     * @param nextID streamID for header
     * @param url server request is sent to
     * @param path the file that is requested
     * @return the Headers message
     * @throws BadAttributeException
     * if error creating Headers
     */
    private Headers createHeaders(int nextID, String url, String path) throws BadAttributeException {
        Headers header = new Headers(nextID, true);
        header.addValue(":method", "GET");
        header.addValue(":path", path);
        header.addValue(":authority", url);
        header.addValue(":scheme", "https");
        return header;
    }

    /**
     * sends all headers to server
     * @param paths all files being requested
     * @param server the server
     * @param pathID track the pathID to file being requested
     * @throws IOException if IOError
     * @throws BadAttributeException if error encoding message
     */
    public void sendHeaders(List<String> paths, String server, Map<Integer, String> pathID) throws IOException, BadAttributeException {
        //can start sending out all the header packets for each path
        int randomOdd = 1;

        for (String path : paths) {

            //assume streamid has to be valid at first
            streamIDs.put(randomOdd, true);
            pathID.put(randomOdd, this.replaceDash(path));

            Message msg = this.createHeaders(randomOdd, server, path);
            this.send(msg);
            randomOdd += 2;
        }
    }


    /**
     * handles what happens when a message is received
     * @return the message that was received, null if invalid/unexpected
     */
    public Message get(){
        //technically I am handling it just not very well
        byte[] buffer;
        try{
            buffer = deframer.getFrame();
            Message returned = factory.decode(buffer);

            if(returned instanceof Data){
                if(!this.receivedHeaders.contains(returned.getStreamID())){
                    System.err.println("Unexpected stream ID: " + returned);
                    return null;
                }
                if(((Data)returned).isEnd()){
                    this.closeStream(returned.getStreamID());
                }
            }
            System.out.println("Received Message: " + returned.toString());


            if(returned instanceof Headers){
                if(!this.getValidStreams().contains(returned.getStreamID())){
                    System.err.println("Unexpected stream ID: " + returned.getStreamID());
                    return null;
                }
                //check that the status is in the range of 200 and not 400
                String fullStatus = ((Headers)returned).getValue(":status");
                String[] split = (fullStatus.split(" "));
                int status = Integer.parseInt(split[0]);
                if(status < 200 || status >= 300){
                    this.closeStream(returned.getStreamID());
                    System.err.println("Bad status: " + status);
                    return null;
                }

            }

            return returned;

        } catch(EOFException | IllegalArgumentException e){
            System.err.println("Unable to parse: " + e.getMessage());
        } catch(IOException ignored){

        } catch(BadAttributeException e){
            if(e.getMessage().equalsIgnoreCase("unrecognized type")){
                System.err.println("Received unknown type: 0x" + e.getAttribute());
                if(e.getAttribute().equals("7")){
                    System.exit(1);
                }
            }
            else{
                System.err.println("Invalid message: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * function to send messages to server
     * @param message the message being sent
     * @throws IOException if IOError
     */
    private void send(Message message) throws IOException {
        this.framer.putFrame(factory.encode(message));
    }

    /**
     * closes a specific streamID as needed
     * @param streamID streamID being closed
     */
    private void closeStream(int streamID){
        this.streamIDs.put(streamID, false);
    }

    /**
     * replace the path of a file with - instead of /
     * @param path path we are changing
     * @return the new string representation of the path
     */
    private String replaceDash(String path){
        return path.replace('/', '-');
    }

    /**
     * delete the file so we can overwrite the data
     * @param file file to be deleted
     */
    private void deleteFiles(File file){
        //should delete file in current directory if it exists
        if(file.exists()){
            boolean deleted = file.delete();
            if(!deleted){
                System.out.println("Failed to delete old file");
                System.exit(1);
            }
        }

    }

    /**
     * write data to a file. Create if needed, or append
     * @param file file to be written to
     * @param data data that is being written
     */
    private void writeToFile(String file, byte[] data){
        //write the data to a file and append it
        //should it create the file if it does not exist?
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(data);
            fileOutputStream.close();
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            System.exit(1);
        }
    }




}
