package org.flamering.component;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.support.AbstractApplicationContext;  
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Wrapper class for Spring (just about the management of beans).
 */
public class Bean {
	
	/** The Spring context. */
	private static AbstractApplicationContext _ctx = null;
	
	/** The logger. */
	private static Logger _log = LoggerFactory.getLogger(Bean.class);
	
	/**
	 * Initialize Spring context with the config file
	 *
	 * @param configFile the config file
	 * @return true, if successful
	 */
	public static boolean init(String configFile) {
		if (_ctx == null) {
			_ctx = new ClassPathXmlApplicationContext(new String[]{configFile}, false); // let's forbid auto refreshing first
			if (_ctx != null) _ctx.registerShutdownHook(); // register Spring hook to destroy beans automatically.
			if (_ctx != null) _ctx.refresh(); // then refresh it here when _appctx is not null (more safe)
		}
		return _ctx != null;
	}
	
	/**
	 * Gets a bean.
	 *
	 * @param beanName the bean name
	 * @return the bean
	 */
	public static Object getBean(String beanName) {
		try {
			if(_ctx != null) return _ctx.getBean(beanName);
			else return null;
		} catch(Exception ex) {
			_log.error(ex.getMessage());
		}
		return null;
	}
	
	/**
	 * Call an object's method.
	 *
	 * @param obj the obj
	 * @param methodName the method name
	 * @param params the params
	 * @return the returned result of the method
	 */
	public static Object callMethod(Object obj, String methodName, Object params) {

        try {
            Class<?> clazz = obj.getClass();
            if (clazz == null) return null;
            Method method = clazz.getMethod(methodName, params == null ? new Class[]{} : new Class[]{params.getClass()});
            if (method == null) return null;
            return method.invoke(obj, params == null ? new Object[]{} : new Object[]{params});
        } catch(Exception ex) {
        	_log.error(ex.getMessage());
        }
        return null;
	}
	
	/**
	 * Call class method.
	 *
	 * @param className the class name
	 * @param methodName the method name
	 * @param params the params
	 * @return the returned result of the method
	 */
	public static Object callClassMethod(String className, String methodName, Object params) {

        try {
            Class<?> clazz = Class.forName(className);
            if (clazz == null) return null;
            Method method = clazz.getMethod(methodName, params == null ? new Class[]{} : new Class[]{params.getClass()});
            if (method == null) return null;
            return method.invoke(clazz.newInstance(), params == null ? new Object[]{} : new Object[]{params});
        } catch(Exception ex) {
        	_log.error(ex.getMessage());
        }
        return null;
	}

}
