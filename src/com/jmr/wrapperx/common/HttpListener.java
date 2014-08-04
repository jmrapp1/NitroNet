package com.jmr.wrapperx.common;

import com.jmr.wrapper.common.listener.IListener;

/**
 * Networking Library
 * HttpListener.java
 * Purpose: The event listener for incoming HTTP protocol packets.
 * 
 * @author Jon R (Baseball435)
 * @version 1.0 8/4/2014
 */

public interface HttpListener extends IListener {

	/** Called when a new packet arrives.
	 * @param session The session.
	 * @param object The object that was sent.
	 */
	void received(HttpSession session, Object object);
	
	/** Called when a new client connects.
	 * @param session The session.
	 */
	void connected(HttpSession session);
}
