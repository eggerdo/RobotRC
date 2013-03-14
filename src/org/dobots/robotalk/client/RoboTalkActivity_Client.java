package org.dobots.robotalk.client;


import org.dobots.robotalk.client.gui.robots.RobotViewFactory;
import org.dobots.robotalk.control.CommandHandler;
import org.dobots.robotalk.video.VideoHandler;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.dobots.robotalk.zmq.ZmqSettings;
import org.dobots.robotalk.zmq.ZmqSettings.SettingsChangeListener;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ;

import robots.RobotType;
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
	
	private VideoHandler m_oVideoHandler;
	
	private ZmqHandler m_oZmqHandler;
	ZmqSettings m_oSettings;
	
	private Button m_btnZmqSettings;
	private Button m_btnConnect;

	private CommandHandler m_oCommandHandler;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setProperties();

        CONTEXT = this;
        Utils.setContext(CONTEXT);
        
        m_oZmqHandler = new ZmqHandler(this);
        m_oSettings = m_oZmqHandler.getSettings();

        m_oVideoHandler = new VideoHandler(m_oZmqHandler.getContext());
        m_oSettings.setSettingsChangeListener(new SettingsChangeListener() {
			
			@Override
			public void onChange() {
				closeConnections();
				setupConnections();
			}
		});
        
        m_oCommandHandler = new CommandHandler(m_oZmqHandler);
        
		PowerManager powerManager =
				(PowerManager)getSystemService(Context.POWER_SERVICE);
		m_oWakeLock =
				powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
						"Full Wake Lock");
		
//		showRobot(RobotType.RBT_SPYKEE);

		if (m_oSettings.isValid()) {
	        setupConnections();
			showRobot(RobotType.RBT_ROMO);
		}
    }

	private void closeConnections() {
		m_oVideoHandler.closeConnections();
		m_oCommandHandler.closeConnections();
	}

	private void setupConnections() {
		setupVideoConnection();
		setupCommandConnection();
	}
	
	private void setupVideoConnection() {
		ZMQ.Socket oVideoSender = m_oZmqHandler.createSocket(ZMQ.PUB);

		// obtain video ports from settings
		// receive port is always equal to send port + 1
		int nVideoSendPort = m_oSettings.getVideoPort();
		
		// set the output queue size down, we don't really want to have old video frames displayed
		// we only want the most recent ones
		oVideoSender.setHWM(20);

		oVideoSender.connect(String.format("tcp://%s:%d", m_oSettings.getAddress(), nVideoSendPort));

		m_oVideoHandler.setupConnections(null, oVideoSender);
	}
	
	private void setupCommandConnection() {
		ZMQ.Socket oCommandReceiver = m_oZmqHandler.createSocket(ZMQ.SUB);

		// obtain command ports from settings
		// receive port is always equal to send port + 1
		int nCommandRecvPort = m_oSettings.getCommandPort() + 1;
		
		oCommandReceiver.connect(String.format("tcp://%s:%d", m_oSettings.getAddress(), nCommandRecvPort));
		oCommandReceiver.subscribe("".getBytes());
		
		m_oCommandHandler.setupConnections(oCommandReceiver, null);

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
			        setupConnections();
					showRobot(RobotType.RBT_ROMO);
				} else {
					Utils.showToast("Zmq Settings invalid", Toast.LENGTH_LONG);
				}
			}
		});
        
    }

	public void showRobot(RobotType i_eType) {
		Intent intent = new Intent(RoboTalkActivity_Client.this, RobotViewFactory.getRobotViewClass(i_eType));
		intent.putExtra("RobotType", i_eType);
		startActivity(intent);
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