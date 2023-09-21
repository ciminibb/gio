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
    // All main() does is process messages!
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

            // Create an object of the HttpRequest class, passing it a reference
            // to our connection socket (our side of the pipe). Reaching this
            // line means a request was received.
            HttpRequest request = new HttpRequest(connectionSocket);

            // Open a new thread to process the request that was just received.
            // For that reason, we supply a reference to the request as an
            // argument to the thread's constructor.
            Thread thread = new Thread(request);
            thread.start(); // Start the thread.
        }
    }
}


// HttpRequest (inheritance restricted) is passed to the constructor of any new
// thread created.
final class HttpRequest implements Runnable {
    // The HTTP specification requires each line in requests/responses to
    // conclude with a carriage return (cr = \r) and a line feed (lf = \n). So,
    // we'll store those characters in a variable which is global to the class
    // and impossible to change. The variable socket stores a reference to the
    // connection socket, which was passed into the constructor of this class.
    final static String crlf = "\r\n";
    Socket socket;

    // The HttpRequest constructor simply sets the objects socket as the one
    // passed in.
    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    // We define a run() method with explicit error catching, in compliance with
    // the Runnable interface. To this point, we've simply been throwing
    // exceptions, as noted in method declarations.
    public void run() {
        // Processing occurs in processRequest method, this one is merely a
        // wrapper meant to catch its exceptions.
        try {
            processRequest();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {
        // Initialize stream variables to reference the socket's input and
        // output streams. These can be retrieved from the object's socket,
        // itself.
        InputStream iStream = socket.getInputStream();
        OutputStream oStream = socket.getOutputStream();

        // The buffered reader can't read the input stream directly. It needs a
        // filter to convert the bytes to characters.
        BufferedReader bReader =
            new BufferedReader(new InputStreamReader(iStream));

        // Get the request line of the HTTP request using the readline() method
        // of the buffered reader. It will only grab one line at a time, because
        // extraction halts at the carriage-return-line-feed sequence.
        String requestLine = bReader.readLine();
        
        System.out.println();            // Output a blank line.
        System.out.println(requestLine); // Output the request line.

        // Display header lines in a loop which terminates when an empty line is
        // read. It is okay that the headerLine variable is overwritten in each
        // iteration, we don't need those lines other than to display.
        String headerLine = null;
        while ((headerLine = bReader.readLine()).length() != 0) {
            System.out.println(headerLine);
        }

        // When processing has concluded, close the open streams and socket to
        // avoid resource leaks.
        oStream.close();
        bReader.close();
        socket.close();
    }
}