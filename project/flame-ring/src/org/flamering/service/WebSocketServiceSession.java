package org.flamering.service;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ServerWebSocket;

public class WebSocketServiceSession implements ServiceSession {
	
	private EventBus _bus = null;
	private ServerWebSocket _ws = null;
	
	public WebSocketServiceSession(ServerWebSocket ws) {
		_ws = ws;
	}
	
	public WebSocketServiceSession(ServerWebSocket ws, EventBus bus) {
		_ws = ws;
		_bus = bus;
	}
	
	public void broadcast(String msg, String... endpoints) {
		if (_bus != null && endpoints != null && endpoints.length > 0) {
			for(String endpoint : endpoints) {
				_bus.send(endpoint, msg);
			}
		}
	}
	
	public void send(String msg) {
		if (_ws != null) _ws.writeFinalTextFrame(msg);
	}
	
	public void close() {
		if (_ws != null) _ws.close();
	}
	
	public String getRemoteAddress() {
		if (_ws != null) return _ws.remoteAddress().toString();
		return "";
	}
	
	public String getSessionName() {
		if (_ws != null) return _ws.textHandlerID();
		return "";
	}

}
