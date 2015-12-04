package org.flamering.service;

import java.lang.reflect.Method;

// TODO: Auto-generated Javadoc
/**
 * The Interface Service.
 */
public interface Service {
	
	/**
	 * Gets the service's function.
	 *
	 * @param key the key
	 * @param regOnly whether only get the registered function names
	 * @return the function
	 */
	Method getFunction(String key, boolean regOnly);

}
