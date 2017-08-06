package itech.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class JsonSerialize implements IJsonSerialize {
	
	private Map<String, Object> objects;
	
	public JsonSerialize(){
		objects = new HashMap<String, Object>();
	}
	
	@Override
	public void addString(String key, String str) {
		objects.put(key, str);
	}

	public void addInteger(String key, int num) {
		objects.put(key, num);
	}

	@Override
	public void addDouble(String key, double num) {
		objects.put(key, num);
	}

	@Override
	public void addArray(String key, Map<String, Object> array) {
		objects.put(key, array);
	}

	@Override
	public String getString() {
		String json = new String("{\n");
		for(String key : objects.keySet()){
			Object value = objects.get(key);
			try {
				String jsonObj = getJSONObject(key, value);
				json += jsonObj + ",\n";
			} catch(Exception MyJSONException){
				System.err.println("Malformed JSON Object - skipped this one.");
			}
		} 
		json += "}";
		
		return json;
	}

	@Override
	public void parseString(String str) {
		Map<String, Object> newObjects = new HashMap<String, Object>();
		try {
			String[] lines = str.split("\n");
			if(!lines[0].equals("{") || !lines[lines.length - 1].equals("}")){
				throw new MyJSONException("");
			}
			for(int i = 1; i < lines.length - 1; i++){
				String line = lines[i];
				addJSONObject(line, newObjects);
			}
			objects = newObjects;
		}catch(Exception MyJSONException){
			System.err.println("Malformed JSON String - stopped parsing.");
		}
	}

	@Override
	public Map<String, Object> getObjects() {
		return objects;
	}

	@Override
	public Object getKey(String key) {
		return objects.get(key);
	}
	
	@SuppressWarnings("unchecked")
	private String getJSONObject(String key, Object object) throws MyJSONException{
		if(object instanceof String){
			return getJSONString(key, (String)object);
		} else if (object instanceof Double){
			return getJSONDouble(key, (Double)object);
		} else if (object instanceof Integer){
			return getJSONInteger(key, (Integer)object);
		} else if (object instanceof Map<?, ?>){
			return getJSONMap(key, (Map<String, Object>)object);
		} 
		throw new MyJSONException("Not parseable JSON-OBJECT");
	}
	
	private void addJSONObject(String line, Map<String, Object> currentObjects) throws MyJSONException {
		if(line.contains("{") && line.contains("}")){
			//Is Map:
			String[] keySplit = line.split(":",2);
			String key = keySplit[0].substring(1, keySplit[0].length() - 1);
			
			String valuesString = keySplit[1].substring(2, keySplit[1].length() -3);

			String[] valuesSplit = valuesString.split(",");	
			Map<String, Object> values = new LinkedHashMap<>();
			
			for(String value : valuesSplit){
				String[] split = value.split(":");
				String currentKey = split[0].substring(2, split[0].length() - 1);
				String currentValue = split[1].substring(1, split[1].length());
				if(StringUtils.isNumeric(currentValue)){
					//Is Integer
					int val = Integer.parseInt(currentValue);
					values.put(currentKey, val);
				} else if(NumberUtils.isNumber(currentValue)){
					//Is Double
					double val = Double.parseDouble(currentValue);
					values.put(currentKey, val);
				} else {
					//Is String
					values.put(currentKey, currentValue.substring(1, currentValue.length() - 1));
				}
			}
			currentObjects.put(key, values);
			
		} else{
			//Is Integer/Double/String
			addPrimitiveJSONObject(line, currentObjects);
		}
		
	}
	
	private void addPrimitiveJSONObject(String line, Map<String, Object> currentObjects){
		String[] split = line.split(":");
		String key = split[0].substring(1, split[0].length() - 1);
		String stringValue = split[1].substring(1, split[1].length() - 1);
		if(StringUtils.isNumeric(stringValue)){
			//Is Integer
			int val = Integer.parseInt(stringValue);
			currentObjects.put(key, val);
		} else if(NumberUtils.isNumber(stringValue)){
			//Is Double
			double val = Double.parseDouble(stringValue);
			currentObjects.put(key, val);
		} else {
			//Is String
			currentObjects.put(key, stringValue.substring(1, stringValue.length() - 1));
		}
	}
	

	private String getJSONString(String key, String str){
		return "\"" + key + "\": \"" + str + "\"";
	}
	
	private String getJSONDouble(String key, double num){
		return "\"" + key + "\": " + num;
	}
	
	private String getJSONInteger(String key, int num){
		return "\"" + key + "\": " + num;
	}
	
	private String getJSONMap(String key, Map<String, Object> array) throws MyJSONException{
		String str = "\"" + key + "\": { ";
		for(String currentKey : array.keySet()){
			Object currentValue = array.get(currentKey);
			String jsonObj = getJSONObject(currentKey, currentValue);
			str += jsonObj + ", ";	
		}
		str = str.substring(0, str.length() -2);
		str += " }";
		return str;
	}
	
	
}
