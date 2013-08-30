/**
* 456789------------------------------------------------------------------------------------------------------------120
*
* @brief: Activity for the Robot part of the Robot RC application
* @file: RobotRC_Robot.java
*
* @desc:  	Offers a set of available robots to choose from. The application will connect to the robot
* 			then stream the video over ZeroMQ. In return it listens for incoming commands over ZeroMQ
* 			and forwards them to the robot
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

import org.dobots.communication.zmq.ZmqActivity;
import org.dobots.communication.zmq.ZmqConnectionHelper;
import org.dobots.communication.zmq.ZmqConnectionHelper.UseCase;
import org.dobots.utilities.Utils;

import robots.RobotType;
import robots.gui.RobotLaunchHelper;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class RobotRC_Robot extends ZmqActivity {
	
	public static final String PREFS_AUTO_CONNECT 	= "autoconnect";
	public static final String PREFS_ROBOT_TYPE		= "robottype";
	public static final boolean DEF_AUTO_CONNECT	= false;
	public static final RobotType DEF_ROBOT_TYPE	= null;
	
	private ZmqConnectionHelper m_oZmqCoordinator;
	
	private RobotType[] mRobotList = {RobotType.RBT_ROMO, RobotType.RBT_AC13ROVER, RobotType.RBT_ROVER2, RobotType.RBT_SPYTANK};
	private boolean mAutoConnect;
	private RobotType mSelectedRobot;
	
	private CheckBox m_cbxAutoConnect;
	private ListView m_lvRobotList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setProperties();
        enable(false);

        m_oZmqCoordinator = new ZmqConnectionHelper(UseCase.ROBOT);
        m_oZmqCoordinator.setup(mZmqHandler, this);
        
	}
	
	private void setProperties() {
		setContentView(R.layout.robotrc_robot);
		
		m_cbxAutoConnect = (CheckBox) findViewById(R.id.autoConnect);
//		autoConnect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(CompoundButton button,
//					boolean checked) {
//				storeAutoConnect(checked);
//			}
//
//		});
		
		m_lvRobotList = (ListView) findViewById(R.id.robotList);
		m_lvRobotList.setAdapter(new ArrayAdapter<RobotType>(this, android.R.layout.simple_list_item_1, 
				mRobotList));
		m_lvRobotList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent,
					View view, int position, long id) {
				RobotType type = (RobotType) parent.getAdapter().getItem(position);
				storeAutoConnectSettings(m_cbxAutoConnect.isChecked(), type);
				RobotLaunchHelper.showRobot(RobotRC_Robot.this, type);
			}
		});
	}

	private boolean checkAutoConnectSettings() {
		SharedPreferences prefs = getPreferences(Activity.MODE_PRIVATE);
		
		String type = prefs.getString(PREFS_ROBOT_TYPE, PREFS_ROBOT_TYPE);
		if (type != null) {
			mSelectedRobot = RobotType.fromString(type);
		}
		mAutoConnect = prefs.getBoolean(PREFS_AUTO_CONNECT, DEF_AUTO_CONNECT);
		
		m_cbxAutoConnect.setChecked(mAutoConnect);
		
		return mAutoConnect;
	}
	
	private void storeAutoConnectSettings(boolean checked, RobotType type) {
		SharedPreferences prefs = getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(PREFS_AUTO_CONNECT, checked);
		editor.putString(PREFS_ROBOT_TYPE, type.name());
		editor.commit();
	}
	
	public void enable(boolean enable) {
		m_cbxAutoConnect.setEnabled(enable);
		m_lvRobotList.setEnabled(enable);
	}
	
	@Override
	public void ready() {
		enable(true);

        if (checkAutoConnectSettings()) {
        	RobotLaunchHelper.showRobot(this, mSelectedRobot);
        }
	}

	@Override
	public void failed() {
		enable(false);
		Utils.showToast("ZMQ failed, check settings!!", Toast.LENGTH_LONG);
	}

}
