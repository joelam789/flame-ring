package org.flamering.service;

import java.util.ArrayList;
import java.util.List;

public class NetworkMessage {
	
	private String message = "";
	
	private List<String> endpoints = new ArrayList<>();

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<String> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(List<String> endpoints) {
		this.endpoints = endpoints;
	}

}
