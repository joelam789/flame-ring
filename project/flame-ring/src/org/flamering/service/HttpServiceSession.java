package org.flamering.service;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class HttpServiceSession implements ServiceSession {
	
	private EventBus _bus = null;
	private HttpServerRequest _request = null;
	private HttpServerResponse _response = null;
	
	public HttpServiceSession(HttpServerRequest request, HttpServerResponse response) {
		_request = request;
		_response = response;
	}
	
	public HttpServiceSession(HttpServerRequest request, HttpServerResponse response, EventBus bus) {
		_bus = bus;
		_request = request;
		_response = response;
	}
	
	public void broadcast(String msg, String... endpoints) {
		if (_bus != null && endpoints != null && endpoints.length > 0) {
			for(String endpoint : endpoints) {
				_bus.send(endpoint, msg);
			}
		}
	}
	
	public void send(String msg) {
		if (_response != null) _response.end(msg);
	}
	
	public void close() {
		if (_response != null) _response.close();
	}
	
	public String getRemoteAddress() {
		if (_request != null) return _request.remoteAddress().toString();
		return "";
	}
	
	public String getSessionName() {
		return getRemoteAddress();
	}

}
