package org.flamering.service;

public class NetworkEventService extends BaseService {
	
	public static final String SERVICE_NAME = "network-event-service";
	public static final String EVENT_OPEN = "onOpen";
	public static final String EVENT_CLOSE = "onClose";
	
	public String onOpen(String sessionName, String remoteAddress) {
		// to be overridden
		System.out.println("New client connected: " + remoteAddress);
		return "";
	}
	
	public String onClose(String sessionName, String remoteAddress) {
		// to be overridden
		System.out.println("Client disconnected: " + remoteAddress);
		return "";
	}

}
