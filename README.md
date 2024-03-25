# MegEx

This Java program implements the functionality of HTTP/2.0 Get over TCP. It allows users to run both a server and a client to handle file requests and responses respectively.

## Features:

- **HTTP/2.0 Functionality:** The program implements the HTTP/2.0 Get method for retrieving files over TCP connections. It adheres to the HTTP/2.0 protocol specifications for efficient and multiplexed communication between clients and servers.

- **Server Implementation:** The server component of the program can be configured by passing parameters via the command line. Users can specify the port number, number of threads for the server's thread pool, and the root directory from which files will be served. This flexibility allows for easy customization and scalability of the server based on resource availability and performance requirements.

- **Client Implementation:** Similarly, the client component accepts command line parameters to specify the server's address, port number, and a list of paths to files requested from the root directory. Upon receiving the requested files from the server, the client saves them locally for further processing or viewing.

## Usage:

### Running the Server:

To run the server, use the following command:
`java Server <port><numThreads><rootDirectory>`

- `<port>`: The port number on which the server will listen for incoming connections.
- `<numThreads>`: The number of threads to allocate for the server's thread pool, which determines the concurrency level of the server.
- `<rootDirectory>`: The root directory from which files will be served by the server.

### Running the Client:

To run the client, use the following command:
`java Client <serverAddress> <port> <filePaths...>`


- `<serverAddress>`: The IP address or hostname of the server to connect to.
- `<port>`: The port number on which the server is listening for connections.
- `<filePaths...>`: A list of paths to files relative to the root directory of the server. Separate multiple paths with spaces.

## Dependencies:

- **Java:** The program is written in Java and requires a Java Runtime Environment (JRE) to execute.



