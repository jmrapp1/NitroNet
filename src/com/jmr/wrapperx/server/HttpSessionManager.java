package com.jmr.wrapperx.server;

import java.util.ArrayList;

import com.jmr.wrapperx.common.HttpSession;

/**
 * Networking Library
 * HttpSessionManager.java
 * Purpose: Holds all connected clients and their session information. Provides a method
 * to get a session with the given cookie.
 *
 * @author Jon R (Baseball435)
 * @version 1.0 8/4/2014
 */

public class HttpSessionManager {

	/** Singleton instance of the manager. */
	private static final HttpSessionManager instance = new HttpSessionManager();
	
	/**  List of all of the sessions. */
	private final ArrayList<HttpSession> sessions = new ArrayList<HttpSession>();
	
	/** Default constructor. */
	private HttpSessionManager() {
		
	}
	
	/** Adds a session to the list.
	 * @param session Session to add.
	 */
	public void addSession(HttpSession session) {
		sessions.add(session);
	}
	
	/** Removes a session from the list.
	 * @param session Session to remove.
	 */
	public void removeSession(HttpSession session) {
		sessions.remove(session);
	}
	
	/** Gets a sessions with the given cookie value.
	 * @param cookie The cookie value.
	 * @return The session if found.
	 */
	public HttpSession getSession(String cookie) {
		for (HttpSession session : sessions)
			if (session.getCookie().equalsIgnoreCase(cookie))
				return session;
		return null;
	}
	
	/** @return Singleton instance of the session manager. */
	public static HttpSessionManager getInstance() {
		return instance;
	}
	
}
