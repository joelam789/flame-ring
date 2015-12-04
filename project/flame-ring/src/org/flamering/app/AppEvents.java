package org.flamering.app;

/**
 * Extend this class to write your own event functions of the application
 */
public class AppEvents {
	
	/** The Constant BEAN_NAME. */
	public static final String BEAN_NAME = "app-events";
	
	/**
	 * Occurs when the application finishes initialization
	 */
	public void onInit() {
		// to be overridden
		System.out.println("Initialization Done");
	}
	
	/**
	 * Occurs when the Apache Ignite starts
	 */
	public void onGridOpen() {
		// to be overridden
		System.out.println("Opened Grid Services");
	}
	
	/**
	 * Occurs when the Vert.x starts
	 */
	public void onWebOpen() {
		// to be overridden
		System.out.println("Opened Web Services");
	}
	
	/**
	 * Occurs when the whole application starts
	 */
	public void onReady() {
		// to be overridden
		System.out.println("Application Ready\n");
	}
	
	/**
	 * Occurs when application is going to close
	 */
	public void onClosing() {
		// to be overridden
		System.out.println("Closing All...");
	}
	
	/**
	 * Occurs when Vert.x closes
	 */
	public void onWebClose() {
		// to be overridden
		System.out.println("Closed Web Services");
	}
	
	/**
	 * Occurs when Apache Ignite closes
	 */
	public void onGridClose() {
		// to be overridden
		System.out.println("Closed Grid Services");
	}
	
	/**
	 * Occurs when the whole application closes
	 */
	public void onEnd() {
		// to be overridden
		System.out.println("Application Ended");
	}
	
}
