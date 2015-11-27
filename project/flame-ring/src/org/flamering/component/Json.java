package org.flamering.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Json {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	public static ObjectNode createJsonNode() {
		return MAPPER.createObjectNode();
	}
	
	public static ObjectNode toJsonNode(Object obj) {
		return MAPPER.convertValue(obj, ObjectNode.class);
	}
	
	public static String toJsonString(Object obj) {
		return toJsonString(obj, obj.getClass());
	}
	
	public static String toJsonString(Object obj, Class<?> rootType) {
		try {
			return MAPPER.writerFor(rootType).writeValueAsString(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String toJsonString(Object obj, TypeReference<?> rootType) {
		try {
			return MAPPER.writerFor(rootType).writeValueAsString(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String toJsonString(Object obj, JavaType rootType) {
		try {
			return MAPPER.writerFor(rootType).writeValueAsString(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static Object toJsonObject(String str, Class<?> rootType) {
		try {
			return MAPPER.readerFor(rootType).readValue(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Object toJsonObject(String str, TypeReference<?> rootType) {
		try {
			return MAPPER.readerFor(rootType).readValue(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Object toJsonObject(String str, JavaType rootType) {
		try {
			return MAPPER.readerFor(rootType).readValue(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
