/**
* 456789------------------------------------------------------------------------------------------------------------120
*
* @brief: Activity for the User (Remote Control) part of the Robot RC application
* @file: RobotRC_User.java
*
* @desc:  	Offers remote control for Robots over ZeroMQ. The robot can be controlled with
* 			either arrow buttons or joystick. At the same time the received video will be
* 			displayed on the screen. Works both in landscape and portray mode.
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

import org.dobots.communication.control.ZmqRemoteControlHelper;
import org.dobots.communication.control.ZmqRemoteControlSender;
import org.dobots.communication.video.VideoDisplayThread;
import org.dobots.communication.video.VideoHelper;
import org.dobots.communication.zmq.ZmqActivity;
import org.dobots.communication.zmq.ZmqConnectionHelper;
import org.dobots.communication.zmq.ZmqConnectionHelper.UseCase;
import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.utilities.Utils;
import org.dobots.utilities.VerticalSeekBar;
import org.zeromq.ZMQ;

import robots.ctrl.RemoteControlHelper;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class RobotRC_User extends ZmqActivity {
	
	private static final int MENU_CAMERA_CONTROL = 1;

	// video
	private ZmqRemoteControlSender m_oZmqRemoteListener;
	private ZmqRemoteControlHelper m_oRemoteCtrl;
	private ZmqConnectionHelper mZmqConnectionHelper;
	
	private String mRobotID = "";
	private VideoDisplayThread m_oVideoDisplayer;
	private LinearLayout m_layVideo;
	
	private VideoHelper mVideoHelper;

	private LinearLayout m_layCamera;

	private VerticalSeekBar m_sbCamera;

	private Button m_btnToggle;
	private boolean m_bCameraControl = false;
	
	private ZMQ.Socket m_oVideoRecvSocket;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setProperties();
        
		m_oRemoteCtrl = new ZmqRemoteControlHelper(this);
		
		if (savedInstanceState != null) {
			m_oRemoteCtrl.setJoystickControl(savedInstanceState.getBoolean("joystickCtrl"));
			m_bCameraControl = savedInstanceState.getBoolean("cameraCtrl");
		}
	}
	
	private void setProperties() {

		setContentView(R.layout.robotrc_user);
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

		m_layVideo = (LinearLayout) findViewById(R.id.layVideo);
		
		m_layCamera = (LinearLayout) findViewById(R.id.layCameraControl);
		
		m_sbCamera = (VerticalSeekBar) findViewById(R.id.sbCamera);
		m_sbCamera.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar paramSeekBar) {
				m_sbCamera.setNewProgress(50);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar paramSeekBar) {
				m_sbCamera.getProgress();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (progress < 50) {
					m_oRemoteCtrl.cameraDown();
				} else if (progress > 50) {
					m_oRemoteCtrl.cameraUp();
				} else {
					m_oRemoteCtrl.cameraStop();
				}
			}
		});
		
		m_btnToggle = (Button) findViewById(R.id.btnToggle);
		m_btnToggle.getBackground().setAlpha(99);
		m_btnToggle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View paramView) {
				m_oRemoteCtrl.toggleCamera();
			}
		});

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("joystickCtrl", m_oRemoteCtrl.isJoystickControl());
		outState.putBoolean("cameraCtrl", m_bCameraControl);

		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mZmqConnectionHelper != null) {
			mZmqConnectionHelper.close();
		}
		
		if (mVideoHelper != null) {
			mVideoHelper.onDestroy();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
//		stopVideo();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		stopVideo();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
//		startVideo();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		startVideo();
	}

    private void setupVideoDisplay() {

        mVideoHelper = new VideoHelper(this, m_layVideo);
        
        startVideo();
	}
    
    private void startVideo() {
    	if (mVideoHelper != null) {

			m_oVideoRecvSocket = ZmqHandler.getInstance().obtainVideoRecvSocket();
			m_oVideoRecvSocket.subscribe(mRobotID.getBytes());
			
			// start a video display thread which receives video frames from the socket and displays them
			m_oVideoDisplayer = new VideoDisplayThread(ZmqHandler.getInstance().getContext().getContext(), m_oVideoRecvSocket);
			m_oVideoDisplayer.setRawVideoListner(mVideoHelper);
			m_oVideoDisplayer.setFPSListener(mVideoHelper);
			m_oVideoDisplayer.start();
			
			mVideoHelper.onStartVideo(false);
    	}
    }
    
    private void stopVideo() {
    	
		if (m_oVideoDisplayer != null) {
			m_oVideoDisplayer.setVideoListener(null);
			m_oVideoDisplayer.close();
			m_oVideoDisplayer = null;
		}

		if (mVideoHelper != null) {
			mVideoHelper.onStopVideo();
			mVideoHelper = null;
		}
    	
		if (m_oVideoRecvSocket != null) {
			m_oVideoRecvSocket.close();
			m_oVideoRecvSocket = null;
		}
		
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	menu.add(Menu.NONE, MENU_CAMERA_CONTROL, MENU_CAMERA_CONTROL, "Camera Control");
    	
    	return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	Utils.updateOnOffMenuItem(menu.findItem(MENU_CAMERA_CONTROL), m_bCameraControl);
    	
    	return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case MENU_CAMERA_CONTROL:
    		showCameraControl(!m_bCameraControl);
    		return true;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }

	private void showCameraControl(boolean show) {
		m_bCameraControl = show;
		Utils.showView(m_layCamera, m_bCameraControl);
	}

	@Override
	public void onZmqReady() {
		mZmqConnectionHelper = new ZmqConnectionHelper(UseCase.USER);
		mZmqConnectionHelper.setup(mZmqHandler, this);

		// don't know the robot id yet
    	m_oZmqRemoteListener = new ZmqRemoteControlSender("");
		m_oRemoteCtrl.setDriveControlListener(m_oZmqRemoteListener);
		m_oRemoteCtrl.setCameraControlListener(m_oZmqRemoteListener);

		setupVideoDisplay();
	}

	@Override
	public void onZmqFailed() {
		showToast("Failed to set-up ZeroMQ, make sure your settings are correct!", Toast.LENGTH_LONG);
	}
    
}
