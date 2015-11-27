package org.flamering.service;

import java.util.List;

import org.flamering.component.Grid;
import org.flamering.component.Network;

public class NetworkService extends BaseService {
	
	public static final String DEFAULT_ADDRESS = "127.0.0.1";
	public static final String SERVICE_NAME = "network-service";
	
	public static final String FUNC_ADDRESS = "getAddress";
	public static final String FUNC_SERVICE_ADDRESS = "getServiceAddress";
	public static final String FUNC_CLIENT_COUNT = "getClientCount";
	public static final String FUNC_SEND = "send";
	public static final String FUNC_BROADCAST = "broadcast";
	public static final String FUNC_DISCONNECT = "disconnect";
	
	private String _address = "";
	
	public String getAddress() {
		if (_address == null || _address.length() <= 0) {
			List<String> list = Grid.getLocalAddressList();
			for (String addr : list) {
				if (addr != null && addr.length() > 0
						&& !addr.equals("0:0:0:0:0:0:0:1") 
						&& !addr.equals("127.0.0.1")) {
					_address = addr;
					break;
				}
			}
		}
		return (_address == null || _address.length() <= 0) ? DEFAULT_ADDRESS : _address;
	}

	public void setAddress(String address) {
		_address = address;
	}
	
	public String getServiceAddress(String protocol) {
		String ipAddress = getAddress();
		if (protocol != null && protocol.equalsIgnoreCase("http")) {
			return ipAddress + ":" + Network.getHttpListeningPort();
		} else {
			return ipAddress + ":" + Network.getWebSocketListeningPort();
		}
	}
	
	public String getClientCount() {
		return String.valueOf(Network.getWebSocketClientCount());
	}

	public String send(NetworkMessage msg) {
		if (msg != null) {	
			String message = msg.getMessage();
			List<String> endpoints = msg.getEndpoints();
			if (message != null && endpoints != null) {
				String[] clients = {};
				clients = endpoints.toArray(clients);
				if (clients != null && clients.length > 0)
					Network.getInstance().send(message, clients);
			}
		}
		return "";
	}
	
	public String broadcast(String param) {
		
		if (param != null && param.length() > 0) {
			Network.getInstance().broadcast(param);
		}
		
		return "";
	}
	
	public String disconnect(String param) {
		
		if (param != null && param.length() > 0) {
			String[] endpoints = param.split(Network.ENDPOINT_SEPARATOR);
			Network.getInstance().disconnect(endpoints);
		} else {
			Network.getInstance().disconnect();
		}
		
		return "";
	}

}
