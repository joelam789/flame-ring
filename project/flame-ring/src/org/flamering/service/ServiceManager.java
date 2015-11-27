package org.flamering.service;

import java.lang.reflect.Method;

//import java.util.UUID;
//import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.flamering.component.Bean;
import org.flamering.component.Json;

public class ServiceManager {
	
	//private static String _id = UUID.randomUUID().toString();
	//private static AtomicInteger _req = new AtomicInteger(0);
	
	public static final String SERVICE_LOCATION_SEPARATOR = ":";
	
	private static ServiceSettings _settings = null;
	
	private static Logger _log = LoggerFactory.getLogger(ServiceManager.class);
	
	public static boolean init(String settings) {
		_settings = (ServiceSettings)Bean.getBean(settings);
		return _settings != null;
	}
	
	public static Logger getLogger() {
		return _log;
	}
	
	public static String[] parseServiceRequest(String content) {
		
		String groupName = "";
		String nodeName = "";
		String beanName = "";
		String funcName = "";
		String funcParam = "";
		
		if (content != null && content.length() > 0) {
			
			String[] serviceInfo = parseRequest(content);
			
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
	
	public static String[] parseRequest(String content) {
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
	
	public static String call(String beanName, String functionName, String param, String requesterInfo, boolean regOnly) {
		Object result = null;
		Object bean = beanName == null || beanName.length() <= 0 ? null : Bean.getBean(beanName);
		if (bean != null && bean instanceof Service) {
			try {
				Method method = ((Service)bean).getFunction(functionName, regOnly);
				if (method != null) {
					Class<?> paramTypes[] = method.getParameterTypes();			
					if (paramTypes != null) {
						if (paramTypes.length == 2 && paramTypes[1].equals(String.class)) {
							if (paramTypes[0].equals(String.class)) {
								Object paramObjs[] = {param, requesterInfo};
								result = method.invoke(bean, paramObjs);
							} else {
								Object paramObjs[] = {Json.toJsonObject(param, paramTypes[0]), requesterInfo};
								result = method.invoke(bean, paramObjs);
							}
						} else if (paramTypes.length == 1) {
							if (paramTypes[0].equals(String.class)) {
								Object paramObjs[] = {param};
								result = method.invoke(bean, paramObjs);
							} else {
								Object paramObjs[] = {Json.toJsonObject(param, paramTypes[0])};
								result = method.invoke(bean, paramObjs);
							}
						} else if (paramTypes.length == 0) {
							Object paramObjs[] = {};
							result = method.invoke(bean, paramObjs);
						}
					} else {
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
	
	public static String call(String beanName, String functionName, String param, String requesterInfo) {
		// normally clients can call only the registered functions directly
		return call(beanName, functionName, param, requesterInfo, true);
	}
	
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
