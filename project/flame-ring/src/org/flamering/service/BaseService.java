package org.flamering.service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BaseService implements Service {
	
	protected Map<String, String> _functionMap = new HashMap<String, String>();
	protected Map<String, Method> _originalMethods = new HashMap<String, Method>();
	
	public BaseService() {
		if (_originalMethods != null) _originalMethods.clear();
		else _originalMethods = new HashMap<String, Method>();
		Method[] methods = getClass().getMethods();
		for (Method method : methods) {
			String methodName = method.getName().toLowerCase();
			if (!_originalMethods.containsKey(methodName)) _originalMethods.put(methodName, method);
		}
	}
	
	public Map<String, String> getFunctionMap() {
		return _functionMap;
	}
	public void setFunctionMap(Map<String, String> functionMap) {
		if (_functionMap != null) _functionMap.clear();
		else _functionMap = new HashMap<String, String>();
		if (functionMap != null) {
			for (String key : functionMap.keySet()) {
				_functionMap.put(key.toLowerCase(), functionMap.get(key).toLowerCase());
			}
		}
	}
	
	public String getFunctionName(String key, boolean regOnly) {
		if (_functionMap != null && key != null && key.length() > 0) {
			String funcName = _functionMap.get(key.toLowerCase());
			if (funcName != null && funcName.length() > 0) return funcName;
		}
		return regOnly ? "" : (key == null ? "" : key.toLowerCase());
	}
	
	@Override
	public Method getFunction(String key, boolean regOnly) {
		String functionName = getFunctionName(key, regOnly);
		if (functionName != null && functionName.length() > 0) {
			return _originalMethods.get(functionName);
		}
		return null;
	}
}
