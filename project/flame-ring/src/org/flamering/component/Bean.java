package org.flamering.component;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.support.AbstractApplicationContext;  
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Bean {
	
	private static AbstractApplicationContext _appctx = null;
	
	private static Logger _log = LoggerFactory.getLogger(Bean.class);
	
	public static boolean init(String configFile) {
		if (_appctx == null) {
			_appctx = new ClassPathXmlApplicationContext(new String[]{configFile}, false); // let's forbid auto refreshing first
			if (_appctx != null) _appctx.registerShutdownHook(); // register Spring hook to destroy beans automatically.
			if (_appctx != null) _appctx.refresh(); // then refresh it here when _appctx is not null (more safe)
		}
		return _appctx != null;
	}
	
	public static Object getBean(String beanName) {
		try {
			if(_appctx != null) return _appctx.getBean(beanName);
			else return null;
		} catch(Exception ex) {
			_log.error(ex.getMessage());
		}
		return null;
	}
	
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
