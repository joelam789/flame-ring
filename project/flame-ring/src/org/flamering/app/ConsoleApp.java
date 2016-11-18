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

// TODO: Auto-generated Javadoc
/**
 * The default console application class.
 */
public class ConsoleApp implements AppMessageHandler {
	
	/** The Constant SYS_INPUT_BUF_SIZE. */
	public static final int SYS_INPUT_BUF_SIZE = 2048;
	
	/** The inputter. */
	protected BufferedReader _inputter = null;
	
	/** The state. */
	protected int _state = -1;
	
	/** The app name. */
	protected String _name = "SampleApp";
	
	/** The app title. */
	protected String _title = "Sample Application";
	
	/** The app version. */
	protected String _version = "0.0.1";
	
	/** The app description. */
	protected String _description = "";
	
	/** The config file path. */
	protected String _configFilePath = System.getProperty("user.dir") + "/config.xml";
	
	/** The grid's config file path. */
	protected String _gridConfigFilePath = System.getProperty("user.dir") + "/grid.xml";
	
	/** The cmds. */
	protected Map<String, String> _cmds = new HashMap<String, String>();
	
	/** The logger. */
	protected static Logger _log = LoggerFactory.getLogger(ConsoleApp.class);;
	
	/** The app settings. */
	protected static AppSettings _settings = null;
	
	/** The app events. */
	protected static AppEvents _events = null;
	
	
	/**
	 * Gets the cmds.
	 *
	 * @return the cmds
	 */
	public Map<String, String> getCmds() {
		return _cmds;
	}
	
	/**
	 * Sets the cmds.
	 *
	 * @param cmdmap the cmdmap
	 */
	public void setCmds(Map<String, String> cmdmap) {
		_cmds = cmdmap;
	}
	
	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public int getState() {
		return _state;
	}

	/**
	 * Gets the app name.
	 *
	 * @return the app name
	 */
	public String getAppName() {
		return _name;
	}

	/**
	 * Gets the app title.
	 *
	 * @return the app title
	 */
	public String getAppTitle() {
		return _title;
	}

	/**
	 * Gets the app version.
	 *
	 * @return the app version
	 */
	public String getAppVersion() {
		return _version;
	}

	/**
	 * Gets the app description.
	 *
	 * @return the app description
	 */
	public String getAppDescription() {
		return _description;
	}

	/**
	 * Gets the config file path.
	 *
	 * @return the config file path
	 */
	public String getConfigFilePath() {
		return _configFilePath;
	}

	/**
	 * Gets the app settings.
	 *
	 * @return the app settings
	 */
	public static AppSettings getAppSettings() {
		return _settings;
	}
	
	/**
	 * Write message to log.
	 *
	 * @param msg the message
	 */
	public static void log(String msg) {
		if(_log != null) _log.warn(msg);
		else System.err.println(msg);
	}
	
	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public static Logger getLogger() {
		return _log;
	}
	
	/**
	 * Initialize the application with the config file.
	 *
	 * @param configFile the config file
	 * @return the application state
	 */
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
			
			_settings = (AppSettings) Bean.getBean(AppSettings.BEAN_NAME);
			
			boolean initOK = _settings != null;
			
			if (initOK) {
				String gridConfigFile = _settings.getGridConfigFile();
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
			
			_name = getClass().getName();
			int pos = _name.lastIndexOf ('.') + 1; 
			if (pos > 0) _name = _name.substring(pos);
			
			_title = _name;
			
			if(_settings != null) {
				
				String appName = _settings.getParams().get("appName");
				if(appName != null && appName.length() > 0) _name = appName;
				
				String appTitle = _settings.getParams().get("appTitle");
				if(appTitle != null && appTitle.length() > 0) _title = appTitle;
				
				String appVersion = _settings.getParams().get("appVersion");
				if(appVersion != null && appVersion.length() > 0) _version = appVersion;
				
				String appInfo = _settings.getParams().get("appInfo");
				if(appInfo != null && appInfo.length() > 0) _description = appInfo;
				
				_cmds = _settings.getCmds();
				regCmds();
			}
			
			_events = (AppEvents) Bean.getBean(AppEvents.BEAN_NAME);
			if (_events != null) _events.onInit();
			
		}
		
