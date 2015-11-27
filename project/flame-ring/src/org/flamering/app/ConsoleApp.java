package org.flamering.app;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.flamering.component.Bean;
import org.flamering.component.Grid;
import org.flamering.component.Network;
import org.flamering.service.ServiceExecutor;
import org.flamering.service.ServiceManager;
import org.flamering.service.ServiceSettings;

public class ConsoleApp implements AppMessageHandler {
	
	public static final int SYS_INPUT_BUF_SIZE = 2048;
	
	private BufferedReader _inputter = null;
	
	private int _state = -1;
	private String _mode = "APP";
	
	private String _appname = "SampleApp";
	private String _apptitle = "Sample Application";
	private String _appversion = "0.0.1";
	private String _appinfo = "";
	
	private String _configFilePath = System.getProperty("user.dir") + "/config.xml";
	private String _gridConfigFilePath = System.getProperty("user.dir") + "/grid.xml";
	
	private Map<String, String> _cmds = new HashMap<String, String>();
	
	private static Logger _log = LoggerFactory.getLogger(ConsoleApp.class);;
	
	private static AppSettings _appsettings = null;
	
	private static AppEvents _appevents = null;
	
	
	public Map<String, String> getCmds() {
		return _cmds;
	}
	
	public void setCmds(Map<String, String> cmdmap) {
		_cmds = cmdmap;
	}
	
	public String getMode() {
		return _mode;
	}
	
	public int getState() {
		return _state;
	}

	public String getAppName() {
		return _appname;
	}

	public String getAppTitle() {
		return _apptitle;
	}

	public String getAppVersion() {
		return _appversion;
	}

	public String getAppInfo() {
		return _appinfo;
	}

	public String getConfigFilePath() {
		return _configFilePath;
	}

	public static AppSettings getAppSettings() {
		return _appsettings;
	}
	
	public static void log(String msg) {
		if(_log != null) _log.warn(msg);
		else System.err.println(msg);
	}
	
	public static Logger getLogger() {
		return _log;
	}
	
