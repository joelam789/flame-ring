package org.flamering.service;

import org.flamering.component.Grid;
import org.flamering.component.Network;

public class ServiceRequester {
	
	private String serverName = "";
	
	private String sessionName = "";
	
	private String remoteAddress = "";
	
	public String getServerName() {
		return serverName;
	}

	public String getSessionName() {
		return sessionName;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public ServiceRequester(String requesterInfo) {
		String[] parts = requesterInfo.split(Network.ENDPOINT_SEPARATOR);
		serverName = parts[0];
		sessionName = parts[1];
		remoteAddress = parts[2];
	}
	
	public static String getRequesterInfo(ServiceSession session) {
		return session == null ? "" 
				: Grid.getLocalName() 
				+ Network.ENDPOINT_SEPARATOR + session.getSessionName() 
				+ Network.ENDPOINT_SEPARATOR + session.getRemoteAddress();
	}

}
