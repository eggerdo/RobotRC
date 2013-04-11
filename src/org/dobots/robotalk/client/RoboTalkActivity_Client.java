package org.dobots.robotalk.client;


import org.dobots.robotalk.zmq.ZmqHandler;
import org.dobots.robotalk.zmq.ZmqMessageHandler;
import org.dobots.robotalk.zmq.ZmqMessageHandler.ZmqMessageListener;
import org.dobots.robotalk.zmq.ZmqSettings;
import org.dobots.robotalk.zmq.ZmqSettings.SettingsChangeListener;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import robots.RobotInventory;
import robots.RobotType;
import robots.ctrl.IRobotDevice;
import robots.ctrl.RobotDeviceFactory;
import robots.gui.RobotViewFactory;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

public class RoboTalkActivity_Client extends Activity {

	private static final String TAG = "RoboTalk";

	private static Activity CONTEXT;
	
	private static final int SETTINGS_ID 		= 0;

	// general
	private WakeLock m_oWakeLock;
	

	private ZmqMessageHandler m_oVideoHandler_External;
	
	private ZmqMessageHandler m_oVideoHandler_Base64;
		
	private ZmqHandler m_oZmqHandler;
	ZmqSettings m_oSettings;
	
	private Button m_btnZmqSettings;
	private Button m_btnConnect;

	private ZmqMessageHandler m_oCmdHandler_External;

	private String m_strCommandSendAddress;
	private String m_strCommandReceiveAddress;
	private String m_strVideoSendAddress;
	private String m_strVideoRecvAddress;

	private boolean m_bRemote;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setProperties();

        CONTEXT = this;
        Utils.setContext(CONTEXT);
        
        m_oZmqHandler = new ZmqHandler(this);
        m_oSettings = m_oZmqHandler.getSettings();

        m_oSettings.setSettingsChangeListener(new SettingsChangeListener() {
			
			@Override
			public void onChange() {
				closeConnections();
	        	setupConnections(m_oSettings.isRemote());
			}

			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				
			}
		});

        m_oVideoHandler_External = new ZmqMessageHandler();
        m_oVideoHandler_Base64 = new ZmqMessageHandler();

        m_oCmdHandler_External = new ZmqMessageHandler();
        
		PowerManager powerManager =
				(PowerManager)getSystemService(Context.POWER_SERVICE);
		m_oWakeLock =
				powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
						"Full Wake Lock");
		
//		showRobot(RobotType.RBT_SPYKEE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	getConnectionFromBundle(extras);
        	setupConnections(m_bRemote);
        } else if (m_oSettings.isValid()) {
        	setupConnections(m_oSettings.isRemote());
			showRobot(RobotType.RBT_ROMO);
		}
    }
    
    private void getConnectionFromBundle(Bundle i_oBundle) {
    	m_strCommandReceiveAddress = i_oBundle.getString("command_recv_address");
    	m_strCommandSendAddress = i_oBundle.getString("command_send_address");
    	m_strVideoRecvAddress = i_oBundle.getString("video_recv_address");
    	m_strVideoSendAddress = i_oBundle.getString("video_send_address");
    	m_bRemote = i_oBundle.getBoolean("remote");
    }

	private void closeConnections() {
		m_oVideoHandler_External.closeConnections();
		m_oVideoHandler_Base64.closeConnections();
		m_oCmdHandler_External.closeConnections();
	}

	private void setupConnections(boolean i_bRemote) {
		setupVideoConnection(i_bRemote);
		setupCommandConnection(i_bRemote);
	}

	private void setupVideoConnection(boolean i_bRemote) {
		ZMQ.Socket oVideoSender = m_oZmqHandler.createSocket(ZMQ.PUB);

		// set the output queue size down, we don't really want to have old video frames displayed
		// we only want the most recent ones
		oVideoSender.setHWM(20);

		if (i_bRemote) {
			oVideoSender.connect(getVideoSendAddress());
		} else {
			try {
				oVideoSender.bind(getVideoSendAddress());
			} catch (Exception e) {
				Utils.showToast("Video Port is already taken", Toast.LENGTH_LONG);
				return;
			}
		}

		m_oVideoHandler_External.setupConnections(null, oVideoSender);

		// link external with internal command handler. incoming commands in external are sent to
		// internal, incoming commands on internal are sent to external
		m_oVideoHandler_External.setIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oZmqHandler.getVideoHandler().sendZmsg(i_oMsg);
			}
		}); 
        m_oZmqHandler.getVideoHandler().setIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oVideoHandler_External.sendZmsg(i_oMsg);
			}
		});
        
		ZMQ.Socket oVideoSenderBase64 = m_oZmqHandler.createSocket(ZMQ.PUB);

		// set the output queue size down, we don't really want to have old video frames displayed
		// we only want the most recent ones
		oVideoSenderBase64.setHWM(20);

