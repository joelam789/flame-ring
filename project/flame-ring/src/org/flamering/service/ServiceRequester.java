package org.flamering.service;

import org.flamering.component.Grid;
import org.flamering.component.Network;

// TODO: Auto-generated Javadoc
/**
 * The Class ServiceRequester.
 */
public class ServiceRequester {
	
	/** The server name. */
	private String serverName = "";
	
	/** The session name. */
	private String sessionName = "";
	
	/** The remote address. */
	private String remoteAddress = "";
	
	/** The protocol. */
	private String protocol = "";
	
	/**
	 * Gets server name.
	 *
	 * @return the server name
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * Gets session name.
	 *
	 * @return the session name
	 */
	public String getSessionName() {
		return sessionName;
	}

	/**
	 * Gets remote address.
	 *
	 * @return the remote address
	 */
	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	/**
	 * Gets protocol name.
	 *
	 * @return the protocol name
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Instantiates a new service requester.
	 *
	 * @param requesterInfo the requester info
	 */
	public ServiceRequester(String requesterInfo) {
		String[] parts = requesterInfo.split(Network.ENDPOINT_SEPARATOR);
		serverName = parts.length >= 1 ? parts[0] : "";
		sessionName = parts.length >= 2 ? parts[1] : "";
		remoteAddress = parts.length >= 3 ? parts[2] : "";
		protocol = parts.length >= 4 ? parts[3] : "";
	}
	
	/**
	 * Gets the requester info from a session
	 *
	 * @param session the session
	 * @return the requester info as string
	 */
	public static String getRequesterInfo(ServiceSession session) {
		return session == null ? "" 
				: Grid.getLocalName() 
				+ Network.ENDPOINT_SEPARATOR + session.getSessionName() 
				+ Network.ENDPOINT_SEPARATOR + session.getRemoteAddress()
				+ Network.ENDPOINT_SEPARATOR + session.getProtocol();
	}

}
