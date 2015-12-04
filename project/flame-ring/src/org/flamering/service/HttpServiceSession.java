package org.flamering.service;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class HttpServiceSession.
 */
public class HttpServiceSession implements ServiceSession {
	
	/** The event bus. */
	private EventBus _bus = null;
	
	/** The http request. */
	private HttpServerRequest _request = null;
	
	/** The http response. */
	private HttpServerResponse _response = null;
	
	/**
	 * Instantiates a new http service session.
	 *
	 * @param request the http request
	 * @param response the http response
	 */
	public HttpServiceSession(HttpServerRequest request, HttpServerResponse response) {
		_request = request;
		_response = response;
	}
	
	/**
	 * Instantiates a new http service session.
	 *
	 * @param request the http request
	 * @param response the http response
	 * @param bus the event bus
	 */
	public HttpServiceSession(HttpServerRequest request, HttpServerResponse response, EventBus bus) {
		_bus = bus;
		_request = request;
		_response = response;
	}
	
	/* (non-Javadoc)
	 * @see org.flamering.service.ServiceSession#broadcast(java.lang.String, java.lang.String[])
	 */
	public void broadcast(String msg, String... endpoints) {
		if (_bus != null && endpoints != null && endpoints.length > 0) {
			for(String endpoint : endpoints) {
				_bus.send(endpoint, msg);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.flamering.service.ServiceSession#send(java.lang.String)
	 */
	public void send(String msg) {
		if (_response != null) _response.end(msg);
	}
	
	/* (non-Javadoc)
	 * @see org.flamering.service.ServiceSession#close()
	 */
	public void close() {
		if (_response != null) _response.close();
	}
	
	/* (non-Javadoc)
	 * @see org.flamering.service.ServiceSession#getRemoteAddress()
	 */
	public String getRemoteAddress() {
		if (_request != null) return _request.remoteAddress().toString();
		return "";
	}
	
	/* (non-Javadoc)
	 * @see org.flamering.service.ServiceSession#getSessionName()
	 */
	public String getSessionName() {
		return getRemoteAddress(); // for now, http session will not get a real session name...
	}
	
	/* (non-Javadoc)
	 * @see org.flamering.service.ServiceSession#getProtocol()
	 */
	public String getProtocol() {
		return "HTTP";
	}

}
