package org.flamering.app;

import java.util.HashMap;
import java.util.Map;

/**
 * The settings of the application
 */
public class AppSettings {
	
	/** The Constant BEAN_NAME. */
	public static final String BEAN_NAME = "app-settings";
	
	/** The cmds. */
	protected Map<String, String> _cmds = new HashMap<String, String>();
	
	/** The params. */
	protected Map<String, String> _params = new HashMap<String, String>();
	
	/** The extra params. */
	protected Map<String, String> _extra = new HashMap<String, String>();
	
	/** The config file of the grid (Apache Ignite config file). */
	protected String _gridConfigFile = "grid.xml";
	
	/** The config file of the logger. */
	protected String _logConfigFile = "log.properties";

	/**
	 * Gets the logger's config file.
	 *
	 * @return the logger's config file
	 */
	public String getLogConfigFile() {
		return _logConfigFile;
	}
	
	/**
	 * Sets the logger's config file.
	 *
	 * @param logConfigFile the new config file
	 */
	public void setLogConfigFile(String logConfigFile) {
		_logConfigFile = logConfigFile;
	}

	/**
	 * Gets the grid's config file.
	 *
	 * @return the grid's config file
	 */
	public String getGridConfigFile() {
		return _gridConfigFile;
	}
	
	/**
	 * Sets the grid's config file.
	 *
	 * @param gridConfigFile the new config file
	 */
	public void setGridConfigFile(String gridConfigFile) {
		_gridConfigFile = gridConfigFile;
	}

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
	 * @param cmds the cmds
	 */
	public void setCmds(Map<String, String> cmds) {
		_cmds = cmds;
	}
	
	/**
	 * Gets the params.
	 *
	 * @return the params
	 */
	public Map<String, String> getParams() {
		return _params;
	}
	
	/**
	 * Sets the params.
	 *
	 * @param params the params
	 */
	public void setParams(Map<String, String> params) {
		_params = params;
	}
	
	/**
	 * Gets the extra params.
	 *
	 * @return the extra params
	 */
	public Map<String, String> getExtra() {
		return _extra;
	}
	
	/**
	 * Sets the extra params.
	 *
	 * @param extra the extra params
	 */
	public void setExtra(Map<String, String> extra) {
		_extra = extra;
	}

}
