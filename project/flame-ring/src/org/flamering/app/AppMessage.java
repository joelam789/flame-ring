package org.flamering.app;

public class AppMessage {
	
	private String _name = "";
	private String _param = "";
	private String _content = "";
	
	public AppMessage(String name, String content) {
		_name = name;
		_content = content;
	}
	
	public AppMessage(String name, String content, String param) {
		_name = name;
		_content = content;
		_param = param;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getContent() {
		return _content;
	}
	
	public String getParam() {
		return _param;
	}
	
	public String setParam(String value) {
		return _param = value;
	}
	
}
