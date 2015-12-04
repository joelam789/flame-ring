package org.flamering.service;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Class ServiceCaller.
 */
public class ServiceCaller implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5137776185264306594L;
	
	/** The bean name. */
	private String _beanName = "";
	
	/** The function name. */
	private String _functionName = "";
	
	/** The param. */
	private String _param = "";
	
	/** The requester. */
	private String _requester = "";
	
	/**
	 * Instantiates a new service caller.
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 */
	public ServiceCaller(String beanName, String functionName, String param) {
		if (_beanName != null) _beanName = beanName;
		if (functionName != null) _functionName = functionName;
		if (param != null) _param = param;
		
		_requester = "";
	}
	
	/**
	 * Instantiates a new service caller.
	 *
	 * @param beanName the bean name
	 * @param functionName the function name
	 * @param param the param
	 * @param request the request
	 */
	public ServiceCaller(String beanName, String functionName, String param, String request) {
		if (beanName != null) _beanName = beanName;
		if (functionName != null) _functionName = functionName;
		if (param != null) _param = param;
		
		if (request != null) _requester = request;
	}
	
	/**
	 * call service
	 *
	 * @return the string
	 */
	public String call() {
		return (_requester != null && _requester.length() > 0)
				? ServiceManager.call(_beanName, _functionName, _param, _requester)
						: ServiceManager.call(_beanName, _functionName, _param);
	}

}
