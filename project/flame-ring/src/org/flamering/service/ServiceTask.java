package org.flamering.service;

import org.flamering.component.Grid;
import org.flamering.component.Network;

// 1. this thread should work just for the client's direct requests (from Vert.x) only.
// 2. because we use this thread just to make sure that the process of 
//    "putting client's requests to grids to compute" would be executed in multi-thread mode.
// 3. and please know that all computations in grids should be executed in some mode like multi-thread/multi-process already.
//    (that's what Apache Ignite would do by default)

public class ServiceTask implements Runnable {
	
	private ServiceSession _session = null;
	private String _sessionName = "";
	private boolean _isRegisteredSession = false;
	
	private String _taskContent = "";
	
	public ServiceTask(String taskContent, ServiceSession session) {
		_session = session;
		_sessionName = "";
		_isRegisteredSession = false;
		
		_taskContent = taskContent;
	}
	
	public ServiceTask(String taskContent, ServiceSession session, String sessionName) {
		_session = session;
		_sessionName = sessionName;
		_isRegisteredSession = _session != null 
				&& _sessionName != null && _sessionName.length() > 0;
		
		_taskContent = taskContent;
	}

	@Override
	public void run() {
		
		try {
			
			if (_session != null) {
				
				// if there is a session, that means the task should be processing a request from Vert.x
				// so we should be able to get some info about the client here
				
				String requesterInfo = ServiceRequester.getRequesterInfo(_session);
				
				String[] serviceInfo = ServiceManager.parseServiceRequest(_taskContent);
				
				// serviceInfo[0] = groupName
				// serviceInfo[1] = nodeName
				// serviceInfo[2] = beanName
				// serviceInfo[3] = functionName
				// serviceInfo[4] = functionParam
				
				String result = "";
				
				try {
				
					if (serviceInfo[1] != null && serviceInfo[1].length() > 0)
						result = Grid.call(serviceInfo[2], serviceInfo[3], serviceInfo[4], requesterInfo, Grid.getGroupByName(serviceInfo[1]));
					else if (serviceInfo[0] != null && serviceInfo[0].length() > 0)
						result = Grid.call(serviceInfo[2], serviceInfo[3], serviceInfo[4], requesterInfo, Grid.getGroup(serviceInfo[0]));
					else
						result = Grid.call(serviceInfo[2], serviceInfo[3], serviceInfo[4], requesterInfo);

				} catch(Exception ex) {
					ServiceManager.getLogger().error(ex.getMessage());
					result = "Service cannot be executed successfully";
				}
				
				if (result != null && result.length() > 0) {
					if (_isRegisteredSession && result.equals(Network.INVALID_SESSION_FLAG)) {
						_session.close();
						ServiceExecutor.getInstance().unregisterSession(_sessionName);
					} else _session.send(result);
				}
				
			}
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			
		} finally {
			
			// this block should always get executed, unless JVM cannot work normally
			
			if (_isRegisteredSession) {
				ServiceExecutor.getInstance().processTask(_sessionName);
			}
		}
		
	}
	
}