		return _state;
	}
	
	/**
	 * Initialize the application with the default config file.
	 *
	 * @return the application state
	 */
	public int init() {
		return this.init("");
	}
	
	/**
	 * Initialize the application with the arguments passed by function main().
	 *
	 * @param args the args
	 * @return the application state
	 */
	public int init(String[] args) {
		String configFile = "";
		if (args.length > 0) {
            File file = new File(args[0]);
            if (file.exists() && !file.isDirectory())
            	configFile = file.getAbsolutePath();
        }
		return this.init(configFile);
	}
	
	/**
	 * Start components.
	 *
	 * @return true, if successful
	 */
	public boolean startComponents() {
		boolean isOK = false;
		if (_state >= 0) {
			isOK = Grid.start(_gridConfigFilePath);
			if (isOK) {
				if (_events != null) _events.onGridOpen();
				isOK = Network.work();
				if (isOK && _events != null) _events.onWebOpen();
			}
		}
		return isOK;
	}
	
	/**
	 * Stop components.
	 */
	public void stopComponents() {
		
		Network.disable();
		if (_events != null) _events.onWebClose();
		
		try {
			Thread.sleep(200);
			ServiceExecutor.stop();
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Grid.stop();
		if (_events != null) _events.onGridClose();
		
		Network.shutdown();
	}
	
	/**
	 * Quit application.
	 *
	 * @return the action message
	 */
	public String quit() {
		_state = -1;
		return "";
	}
	
	/**
	 * Run application.
	 *
	 * @return the application state
	 */
	public int run() {
		
		printAppInfo();
		
		log("Starting application ... \n");
		
		if (!startComponents()) {
			
			System.err.println("Failed to start components!");
			System.err.println("Stopping all and going to exit ...");
			
			if (_events != null) _events.onClosing();
			
			try {
				stopComponents();
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.err.println("--- Application stopped ---");
			if (_events != null) _events.onEnd();
			
			return _state;
		}
		
		AppMessageManager.start();
		
		if (_events != null) _events.onReady();

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
					if (result != null && result.length() > 0) {
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
		
		if (_events != null) _events.onClosing();
		
		stopComponents();
		
		AppMessageManager.shutdown();
		
		log("--- Application stopped --- \n");
		if (_events != null) _events.onEnd();
		
		return _state;
	}
	
	/**
	 * Gets user input from console.
	 *
	 * @return the input content
	 */
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
	
	/**
	 * Register commands.
	 */
	protected void regCmds() {
		
		ArrayList<String> cmdList = new ArrayList<String>();
		
		Set<String> cmdSet = _cmds.keySet();
		for(String cmd : cmdSet) cmdList.add(cmd);
		
		Collections.sort(cmdList);
		
		for(String cmd : cmdList) {
			AppMessageManager.regMessage(cmd.trim(), getClass().getName());
		}
	}
	
	/**
	 * Execute a command.
	 *
	 * @param cmdLine the command line
	 * @return the result string
	 */
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
		if (cmdInfo != null) {
			if (param != null && param.length() > 0) rsl = Bean.callMethod(this, cmd, param);
			else rsl = Bean.callMethod(this, cmd, null);
		} else {
			rsl = getWrongCmdAlert();
		}
		if (rsl != null) return rsl.toString();
		else return "";
	}
	
	/**
	 * Gets description of all available cmds.
	 *
	 * @return the result string
	 */
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
		
		for (String cmd : cmdList) {
			String cmdInfo = _cmds.get(cmd);
			if (cmdInfo == null) cmdInfo = "";
			if (cmdInfo != null) {
				String output = String.format("    %" + maxLen + "s - %s", cmd, cmdInfo);
				rsl = rsl + output + "\n";
			}
		}
		
		return rsl;
	}	
	
	/* (non-Javadoc)
	 * @see org.flamering.app.AppMessageHandler#handleMessage(org.flamering.app.AppMessage)
	 */
	@Override
	public void handleMessage(AppMessage msg) {
		if(msg == null) return;
		String name = msg.getName();
		String content = msg.getContent();
		String result = execCmd(name + " " + content);
		if (result != null && result.length() > 0) {
			String[] lines = result.split("\n");
			for(String line : lines) {
				System.out.println(line);
			}
		}
	}
	
	/**
	 * Prints the tips.
	 */
	public void printTips() {
		System.out.println("");
		System.out.println("Tips: type [help] to list all commands");
		System.out.println("");
	}
	
	/**
	 * Prints application info.
	 */
	public void printAppInfo() {
		System.out.println("");
		System.out.println("**************************************************************");
		System.out.println("Application: " + _title);
		System.out.println("VersionInfo: " + _version);
		System.out.println("Description: " + _description);
		System.out.println("**************************************************************");
		System.out.println("");
	}
	
	/**
	 * Prints the input prefix.
	 */
	public void printInputPrefix() {
		System.out.print("ring>");
	}
	
	/**
	 * Gets alert message for the wrong cmd input.
	 *
	 * @return the alert message
	 */
	public String getWrongCmdAlert() {
		return "Wrong command! Please type [help] to list all commands";
	}
	
	/**
	 * Gets system state description.
	 *
	 * @return the description of current state
	 */
	public String state() {
		
		String rsl = "";
		
		int httpPort = Network.getHttpListeningPort();
		int wsPort = Network.getWebSocketListeningPort();
		
		if (httpPort > 0 || wsPort > 0) {
			rsl += "Web server address is " + Network.getWebServerAddress() + "\n";
		}
		
		if (httpPort > 0) {
			rsl += "HTTP service is working on port " + httpPort + (Network.isHttpWorkingWithSsl() ? " (with SSL)\n" : "\n");
			String crossDomains = Network.getHttpAccessControlAllowOrigin();
			if (crossDomains.length() > 0) rsl += "Access-Control-Allow-Origin: " + crossDomains + "\n";
		} else rsl += "No HTTP service available" + "\n";
		
		if (wsPort > 0) {
			rsl += "WebSocket service is working on port " + wsPort + (Network.isWebSocketWorkingWithSsl() ? " (with SSL)\n" : "\n");
			rsl += "Number of the clients on WebSocket: " + Network.getWebSocketClientCount() + "\n";
		} else rsl += "No WebSocket service available" + "\n";
		
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
	
	/**
	 * Gets help content.
	 *
	 * @return the help content
	 */
	public String help() {
		String rsl = "";
		rsl += "Here are all the commands you can use: \n";
		rsl += cmdsToString();
		return rsl;
	}
	
}
