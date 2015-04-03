import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Dennis on 3/29/2015.
 *
 *
 * Notes: The ThreadPooledServer, ServerMain and WorkerRunable code is based off of the following url: http://tutorials.jenkov.com/java-multithreaded-servers/thread-pooled-server.html
 *          Some of the html tags/formatting was taken from this example: http://www.dreamincode.net/forums/topic/357493-css-on-java-web-server/
 *
 * I have noted where I have added my own logic in the code determined by the following string: //*DENNIS-BEGIN* //*DENNIS-END*
 * Server.properties file a static properites file loaded at run time  and is used to make the server more generic and user friendly and provides String variables to increase code readability
 *
 *
 */
public class ServerMain {
    //*DENNIS-BEGIN*
    static Properties properties;
    //LOAD STATIC PROPERTIES FILE
    static {
        AEMPropertyValues props = new AEMPropertyValues();
        try {
            properties = props.getPropValues();
        } catch (IOException e) {
            System.err.println("Unable to retrieve server properties file. Using defaults.");
            System.err.println(e.getMessage());
        }
    }
    //*DENNIS-END*
    public static void main(String[] args) {
        //*DENNIS-BEGIN*
        String wwwroot = null;
        String port = null;
        try {
            boolean run = true;
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];

                if (arg.equals("-serverdir")) {
                    if (i < args.length) {
                        wwwroot = args[++i];
                    } else {
                        System.err.println("-serverdir option requires a port value");
                    }
                } else if (arg.equals("-port")) {
                    if (i < args.length) {
                        port = args[++i];
                    } else {
                        System.err.println("-port option require a port value");
                    }
                } else if (arg.equals("-help")){
                    System.out.println("Usage: ServerMain [-serverdir adir] [-port aport] [-help]");
                    System.exit(0);
                } else {
                    System.err.println("Wrong arguments given. Check your arguments and try again. ");
                    System.out.println("    Usage: ServerMain [-serverdir adir] [-port aport] [-help]");
                    System.exit(0);
                }
            }
            //use serverdir from args first, else defaults from server.properties file
            File serverDirectory = null;
            if (wwwroot != null) {
                serverDirectory = new File(wwwroot);
            } else {
                serverDirectory = new File(properties.getProperty("wwwroot"));
            }
            if (serverDirectory.exists() && serverDirectory.isDirectory()) {
                properties.setProperty("wwwrootpath", serverDirectory.getAbsolutePath());
                //*DENNIS-END*
                //Use port from args first, else defaults from server.properties file
                ThreadPooledServer server;
                if (port != null) {
                    server = new ThreadPooledServer(Integer.parseInt(port));
                } else {
                    server = new ThreadPooledServer(Integer.parseInt(properties.getProperty("port")));
                }
                new Thread(server).start();
                //*DENNIS-BEGIN*
                System.out.println("Server started with wwwroot directory: " + serverDirectory.getAbsolutePath());
                System.out.println("Server started on port: " + server.serverPort);
                System.out.println("");
                System.out.println("Press 'q' to stop server.");
                //*DENNIS-END*
                while (run) {
                    try {
                        Thread.sleep(500);
                        char quit = (char) System.in.read();
                        if (quit == 'q') {
                            run = false;
                        }
                    } catch (InterruptedException ex) {
                        System.err.println(ex.getMessage());
                    } finally {
                        server.stop();
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
}