package org.flamering.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import org.flamering.service.HttpServiceSession;
import org.flamering.service.NetworkService;
import org.flamering.service.ServiceExecutor;
import org.flamering.service.ServiceManager;
import org.flamering.service.WebSocketServiceSession;

public class Network extends AbstractVerticle {
	
	public static final String BEAN_NAME = "network-manager";
	
	public static final String PATH_SEPARATOR = "/";
	public static final String PATH_WILDCARD = "*";
	public static final String ENDPOINT_SEPARATOR = ",";
	public static final String INVALID_SESSION_FLAG = "INVALID_SESSION_FLAG";
	
	// should create another class named NetworkSettings to hold the info
	// but here i just mixed them up...
	
	private String _httpRoot = "";
	private int _httpPort = 0;
	private int _httpIdleTimeout = 0;
	private int _httpState = -1;
	
	private String _webSocketRoot = "";
	private int _webSocketPort = 0;
	private int _webSocketIdleTimeout = 0;
	private int _webSocketState = -1;
	
	private boolean _active = true;

	private EventBus _sessionBus = null;
	private List<String> _endpoints = new ArrayList<String>();
	private Map<String, ServerWebSocket> _wsendpoints = new HashMap<>();
	
	private static Network _instance = null;
	private static Vertx _vertx = null;
	
	public String getHttpRoot() {
		return _httpRoot;
	}
	public void setHttpRoot(String httpRoot) {
		_httpRoot = httpRoot;
	}

	public int getHttpPort() {
		return _httpPort;
	}
	public void setHttpPort(int httpPort) {
		_httpPort = httpPort;
	}
	
	public int getHttpIdleTimeout() {
		return _httpIdleTimeout;
	}
	public void setHttpIdleTimeout(int httpIdleTimeout) {
		_httpIdleTimeout = httpIdleTimeout;
	}

	public String getWebSocketRoot() {
		return _webSocketRoot;
	}
	public void setWebSocketRoot(String webSocketRoot) {
		_webSocketRoot = webSocketRoot;
	}

	public int getWebSocketPort() {
		return _webSocketPort;
	}
	public void setWebSocketPort(int webSocketPort) {
		_webSocketPort = webSocketPort;
	}

	public int getWebSocketIdleTimeout() {
		return _webSocketIdleTimeout;
	}
	public void setWebSocketIdleTimeout(int webSocketIdleTimeout) {
		_webSocketIdleTimeout = webSocketIdleTimeout;
	}

