package itech.webserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import itech.json.JsonSerialize;
import itech.util.HTTPUtil;
import itech.util.ToDoList;

/**
 * Created by leonakuse on 18.07.17.
 */
public class HttpThread extends Thread {
	
	
	private MyHttpServer httpServer;
    private File fileRoot;
    private ServerSocket server = null;
    private Socket socket = null;
    private ArrayList<String> requestHeader;
    private String requestBody;
    private String clientIP;
    
    
    private BufferedReader in;
    private BufferedOutputStream out;
    
    


    public HttpThread(MyHttpServer httpServer, Socket socket,File fileRoot) {
    		this.httpServer = httpServer;
            this.socket = socket;
            this.fileRoot = fileRoot;
    }

    public void run() {
        // Vorbereitung und Einrichtung des BufferedReader und BufferedOutputStream
        // zum Lesen des Requests und zur Ausgabe der Response

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
            out = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.print(e.getMessage());
            return;
        }

        // Timeout für die Verbindung von 10 Sekunden
        try {
            socket.setSoTimeout(10000);
        } catch (SocketException e) {
            System.out.print(e.getMessage());
        }
        
        
        requestHeader = new ArrayList<>();
        try {
        	boolean headersFinished = false;
        	int contentLength = -1;

        	while (!headersFinished) {
             String line = in.readLine();
             
             if(line != null){
                 headersFinished = line.isEmpty();
             } else {
            	 headersFinished = true;
            	 continue;
             }
             
             if(!headersFinished){
            	 requestHeader.add(line);
             }
             if (line.startsWith("Content-Length:")) {
                 String cl = line.substring("Content-Length:".length()).trim();
                 contentLength = Integer.parseInt(cl);
             }
             if (line.startsWith("Host:")) {
                 String host = line.substring("Host".length()).trim();
                 clientIP = host.split(":")[0];
             }
        	}
        	if(contentLength != -1){
            	char[] buf = new char[contentLength];  //<-- http body is here
    			in.read(buf);
    			requestBody = new String(buf);
        	}
		} catch (IOException e) {
            sendError(out, 400, "Bad Request");
            System.out.print(e.getMessage()+"\n");
            return;
		}
        
        // Request war leer; sollte nicht auftreten
        if (requestHeader.isEmpty()) return;

        // Nur Requests mit dem HTTP 1.0 / 1.1 Protokoll erlaubt
        if (!requestHeader.get(0).endsWith(" HTTP/1.0") && !requestHeader.get(0).endsWith(" HTTP/1.1")) {
            sendError(out, 400, "Bad Request");
            System.err.print(400+ "Bad Request: " + requestHeader.get(0)+ socket.getInetAddress().toString()+"\n");
            return;
        }

