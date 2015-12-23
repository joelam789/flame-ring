package org.flamering.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import org.flamering.component.Bean;

// TODO: Auto-generated Javadoc
/**
 * The Class ServiceExecutor.
 */
public class ServiceExecutor {
	
	/** The Constant BEAN_NAME. */
	public static final String BEAN_NAME = "service-executor";
	
	/** The state. */
	protected int _state = 0;
	
	/** The minimum thread pool size. */
	protected int _minThreadPoolSize = 128;
	
	/** The maximum thread pool size. */
	protected int _maxThreadPoolSize = 2048;
	
	/** The thread queue size. */
	protected int _maxThreadQueueSize = 8192;
	
	/** The maximum idle time for every thread (in seconds) */
	protected int _maxThreadIdleTime = 180;
	
	/** The queue map. */
	protected Map<String, Queue<String>> _queueMap = new ConcurrentHashMap<String, Queue<String>>();
	
	/** The session map. */
	protected Map<String, ServiceSession> _sessionMap = new ConcurrentHashMap<String, ServiceSession>();
	
	/** The thread pool. */
	protected ThreadPoolExecutor _threadPool = null;
	
	/** The singleton instance. */
	protected static ServiceExecutor _instance = null;
	
	/**
	 * Instantiates a new service executor.
	 */
	public ServiceExecutor() {
		_state = 1;
		_threadPool = new ThreadPoolExecutor(
							_minThreadPoolSize,    // core pool size
							_maxThreadPoolSize,   // max pool size
							_maxThreadIdleTime, TimeUnit.SECONDS, // max idle time
							new ArrayBlockingQueue<Runnable>(_maxThreadQueueSize),
							new ThreadPoolExecutor.AbortPolicy()
							);
	}
	
	/**
	 * Instantiates a new service executor.
	 *
	 * @param minPoolSize the minimum pool size
	 * @param maxPoolSize the maximum pool size
	 * @param queueSize the queue size
	 * @param maxIdleTime the maximum idle time of every thread (in seconds)
	 */
	public ServiceExecutor(int minPoolSize, int maxPoolSize, int queueSize, int maxIdleTime) {
		
		_minThreadPoolSize = minPoolSize;
		_maxThreadPoolSize = maxPoolSize;
		_maxThreadQueueSize = queueSize;
		_maxThreadIdleTime = maxIdleTime;
		
		_state = 1;
		
		_threadPool = new ThreadPoolExecutor(
						_minThreadPoolSize,    // core pool size
						_maxThreadPoolSize,   // max pool size
						_maxThreadIdleTime, TimeUnit.SECONDS, // max idle time
						new ArrayBlockingQueue<Runnable>(_maxThreadQueueSize),
						new ThreadPoolExecutor.AbortPolicy()
						);
	}
	
	/**
	 * Initialize with a bean name so that some properties could be set with Spring config file
	 *
	 * @param beanName the bean name
	 * @return true, if successful
	 */
	public static boolean init(String beanName) {
		_instance = (ServiceExecutor) Bean.getBean(beanName);
		return _instance != null;
	}
	
	/**
	 * Stop the service executor
	 */
	public static void stop() {
		if (_instance != null)  _instance.shutdown();
	}
	
	/**
	 * Gets the single instance of ServiceExecutor.
	 *
	 * @return single instance of ServiceExecutor
	 */
	public static ServiceExecutor getInstance() {
		return _instance;
	}
	
	/**
	 * Shutdown the internal thread pool
	 */
	protected void shutdown() {
		_state = -1;
		if(_threadPool != null) _threadPool.shutdown();
	}
	
	/**
	 * Register a service session by session name.
	 *
	 * @param sessionName the session name
	 * @param session the session
	 */
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
								ServiceManager.callService(NetworkEventService.BEAN_NAME, 
										NetworkEventService.EVENT_OPEN, 
										sessionName, session.getRemoteAddress());
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
	
	/**
	 * Unregister a service session.
	 *
	 * @param sessionName the session name of the service session
	 */
	public void unregisterSession(String sessionName) {
		
		_queueMap.remove(sessionName);
		ServiceSession session = _sessionMap.remove(sessionName);
		
		if (session != null) {
			
			if (_state >= 0) { // try to fire the on-close event
				
				Runnable task = new Runnable() {
					@Override
					public void run() {
						try {
							ServiceManager.callService(NetworkEventService.BEAN_NAME, 
									NetworkEventService.EVENT_CLOSE, 
									sessionName, session.getRemoteAddress());
						} catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				};
				_threadPool.execute(task);
			}
		}
	}
	
	/**
	 * Process task.
	 *
	 * @param sessionName the session name
	 * @param taskContent the task content
	 */
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
	
	/**
	 * Process task of a session.
	 *
	 * @param sessionName the session name
	 */
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
	
	/**
	 * Run a session's task.
	 *
	 * @param taskContent the task content
	 * @param session the session
	 */
	public void runTask(String taskContent, ServiceSession session) {
		if (_state < 0) return;
		if (taskContent != null && session != null) {
			ServiceTask task = new ServiceTask(taskContent, session);
			_threadPool.execute(task);
		}
	}
	
	/**
	 * Gets the executor's state.
	 *
	 * @return the state
	 */
	public int getState() {
		return _state;
	}
	
	/**
	 * Gets the minimum thread pool size.
	 *
	 * @return the minimum thread pool size
	 */
	public int getMinThreadPoolSize() {
		return _minThreadPoolSize;
	}

	/**
	 * Sets the minimum thread pool size.
	 *
	 * @param minThreadPoolSize the new minimum thread pool size
	 */
	public void setMinThreadPoolSize(int minThreadPoolSize) {
		_minThreadPoolSize = minThreadPoolSize;
	}

	/**
	 * Gets the maximum thread pool size.
	 *
	 * @return the maximum thread pool size
	 */
	public int getMaxThreadPoolSize() {
		return _maxThreadPoolSize;
	}

	/**
	 * Sets the maximum thread pool size.
	 *
	 * @param maxThreadPoolSize the new maximum thread pool size
	 */
	public void setMaxThreadPoolSize(int maxThreadPoolSize) {
		_maxThreadPoolSize = maxThreadPoolSize;
	}

	/**
	 * Gets the thread queue size.
	 *
	 * @return the thread queue size
	 */
	public int getMaxThreadQueueSize() {
		return _maxThreadQueueSize;
	}

	/**
	 * Sets the thread queue size.
	 *
	 * @param threadQueueSize the new thread queue size
	 */
	public void setMaxThreadQueueSize(int threadQueueSize) {
		_maxThreadQueueSize = threadQueueSize;
	}

	/**
	 * Gets the thread's maximum idle time (in seconds)
	 *
	 * @return the thread's maximum idle time (in seconds)
	 */
	public int getMaxThreadIdleTime() {
		return _maxThreadIdleTime;
	}

	/**
	 * Sets every thread's maximum idle time (in seconds)
	 *
	 * @param maxIdleTime the new maximum idle time (in seconds)
	 */
	public void setMaxThreadIdleTime(int maxIdleTime) {
		_maxThreadIdleTime = maxIdleTime;
	}

	

}
