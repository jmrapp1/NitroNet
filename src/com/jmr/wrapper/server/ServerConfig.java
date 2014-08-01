package com.jmr.wrapper.server;

import com.jmr.wrapper.common.config.Config;

/**
 * Networking Library
 * SeverConfig.java
 * Purpose: Stores the server configuration settings. 
 *
 * @author Jon R (Baseball435)
 * @version 1.0 7/19/2014
 */

public class ServerConfig extends Config {
	
	/** Whether or not to ping connections to make sure they are still connected. */
	public boolean PING_CLIENTS = true;
	
	/** The time between each ping being sent. */
	public int PING_SLEEP_TIME = 5000;
	
}
