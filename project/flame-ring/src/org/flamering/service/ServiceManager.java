package org.flamering.service;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.flamering.component.Bean;
import org.flamering.component.Json;

// TODO: Auto-generated Javadoc
/**
 * The Class ServiceManager.
 */
public class ServiceManager {
	
	/** The Constant SERVICE_LOCATION_SEPARATOR. */
	public static final String SERVICE_LOCATION_SEPARATOR = ":";
	
	/** The service settings. */
	protected static ServiceSettings _settings = null;
	
	/** The logger. */
	protected static Logger _log = LoggerFactory.getLogger(ServiceManager.class);
	
	/**
	 * Initialize with service settings
	 *
	 * @param settings the bean name of the service settings
	 * @return true, if successful
	 */
	public static boolean init(String settings) {
		_settings = (ServiceSettings)Bean.getBean(settings);
		return _settings != null;
	}
	
	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public static Logger getLogger() {
		return _log;
	}
	
	/**
	 * Parses a service request.
	 *
	 * @param content the content of the request
	 * @return the output array including groupName, nodeName, beanName, funcName, funcParam
	 */
	public static String[] parseServiceRequest(String content) {
		
		String groupName = "";
		String nodeName = "";
		String beanName = "";
		String funcName = "";
		String funcParam = "";
		
		if (content != null && content.length() > 0) {
			
			String[] serviceInfo = parseBasicRequest(content);
			
			String[] beanInfo = parseService(serviceInfo[0]);
			String[] funcInfo = parseFunction(serviceInfo[1]);
			
			groupName = beanInfo[0];
			nodeName = beanInfo[1];
			beanName = beanInfo[2];
			
			funcName = funcInfo[0];
			funcParam = funcInfo[1];
		}
		
		return new String[]{groupName, nodeName, beanName, funcName, funcParam};
	}
	
	/**
	 * Parses a basic request.
	 *
	 * @param content the content of a basic request
	 * @return the output array including serviceName, serviceParam
	 */
	public static String[] parseBasicRequest(String content) {
		String serviceName = "";
		String serviceParam = ""; // this should include function name and function parameter
		if (content != null && content.length() > 0) {
			String validparam = content;
			if (validparam.charAt(0) == '/') validparam = validparam.substring(1);
			int pos = validparam.indexOf('/');
			if (pos >= 0) {
				serviceName = validparam.substring(0, pos);
				serviceParam = validparam.substring(pos+1);
			} else {
				serviceName = validparam;
			}
		}
		return new String[]{serviceName, serviceParam};
	}
	
	/**
	 * Parses the function info.
	 *
	 * @param content the content to parse
	 * @return the output array including funcName, funcParam
	 */
	public static String[] parseFunction(String content) {
		String funcName = "";
		String funcParam = "";
		if (content != null && content.length() > 0) {
			int pos = content.indexOf("/{");
			if (pos >= 0) {
				funcName = content.substring(0, pos);
				funcParam = content.substring(pos+1);
				int len = funcName.length();
				if (len > 0) {
					pos = funcName.lastIndexOf('/');
					if (pos >= 0 && pos < len - 1) {
						funcName = funcName.substring(pos + 1);
					} else if (pos == len - 1) {
						funcName = "";
					}
				}
			} else {
				String[] parts = content.split("/");
				if (parts.length >= 3) {
					funcName = parts[1];
					for (int i = 2; i < parts.length; i++) {
						if (i == 2) funcParam = parts[i];
						else funcParam += "/" + parts[i];
					}
				} else if (parts.length == 2) {
					funcName = parts[0];
					funcParam = parts[1];
				} else if (parts.length == 1) {
					funcName = parts[0];
				}
			}
		}
		return new String[]{funcName, funcParam};
	}
	
	/**
	 * Parses the service info.
	 *
	 * @param serviceName the full name of the service
	 * @return the output array including groupName, nodeName, beanName
	 */
	public static String[] parseService(String serviceName) {
		String groupName = "";
		String nodeName = "";
		String beanName = "";
		String serviceBeanName = _settings.getServiceBeanName(serviceName);
		if (serviceBeanName != null && serviceBeanName.length() > 0) {
			String[] names = serviceBeanName.split(ServiceManager.SERVICE_LOCATION_SEPARATOR);
			if (names.length >= 3) {
				beanName = names[2];
				nodeName = names[1];
				groupName = names[0];
			} else if(names.length == 2) {
				beanName = names[1];
				nodeName = names[0];
			} else if(names.length == 1) {
				beanName = names[0];
			}
		}
		return new String[]{groupName, nodeName, beanName};
	}
	
