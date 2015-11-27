package org.flamering.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.flamering.component.Bean;

public class AppMessageDispatcher extends Thread {
	
	public static final int MAX_MSG_COUNT = 8192;
	
	public static final String DEFAULT_SVC_FUNC_NAME = "handleMessage";
	
	private int _workState = 0;
	
	private Stack<AppMessage> _msgs = new Stack<AppMessage>();
	private Map<String, String> _handlerlist = new HashMap<String, String>();
	
	public synchronized void handleMessages() {
		if(_msgs.size() == 0) return;
		AppMessage msg = _msgs.pop();
		if(msg == null) return;
		String msgName = msg.getName();
		String handlerClassName = _handlerlist.get(msgName);
		if(handlerClassName == null) return;
		Bean.callClassMethod(handlerClassName, DEFAULT_SVC_FUNC_NAME, msg);
	}
	
	public synchronized void postMessage(String msgName, String msgContent) {
		if(_msgs.size() > MAX_MSG_COUNT) return;
		AppMessage msg = new AppMessage(msgName, msgContent);
		_msgs.push(msg);
	}
	
	public synchronized void postMessage(String msgName, String msgContent, String msgParam) {
		if(_msgs.size() > MAX_MSG_COUNT) return;
		AppMessage msg = new AppMessage(msgName, msgContent, msgParam);
		_msgs.push(msg);
	}
	
	public synchronized int regMessage(String msgName, String handlerClassName) {
		if(_handlerlist.get(msgName) != null) return -1;
		_handlerlist.put(msgName, handlerClassName);
		return _handlerlist.size();
	}
	
	public synchronized int getWorkState() {
		return _workState;
	}

	public synchronized void setWorkState(int workstate) {
		this._workState = workstate;
	}
	
	@Override
	public void run() {
		
		while (getWorkState() >= 0) {
			handleMessages();
			try {
				sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
