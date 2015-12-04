package org.flamering.service;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class NetworkMessage.
 */
public class NetworkMessage {
	
	/** The message. */
	protected String message = "";
	
	/** The endpoints. */
	protected List<String> endpoints = new ArrayList<>();
	
	/**
	 * Instantiates a new network message.
	 */
	public NetworkMessage() {
		message = "";
		endpoints = new ArrayList<>();
	}
	
	/**
	 * Instantiates a new network message.
	 *
	 * @param msg the message
	 * @param endpointList the list of endpoints
	 */
	public NetworkMessage(String msg, List<String> endpointList) {
		if (msg != null) message = msg;
		if (endpointList != null) {
			endpoints.clear();
			endpoints.addAll(endpointList);
		}
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message the new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Gets the endpoints.
	 *
	 * @return the endpoints
	 */
	public List<String> getEndpoints() {
		return endpoints;
	}

	/**
	 * Sets the endpoints.
	 *
	 * @param endpoints the new endpoints
	 */
	public void setEndpoints(List<String> endpoints) {
		this.endpoints = endpoints;
	}

}
