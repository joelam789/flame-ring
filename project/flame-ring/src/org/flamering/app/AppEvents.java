package org.flamering.app;

public class AppEvents {
	
	public static final String BEAN_NAME = "app-events";
	
	public void onInit() {
		// to be overridden
		System.out.println("Initialization Done");
	}
	
	public void onGridOpen() {
		// to be overridden
		System.out.println("Opened Grid Services");
	}
	
	public void onWebOpen() {
		// to be overridden
		System.out.println("Opened Web Services");
	}
	
	public void onReady() {
		// to be overridden
		System.out.println("Application Ready\n");
	}
	
	public void onClosing() {
		// to be overridden
		System.out.println("Closing All...");
	}
	
	public void onWebClose() {
		// to be overridden
		System.out.println("Closed Web Services");
	}
	
	public void onGridClose() {
		// to be overridden
		System.out.println("Closed Grid Services");
	}
	
	public void onEnd() {
		// to be overridden
		System.out.println("Application Ended");
	}
	
}
