package itech.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class HTTPUtil {
	
	private static final String addToListLink = "/addtolist.html";
	private static final String addToListItem = "listitem=";
	private static final String usernameTAG = "username=";
	private static final String passwordTAG = "password=";


    private static HashMap<String, String> mimeTypes = new HashMap<>();
    private static boolean mimeTypesInitCompleted = false;
	
	public static String getItemFromURL(String url){
		String totalscheme = addToListLink + "?" + addToListItem;
		if(!url.startsWith(totalscheme)){
			return null;
		}
		String item = url.substring(totalscheme.length(), url.length());
		
		return item;
	}

	public static String parseUsername(String body){
		String user = body.split("&")[0];
		user = user.substring(usernameTAG.length(), user.length());
		return user;
	}

	public static String parsePassword(String body){
		String password = body.split("&")[1];
		password = password.substring(passwordTAG.length(), password.length());
		return password;
	}

	public static String getToDoItemFromBody(String body){
		if(!body.startsWith(addToListItem)){
			return null;
		}
		String item = body.substring(addToListItem.length(), body.length());
		
		return item;
	}
	
	public static String generateToDoListWebsite(List<String> toDoList){
		String site = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<title>" + "ToDo-Items" + "</title>" +
                "</head>" +
                "<body>" +
                "<h1>" + "ToDo-Items:" + "</h1>" +
                "<ol>";
		
		for(String s : toDoList){
			s = s.replaceAll("[<>]", "");
			site += "<li>" + s + "</li>";
		}
		
		site +=  "</ol>" + "</body>" + "</html>";
		return site;
	}
	
	  public static HashMap<String, String> getMimeTypes() {
	        if (mimeTypesInitCompleted) return mimeTypes;

	        // Bilder
	        mimeTypes.put(".gif", "image/gif");
	        mimeTypes.put(".jpg", "image/jpeg");
	        mimeTypes.put(".jpeg", "image/jpeg");
	        mimeTypes.put(".png", "image/png");

	        // Audio
	        mimeTypes.put(".mp3", "audio/mpeg");
	        mimeTypes.put(".mp4", "video/mp4");
	        mimeTypes.put(".flv", "video/x-flv");

	        // Webseiten
	        mimeTypes.put(".html", "text/html");
	        mimeTypes.put(".htm", "text/html");
	        mimeTypes.put(".shtml", "text/html");
	        mimeTypes.put(".xhtml", "text/html");
	        mimeTypes.put(".css", "text/css");
	        mimeTypes.put(".js", "text/js");

	        // Anderes
	        mimeTypes.put(".txt", "text/plain");
	        mimeTypes.put(".log", "text/plain");
	        mimeTypes.put(".md", "text/x-markdown");
	        mimeTypes.put(".pdf", "application/pdf");
	        mimeTypes.put(".xml", "application/xml");
	        mimeTypes.put(".java", "text/plain");
	        mimeTypes.put(".json", "application/json");

	        mimeTypesInitCompleted = true;
	        return mimeTypes;
	    }

	    public static String getFileExtension(File file) {
	        String filename = file.getName();
	        int pos = filename.lastIndexOf(".");
	        if (pos >= 0) return filename.substring(pos).toLowerCase();
	        return "";
	    }

	    public static String getErrorTemplate(String error) {
	        return "<!DOCTYPE html>" +
	                "<html>" +
	                "<head>" +
	                "<title>" + error + "</title>" +
	                "</head>" +
	                "<body>" +
	                "<h1>" + error + "</h1>" +
	                "</body>" +
	                "</html>";
	    }
}
