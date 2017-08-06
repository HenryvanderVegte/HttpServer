package itech.util;

import java.util.ArrayList;
import java.util.List;

public class ToDoList {
	private List<String> todolist;
	
	public ToDoList(){
		todolist = new ArrayList<>();
	}
	
	
	public void add(String item){
		if(!todolist.contains(item))
			todolist.add(item);
	}
	
	
	public List<String> getList(){
		return todolist;
	}
}
