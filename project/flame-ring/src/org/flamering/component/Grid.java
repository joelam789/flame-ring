package org.flamering.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;

import org.flamering.service.ServiceCaller;
import org.flamering.service.ServiceManager;

// TODO: Auto-generated Javadoc
/**
 * Wrapper class for Apache Ignite
 */
public class Grid {
	
	/** The Constant NODE_TYPE. */
	public static final String NODE_TYPE = "ring-type";
	
	/** The Constant NODE_NAME. */
	public static final String NODE_NAME = "ring-name";
	
	/** The Constant ENDPOINT_SEPARATOR. */
	public static final String ENDPOINT_SEPARATOR = ",";
	
	/** The grid (the main object of Apache Ignite). */
	private static Ignite _grid = null;
	
	/**
	 * Start Apache Ignite with config file.
	 *
	 * @param configFile the config file
	 * @return true, if successful
	 */
	public static boolean start(String configFile) {
		
		System.out.println("Starting Ignite (with config file " + configFile + ") ...");
		
		if (_grid == null) _grid = Ignition.start(configFile);
		
		if (_grid != null) {
			
			try {
				
				Thread.sleep(100);
				
				String nodeType = getLocalType();
				if (nodeType == null || nodeType.length() <= 0) {
					nodeType = null;
					System.err.println("Need to set node type via Ignite property \"userAttributes\"");
					System.err.println("e.g. <entry key=\"" + NODE_TYPE + "\" value=\"SampleType\"/>");
				}
				String nodeName = getLocalName();
				if (nodeName == null || nodeName.length() <= 0) {
					nodeName = null;
					System.err.println("Need to set node name via Ignite property \"userAttributes\"");
					System.err.println("e.g. <entry key=\"" + NODE_NAME + "\" value=\"SampleName\"/>");
				} else {
					if (_grid.cluster().forAttribute(NODE_NAME, nodeName).nodes().size() > 1) {
						System.err.println("There is another node with the same name [" + nodeName + "] running in the grid system.");
						System.err.println("Please change the name of the node so that it can join the grid system.");
						nodeName = null;
					}
				}
				
				if (nodeType != null && nodeName != null) {
					System.out.println("Local Node Type: " + nodeType);
					System.out.println("Local Node Name: " + nodeName);
					System.out.println("Local Node Addresses: " + getLocalAddresses());
					System.out.println("Local Node Communication Port: " + getLocalCommunicationPortInfo());
					System.out.println("Local Node Discovery Port: " + getLocalDiscoveryPortInfo());
					//System.out.println("Local Node Info: " + getLocalInfo());
					
				} else {
					if (_grid != null) {
						_grid.close();
						_grid = null;
						Thread.sleep(100);
					}
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		if (_grid != null) System.out.println("Ignite started!");
		else System.err.println("Failed to start Ignite!");
		
		return _grid != null;
	}
	
	/**
	 * Stop Apache Ignite
	 */
	public static void stop() {
		try {
			if (_grid != null) {
				_grid.close();
				_grid = null;
				Thread.sleep(100);
			}
			System.out.println("Ignite stopped!");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the main Ignite object.
	 *
	 * @return the Ignite object
	 */
	public static Ignite getGrid() {
		return _grid;
	}
	
	/**
	 * Gets the server type of local server.
	 *
	 * @return the local server type
	 */
	public static String getLocalType() {
		if (_grid != null) {
			Object value = _grid.cluster().localNode().attribute(NODE_TYPE);
			if (value != null) return value.toString();
		}
		return "";
	}
	
	/**
	 * Gets the server name of local server.
	 *
	 * @return the local server name
	 */
	public static String getLocalName() {
		if (_grid != null) {
			Object value = _grid.cluster().localNode().attribute(NODE_NAME);
			if (value != null) return value.toString();
		}
		return "";
	}
	
	/**
	 * Gets the address list of local server.
	 *
	 * @return the local server address list
	 */
	public static List<String> getLocalAddressList() {
		List<String> result = new ArrayList<>();
		if (_grid != null) {
			result.addAll(_grid.cluster().localNode().addresses());
		}
		return result;
	}
	
	/**
	 * Gets local server's addresses as a string.
	 *
	 * @return the local server addresses
	 */
	public static String getLocalAddresses() {
		if (_grid != null) {
			String[] array = {};
			List<String> list = getLocalAddressList();
			return String.join(ENDPOINT_SEPARATOR, list.toArray(array));
		}
		return "";
	}
	
	/**
	 * Gets the communication port info of local server.
	 *
	 * @return the local communication port info
	 */
	public static String getLocalCommunicationPortInfo() {
		if (_grid != null) {
			TcpCommunicationSpi spi = (TcpCommunicationSpi) _grid.configuration().getCommunicationSpi();
			int port = spi.getLocalPort();
			int range = spi.getLocalPortRange();
			return port + " - " + (port + range);
		}
		return "";
	}
	
	/**
	 * Gets the discovery port info of local server.
	 *
	 * @return the local discovery port info
	 */
	public static String getLocalDiscoveryPortInfo() {
		if (_grid != null) {
			TcpDiscoverySpi spi = (TcpDiscoverySpi) _grid.configuration().getDiscoverySpi();
			int port = spi.getLocalPort();
			//int range = spi.getLocalPortRange();
			//return port + " - " + (port + range);
			return String.valueOf(port);
		}
		return "";
	}
	
	/**
	 * Gets summary info of local server.
	 *
	 * @return the local server info
	 */
	public static String getLocalInfo() {
		if (_grid != null) {
			return _grid.cluster().localNode().metrics().toString();
		}
		return "";
	}
	
	/**
	 * Gets the address list of all grids.
	 *
	 * @return the address list of all grids
	 */
	public static List<String> getGridAddressList() {
		List<String> list = new ArrayList<String>();
		if (_grid != null) {
			Collection<ClusterNode> nodeList = _grid.cluster().nodes();
			if (nodeList != null && nodeList.size() > 0) {
				for (ClusterNode node : nodeList) {
					Object nodeType = node.attribute(NODE_TYPE);
					Object nodeName = node.attribute(NODE_NAME);
					String fullName = "";
					if (nodeType != null) fullName += nodeType.toString();
					if (nodeName != null) fullName += ":" + nodeName.toString();
					String[] array = {};
					String addresses = String.join(ENDPOINT_SEPARATOR, node.addresses().toArray(array));
					if (fullName != null && fullName.length() > 0) list.add(fullName + " - " + addresses);
					else list.add(addresses);
				}
			}
		}
		return list;
	}
	
	/**
	 * Gets the cache by name
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param cacheName the cache name
	 * @return the cache
	 */
	public static <K,V> IgniteCache<K,V> getCache(String cacheName) {
		if(_grid != null) return _grid.<K, V>cache(cacheName);
		else return null;
	}
	
	/**
	 * Gets the group with filters
	 *
	 * @param filters the filters
	 * @return the group
	 */
	public static ClusterGroup getGroup(Map<String, String> filters) {
		if (_grid != null) {
			ClusterGroup group = null;
			IgniteCluster cluster = _grid.cluster();
			if (filters == null || filters.size() <= 0) {
				group = cluster.forServers();
			} else {
				for (String key : filters.keySet()) {
					if (group == null) group = cluster.forAttribute(key, filters.get(key));
					else group = group.forAttribute(key, filters.get(key));
				}
			}
			return group;
		}
		return null;
	}
	
	/**
	 * Gets the group by server type (node type)
	 *
	 * @param nodeType the node type
	 * @return the group
	 */
	public static ClusterGroup getGroup(String nodeType) {
		if (_grid != null) {
			return _grid.cluster().forAttribute(NODE_TYPE, nodeType);
		}
		return null;
	}
	
	/**
	 * Gets the group by server name (node name)
	 *
	 * @param nodeName the node name
	 * @return the group
	 */
	public static ClusterGroup getGroupByName(String nodeName) {
		ClusterGroup group = null;
		if (_grid != null) {
			group = _grid.cluster().forAttribute(NODE_NAME, nodeName);
			if (group != null && group.nodes().size() > 1) {
				_grid.log().error("Found more than one node with the name [" + nodeName + "]");
				return null;
			}
		}
		return group;
	}
	
	/**
	 * remote procedure call among the grids in the group
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 * @param requesterInfo the requester info
	 * @param group the group
	 * @return the result string
	 */
	public static String call(String beanName, String functionName, String param, String requesterInfo, ClusterGroup group) {
		if (_grid != null) {
			Collection<IgniteCallable<String>> calls = new ArrayList<>();
			ServiceCaller callable = new ServiceCaller(beanName, functionName, param, requesterInfo);
			calls.add(callable::call);
			Collection<String> results = group == null ? _grid.compute().call(calls) : _grid.compute(group).call(calls);
			for (String result : results) return result;
		}
		return "";
	}
	
	/**
	 * remote procedure call among all grids
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 * @param requesterInfo the requester info
	 * @return the result string
	 */
	public static String call(String beanName, String functionName, String param, String requesterInfo) {
		return call(beanName, functionName, param, requesterInfo, null);
	}
	
	/**
	 * remote procedure call among the grids in the group
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 * @param group the group
	 * @return the result string
	 */
	public static String call(String beanName, String functionName, Object param, ClusterGroup group) {
		if (_grid != null) {
			String input = param == null ? "" : (param instanceof String ? param.toString() : Json.toJsonString(param));
			Collection<IgniteCallable<String>> calls = new ArrayList<>();
			ServiceCaller callable = new ServiceCaller(beanName, functionName, input);
			calls.add(callable::call);
			Collection<String> results = group == null ? _grid.compute().call(calls) : _grid.compute(group).call(calls);
			for (String result : results) return result;
		}
		return "";
	}
	
	/**
	 * remote procedure call among all grids
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 * @return the result string
	 */
	public static String call(String beanName, String functionName, Object param) {
		return call(beanName, functionName, param, null);
	}
	
	/**
	 * batch of remote procedure calls among the grids in the group
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param batch the batch
	 * @param group the group
	 * @return the result list
	 */
	public static List<String> callBatch(String beanName, String functionName, List<Object> batch, ClusterGroup group) {
		List<String> result = new ArrayList<String>();
		if (_grid != null) {
			Collection<IgniteCallable<String>> calls = new ArrayList<>();
			for (Object param : batch) {
				String input = param == null ? "" : (param instanceof String ? param.toString() : Json.toJsonString(param));
				ServiceCaller callable = new ServiceCaller(beanName, functionName, input);
				calls.add(callable::call);
			}
			result.addAll(group == null ? _grid.compute().call(calls) : _grid.compute(group).call(calls));
		}
		return result;
	}
	
	/**
	 * batch of remote procedure calls among all grids
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param batch the batch
	 * @return the result list
	 */
	public static List<String> callBatch(String beanName, String functionName, List<Object> batch) {
		return callBatch(beanName, functionName, batch, null);
	}
	
	/**
	 * no-return-value remote procedure call among the grids in the group
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 * @param group the group
	 */
	public static void run(String beanName, String functionName, Object param, ClusterGroup group) {
		if (_grid != null) {
			String input = param == null ? "" : (param instanceof String ? param.toString() : Json.toJsonString(param));
			if (group != null) {
				_grid.compute(group).withAsync().run(() -> {
					ServiceManager.call(beanName, functionName, input);
				});
			} else {
				_grid.compute().withAsync().run(() -> {
					ServiceManager.call(beanName, functionName, input);
				});
			}
		}
	}
	
	/**
	 * no-return-value remote procedure call among all grids
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 */
	public static void run(String beanName, String functionName, Object param) {
		run(beanName, functionName, param, null);
	}
	
	/**
	 * batch of no-return-value remote procedure calls among the grids in the group
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param batch the batch
	 * @param group the group
	 */
	public static void runBatch(String beanName, String functionName, List<Object> batch, ClusterGroup group) {
		if (_grid != null) {
			Collection<IgniteRunnable> funcs = new ArrayList<>();
			for (Object param : batch) {
				funcs.add(() -> {
					ServiceManager.call(beanName, functionName, 
							param == null ? "" : (param instanceof String ? param.toString() : Json.toJsonString(param)));
				});
			}
			if (group != null) _grid.compute(group).withAsync().run(funcs);
			else _grid.compute().withAsync().run(funcs);
		}
	}
	
	/**
	 * batch of no-return-value remote procedure calls among all grids
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param batch the batch
	 */
	public static void runBatch(String beanName, String functionName, List<Object> batch) {
		runBatch(beanName, functionName, batch, null);
	}
	
	/**
	 * broadcast remote procedure call among the grids in the group
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 * @param group the group
	 */
	public static void broadcast(String beanName, String functionName, Object param, ClusterGroup group) {
		if (_grid != null) {
			String input = param == null ? "" : (param instanceof String ? param.toString() : Json.toJsonString(param));
			if (group != null) {			
				_grid.compute(group).withAsync().broadcast(() -> {
					ServiceManager.call(beanName, functionName, input);
				});
			} else {
				_grid.compute().withAsync().broadcast(() -> {
					ServiceManager.call(beanName, functionName, input);
				});
			}
		}
	}
	
	/**
	 * broadcast remote procedure call among all grids
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 */
	public static void broadcast(String beanName, String functionName, Object param) {
		broadcast(beanName, functionName, param, null);
	}
	
	/**
	 * broadcast remote procedure call among the grids in the group, and get the list of results returned
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 * @param group the group
	 * @return the result list
	 */
	public static List<String> broadcastCall(String beanName, String functionName, Object param, ClusterGroup group) {
		List<String> result = new ArrayList<String>();
		if (_grid != null) {
			String input = param == null ? "" : (param instanceof String ? param.toString() : Json.toJsonString(param));
			ServiceCaller callable = new ServiceCaller(beanName, functionName, input);
			result.addAll(group == null ? _grid.compute().broadcast(callable::call) : _grid.compute(group).broadcast(callable::call));
		}
		return result;
	}
	
	/**
	 * broadcast remote procedure call among all grids, and get the list of results returned
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 * @return the result list
	 */
	public static List<String> broadcastCall(String beanName, String functionName, Object param) {
		return broadcastCall(beanName, functionName, param, null);
	}
	
}
