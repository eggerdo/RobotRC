/**
* 456789------------------------------------------------------------------------------------------------------------120
*
* @brief: Robot RC application
* @file: RobotRCActivity.java
*
* @desc:  	Main Activity for the Robot RC application. choose between robot and user mode.
*
*
* Copyright (c) 2013 Dominik Egger <dominik@dobots.nl>
*
* @author:		Dominik Egger
* @date:		30.08.2013
* @project:		RobotRC
* @company:		Distributed Organisms B.V.
*/
package org.dobots.robotrc;


import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.communication.zmq.ZmqSettings;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;

public class RobotRCActivity extends BaseActivity {

	private static final String TAG = "RoboTalk";

	private static Activity CONTEXT;

	// general

	private ZmqHandler m_oZmqHandler;
	private ZmqSettings m_oSettings;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setProperties();

        CONTEXT = this;
        Utils.setContext(CONTEXT);
        
        m_oZmqHandler = new ZmqHandler(this);
        m_oSettings = m_oZmqHandler.getSettings();
        
        if (!m_oSettings.checkSettings()) {
        	m_oSettings.showDialog(this);
        }
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	m_oZmqHandler.onDestroy();
    }
    
//    private void getConnectionFromBundle(Bundle i_oBundle) {
//    	m_strCommandReceiveAddress = i_oBundle.getString("command_recv_address");
//    	m_strCommandSendAddress = i_oBundle.getString("command_send_address");
//    	m_strVideoRecvAddress = i_oBundle.getString("video_recv_address");
//    	m_strVideoSendAddress = i_oBundle.getString("video_send_address");
//    	m_bRemote = i_oBundle.getBoolean("remote");
//    }

    private void setProperties() {
        setContentView(R.layout.main);

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		ImageButton btnRobot = (ImageButton) findViewById(R.id.btnRobot);
		btnRobot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RobotRCActivity.this, RobotRC_Robot.class);
				startActivity(intent);
			}
		});
		
		ImageButton btnUser = (ImageButton) findViewById(R.id.btnUser);
		btnUser.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RobotRCActivity.this, RobotRC_User.class);
				startActivity(intent);
			}
		});
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
    public Dialog onCreateDialog(int id) {
    	return m_oZmqHandler.getSettings().onCreateDialog(this, id);
    }
    
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		m_oZmqHandler.getSettings().onPrepareDialog(id, dialog);
	}

}