        // Process GET-Request
        if(requestHeader.get(0).startsWith("GET ")){
        	System.out.println("GET-Request");
        	try{
        		processGETRequest();
        	} catch(IOException e){
        		System.out.print(e.getMessage()+"\n");
                return;
        	}
        // Process POST-Request
        } else if(requestHeader.get(0).startsWith("POST ")){	
        	System.out.println("POST-Request");
        	try{
            	processPOSTRequest();
        	} catch(IOException e){
        		System.out.print(e.getMessage()+"\n");
                return;
        	}
        // Else: Send Error
        } else {
            sendError(out, 501, "Not Implemented");
            System.err.print(501+ "Not Implemented: " + requestHeader.get(0)+socket.getInetAddress().toString()+"\n");
            return;
        }
    }
    
    private void processGETRequest() throws IOException, FileNotFoundException{
    	String wantedFile = requestHeader.get(0).substring(4, requestHeader.get(0).length() - 9);
    	String path = wantedFile;
        if(path.startsWith("/todoget.html")){
        	ToDoList todolist = httpServer.getToDoList(clientIP);
        	
        	
        	String toDoWebsite = HTTPUtil.generateToDoListWebsite(todolist.getList());
        	sendStringMessage(out, toDoWebsite);
        	return;
        } else if (wantedFile.contains("?")) {
            path = wantedFile.substring(0, wantedFile.indexOf("?"));
        } 
        if(path.equals("/")){
        	path = "/index.html";
        }
        

        System.out.println("PATH: " + path);
        File file = new File(fileRoot, URLDecoder.decode(path, "UTF-8")).getCanonicalFile();
        sendFile(file);
    }
    
    /**
     * Processing all POST-Requests
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void processPOSTRequest() throws IOException, FileNotFoundException{


    	String wantedFile = requestHeader.get(0).substring(5, requestHeader.get(0).length() - 9);
    	String path = wantedFile;

        System.out.println("Got post request:" + path);
        System.out.println("Body: " + requestBody);
    	if(path.startsWith("/addtolist.html")){
        	String toDoItem = HTTPUtil.getToDoItemFromBody(requestBody);
        	System.out.println(toDoItem);

        	if(toDoItem != null && !toDoItem.equals("")){
        		httpServer.putItemToToDoList(clientIP, toDoItem);
        		path = "/todoadd_success.html";
        	} else {
        		path = "/todoadd_fail.html";
        	}

        } else if(path.startsWith("/login")){
            String username = HTTPUtil.parseUsername(requestBody);
            String password = HTTPUtil.parsePassword(requestBody);
            System.out.println(username + "   " +password);
            boolean loginSucceed = httpServer.isAuthorizedUser(username, password);
            JsonSerialize jsonObj = new JsonSerialize();
            if(loginSucceed){
                System.out.println("Login succeed!");
                jsonObj.addString("success","true");
            } else {
                System.out.println("Login failed!");
                jsonObj.addString("success","false");
            }
            sendStringMessage(out, jsonObj.getString());
            return;
        } else if (wantedFile.contains("?")) {
            path = wantedFile.substring(0, wantedFile.indexOf("?"));
        } else {
            path = wantedFile;
        }
        
         File file = new File(fileRoot, URLDecoder.decode(path, "UTF-8")).getCanonicalFile();
         sendFile(file);
    }
    
    private void sendFile(File file) throws FileNotFoundException{
    	if (!file.exists()) {
            // Datei existiert nicht: Fehlerseite senden
            sendError(out, 404, "Not Found");
            System.err.print(404+ file.getAbsolutePath() + socket.getInetAddress().toString()+"\n");
            return;
        } else {
        	InputStream reader = new BufferedInputStream(new FileInputStream(file));

            // Falls es keinen festgelegten ContentType zur Dateiendung gibt, wird der Download gestartet
        	String contentType = HTTPUtil.getMimeTypes().get(HTTPUtil.getFileExtension(file));
        	
        	sendHeader(out, 200, "OK", contentType, file.length(), file.lastModified());
            System.out.print("[200] [" + socket.getInetAddress().toString() + "] " + file.getAbsolutePath());
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = reader.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                reader.close();
            } catch (NullPointerException | IOException e) {
                // Wirft eine "Broken Pipe" oder "Socket Write Error" Exception,
                // wenn der Download / Stream abgebrochen wird
                System.err.print(e.getMessage());
            }
        }
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            System.err.print(e.getMessage());
        }
    }
    

    /**
     * Sende den HTTP 1.1 Header zum Client
     *
     * @param out           Genutzter OutputStream
     * @param code          Status-Code, der gesendet werden soll
     * @param codeMessage   Zum Status-Code gehörende Nachricht
     * @param contentType   ContentType des Inhalts
     * @param contentLength Größe des Inhalts
     * @param lastModified  Wann die Datei zuletzt verändert wurde (zum Caching des Browsers)
     */
    private void sendHeader(BufferedOutputStream out, int code, String codeMessage, String contentType, long contentLength, long lastModified) {
        try {
            out.write(("HTTP/1.1 " + code + " " + codeMessage + "\r\n" +
                    "Date: " + new Date().toString() + "\r\n" +
                    "Server: Our HTTP-Server\r\n" +
                    "Content-Type: " + contentType + "; charset=utf-8\r\n" +
                    ((contentLength != -1) ? "Content-Length: " + contentLength + "\r\n" : "") +
                    "Last-modified: " + new Date(lastModified).toString() + "\r\n" +
                    "\r\n").getBytes());
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }

    /**
     * Sendet eine Fehlerseite zum Browser
     *
     * @param out     Genutzter OutputStream
     * @param code    Fehler-Code, der gesendet werden soll (403, 404, ...)
     * @param message Zusätzlicher Text ("Not Found", ...)
     */
    private void sendError(BufferedOutputStream out, int code, String message) {
        // Bereitet Daten der Response vor
        String output = HTTPUtil.getErrorTemplate("Error " + code + ": " + message);

        // Sendet Header der Response
        sendHeader(out, code, message, "text/html", output.length(), System.currentTimeMillis());

        try {
            // Sendet Daten der Response
            out.write(output.getBytes());
            out.flush();
            out.close();

            // Schließt den Socket; "keep-alive" wird also ignoriert
            socket.close();
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }
    
    private void sendStringMessage(BufferedOutputStream out, String message){
    	String contentType = HTTPUtil.getMimeTypes().get(".html");
    	sendHeader(out, 200, "OK", contentType, message.length(),  System.currentTimeMillis());
        System.out.println("[200] [" + socket.getInetAddress().toString() + "] " + message);
        
        try {
            out.write(message.getBytes());
            out.flush();
            out.close();
            
        } catch (NullPointerException | IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
  
}
