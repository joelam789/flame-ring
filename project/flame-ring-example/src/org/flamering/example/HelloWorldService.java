package org.flamering.example;

import org.flamering.service.BaseService;

public class HelloWorldService extends BaseService {
	
	public String hello(String input) {
		return "Hello, " + input + "!";
	}
	
}
