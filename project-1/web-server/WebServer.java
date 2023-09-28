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
        DataOutputStream oStream =
            new DataOutputStream(socket.getOutputStream());

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
        System.out.println("REQUEST");
        String headerLine = null;
        while ((headerLine = bReader.readLine()).length() != 0) {
            System.out.println(headerLine);
        }

        // Create an object of the StringTokenizer class with which to extract
        // the filename from the HTTP request line (we are beginning to work on
        // a response now). For the purposes of this project, we're ignoring
        // header information and assuming everything is a GET. For that reason,
        // in the request line itself, we skip over the method specification.
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken();
        String filename = tokens.nextToken(); // We have the filename now!

        filename = "." + filename; // The "." at the front of the filename will
                                   // ensure it exists at the current directory.
                                   // The browser prefixes a slash, as well.
        
        // The first step in sending the file to a client is opening it!
        // However, it's worth considering that the file may not exist. We don't
        // want the thread to close as a result (as is default), but we also
        // don't want to attempt sending a nonexistent file. A try/catch will be
        // used to massage these edge cases.
        FileInputStream fStream = null;
        boolean fileExists = true; // Initialize a boolean to hold whether the
                                   // file exists or not.
        
        // Handle nonexistent file exception.
        try {
            fStream = new FileInputStream(filename); // Attempt to open stream.
        }
        catch (FileNotFoundException e) {
            fileExists = false;
        }

        // We construct a response message with variables representing each
        // basic part: status line, headers, and body. Nonexistent files will
        // result in a 404 Not Found in the status line and an error message for
        // the HTML body.
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;

        // Check if file exists.
        if (fileExists) {
            // If the file exists, the HTTP request can be satisfied without
            // issue. It should return a 200 OK status.
            statusLine = "200 OK";
            contentTypeLine = "Content-type: " +
                URLConnection.guessContentTypeFromName(filename) +
                crlf; // Concatenate text, content type (possible bug), and CRLF
                      // into single string.
        }
        else {
            statusLine = "404 Not Found";
            contentTypeLine = "Content-type: " +
                contentType(filename) +
                crlf;
            entityBody = "<HTML>" +
                "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
                "<BODY>Not Found</BODY></HTML>"; // This string contains HTML
                                                 // syntax and will be rendered
                                                 // as such by the browser.
        }

        // Send status line and header to the browser by writing to the socket's
        // output stream.
        oStream.writeBytes(statusLine);      // Send status line.
        oStream.writeBytes(contentTypeLine); // Send content type line.
        oStream.writeBytes(crlf);            // Send end of header lines.

        // Send entity body to browser by writing to the socket's output stream.
        if (fileExists) {
            sendBytes(fStream, oStream);
            fStream.close(); // Close file input stream if the file is sent.
        }
        else {
            oStream.writeBytes(entityBody); // Send error message if file
                                            // doesn't exist.
        }

        // Display HTTP response.
        System.out.println("RESPONSE");
        System.out.println(statusLine);
        System.out.println(contentTypeLine);
        System.out.println(crlf);
        if (!fileExists) { // Only print entity body in the case of a 404 Not
                           // Found being sent.
            System.out.println(entityBody);
        }

        // When reading has concluded, close the open streams and socket to
        // avoid resource leaks.
        oStream.close();
        bReader.close();
        socket.close();
    }

    // This method writes the requested file to the socket's output stream.
    private static void sendBytes
        (FileInputStream fStream, OutputStream oStream) throws Exception {
        // Build a buffer to hold bytes that are on their way to the socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;

        // Copy requested file to socket's output stream. This repeats while
        // there are still bytes to copy. Read returns the number of bytes that
        // were placed in buffer, or -1 when there weren't any.
        while ((bytes = fStream.read(buffer)) != -1) {
            oStream.write(buffer, 0, bytes); // 0 is start of array.
        }

        // Exceptions are thrown to be handled by the calling method, we don't
        // deal with them here!
    }

    // This method examines the extension of a filename to return its MIME type.
    private static String contentType(String filename) {
        if (filename.endsWith(".htm") || filename.endsWith(".html")) {
            // We want to send HTML files.
            return "text/html";
        }
        else if (filename.endsWith(".jpeg") || filename.endsWith(".jpg")) {
            // We want to send .jpeg images.
            return "image/jpeg";
        }
        else if (filename.endsWith(".gif")) {
            // We want to send GIFS.
            return "image/gif";
        }
        else {
            // Uknown file extension. Note, there are many other file extensions
            // that aren't captured here. For the sake of this project, we're
            // leaving them out. What's here is sufficient to send a basic HTML
            // homepage.
            return "application/octet-stream";
        }
    }
}