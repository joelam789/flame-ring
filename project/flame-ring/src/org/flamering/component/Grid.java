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

public class Grid {
	
	public static final String NODE_TYPE = "ring-type";
	public static final String NODE_NAME = "ring-name";
	public static final String ENDPOINT_SEPARATOR = ",";
	
	private static Ignite _grid = null;
	
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
	
	public static Ignite getGrid() {
		return _grid;
	}
	
	public static String getLocalType() {
		if (_grid != null) {
			Object value = _grid.cluster().localNode().attribute(NODE_TYPE);
			if (value != null) return value.toString();
		}
		return "";
	}
	
	public static String getLocalName() {
		if (_grid != null) {
			Object value = _grid.cluster().localNode().attribute(NODE_NAME);
			if (value != null) return value.toString();
		}
		return "";
	}
	
	public static List<String> getLocalAddressList() {
		List<String> result = new ArrayList<>();
		if (_grid != null) {
			result.addAll(_grid.cluster().localNode().addresses());
		}
		return result;
	}
	
	public static String getLocalAddresses() {
		if (_grid != null) {
			String[] array = {};
			List<String> list = getLocalAddressList();
			return String.join(ENDPOINT_SEPARATOR, list.toArray(array));
		}
		return "";
	}
	
	public static String getLocalCommunicationPortInfo() {
		if (_grid != null) {
			TcpCommunicationSpi spi = (TcpCommunicationSpi) _grid.configuration().getCommunicationSpi();
			int port = spi.getLocalPort();
			int range = spi.getLocalPortRange();
			return port + " - " + (port + range);
		}
		return "";
	}
	
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
	
	public static String getLocalInfo() {
		if (_grid != null) {
			return _grid.cluster().localNode().metrics().toString();
		}
		return "";
	}
	
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
	
	public static <K,V> IgniteCache<K,V> getCache(String cacheName) {
		if(_grid != null) return _grid.<K, V>cache(cacheName);
		else return null;
	}
	
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
	
	public static ClusterGroup getGroup(String nodeType) {
		if (_grid != null) {
			return _grid.cluster().forAttribute(NODE_TYPE, nodeType);
		}
		return null;
	}
	
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
	
	public static String call(String beanName, String functionName, String param, String requesterInfo) {
		return call(beanName, functionName, param, requesterInfo, null);
	}
	
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
	
	public static String call(String beanName, String functionName, Object param) {
		return call(beanName, functionName, param, null);
	}
	
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
	
	public static List<String> callBatch(String beanName, String functionName, List<Object> batch) {
		return callBatch(beanName, functionName, batch, null);
	}
	
	public static void run(String beanName, String functionName, Object param, ClusterGroup group) {
		if (_grid != null) {
			String input = param == null ? "" : (param instanceof String ? param.toString() : Json.toJsonString(param));
			if (group != null) {			
				_grid.compute(group).run(() -> {
					ServiceManager.call(beanName, functionName, input);
				});
			} else {
				_grid.compute().run(() -> {
					ServiceManager.call(beanName, functionName, input);
				});
			}
		}
	}
	
	public static void run(String beanName, String functionName, Object param) {
		run(beanName, functionName, param, null);
	}
	
	public static void runBatch(String beanName, String functionName, List<Object> batch, ClusterGroup group) {
		if (_grid != null) {
			Collection<IgniteRunnable> funcs = new ArrayList<>();
			for (Object param : batch) {
				funcs.add(() -> {
					ServiceManager.call(beanName, functionName, 
							param == null ? "" : (param instanceof String ? param.toString() : Json.toJsonString(param)));
				});
			}
			if (group != null) _grid.compute(group).run(funcs);
			else _grid.compute().run(funcs);
		}
	}
	
	public static void runBatch(String beanName, String functionName, List<Object> batch) {
		runBatch(beanName, functionName, batch, null);
	}
	
	public static void broadcast(String beanName, String functionName, Object param, ClusterGroup group) {
		if (_grid != null) {
			String input = param == null ? "" : (param instanceof String ? param.toString() : Json.toJsonString(param));
			if (group != null) {			
				_grid.compute(group).broadcast(() -> {
					ServiceManager.call(beanName, functionName, input);
				});
			} else {
				_grid.compute().broadcast(() -> {
					ServiceManager.call(beanName, functionName, input);
				});
			}
		}
	}
	
	public static void broadcast(String beanName, String functionName, Object param) {
		broadcast(beanName, functionName, param, null);
	}
	
	public static List<String> broadcastCall(String beanName, String functionName, Object param, ClusterGroup group) {
		List<String> result = new ArrayList<String>();
		if (_grid != null) {
			String input = param == null ? "" : (param instanceof String ? param.toString() : Json.toJsonString(param));
			ServiceCaller callable = new ServiceCaller(beanName, functionName, input);
			result.addAll(group == null ? _grid.compute().broadcast(callable::call) : _grid.compute(group).broadcast(callable::call));
		}
		return result;
	}
	
	public static List<String> broadcastCall(String beanName, String functionName, Object param) {
		return broadcastCall(beanName, functionName, param, null);
	}
	
}
