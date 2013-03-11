package org.dobots.robotalk.client;

import org.zeromq.ZContext;

import android.app.Activity;

public class ZmqHandler {
	
	private static ZmqHandler INSTANCE;
	
	private ZContext m_oZmqContext;
	
	private Activity m_oActivity;

	private ZmqSettings m_oSettings;

	public ZmqHandler(Activity i_oActivity) {
		m_oActivity = i_oActivity;
		
		INSTANCE = this;

        m_oZmqContext = new ZContext();
		
		m_oSettings = new ZmqSettings(m_oActivity);
		m_oSettings.checkSettings();
        
	}
	
	public static ZmqHandler getInstance() {
		return INSTANCE;
	}

	public ZContext getContext() {
		return m_oZmqContext;
	}

	public ZmqSettings getSettings() {
		return m_oSettings;
	}

	public void createSocket(int type) {
		m_oZmqContext.createSocket(type);
	}

}
