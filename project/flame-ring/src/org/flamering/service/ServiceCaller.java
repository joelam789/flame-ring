package org.flamering.service;

import java.io.Serializable;

public class ServiceCaller implements Serializable {
	
	private static final long serialVersionUID = 5137776185264306594L;
	
	private String _beanName = "";
	private String _functionName = "";
	private String _param = "";
	
	private String _requester = "";
	
	public ServiceCaller(String beanName, String functionName, String param) {
		if (_beanName != null) _beanName = beanName;
		if (functionName != null) _functionName = functionName;
		if (param != null) _param = param;
		
		_requester = "";
	}
	
	public ServiceCaller(String beanName, String functionName, String param, String request) {
		if (beanName != null) _beanName = beanName;
		if (functionName != null) _functionName = functionName;
		if (param != null) _param = param;
		
		if (request != null) _requester = request;
	}
	
	public String call() {
		return (_requester != null && _requester.length() > 0)
				? ServiceManager.call(_beanName, _functionName, _param, _requester)
						: ServiceManager.call(_beanName, _functionName, _param);
	}

}
