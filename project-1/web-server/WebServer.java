/**
 * Project 1
 * Ben Cimini
 */




import java.io.*;
import java.net.*;
import java.util.*;




// WebServer is a public class whose inheritance is restricted by keyword
// "final." It cannot be extended or inherited.
public final class WebServer {
    public static void main(String argv[]) throws Exception {
        // Set port number to an arbitrary value between 1024 and 49151. Port
        // 6789 is used for this project. Crucially, this port number must be
        // used for requests to my server.
        int port = 6789;

        // Create welcomeSocket, an object that listens for a client on the port
        // specified above: 6789.
        ServerSocket welcomeSocket = new ServerSocket(port);

        // Process HTTP requests in an infinite loop, per the following.
        while (true) {
            // Create connectionSocket when the port receives a request from
            // some client. There is now a pipe ready for transport.
            Socket connectionSocket = welcomeSocket.accept();

            // NEW THREADS AND STREAMS? SYNTHESIZE BOTH INSTRUCTIONS!
        }
    }
}


// HttpRequest (inheritance restricted) is passed to the constructor of any new
// thread created.
final class HttpRequest implements Runnable {
    
}