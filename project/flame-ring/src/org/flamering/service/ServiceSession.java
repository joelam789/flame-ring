package org.flamering.service;

public interface ServiceSession {
	
	String getRemoteAddress();
	
	String getSessionName();
	
	void send(String msg);
	
	void broadcast(String msg, String... endpoints);
	
	void close();

}
