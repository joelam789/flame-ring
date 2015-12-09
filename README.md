# Flame Ring

Flame Ring is a distributed RESTful API server framework based on Apache Ignite and Vert.x

- It is a very lightweight framework and developers can access Apache Ignite directly.

- Provide both HTTP and WebSocket bindings for all RESTful APIs.

- No annotations supported in RESTful API development, all settings will be stored in Spring XML configuration files.


# A Minimal Example

The class that contains the main() method

```javascript
public class HelloWorldApp extends ConsoleApp {
	public static void main(String[] args) {
		HelloWorldApp app = new HelloWorldApp();
		if (app.init(args) >= 0) app.run();
		System.exit(0);
	}
}
```
The class that contains the method to implement the RESTful API

```javascript
public class HelloWorldService extends BaseService {
	public String hello(String input) {
		return "Hello, " + input + "!";
	}
}
```

The main configuration file which contains the settings of the RESTful APIs

```xml
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/util
        http://www.springframework.org/schema/util/spring-util.xsd">
        
    <!-- general application settings -->
	<bean id="app-settings" class="org.flamering.app.AppSettings">
		<!-- the commands provided by the console application -->
		<property name="cmds">
			<map>
				<entry key="help" value="List all commands" />
				<entry key="quit" value="Exit program" />
				<entry key="state" value="Show server state" />
			</map>
		</property>
		
		<!-- the basic info of the console application -->
		<property name="params">
			<map>
				<entry key="appName" value="Hello Server" />
				<entry key="appTitle" value="Hello Server Console Application" />
				<entry key="appVersion" value="0.0.1" />
				<entry key="appInfo" value="A Minimal Console Example Of Flame Ring Framework" />
			</map>
		</property>
		
		<!-- the file path of the Apache Ignite configuration file -->
		<property name="gridConfigFile" value="grid.xml" />
	</bean>
	
	<!-- basic application event handler -->
	<bean id="app-events" class="org.flamering.app.AppEvents"/>
	
	<!-- thread pool settings -->
	<bean id="service-executor" class="org.flamering.service.ServiceExecutor">
		<property name="minThreadPoolSize" value="128" />
		<property name="maxThreadPoolSize" value="2048" />
		<property name="threadQueueSize" value="8192" />
		<!-- the idle timeout for every thread in the pool (in seconds) -->
		<property name="threadMaxIdleTime" value="180" />
    </bean>
    
    <!-- http and websocket settings (for vert.x) -->
    <bean id="network-manager" class="org.flamering.component.Network">
    	<!-- the root directory of HTTP server -->
		<property name="httpRoot" value="" />
		<!-- the listening port of HTTP server, might set it to "0" if do not need HTTP -->
		<property name="httpPort" value="10080" />
		<!-- the idle timeout of the connections on HTTP server (in seconds) -->
		<property name="httpIdleTimeout" value="300" />
		
		<!-- the root directory of WebSocket server -->
		<property name="webSocketRoot" value="" />
		<!-- the listening port of WebSocket server, might set it to "0" if do not need WebSocket -->
		<property name="webSocketPort" value="0" />
		<!-- the idle timeout of the connections on WebSocket server (in seconds) -->
		<property name="webSocketIdleTimeout" value="300" />
    </bean>
    
    <!-- basic networking service -->
    <bean id="network-service" class="org.flamering.service.NetworkService">
    	<!-- this is the external IP address for clients to connect -->
    	<property name="address" value="127.0.0.1"/>
    </bean>
    
    <!-- basic network event handler (work for WebSocket only) -->
    <bean id="network-event-service" class="org.flamering.service.NetworkEventService"/>
    
    
    <!-- The RESTful services exposed to clients directly -->
    <bean id="service-settings" class="org.flamering.service.ServiceSettings">
		<property name="serviceMap">
			<map>
				<!-- the "key" is the name of the service provided for clients to call -->
				<!-- the "value" is the name of the service bean -->
				<entry key="hello" value="hello-service" />
			</map>
		</property>
    </bean>
    
    <!-- The service bean -->
    <bean id="hello-service" class="org.flamering.example.HelloWorldService">
		<property name="functionMap">
			<map>
				<!-- the "key" is the name of the function provided for clients to call -->
				<!-- the "value" is the name of the real method of the service bean -->
				<entry key="say-hello" value="hello" />
			</map>
		</property>
    </bean>
    
    <!-- the request URL from the clients might be one of the following: -->
    <!-- http://127.0.0.1:10080/hello/say-hello/world -->
    <!-- http://127.0.0.1:10080/hello/say-hello/boy -->
    <!-- http://127.0.0.1:10080/hello/say-hello/girl -->
    
    <!-- "hello" is the service name provided for clients to call -->
    <!-- "say-hello" is the function name provided for clients to call -->
    <!-- "world/boy/girl" is the input content provided by the clients -->
    
    <!-- you may open the URL with your browser to see what will be returned -->
    <!-- you may modify   HelloWorldService.hello()  to change the output content -->
    
</beans>
```

The configuration file of Apache Ignite

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/util
        http://www.springframework.org/schema/util/spring-util.xsd">
    <!-- Ignite settings -->
    <bean id="ignite.cfg" class="org.apache.ignite.configuration.IgniteConfiguration">
        <!-- These are very important settings for flame-ring framework -->
        <property name="userAttributes">
			<map>
				<!-- The TYPE of this node -->
				<entry key="ring-type" value="hello-server"/>
				<!-- The NAME of this node -->
				<entry key="ring-name" value="hello-server#1"/>
			</map>
		</property>
		<!-- flame-ring framework would use slf4j as its logger interface -->
		<property name="gridLogger">
			<bean class="org.apache.ignite.logger.slf4j.Slf4jLogger"/>
		</property>
    </bean> 
</beans>
```

You can find the source code of the minimal example here:

https://github.com/joelam789/flame-ring/tree/master/project/flame-ring-example


# A Chatroom Example

You can find a more complicated chatroom example here:

https://github.com/joelam789/flame-ring-sample

