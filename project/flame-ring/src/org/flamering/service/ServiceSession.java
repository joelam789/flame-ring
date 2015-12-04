package org.flamering.service;

// TODO: Auto-generated Javadoc
/**
 * The Interface ServiceSession.
 */
public interface ServiceSession {
	
	/**
	 * Gets protocol name.
	 *
	 * @return the protocol name
	 */
	String getProtocol();
	
	/**
	 * Gets remote address.
	 *
	 * @return the remote address
	 */
	String getRemoteAddress();
	
	/**
	 * Gets session name.
	 *
	 * @return the session name
	 */
	String getSessionName();
	
	/**
	 * Send message.
	 *
	 * @param msg the message
	 */
	void send(String msg);
	
	/**
	 * Broadcast message.
	 *
	 * @param msg the message
	 * @param endpoints the endpoints
	 */
	void broadcast(String msg, String... endpoints);
	
	/**
	 * Close session.
	 */
	void close();

}
