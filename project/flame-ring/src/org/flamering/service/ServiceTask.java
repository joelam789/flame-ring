package org.flamering.service;

import org.flamering.component.Grid;

// 1. this thread should work just for the client's direct requests (from Vert.x) only.
// 2. because we use this thread just to make sure that the process of 
//    "putting client's requests to grids to compute" would be executed in multi-thread mode.
// 3. and please know that all computations in grids should be executed in some mode like multi-thread/multi-process already.
//    (that's what Apache Ignite would do by default)

/**
 * The Class ServiceTask.
 */
public class ServiceTask implements Runnable {
	
	/** The service session. */
	protected ServiceSession _session = null;
	
	/** The session name. */
	protected String _sessionName = "";
	
	/** Whether the session is a registered session. */
	protected boolean _isRegisteredSession = false;
	
	/** The task content. */
	protected String _taskContent = "";
	
	/**
	 * Instantiates a new service task.
	 *
	 * @param taskContent the task content
	 * @param session the service session
	 */
	public ServiceTask(String taskContent, ServiceSession session) {
		_session = session;
		_sessionName = "";
		_isRegisteredSession = false;
		
		_taskContent = taskContent;
	}
	
	/**
	 * Instantiates a new service task.
	 *
	 * @param taskContent the task content
	 * @param session the service session
	 * @param sessionName the session name
	 */
	public ServiceTask(String taskContent, ServiceSession session, String sessionName) {
		_session = session;
		_sessionName = sessionName;
		_isRegisteredSession = _session != null 
				&& _sessionName != null && _sessionName.length() > 0;
		
		_taskContent = taskContent;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
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
					_session.send(result);
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
