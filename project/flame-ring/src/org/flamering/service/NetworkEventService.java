package org.flamering.service;

/**
 * Need to extend this class if you want to process something when clients connect or disconnect
 */
public class NetworkEventService extends BaseService {
	
	/** The Constant BEAN_NAME. */
	public static final String BEAN_NAME = "network-event-service";
	
	/** The Constant EVENT_OPEN. */
	public static final String EVENT_OPEN = "onOpen";
	
	/** The Constant EVENT_CLOSE. */
	public static final String EVENT_CLOSE = "onClose";
	
	/**
	 * Occurs when a new client connects to the server
	 *
	 * @param sessionName the session name
	 * @param remoteAddress the remote address
	 * @return return empty string normally
	 */
	public String onOpen(String sessionName, String remoteAddress) {
		// to be overridden
		System.out.println("New client connected: " + remoteAddress);
		return "";
	}
	
	/**
	 * Occurs when a client disconnects from the server
	 *
	 * @param sessionName the session name
	 * @param remoteAddress the remote address
	 * @return return empty string normally
	 */
	public String onClose(String sessionName, String remoteAddress) {
		// to be overridden
		System.out.println("Client disconnected: " + remoteAddress);
		return "";
	}

}
