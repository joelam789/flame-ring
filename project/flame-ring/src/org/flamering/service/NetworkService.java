package org.flamering.service;

import java.util.List;

import org.flamering.component.Grid;
import org.flamering.component.Network;

// TODO: Auto-generated Javadoc
/**
 * The Class NetworkService.
 */
public class NetworkService extends BaseService {
	
	/** The Constant DEFAULT_ADDRESS. */
	public static final String DEFAULT_ADDRESS = "127.0.0.1";
	
	/** The Constant BEAN_NAME. */
	public static final String BEAN_NAME = "network-service";
	
	/** The Constant FUNC_ADDRESS. */
	public static final String FUNC_ADDRESS = "getAddress";
	
	/** The Constant FUNC_SERVICE_ADDRESS. */
	public static final String FUNC_SERVICE_ADDRESS = "getServiceAddress";
	
	/** The Constant FUNC_CLIENT_COUNT. */
	public static final String FUNC_CLIENT_COUNT = "getClientCount";
	
	/** The Constant FUNC_SEND. */
	public static final String FUNC_SEND = "send";
	
	/** The Constant FUNC_BROADCAST. */
	public static final String FUNC_BROADCAST = "broadcast";
	
	/** The Constant FUNC_DISCONNECT. */
	public static final String FUNC_DISCONNECT = "disconnect";
	
	/** The address of the web server (should be an external address). */
	protected String _address = "";
	
	/**
	 * Gets the address of the web server (should be an external address).
	 *
	 * @return the address
	 */
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

	/**
	 * Sets the address of the web server (should be an external address).
	 *
	 * @param address the new address
	 */
	public void setAddress(String address) {
		_address = address;
	}
	
	/**
	 * Gets the service address.
	 *
	 * @param protocol the protocol name, could be "http" or "ws"
	 * @return the service address
	 */
	public String getServiceAddress(String protocol) {
		String ipAddress = getAddress();
		if (protocol != null && protocol.equalsIgnoreCase("http")) {
			return ipAddress + ":" + Network.getHttpListeningPort();
		} else {
			return ipAddress + ":" + Network.getWebSocketListeningPort();
		}
	}
	
	/**
	 * Gets the client count.
	 *
	 * @return the client count
	 */
	public String getClientCount() {
		return String.valueOf(Network.getWebSocketClientCount());
	}

	/**
	 * Send message
	 *
	 * @param msg the message
	 * @return empty string
	 */
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
	
	/**
	 * Broadcast message
	 *
	 * @param param the message
	 * @return empty string
	 */
	public String broadcast(String param) {
		if (param != null && param.length() > 0) {
			Network.getInstance().broadcast(param);
		}
		return "";
	}
	
	/**
	 * Disconnect some clients
	 *
	 * @param param the clients, empty string means want to disconnect all
	 * @return empty string
	 */
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