	@Override
	public void start(Future<Void> fut) {

		if (_instance._httpPort > 0 || _instance._webSocketPort > 0) {
			_instance._sessionBus = vertx.eventBus();
			synchronized(_instance._endpoints) {
				_instance._endpoints.clear();
				_instance._wsendpoints.clear();
			}
		}
		
		// http
		
		if (_instance._httpPort > 0) {
			
			String httpRootPath = _instance._httpRoot == null 
					? PATH_SEPARATOR : (_instance._httpRoot.startsWith(PATH_SEPARATOR) 
								? _instance._httpRoot : PATH_SEPARATOR + _instance._httpRoot);
			
			String routeRootPath = httpRootPath;
			
			if (routeRootPath.endsWith(PATH_WILDCARD)) 
				routeRootPath = routeRootPath.substring(0, routeRootPath.length() - 1);
			
			if (!routeRootPath.endsWith(PATH_SEPARATOR)) routeRootPath += PATH_SEPARATOR;
			routeRootPath += PATH_WILDCARD;
			
			Router router = Router.router(vertx);
			
			// enable the process of every request's body for all routes
			router.route().handler(BodyHandler.create());

			router.route(routeRootPath).handler(routingContext -> {
				
				if (_instance._httpState > 0 && _instance._active) { 

					String path = routingContext.request().path();
					String content = routingContext.getBodyAsString();
					
					if (!path.endsWith(PATH_SEPARATOR)) path += PATH_SEPARATOR;
					
					String cmd = path.substring(path.indexOf(httpRootPath) + httpRootPath.length()).trim();
					if (cmd.startsWith(PATH_SEPARATOR)) cmd = cmd.substring(1);
					if (cmd.length() > 0 && !cmd.equals(PATH_SEPARATOR)) {
						if (cmd.endsWith(PATH_SEPARATOR)) content = cmd + content;
						else content = cmd + PATH_SEPARATOR + content;
					}
					
					ServiceExecutor.getInstance().runTask(content, 
							new HttpServiceSession(routingContext.request(), routingContext.response()));
					
				}

			});
		
			_instance._httpState = 0;
			
			int httpIdleTimeout = _instance._httpIdleTimeout;
			if (httpIdleTimeout < 0) httpIdleTimeout = 0;
			HttpServerOptions httpOptions = new HttpServerOptions();
			httpOptions.setIdleTimeout(httpIdleTimeout);
	
			vertx.createHttpServer(httpOptions).requestHandler(router::accept).listen(_instance._httpPort, result -> {
				if (result.succeeded()) {
					System.out.println("HTTP service started on " + _instance._httpPort);
					_instance._httpState = 1;
					if (_instance._httpState > 0 && _instance._webSocketState > 0) fut.complete();
				} else {
					System.out.println("Failed to start HTTP service on " + _instance._httpPort);
					_instance._httpState = -1;
					fut.fail(result.cause());
				}
			});
		
		}
		
		
		if (_instance._webSocketPort > 0) {
		
			_instance._webSocketState = 0;
			
			int wsIdleTimeout = _instance._webSocketIdleTimeout;
			if (wsIdleTimeout < 0) wsIdleTimeout = 0;
			HttpServerOptions wsOptions = new HttpServerOptions();
			wsOptions.setIdleTimeout(wsIdleTimeout);
			
			String websocketRootPath = _instance._webSocketRoot == null 
					? PATH_SEPARATOR : (_instance._webSocketRoot.startsWith(PATH_SEPARATOR) 
								? _instance._webSocketRoot : PATH_SEPARATOR +  _instance._webSocketRoot);
			
			String rootPath = (websocketRootPath.endsWith(PATH_SEPARATOR) && websocketRootPath.length() > 1)
					? websocketRootPath.substring(0, websocketRootPath.length() - 1)
							: websocketRootPath;
	
			// websocket
			vertx.createHttpServer(wsOptions).websocketHandler(ws -> {
				
				String path = ws.path();
				
				if (_instance._webSocketState > 0 && _instance._active && path.startsWith(rootPath)) {
					
					String cmd = path.substring(path.indexOf(rootPath) + rootPath.length()).trim();
					if (cmd.startsWith(PATH_SEPARATOR)) cmd = cmd.substring(1);
					
					String svc = cmd;
				
					String location = ws.textHandlerID();
					
					synchronized(_instance._endpoints) {
						if (!_instance._endpoints.contains(location)) _instance._endpoints.add(location);
						if (!_instance._wsendpoints.containsKey(location))
							_instance._wsendpoints.put(location, ws);
					}
					
					ServiceExecutor.getInstance().registerSession(location, new WebSocketServiceSession(ws));
		
					ws.closeHandler(v -> {
						synchronized(_instance._endpoints) {
							_instance._endpoints.remove(location);
							_instance._wsendpoints.remove(location);
						}
						ServiceExecutor.getInstance().unregisterSession(location);
					});
		
					ws.exceptionHandler(ex -> {
						ws.close();
						ServiceExecutor.getInstance().unregisterSession(location);
					});
		
					ws.frameHandler(frame -> {
						
						if (_instance._webSocketState > 0 && _instance._active) {
		
							if (!frame.isText()) {
								ws.close();
								ServiceExecutor.getInstance().unregisterSession(location);
							} else {
								try {
									String content = frame.textData();
									if (content != null && content.length() > 0) {
										if (svc.length() > 0 && !svc.equals(PATH_SEPARATOR)) {
											if (svc.endsWith(PATH_SEPARATOR)) content = svc + content;
											else content = svc + PATH_SEPARATOR + content;
										}
										ServiceExecutor.getInstance().processTask(location, content);
									}
								} catch (Exception e) {
									ws.close();
									ServiceExecutor.getInstance().unregisterSession(location);
								}
							}
							
						}
		
					});
					
				} else {
					ws.reject();
				}
	
			}).listen(_instance._webSocketPort, result -> {
				if (result.succeeded()) {
					System.out.println("WebSocket service started on " + _instance._webSocketPort);
					_instance._webSocketState = 1;
					if (_instance._httpState > 0 && _instance._webSocketState > 0) fut.complete();
				} else {
					System.out.println("Failed to start WebSocket service on " + _instance._webSocketPort);
					_instance._webSocketState = -1;
					fut.fail(result.cause());
				}
			});
		
		}

	}
	
	public static Network getInstance() {
		return _instance;
	}
	
	public static boolean init(String beanName) {
		// would use slf4j logger (see http://vertx.io/docs/apidocs/io/vertx/core/logging/Logger.html )
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
		_instance = (Network) Bean.getBean(beanName);
		if (_instance == null) _instance = new Network();
		return _instance != null;
	}
	
	public static void enable() {
		if (_instance != null) _instance._active = true;
	}
	
	public static void disable() {
		if (_instance != null) _instance._active = false;
	}
	
	public static boolean work() {
		if (_instance != null && _instance._httpState < 0 && _instance._webSocketState < 0)
			return _instance.startNetwork();
		return _instance != null;
	}
	
