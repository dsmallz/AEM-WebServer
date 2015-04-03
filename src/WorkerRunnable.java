/**
 * Created by Dennis on 3/29/2015.
 */
import java.io.*;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.Date;

public class WorkerRunnable implements Runnable {

    protected Socket clientSocket = null;
    private final String CRLF = "\r\n";

    public WorkerRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
            processReadRequest();
    }

    private void processReadRequest() {
        BufferedReader input = null;
        PrintStream output = null;
        while (true) {
            try {
                String path;
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                output = new PrintStream((clientSocket.getOutputStream()));

                String request = input.readLine();
                //*DENNIS-BEGIN*
                //TODO: Future: Iterate through all headers for more information (ie. keep-alive)
                String data = input.readLine();
                if (data == null || data.length() == 0) {
                    output.write(ServerMain.properties.getProperty("badRequestString").getBytes());
                    break;
                }
                //PARSE HTTP
                if (request != null && request.startsWith("GET ") && (request.endsWith(" HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
                    String filePath = null;
                    String[] requestArray = request.split(" ");
                    //REQUEST GET HEADER SYNTAX: GET /test.jpg HTTP/1.1
                    if (requestArray.length > 1) {
                        filePath = requestArray[1];
                    } else {
                        output.write(ServerMain.properties.getProperty("methodNotAllowedString").getBytes());
                        break;
                    }

                    if (filePath.endsWith("/")) {
                        File file = new File(ServerMain.properties.getProperty("wwwrootpath") + filePath);
                        File[] files = file.listFiles();

                        if (files != null) {
                            output.write(MessageFormat.format((String) ServerMain.properties.getProperty("okString"), filePath).getBytes());

                            for (int i = 0; i < files.length; i++) {
                                file = files[i];
                                if (file.isDirectory()) {
                                    output.write(("<tr><td><b><a href=\"" + filePath + file.getName() + "/\">" + file.getName() + "/</a></b></td><td></td><td></td></tr>").getBytes());
                                } else {
                                    output.write(("<tr><td><a href=\"" + filePath + file.getName() + "\">" + file.getName() + "</a></td><td align=\"right\">" + file.length() + "</td><td>" + new Date(file.lastModified()).toString() + "</td></tr>").getBytes());
                                }
                            }
                        }
                        else{
                            String directoryNotFound = MessageFormat.format((String) ServerMain.properties.getProperty("directoryNotFoundString"), filePath);
                            output.write(directoryNotFound.getBytes());
                        }
                    } else {
                        try {
                            FileInputStream outputFile = new FileInputStream(ServerMain.properties.getProperty("wwwrootpath") + filePath);
                            String header = createHttpHeader(filePath, String.valueOf(outputFile.available()));
                            processHttpResponse(header, outputFile, output);
                        } catch (FileNotFoundException ex) {
                            //Display empty directory, but allow for clickable link to parent directory.
                            String fileNotFound = MessageFormat.format((String) ServerMain.properties.getProperty("fileNotFoundString"), filePath);
                            output.write(fileNotFound.getBytes());
                        }
                    }
                }
                else
                    {
                        //Display Method Not Allowed Message to the browser
                        output.write(ServerMain.properties.getProperty("methodNotAllowedString").getBytes());
                    }
            } catch (IOException e) {

            } finally {
                if (output != null) {
                    output.flush();
                    output.close();
                }
                try {
                    if (input != null)
                        input.close();
                } catch (IOException e) {
                    //Ingore- Closing stream
                }
            }
        }
    }

    private String createHttpHeader(String filePath, String contentLength) {
        // Determine the MIME type and print HTTP header
        String mimeType = "text/plain";
        if (filePath.endsWith(".html") || filePath.endsWith(".htm"))
            mimeType = "text/html";
        else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg"))
            mimeType = "image/jpeg";
        else if (filePath.endsWith(".gif"))
            mimeType = "image/gif";
        else if (filePath.endsWith(".pdf"))
            mimeType = "application/pdf";
        else if (filePath.endsWith(".exe") || (filePath.endsWith(".iso")))
            mimeType = "application/octet-stream";
        else if (filePath.endsWith(".mp3"))
            mimeType = "audio/mpeg3";
        else if (filePath.endsWith(".class"))
            mimeType = "application/octet-stream";

        return "HTTP/1.1 200 OK" + CRLF +
                "Content-type: " + mimeType + CRLF +
                "Content-Length: " + contentLength + CRLF;
    }

    private void processHttpResponse(String header, FileInputStream inputStream, PrintStream printStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytes = 0;

        printStream.write(header.getBytes());
        printStream.write(CRLF.getBytes());

        //Write file contents to the client
        while ((bytes = inputStream.read(buffer)) != -1) {
            printStream.write(buffer, 0, bytes);
        }
    }
    //*DENNIS-END*
}