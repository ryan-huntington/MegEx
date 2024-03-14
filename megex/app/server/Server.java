/************************************************
 *
 * Author: Ryan Huntington
 * Assignment: Program 3
 * Class: CSI 4321
 *
 ************************************************/
package megex.app.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.*;

import static tls.TLSFactory.getServerConnectedSocket;
import static tls.TLSFactory.getServerListeningSocket;


/**
 * Server class to run
 */
public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    /**
     * Max size sent over Data payload
     */
    public static final int MAXDATASIZE = 8000; //in bytes
    /**
     * Interval between Data messages
     */
    public static final int MINDATAINTERVAL = 250; //in milliseconds so half a second

    /**
     * override default constructor
     */
    private Server(){

    }

    /**
     * main method of server
     * @param args the command line parameters, should be port threads root
     */
    public static void main(String[] args) {
        // Load the logging configuration from file
        LogManager logManager = LogManager.getLogManager();
        //src/main/java/
        try (FileInputStream configFile = new FileInputStream("logging.properties")) {
            logManager.readConfiguration(configFile);
        } catch (IOException ex) {
            LOGGER.severe("Failed to load logging configuration: " + ex.getMessage());
            System.exit(1);
        }

        if (args.length != 3) { // Test for correct # of args
            LOGGER.severe("Invalid args on startup. Expected: <port> <threads> <root>");
            System.exit(1);
        }

        int servPort = 0;
        int threadPoolSize = 0;
        try{
            servPort = Integer.parseInt(args[0]); // Server port
            threadPoolSize = Integer.parseInt(args[1]);
        }catch(NumberFormatException e){
            LOGGER.log(Level.SEVERE, "Error: Invalid port/thread pool. Use numbers. ");
            System.exit(1);
        }
        String rootDirectory = args[2];
        File directory = new File(rootDirectory);
        if (!directory.exists() && !directory.isDirectory()) {
            LOGGER.log(Level.SEVERE, "Invalid root directory. Not found");
            System.exit(1);
        }

        // Create a server socket to accept client connection requests
        final ServerSocket servSock;
        try {
            //src/main/java/
            servSock = getServerListeningSocket(servPort, "keystore", "Maryland12301");

            // Spawn a fixed number of threads to service clients
            //thread keeps running as long as there is not an exception that kills it, so good right?
            //especially since the criteria says that the client should never be able to kill the server
            for (int i = 0; i < threadPoolSize; i++) {
                Thread thread = new Thread(() -> {
                    while (true) {
                        try {
                            Socket clntSock = getServerConnectedSocket(servSock); // Wait for a connection
                            LOGGER.log(Level.INFO, "Client accepted");
                            Protocol.handleClient(clntSock, LOGGER, directory); // Handle it
                        } catch (IOException ex) {
                            LOGGER.warning("Client accept failed: " + ex);
                        }
                    }
                });
                thread.start();
            }
        } catch (Exception e) {
            LOGGER.severe("Failure to create server socket");
            System.exit(1);
        }
    }

}