	/**
	 * call service
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 * @param requesterInfo the requester info
	 * @param regOnly whether call the function only when it is one of the registered functions
	 * @return the result string
	 */
	public static String call(String beanName, String functionName, String param, String requesterInfo, boolean regOnly) {
		Object result = null;
		boolean mustReturnNothing = false;
		Object bean = beanName == null || beanName.length() <= 0 ? null : Bean.getBean(beanName);
		if (bean != null && bean instanceof Service) {
			try {
				Method method = ((Service)bean).getFunction(functionName, regOnly);
				if (method != null) {
					Class<?> paramTypes[] = method.getParameterTypes();			
					if (paramTypes != null) {
						if (paramTypes.length == 2 && paramTypes[1].equals(ServiceRequester.class)) {
							if (paramTypes[0].equals(String.class)) {
								Object paramObjs[] = {param, new ServiceRequester(requesterInfo)};
								result = method.invoke(bean, paramObjs);
								mustReturnNothing = result == null;
							} else {
								Object paramObjs[] = {Json.toJsonObject(param, paramTypes[0]), new ServiceRequester(requesterInfo)};
								result = method.invoke(bean, paramObjs);
								mustReturnNothing = result == null;
							}
						} else if (paramTypes.length == 1) {
							if (paramTypes[0].equals(String.class)) {
								Object paramObjs[] = {param};
								result = method.invoke(bean, paramObjs);
								mustReturnNothing = result == null;
							} else {
								Object paramObjs[] = {Json.toJsonObject(param, paramTypes[0])};
								result = method.invoke(bean, paramObjs);
								mustReturnNothing = result == null;
							}
						} else if (paramTypes.length == 0) {
							Object paramObjs[] = {};
							result = method.invoke(bean, paramObjs);
							mustReturnNothing = result == null;
						}
					} else {
						Object paramObjs[] = {};
						result = method.invoke(bean, paramObjs);
						mustReturnNothing = result == null;
					}
					
				}
			} catch (Exception ex) {
				_log.error(ex.getMessage());
			}
		}
		
		if (result == null) result = mustReturnNothing ? "" : "Invalid service call";
		
		return result instanceof String 
				? result.toString() 
						: Json.toJsonString(result);
	}
	
	/**
	 * call service
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 * @param requesterInfo the requester info
	 * @return the result string
	 */
	public static String call(String beanName, String functionName, String param, String requesterInfo) {
		// normally clients can call only the registered functions directly
		return call(beanName, functionName, param, requesterInfo, true);
	}
	
	/**
	 * call service
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 * @return the result string
	 */
	public static String call(String beanName, String functionName, String param) {
		Object result = null;
		Object bean = beanName == null || beanName.length() <= 0 ? null : Bean.getBean(beanName);
		if (bean != null && bean instanceof Service) {
			try {
				Method method = ((Service)bean).getFunction(functionName, false); // can call all public functions
				if (method != null) {
					Class<?> paramTypes[] = method.getParameterTypes();
					if (paramTypes != null && paramTypes.length == 1) {
						if (paramTypes[0].equals(String.class)) {
							Object paramObjs[] = {param};
							result = method.invoke(bean, paramObjs);
						} else {
							Object paramObjs[] = {Json.toJsonObject(param, paramTypes[0])};
							result = method.invoke(bean, paramObjs);
						}
					} else if (paramTypes == null || paramTypes.length == 0) {
						Object paramObjs[] = {};
						result = method.invoke(bean, paramObjs);
					}
				}
			} catch (Exception ex) {
				_log.error(ex.getMessage());
			}
		}
		
		if (result == null) result = "Invalid service call";
		
		return result instanceof String 
				? result.toString() 
						: Json.toJsonString(result);
	}
	
	/**
	 * call service with multiple input arguments
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param args the arguments
	 * @return the returned result
	 */
	public static Object callService(String beanName, String functionName, Object... args) {
		Object bean = beanName == null || beanName.length() <= 0 ? null : Bean.getBean(beanName);
		if (bean != null && bean instanceof Service) {
			Method method = ((Service)bean).getFunction(functionName, false); // can call all public functions
			if (method != null) {
				try {
					return method.invoke(bean, args);
				} catch (Exception ex) {
					_log.error(ex.getMessage());
				}
			}
		}
		return null;
	}

}
