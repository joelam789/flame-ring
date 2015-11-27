package org.flamering.app;

import java.util.HashMap;
import java.util.Map;

public class AppSettings {
	
	public static final String BEAN_NAME = "app-settings";
	
	private Map<String, String> _cmds = new HashMap<String, String>();
	private Map<String, String> _params = new HashMap<String, String>();
	private Map<String, String> _extra = new HashMap<String, String>();
	
	private String _gridConfigFile = "grid.xml";
	private String _logConfigFile = "log.properties";

	public String getLogConfigFile() {
		return _logConfigFile;
	}
	public void setLogConfigFile(String logConfigFile) {
		_logConfigFile = logConfigFile;
	}

	public String getGridConfigFile() {
		return _gridConfigFile;
	}
	public void setGridConfigFile(String gridConfigFile) {
		_gridConfigFile = gridConfigFile;
	}

	public Map<String, String> getCmds() {
		return _cmds;
	}
	public void setCmds(Map<String, String> cmds) {
		_cmds = cmds;
	}
	
	public Map<String, String> getParams() {
		return _params;
	}
	public void setParams(Map<String, String> params) {
		_params = params;
	}
	
	public Map<String, String> getExtra() {
		return _extra;
	}
	public void setExtra(Map<String, String> extra) {
		_extra = extra;
	}

}
