package org.flamering.app;

public class AppMessageManager {

	private static AppMessageDispatcher _dispatcher = new AppMessageDispatcher();
	
	public static void postMessage(String msgName, String msgContent) {
		if(_dispatcher != null) _dispatcher.postMessage(msgName, msgContent);
	}
	
	public static void postMessage(String msgName, String msgContent, String msgParam) {
		if(_dispatcher != null) _dispatcher.postMessage(msgName, msgContent, msgParam);
	}
	
	public static int regMessage(String msgName, String handlerClassName) {
		if(_dispatcher != null) return _dispatcher.regMessage(msgName, handlerClassName);
		return 0;
	}
	
	public static void start() {
		if(_dispatcher != null) {
			_dispatcher.start();
			System.out.println("");
			System.out.println("Enabled application message processing!\n");
		}
	}
	
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
