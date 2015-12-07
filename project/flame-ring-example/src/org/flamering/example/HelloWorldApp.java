package org.flamering.example;

import org.flamering.app.ConsoleApp;

public class HelloWorldApp extends ConsoleApp {
	
	public static void main(String[] args) {
		
		HelloWorldApp app = new HelloWorldApp();
		if (app.init(args) >= 0) app.run();
		
		System.exit(0);
	}

}
