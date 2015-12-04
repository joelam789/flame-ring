package org.flamering.app;

// TODO: Auto-generated Javadoc
/**
 * The Class AppMessageManager.
 */
public class AppMessageManager {

	/** The dispatcher. */
	private static AppMessageDispatcher _dispatcher = new AppMessageDispatcher();
	
	/**
	 * Post message.
	 *
	 * @param msgName the message name
	 * @param msgContent the message content
	 */
	public static void postMessage(String msgName, String msgContent) {
		if(_dispatcher != null) _dispatcher.postMessage(msgName, msgContent);
	}
	
	/**
	 * Post message.
	 *
	 * @param msgName the message name
	 * @param msgContent the message content
	 * @param msgParam the parameter
	 */
	public static void postMessage(String msgName, String msgContent, String msgParam) {
		if(_dispatcher != null) _dispatcher.postMessage(msgName, msgContent, msgParam);
	}
	
	/**
	 * Register message.
	 *
	 * @param msgName the message name
	 * @param handlerClassName the handler's class name
	 * @return the result code
	 */
	public static int regMessage(String msgName, String handlerClassName) {
		if(_dispatcher != null) return _dispatcher.regMessage(msgName, handlerClassName);
		return 0;
	}
	
	/**
	 * Start to process application messages.
	 */
	public static void start() {
		if(_dispatcher != null) {
			_dispatcher.start();
			System.out.println("");
			System.out.println("Enabled application message processing!\n");
		}
	}
	
	/**
	 * Stop processing application messages.
	 */
	public static void shutdown() {
		try {
			if (_dispatcher != null) {
				_dispatcher.setWorkState(-1);
				_dispatcher.join();
			}
			System.out.println("");
			System.out.println("Disabled application message processing!\n");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