	public static void shutdown() {
		if (_instance != null) {
			try {
				_instance._httpState = 0;
				_instance._webSocketState = 0;
				_instance.disconnect();
				Thread.sleep(100);
				_instance.stopNetwork();
				_instance._httpState = -1;
				_instance._webSocketState = -1;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static int getHttpListeningPort() {
		return _instance != null && _instance._httpState > 0 ? _instance._httpPort : 0;
	}
	
	public static int getWebSocketListeningPort() {
		return _instance != null && _instance._webSocketState > 0 ? _instance._webSocketPort : 0;
	}
	
	public static int getWebSocketClientCount() {
		return _instance != null && _instance._webSocketState > 0 ? _instance._endpoints.size() : 0;
	}
	
	public static String getWebServerAddress() {
		String addr = "";
		try {
			if (_instance != null && (_instance._httpState > 0 || _instance._webSocketState > 0))
				addr = ServiceManager.call(NetworkService.SERVICE_NAME, 
						NetworkService.FUNC_ADDRESS, "");
		} catch(Exception ex) {
			System.err.println(ex.getMessage());
		}
		return (addr == null || addr.length() <= 0) ? "" : addr;
	}
	
	protected boolean startNetwork() {
		
		boolean isOK = false;
		
		if (_instance != null) {
			
			try {
				
				System.out.println("");
				System.out.println("Starting Vert.x ...");
				_vertx = Vertx.vertx();
				
				_instance._httpState = -1;
				_instance._webSocketState = -1;
				
				_instance._active = true; // reset it to default value
				
				if (_instance._httpPort > 0 || _instance._webSocketPort > 0) {
					
					_vertx.deployVerticle(_instance.getClass().getName());
					
					Thread.sleep(500);
					for (int i=1; i<=50; i++) {
						if (_instance._httpPort > 0 && _instance._httpState != 0) break;
						if (_instance._webSocketPort > 0 && _instance._webSocketState != 0) break;
						Thread.sleep(100);
					}
					
					isOK = (_instance._httpPort <= 0 || _instance._httpState > 0)
								&& (_instance._webSocketPort <= 0 || _instance._webSocketState > 0);
					
				} else isOK = true;
				
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			
		} else {
			System.err.println("Please run Network.init() first.");
		}
		
		if (isOK) {
			if (_instance != null) {
				if (_instance._httpPort > 0 || _instance._webSocketPort > 0) {
					String svrAddr = getWebServerAddress();
					if (svrAddr == null || svrAddr.trim().length() <= 0) svrAddr = "127.0.0.1";
					System.out.println("Web server address is " + svrAddr);
				}
			}
			System.out.println("Vert.x started!");
		}
		
		return isOK;
	}
	
	protected void stopNetwork() {
		if (_vertx != null) _vertx.close();
		if (_instance != null) {
			_instance._httpState = -1;
			_instance._webSocketState = -1;
			_instance._sessionBus = null;
			synchronized(_instance._endpoints) {
				_instance._endpoints.clear();
				_instance._wsendpoints.clear();
			}
		}
		System.out.println("Vert.x stopped!");
	}

	public void send(Object data, String... endpoints) {
		if (_sessionBus != null) {
			List<String> endpointList = new ArrayList<String>();
			synchronized(_endpoints) {
				for (String endpoint : endpoints) {
					if(_endpoints.contains(endpoint))
						endpointList.add(endpoint);
				}
			}
			if (endpointList.size() > 0) {
				String msg = "";
				if (data instanceof String) msg = data.toString();
				else msg = Json.toJsonString(data);
				if (msg != null && msg.length() > 0) {
					for (String endpoint : endpointList) {
						try {
							_sessionBus.send(endpoint, msg);
						} catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public void broadcast(Object data) {
		if (_sessionBus != null) {
			List<String> endpointList = new ArrayList<String>();
			synchronized(_endpoints) {
				endpointList.addAll(_endpoints);
			}
			if (endpointList.size() > 0) {
				String msg = "";
				if (data instanceof String) msg = data.toString();
				else msg = Json.toJsonString(data);
				if (msg != null && msg.length() > 0) {
					for (String endpoint : endpointList) {
						_sessionBus.send(endpoint, msg);
					}
				}
			}
		}
	}
	
	public void disconnect(String... endpoints) { // kick some websocket clients ...
		if (_sessionBus != null && endpoints != null && endpoints.length > 0) {
			List<ServerWebSocket> wsList = new ArrayList<>();
			synchronized(_endpoints) {
				for (String endpoint : endpoints) {
					ServerWebSocket ws = _wsendpoints.get(endpoint);
					if (ws != null) wsList.add(ws);
				}
			}
			for (ServerWebSocket ws : wsList) {
				ws.close();
			}
		}
	}
	
	public void disconnect() { // kick all websocket clients ...
		if (_sessionBus != null) {
			List<ServerWebSocket> wsList = new ArrayList<>();
			synchronized(_endpoints) {
				for (String endpoint : _endpoints) {
					ServerWebSocket ws = _wsendpoints.get(endpoint);
					if (ws != null) wsList.add(ws);
				}
			}
			for (ServerWebSocket ws : wsList) {
				ws.close();
			}
		}
	}
	
	

}
