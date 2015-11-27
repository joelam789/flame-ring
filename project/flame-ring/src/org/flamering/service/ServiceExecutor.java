package org.flamering.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import org.flamering.component.Bean;

public class ServiceExecutor {
	
	public static final String BEAN_NAME = "service-executor";
	
	private int _state = 0;
	
	private int _minThreadPoolSize = 128;
	private int _maxThreadPoolSize = 2048;
	private int _threadQueueSize = 8192;
	private int _threadMaxIdleTime = 180;
	
	private Map<String, Queue<String>> _queueMap = new ConcurrentHashMap<String, Queue<String>>();
	private Map<String, ServiceSession> _sessionMap = new ConcurrentHashMap<String, ServiceSession>();
	
	private ThreadPoolExecutor _threadPool = null;
	
	private static ServiceExecutor _instance = null;
	
	public ServiceExecutor() {
		_state = 1;
		_threadPool = new ThreadPoolExecutor(
							_minThreadPoolSize,    // core pool size
							_maxThreadPoolSize,   // max pool size
							_threadMaxIdleTime, TimeUnit.SECONDS, // max idle time
							new ArrayBlockingQueue<Runnable>(_threadQueueSize),
							new ThreadPoolExecutor.AbortPolicy()
							);
	}
	
	public ServiceExecutor(int minPoolSize, int maxPoolSize, int queueSize, int maxIdleTime) {
		
		_minThreadPoolSize = minPoolSize;
		_maxThreadPoolSize = maxPoolSize;
		_threadQueueSize = queueSize;
		_threadMaxIdleTime = maxIdleTime;
		
		_state = 1;
		
		_threadPool = new ThreadPoolExecutor(
						_minThreadPoolSize,    // core pool size
						_maxThreadPoolSize,   // max pool size
						_threadMaxIdleTime, TimeUnit.SECONDS, // max idle time
						new ArrayBlockingQueue<Runnable>(_threadQueueSize),
						new ThreadPoolExecutor.AbortPolicy()
						);
	}
	
	public static boolean init(String beanName) {
		_instance = (ServiceExecutor) Bean.getBean(beanName);
		return _instance != null;
	}
	
	public static void stop() {
		if (_instance != null)  _instance.shutdown();
	}
	
	public static ServiceExecutor getInstance() {
		return _instance;
	}
	
	protected void shutdown() {
		_state = -1;
		if(_threadPool != null) _threadPool.shutdown();
	}
	
	public void registerSession(String sessionName, ServiceSession session) {
		
		_queueMap.remove(sessionName);
		_sessionMap.remove(sessionName);
		_queueMap.put(sessionName, new ArrayDeque<String>());
		_sessionMap.put(sessionName, session);
		
		if (_state >= 0) { // try to fire the on-open event
			
			Queue<String> queue = _queueMap.get(sessionName);
			if (session != null && queue != null) {
				boolean busy = false;
				synchronized(queue) {
					busy = queue.size() > 0;
					queue.add(sessionName);
				}
				if (!busy) {
					Runnable task = new Runnable() {
						@Override
						public void run() {
							try {
								ServiceManager.call(NetworkEventService.SERVICE_NAME, 
										NetworkEventService.EVENT_OPEN, 
										sessionName, session.getRemoteAddress(), false);
							} catch(Exception ex) {
								ex.printStackTrace();
							} finally {
								ServiceExecutor.getInstance().processTask(sessionName);
							}
						}
					};
					_threadPool.execute(task);
				}
			}
		}
		
	}
	
	public void unregisterSession(String sessionName) {
		
		_queueMap.remove(sessionName);
		ServiceSession session = _sessionMap.remove(sessionName);
		
		if (session != null) {
			
			if (_state >= 0) { // try to fire the on-close event
				
				Runnable task = new Runnable() {
					@Override
					public void run() {
						try {
							ServiceManager.call(NetworkEventService.SERVICE_NAME, 
									NetworkEventService.EVENT_CLOSE, 
									sessionName, session.getRemoteAddress(), false);
						} catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				};
				_threadPool.execute(task);
			}
		}
	}
	
	public void processTask(String sessionName, String taskContent) {
		if (_state < 0) return;
		ServiceSession session = _sessionMap.get(sessionName);
		Queue<String> queue = _queueMap.get(sessionName);
		if (session != null && queue != null) {
			boolean busy = false;
			synchronized(queue) {
				busy = queue.size() > 0;
				queue.add(taskContent);
			}
			if (!busy) {
				ServiceTask task = new ServiceTask(taskContent, session, sessionName);
				_threadPool.execute(task);
			}
		}
	}
	
	public void processTask(String sessionName) {
		if (_state < 0) return;
		String taskContent = null;
		ServiceSession session = _sessionMap.get(sessionName);
		Queue<String> queue = _queueMap.get(sessionName);
		if (session != null && queue != null) {
			synchronized(queue) {
				queue.poll();
				taskContent = queue.peek();
			}
			if (taskContent != null) {
				ServiceTask task = new ServiceTask(taskContent, session, sessionName);
				_threadPool.execute(task);
			}
		}
	}
	
	public void runTask(String taskContent, ServiceSession session) {
		if (_state < 0) return;
		if (taskContent != null && session != null) {
			ServiceTask task = new ServiceTask(taskContent, session);
			_threadPool.execute(task);
		}
	}
	
	public int getState() {
		return _state;
	}
	
	public int getMinThreadPoolSize() {
		return _minThreadPoolSize;
	}

	public void setMinThreadPoolSize(int minThreadPoolSize) {
		_minThreadPoolSize = minThreadPoolSize;
	}

	public int getMaxThreadPoolSize() {
		return _maxThreadPoolSize;
	}

	public void setMaxThreadPoolSize(int maxThreadPoolSize) {
		_maxThreadPoolSize = maxThreadPoolSize;
	}

	public int getThreadQueueSize() {
		return _threadQueueSize;
	}

	public void setThreadQueueSize(int threadQueueSize) {
		_threadQueueSize = threadQueueSize;
	}

	public int getThreadMaxIdleTime() {
		return _threadMaxIdleTime;
	}

	public void setThreadMaxIdleTime(int threadMaxIdleTime) {
		_threadMaxIdleTime = threadMaxIdleTime;
	}

	

}