	public int init(String configFile) {
		
		// change default log to a simple mode (just for the default logger JUL)
		
		try {
			String simpleLogSettings = "java.util.logging.ConsoleHandler.level=WARNING\n";
			simpleLogSettings += "java.util.logging.SimpleFormatter.format=[%4$s][%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS] %5$s %6$s%n";
			try (InputStream stream = new ByteArrayInputStream(simpleLogSettings.getBytes(StandardCharsets.UTF_8))) {
				LogManager.getLogManager().readConfiguration(stream);
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// check config file
		
		if (configFile == null || configFile.trim().length() == 0)
			configFile = _configFilePath;
		
		File file = new File(configFile);
		if (!file.exists() || file.isDirectory()) {
			System.err.println("Valid config file not found: " + configFile);
			return _state;
		}
		
		// start to init ...
		
		System.out.println("Initializing (with config file " + file.getAbsolutePath() + ") ...");
		
		if (_inputter == null) _inputter = new BufferedReader(new InputStreamReader(System.in));
		
		if (Bean.init("file:" + configFile)) {
			
			_configFilePath = configFile;
			
			_appsettings = (AppSettings) Bean.getBean(AppSettings.BEAN_NAME);
			
			boolean initOK = _appsettings != null;
			
			if (initOK) {
				String gridConfigFile = _appsettings.getGridConfigFile();
				if (gridConfigFile != null && gridConfigFile.length() > 0)
					gridConfigFile = gridConfigFile.trim().replace('\\', '/');
				if (gridConfigFile != null && gridConfigFile.length() > 0 && gridConfigFile.indexOf('/') < 0)
					gridConfigFile = System.getProperty("user.dir") + "/" + gridConfigFile;
				if (gridConfigFile != null && gridConfigFile.length() > 0)
					_gridConfigFilePath = gridConfigFile.trim().replace('\\', '/');
			}
			
			if (initOK) initOK = ServiceManager.init(ServiceSettings.BEAN_NAME);
			if (initOK) initOK = ServiceExecutor.init(ServiceExecutor.BEAN_NAME);
			
			if (initOK) initOK = Network.init(Network.BEAN_NAME);
			
			if (initOK) _state = 0;
			
		} else {
			return _state;
		}
		
		if (_state >= 0) {
			
			_appname = getClass().getName();
			int pos = _appname.lastIndexOf ('.') + 1; 
			if (pos > 0) _appname = _appname.substring(pos);
			
			_apptitle = _appname;
			
			if(_appsettings != null) {
				
				String appName = _appsettings.getParams().get("appName");
				if(appName != null && appName.length() > 0) _appname = appName;
				
				String appTitle = _appsettings.getParams().get("appTitle");
				if(appTitle != null && appTitle.length() > 0) _apptitle = appTitle;
				
				String appVersion = _appsettings.getParams().get("appVersion");
				if(appVersion != null && appVersion.length() > 0) _appversion = appVersion;
				
				String appInfo = _appsettings.getParams().get("appInfo");
				if(appInfo != null && appInfo.length() > 0) _appinfo = appInfo;
				
				_cmds = _appsettings.getCmds();
				regMsgsWithCmds();
			}
			
			_appevents = (AppEvents) Bean.getBean(AppEvents.BEAN_NAME);
			if (_appevents != null) _appevents.onInit();
			
		}
		
		return _state;
	}
	
	public int init() {
		return this.init("");
	}
	
	public int init(String[] args) {
		String configFile = "";
		if (args.length > 0) {
            File file = new File(args[0]);
            if (file.exists() && !file.isDirectory())
            	configFile = file.getAbsolutePath();
        }
		return this.init(configFile);
	}
	
	public boolean startComponents() {
		boolean isOK = false;
		if (_state >= 0) {
			isOK = Grid.start(_gridConfigFilePath);
			if (isOK) {
				if (_appevents != null) _appevents.onGridOpen();
				isOK = Network.work();
				if (isOK && _appevents != null) _appevents.onWebOpen();
			}
		}
		return isOK;
	}
	
	public void stopComponents() {
		
		Network.disable();
		if (_appevents != null) _appevents.onWebClose();
		
		try {
			Thread.sleep(200);
			ServiceExecutor.stop();
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Grid.stop();
		if (_appevents != null) _appevents.onGridClose();
		
		Network.shutdown();
	}
	
	public String quit() {
		_state = -1;
		return "";
	}
	
	public int run() {
		
		System.out.println("");
		System.out.println("**************************************************************");
		System.out.println("Application: " + _apptitle);
		System.out.println("VersionInfo: " + _appversion);
		System.out.println("Description: " + _appinfo);
		System.out.println("**************************************************************");
		System.out.println("");
		
		log("Starting application ... \n");
		
		if (!startComponents()) {
			
			System.err.println("Failed to start components!");
			System.err.println("Stopping all and going to exit ...");
			
			if (_appevents != null) _appevents.onClosing();
			
			try {
				stopComponents();
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.err.println("--- Application stopped ---");
			if (_appevents != null) _appevents.onEnd();
			
			return _state;
		}
		
		AppMessageManager.start();
		
		if (_appevents != null) _appevents.onReady();

		try {
			
			log("--- Ready ---");
			
			Thread.sleep(100);
			
			printTips();
			
			_state = 1;
		
			Selector selector = Selector.open();
			ConsoleInputPipe stdinPipe = new ConsoleInputPipe();
			SelectableChannel stdin = stdinPipe.getStdinChannel();
			ByteBuffer buffer = ByteBuffer.allocate(SYS_INPUT_BUF_SIZE * 2);
			
			stdin.register(selector, SelectionKey.OP_READ);
			stdinPipe.start();
			
			while(_state >= 0) {
				
				printInputPrefix();
				
				String inputCmd = "";
	
				while (inputCmd.length() == 0 && _state >= 0) {
	
					selector.select(SYS_INPUT_BUF_SIZE);
	
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
	
					if (!it.hasNext()) continue;
	
					SelectionKey key = it.next();
	
					it.remove();
					buffer.clear();
	
					ReadableByteChannel channel = (ReadableByteChannel) key.channel();
					int count = channel.read(buffer);
	
					if (count < 0) {
						channel.close();
						break;
					}
	
					buffer.flip();
	
					while (buffer.hasRemaining()) {
						inputCmd = inputCmd + (char) buffer.get();
					}
	
					if (inputCmd.length() > 0) inputCmd = inputCmd.trim();
				}
				
				if (inputCmd.length() > 0) {
					
					String result = execCmd(inputCmd);
					if(result != null && result.length() > 0) {
						printOutputPrefix();
						String[] lines = result.split("\n");
						for(String line : lines) {
							System.out.println(line);
						}
					}
					
				}
				
			}
			
			if (stdinPipe != null) stdinPipe.shutdown();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		log("Stopping application ... \n");
		
		if (_appevents != null) _appevents.onClosing();
		
		stopComponents();
		
		AppMessageManager.shutdown();
		
		log("--- Application stopped --- \n");
		if (_appevents != null) _appevents.onEnd();
		
		return _state;
	}
	
	protected String acceptInput() {
		
		printInputPrefix();
		
		try {
			while(_state >= 0 && !_inputter.ready()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					_state = -1;
					e.printStackTrace();
				}
			}
		} catch (IOException ex) {
			_state = -1;
			ex.printStackTrace();
		}
		
		if(_state < 0) return "";
		
		String str = "";
        try {
			str = _inputter.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return str;
	}
	
	protected void regMsgsWithCmds() {
		
		ArrayList<String> cmdList = new ArrayList<String>();
		
		Set<String> cmdSet = _cmds.keySet();
		for(String cmd : cmdSet) cmdList.add(cmd);
		
		Collections.sort(cmdList);
		
		for(String cmd : cmdList) {
			AppMessageManager.regMessage(cmd.trim(), getClass().getName());
		}
	}
	
	protected String execCmd(String cmdLine) {
		cmdLine = cmdLine.trim();
		if(cmdLine.length() == 0) return "";
		String cmd = cmdLine;
		String param = "";
		int pos = cmdLine.indexOf(' ');
		if (pos > 0) {
			cmd = cmdLine.substring(0, pos);
			param = cmdLine.substring(pos+1);
		}
		cmd = cmd.trim();
		param = param.trim();
		Object rsl = null;
		String cmdInfo = _cmds.get(cmd);
		if(cmdInfo != null) {
			if(param != null && param.length() > 0) {
				rsl = Bean.callMethod(this, cmd, param);
			} else {
				rsl = Bean.callMethod(this, cmd, null);
			}
		}
		if(rsl != null) return rsl.toString();
		else return "";
	}
	
	protected String cmdsToString() {
		
		String rsl = "";
		
		ArrayList<String> cmdList = new ArrayList<String>();
		
		Set<String> cmdSet = _cmds.keySet();
		int maxLen = 0;
		for(String cmd : cmdSet) {
			cmdList.add(cmd);
			if(cmd.length() > maxLen) maxLen = cmd.length();
		}
		
		Collections.sort(cmdList);
		
		for(String cmd : cmdList) {
			String cmdInfo = _cmds.get(cmd);
			if(cmdInfo == null) cmdInfo = "";
			if(cmdInfo != null) {
				String output = String.format("    %" + maxLen + "s - %s", cmd, cmdInfo);
				rsl = rsl + output + "\n";
			}
		}
		
		return rsl;
	}	
	
	@Override
	public void handleMessage(AppMessage msg) {
		if(msg == null) return;
		String name = msg.getName();
		String content = msg.getContent();
		String result = execCmd(name + " " + content);
		if(result != null && result.length() > 0) {
			printOutputPrefix();
			String[] lines = result.split("\n");
			for(String line : lines) {
				System.out.println(line);
			}
		}
	}
	
	public void printTips() {
		System.out.println("");
		System.out.println("Tips: type [help] to list all commands");
		System.out.println("");
	}
	
	public void printInputPrefix() {
		System.out.print("ring>");
	}
	
	public void printOutputPrefix() {
		//System.out.println(">> ");
	}
	
	public void printWrongCmd() {
		System.out.println(getWrongCmdTips());
	}
	
	public String getWrongCmdTips() {
		return "Wrong command! Please type [help] to list all commands";
	}
	
	public String state() {
		
		String rsl = "";
		
		int httpPort = Network.getHttpListeningPort();
		int wsPort = Network.getWebSocketListeningPort();
		
		if (httpPort > 0) rsl += "HTTP service is working on port " + httpPort + "\n";
		else rsl += "No HTTP service available" + "\n";
		
		if (wsPort > 0) rsl += "WebSocket service is working on port " + wsPort + "\n";
		else rsl += "No WebSocket service available" + "\n";
		
		if (httpPort > 0 || wsPort > 0) {
			rsl += "Web server address is " + Network.getWebServerAddress() + "\n";
		}
		
		if (wsPort > 0)
			rsl += "Number of the clients on WebSocket: " + Network.getWebSocketClientCount() + "\n";
		
		rsl += "----------------------------------------------------------------------------\n";
		
		rsl += "Local Node Type: " + Grid.getLocalType() + "\n";
		rsl += "Local Node Name: " + Grid.getLocalName() + "\n";
		rsl += "Local Node Addresses: " + Grid.getLocalAddresses() + "\n";
		rsl += "Local Node Communication Port: " + Grid.getLocalCommunicationPortInfo() + "\n";
		rsl += "Local Node Discovery Port: " + Grid.getLocalDiscoveryPortInfo() + "\n";
		
		List<String> nodes = Grid.getGridAddressList();
		rsl += "All Nodes Found: " + nodes.size() + "\n";
		for (String node : nodes) rsl += node + "\n";
		
		rsl += "\n";
		
		return rsl;
		
	}
	
	public String help() {
		String rsl = "";
		rsl += "Here are all the commands you can use: \n";
		rsl += cmdsToString();
		return rsl;
	}
	
}
