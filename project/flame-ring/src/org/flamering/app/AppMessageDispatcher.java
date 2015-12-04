package org.flamering.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.flamering.component.Bean;

/**
 * A thread which is responsible for dispatching application messages
 */
public class AppMessageDispatcher extends Thread {
	
	/** The Constant MAX_MSG_COUNT. */
	public static final int MAX_MSG_COUNT = 8192;
	
	/** The Constant DEFAULT_SVC_FUNC_NAME. */
	public static final String DEFAULT_SVC_FUNC_NAME = "handleMessage";
	
	/** The work state. */
	private int _workState = 0;
	
	/** The message stack. */
	private Stack<AppMessage> _msgs = new Stack<AppMessage>();
	
	/** The list of registered handlers. */
	private Map<String, String> _handlerlist = new HashMap<String, String>();
	
	/**
	 * Handle messages.
	 */
	public synchronized void handleMessages() {
		if(_msgs.size() == 0) return;
		AppMessage msg = _msgs.pop();
		if(msg == null) return;
		String msgName = msg.getName();
		String handlerClassName = _handlerlist.get(msgName);
		if(handlerClassName == null) return;
		Bean.callClassMethod(handlerClassName, DEFAULT_SVC_FUNC_NAME, msg);
	}
	
	/**
	 * Post message.
	 *
	 * @param msgName the message name
	 * @param msgContent the message content
	 */
	public synchronized void postMessage(String msgName, String msgContent) {
		if(_msgs.size() > MAX_MSG_COUNT) return;
		AppMessage msg = new AppMessage(msgName, msgContent);
		_msgs.push(msg);
	}
	
	/**
	 * Post message.
	 *
	 * @param msgName the message name
	 * @param msgContent the message content
	 * @param msgParam the parameter
	 */
	public synchronized void postMessage(String msgName, String msgContent, String msgParam) {
		if(_msgs.size() > MAX_MSG_COUNT) return;
		AppMessage msg = new AppMessage(msgName, msgContent, msgParam);
		_msgs.push(msg);
	}
	
	/**
	 * Register message.
	 *
	 * @param msgName the message name
	 * @param handlerClassName the handler's class name
	 * @return the handler count
	 */
	public synchronized int regMessage(String msgName, String handlerClassName) {
		if(_handlerlist.get(msgName) != null) return -1;
		_handlerlist.put(msgName, handlerClassName);
		return _handlerlist.size();
	}
	
	/**
	 * Gets the work state.
	 *
	 * @return the work state
	 */
	public synchronized int getWorkState() {
		return _workState;
	}

	/**
	 * Sets the work state.
	 *
	 * @param workstate the new work state
	 */
	public synchronized void setWorkState(int workstate) {
		this._workState = workstate;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		
		while (getWorkState() >= 0) {
			try {
				handleMessages();
				sleep(50);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
}