//		if (i_bRemote) {
//			oVideoSender.connect(getVideoSendAddress());
//		} else {
//			try {
//				oVideoSender.bind(getVideoSendAddress());
//			} catch (Exception e) {
//				Utils.showToast("Video Port is already taken", Toast.LENGTH_LONG);
//				return;
//			}
//		}
		
		String strAddress = String.format("tcp://%s:%d", m_oZmqHandler.getInstance().getSettings().getAddress(), 4030); 
		oVideoSenderBase64.connect(strAddress);
		
        m_oVideoHandler_Base64.setupConnections(null, oVideoSenderBase64);
        
        m_oVideoHandler_Base64.setIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oZmqHandler.getVideoBase64Handler().sendZmsg(i_oMsg);
			}
		});
        m_oZmqHandler.getVideoBase64Handler().setIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oVideoHandler_Base64.sendZmsg(i_oMsg);
			}
		});
	}

	private String getVideoReceiveAddress() {
		
		if (m_strVideoRecvAddress != null) {
			return m_strVideoRecvAddress;
		} else {
			// obtain command ports from settings
			// receive port is always equal to send port + 1
			int nVideoRecvPort = m_oSettings.getVideoPort() + 1;

			return assembleAddress(m_oSettings.isRemote(), nVideoRecvPort);
		}
	}

	private String getVideoSendAddress() {
		
		if (m_strVideoSendAddress != null) {
			return m_strVideoSendAddress;
		} else {
			// obtain command ports from settings
			// receive port is always equal to send port + 1
			int nVideoSendPort = m_oSettings.getVideoPort();

			return assembleAddress(m_oSettings.isRemote(), nVideoSendPort);
		}
	}
	
	private String getCommandReceiveAddress() {
		
		if (m_strCommandReceiveAddress != null) {
			return m_strCommandReceiveAddress;
		} else {
			// obtain command ports from settings
			// receive port is always equal to send port + 1
			int nCommandRecvPort = m_oSettings.getCommandPort() + 1;

			return assembleAddress(m_oSettings.isRemote(), nCommandRecvPort);
		}
	}

	private String getCommandSendAddress() {
		
		if (m_strCommandSendAddress != null) {
			return m_strCommandSendAddress;
		} else {
			// obtain command ports from settings
			// receive port is always equal to send port + 1
			int nCommandSendPort = m_oSettings.getCommandPort();
			
			return assembleAddress(m_oSettings.isRemote(), nCommandSendPort);
		}
	}
	
	private String assembleAddress(boolean i_bRemote, int i_nPort) {
		if (i_bRemote) {
			return String.format("tcp://%s:%d", m_oSettings.getAddress(), i_nPort);
		} else {
			return String.format("tcp://127.0.0.1:%d", i_nPort);
		}
	}
	
	private void setupCommandConnection(boolean i_bRemote) {
		ZMQ.Socket oCommandReceiver = m_oZmqHandler.createSocket(ZMQ.SUB);

		if (i_bRemote) {
			oCommandReceiver.connect(getCommandReceiveAddress());
		} else {
			try {
				oCommandReceiver.bind(getCommandReceiveAddress());
			} catch (Exception e) {
				Utils.showToast("Video Port is already taken", Toast.LENGTH_LONG);
				return;
			}
		}

		oCommandReceiver.subscribe("".getBytes());
		
		m_oCmdHandler_External.setupConnections(oCommandReceiver, null);
		
		// link external with internal command handler. incoming commands in external are sent to
		// internal, incoming commands on internal are sent to external
        m_oCmdHandler_External.setIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oZmqHandler.getCommandHandler().sendZmsg(i_oMsg);
			}
		});     
        m_oZmqHandler.getCommandHandler().setIncomingMessageListener(new ZmqMessageListener() {
			
			@Override
			public void onMessage(ZMsg i_oMsg) {
				m_oCmdHandler_External.sendZmsg(i_oMsg);
			}
		});
        
	}
	
    private void setProperties() {
        setContentView(R.layout.main);

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_btnZmqSettings = (Button) findViewById(R.id.btnZMQSettings);
		m_btnZmqSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				m_oZmqHandler.getSettings().showDialog();
			}
		});
		
		m_btnConnect = (Button) findViewById(R.id.btnConnect);
		m_btnConnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (m_oZmqHandler.getSettings().isValid()) {
//		        	setupConnections(m_oSettings.isRemote(), null, null);
					showRobot(RobotType.RBT_ROMO);
				} else {
					Utils.showToast("Zmq Settings invalid", Toast.LENGTH_LONG);
				}
			}
		});
        
    }

	public void showRobot(RobotType i_eType) {
		try {
			String i_strRobotID = createRobot(i_eType);
			createRobotView(i_eType, i_strRobotID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createRobotView(RobotType i_eType, String i_strRobotID) {
		Intent intent = new Intent(RoboTalkActivity_Client.this, RobotViewFactory.getRobotViewClass(i_eType));
		intent.putExtra("RobotType", i_eType);
		intent.putExtra("RobotID", i_strRobotID);
		intent.putExtra("OwnsRobot", true);
		startActivity(intent);
	}
	
	public String createRobot(RobotType i_eType) throws Exception {
		IRobotDevice oRobot = RobotDeviceFactory.createRobotDevice(i_eType);
		String i_strRobotID = RobotInventory.getInstance().addRobot(oRobot);
		return i_strRobotID;
	}

	public static Activity getActivity() {
		return CONTEXT;
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		
		// special handling, if the main activity get's restarted we are actually supposed to close the app
		// because we navigated back from the robot
//		finish();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!m_oWakeLock.isHeld()) {
			m_oWakeLock.acquire();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (m_oWakeLock.isHeld()) {
			m_oWakeLock.release();
		}

	}

	@Override
    public Dialog onCreateDialog(int id) {
    	return m_oZmqHandler.getSettings().onCreateDialog(id);
    }
    
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		m_oZmqHandler.getSettings().onPrepareDialog(id, dialog);
	}

}