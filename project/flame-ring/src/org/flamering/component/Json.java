package org.flamering.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

// TODO: Auto-generated Javadoc
/**
 * Wrapper class for Jackson
 */
public class Json {
	
	/** The Constant MAPPER. */
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * Creates a new JSON node.
	 *
	 * @return the new node
	 */
	public static ObjectNode createJsonNode() {
		return MAPPER.createObjectNode();
	}
	
	/**
	 * convert POJO to JSON node.
	 *
	 * @param obj the POJO
	 * @return the node
	 */
	public static ObjectNode toJsonNode(Object obj) {
		return MAPPER.convertValue(obj, ObjectNode.class);
	}
	
	/**
	 * convert POJO to JSON string.
	 *
	 * @param obj the POJO
	 * @return the JSON string
	 */
	public static String toJsonString(Object obj) {
		return toJsonString(obj, obj.getClass());
	}
	
	/**
	 * convert POJO to JSON string.
	 *
	 * @param obj the POJO
	 * @param rootType the class of the POJO
	 * @return the JSON string
	 */
	public static String toJsonString(Object obj, Class<?> rootType) {
		try {
			return MAPPER.writerFor(rootType).writeValueAsString(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * convert POJO to JSON string.
	 *
	 * @param obj the POJO
	 * @param rootType the reference of the type of the POJO
	 * @return the JSON string
	 */
	public static String toJsonString(Object obj, TypeReference<?> rootType) {
		try {
			return MAPPER.writerFor(rootType).writeValueAsString(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * convert POJO to JSON string.
	 *
	 * @param obj the POJO
	 * @param rootType the type of the POJO
	 * @return the JSON string
	 */
	public static String toJsonString(Object obj, JavaType rootType) {
		try {
			return MAPPER.writerFor(rootType).writeValueAsString(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * convert JSON string to POJO
	 *
	 * @param str the JSON string
	 * @param rootType the class of the POJO
	 * @return the POJO
	 */
	public static Object toJsonObject(String str, Class<?> rootType) {
		try {
			return MAPPER.readerFor(rootType).readValue(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * convert JSON string to POJO
	 *
	 * @param str the JSON string
	 * @param rootType the reference of the type of the POJO
	 * @return the POJO
	 */
	public static Object toJsonObject(String str, TypeReference<?> rootType) {
		try {
			return MAPPER.readerFor(rootType).readValue(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * convert JSON string to POJO
	 *
	 * @param str the JSON string
	 * @param rootType the type of the POJO
	 * @return the POJO
	 */
	public static Object toJsonObject(String str, JavaType rootType) {
		try {
			return MAPPER.readerFor(rootType).readValue(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
