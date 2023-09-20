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

    }
}


// HttpRequest (inheritance restricted) is passed to the constructor of any new
// thread created.
final class HttpRequest implements Runnable {
    
}