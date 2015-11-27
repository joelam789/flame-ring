package org.flamering.service;

import java.lang.reflect.Method;

public interface Service {
	
	Method getFunction(String key, boolean regOnly);

}
