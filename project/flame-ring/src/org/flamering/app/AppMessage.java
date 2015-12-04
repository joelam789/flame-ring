package org.flamering.app;

// TODO: Auto-generated Javadoc
/**
 * The Class AppMessage.
 */
public class AppMessage {
	
	/** The name. */
	private String _name = "";
	
	/** The param. */
	private String _param = "";
	
	/** The content. */
	private String _content = "";
	
	/**
	 * Instantiates a new app message.
	 *
	 * @param name the name
	 * @param content the content
	 */
	public AppMessage(String name, String content) {
		_name = name;
		_content = content;
	}
	
	/**
	 * Instantiates a new app message.
	 *
	 * @param name the name
	 * @param content the content
	 * @param param the param
	 */
	public AppMessage(String name, String content, String param) {
		_name = name;
		_content = content;
		_param = param;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Gets the content.
	 *
	 * @return the content
	 */
	public String getContent() {
		return _content;
	}
	
	/**
	 * Gets the param.
	 *
	 * @return the param
	 */
	public String getParam() {
		return _param;
	}
	
	/**
	 * Sets the param.
	 *
	 * @param value the value of the param
	 */
	public void setParam(String value) {
		_param = value;
	}
	
}
