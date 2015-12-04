package org.flamering.service;

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ServiceSettings.
 */
public class ServiceSettings {
	
	/** The Constant BEAN_NAME. */
	public static final String BEAN_NAME = "service-settings";
	
	/** The service map. */
	protected Map<String, String> _serviceMap = new HashMap<String, String>();
	
	/**
	 * Gets the service map.
	 *
	 * @return the service map
	 */
	public Map<String, String> getServiceMap() {
		return _serviceMap;
	}
	
	/**
	 * Sets the service map.
	 *
	 * @param serviceMap the service map
	 */
	public void setServiceMap(Map<String, String> serviceMap) {
		if (_serviceMap != null) _serviceMap.clear();
		else _serviceMap = new HashMap<String, String>();
		if (serviceMap != null) {
			for (String key : serviceMap.keySet()) {
				_serviceMap.put(key.toLowerCase(), serviceMap.get(key));
			}
		}
	}
	
	/**
	 * Gets the bean name with the service name.
	 *
	 * @param serviceName the service name
	 * @return the bean name of the service
	 */
	public String getServiceBeanName(String serviceName) {
		// _serviceMap should be an immutable HashMap once it got initialized from ServiceManager.init()
		// so it should be okay to call this function in multiple threads
		if (_serviceMap != null && serviceName != null && serviceName.length() > 0) {
			String beanName = _serviceMap.get(serviceName.toLowerCase());
			return (beanName == null || beanName.length() <= 0) ? "" : beanName;
		}
		return "";
	}
	
}
