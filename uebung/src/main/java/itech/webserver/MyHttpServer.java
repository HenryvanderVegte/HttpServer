package itech.webserver;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.*;

import itech.util.ToDoList;;

/**
 * Created by leonakuse on 18.07.17.
 */
public class MyHttpServer {
	
	Map<String, ToDoList> clientToDoLists;

    public MyHttpServer(int port, final File fileRoot){
    	clientToDoLists = new HashMap<>();
    	
    	
    	System.out.println("Server started...");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException | IllegalArgumentException e) {
            // Port bereits belegt, darf nicht genutzt werden, ...: Abbruch
            System.err.print(e.getMessage());
            System.out.print("Beende...");
            System.exit(1);
        }

        final ServerSocket finalServerSocket = serverSocket;
        Thread connectionListener = new Thread(() -> {
            while (true) {
                try {
                    HttpThread thread = new HttpThread(this, finalServerSocket.accept(), fileRoot);
                    thread.start();
                } catch (IOException e) {
                    System.err.print(e.getMessage());
                    System.exit(1);
                }
            }
        });
        connectionListener.start();
        
    }
    
    public void putItemToToDoList(String clientIP, String item){
    	if(clientToDoLists.containsKey(clientIP)){
    		ToDoList todolist = clientToDoLists.get(clientIP);
    		todolist.add(item);
    	}else {
    		ToDoList newlist = new ToDoList();
    		newlist.add(item);
    		clientToDoLists.put(clientIP, newlist);
    	}
    }
    
    public ToDoList getToDoList(String clientIP){
    	if(clientToDoLists.containsKey(clientIP)){
    		return clientToDoLists.get(clientIP);
    	} else {
    		ToDoList newlist = new ToDoList();
    		clientToDoLists.put(clientIP, newlist);
    		return newlist;
    	}
    }
}
