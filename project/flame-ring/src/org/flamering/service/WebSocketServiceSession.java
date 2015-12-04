package org.flamering.service;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ServerWebSocket;

// TODO: Auto-generated Javadoc
/**
 * The Class WebSocketServiceSession.
 */
public class WebSocketServiceSession implements ServiceSession {
	
	/** The event bus. */
	private EventBus _bus = null;
	
	/** The websocket. */
	private ServerWebSocket _ws = null;
	
	/**
	 * Instantiates a new websocket service session.
	 *
	 * @param ws the websocket
	 */
	public WebSocketServiceSession(ServerWebSocket ws) {
		_ws = ws;
	}
	
	/**
	 * Instantiates a new websocket service session.
	 *
	 * @param ws the websocket
	 * @param bus the event bus
	 */
	public WebSocketServiceSession(ServerWebSocket ws, EventBus bus) {
		_ws = ws;
		_bus = bus;
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
		if (_ws != null) _ws.writeFinalTextFrame(msg);
	}
	
	/* (non-Javadoc)
	 * @see org.flamering.service.ServiceSession#close()
	 */
	public void close() {
		if (_ws != null) {
			try {
				_ws.close();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.flamering.service.ServiceSession#getRemoteAddress()
	 */
	public String getRemoteAddress() {
		if (_ws != null) return _ws.remoteAddress().toString();
		return "";
	}
	
	/* (non-Javadoc)
	 * @see org.flamering.service.ServiceSession#getSessionName()
	 */
	public String getSessionName() {
		if (_ws != null) return _ws.textHandlerID();
		return "";
	}
	
	/* (non-Javadoc)
	 * @see org.flamering.service.ServiceSession#getProtocol()
	 */
	public String getProtocol() {
		return "WebSocket";
	}

}
