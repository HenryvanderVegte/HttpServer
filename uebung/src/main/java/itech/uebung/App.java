package itech.uebung;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import itech.json.JsonSerialize;
import itech.webserver.MyHttpServer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        /*
    	JsonSerialize jsonSerialize = new JsonSerialize();
    	jsonSerialize.addInteger("hausnummer", 14);
    	jsonSerialize.addString("name", "mueller");
    	jsonSerialize.addDouble("gehalt", 450);
    	
    	
    	Map<String, Object> map = new LinkedHashMap<>();
    	map.put("a1", "a2");
    	map.put("b1", "b2");
    	map.put("c1", "c2");
    	
    	
    	
    	jsonSerialize.addArray("irgendwas", map);
    	
    	String JSONString = jsonSerialize.getString();
    	System.out.println(JSONString);
    	jsonSerialize.parseString(JSONString);
    	
    	System.out.println(jsonSerialize.getString());

        */
    	Path targetPath = Paths.get("target");
        MyHttpServer httpServer = new MyHttpServer(8888, targetPath.toFile());

    }
    
}
