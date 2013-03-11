package org.dobots.robotalk.client.gui.robots.romo;

import org.dobots.robotalk.client.R;
import org.dobots.robotalk.client.control.IRemoteControlListener;
import org.dobots.robotalk.client.control.RemoteControlHelper;
import org.dobots.robotalk.client.control.RemoteControlHelper.Move;
import org.dobots.robotalk.client.gui.robots.RobotType;
import org.dobots.robotalk.client.gui.robots.RobotView;
import org.dobots.robotalk.client.gui.robots.SensorGatherer;
import org.dobots.robotalk.client.robots.IRobotDevice;
import org.dobots.robotalk.client.robots.romo.Romo;
import org.dobots.utilities.CameraPreview;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;

public class RomoRobot extends RobotView implements IRemoteControlListener {
	
	private static Activity CONTEXT;
	
	private Romo m_oRomo;
	
	private CameraPreview m_oCamera;
	
	private ImageButton m_btnCameraToggle;

	private RemoteControlHelper m_oRemoteCtrl;
	
	private RomoSensorGatherer m_oSensorGatherer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        CONTEXT = this;
        
        m_oRomo = new Romo();

		m_oRemoteCtrl = new RemoteControlHelper(m_oActivity, m_oRomo, this);
        m_oRemoteCtrl.setProperties();

        m_oSensorGatherer = new RomoSensorGatherer(this);
		m_oCamera.setFrameListener(m_oSensorGatherer);
        
	}

	@Override
	protected void setProperties(RobotType i_eRobot) {

		setContentView(R.layout.romo_main);

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_oCamera = (CameraPreview) findViewById(R.id.svCamera);
		m_oCamera.setScale(false);
		m_oCamera.setPreviewSize(640, 480);
	
		m_btnCameraToggle = (ImageButton) findViewById(R.id.btnCameraToggle);
		if (Camera.getNumberOfCameras() <= 1) {
			m_btnCameraToggle.setVisibility(View.GONE);
		} else {
			m_btnCameraToggle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					m_oCamera.toggleCamera();
				}
			});
		}
	}

	public static Activity getContext() {
		return CONTEXT;
	}

	@Override
	protected void onConnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDisconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onConnectError() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void connectToRobot() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void resetLayout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateButtons(boolean i_bEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected IRobotDevice getRobot() {
		// TODO Auto-generated method stub
		return m_oRomo;
	}

	@Override
	protected SensorGatherer getSensorGatherer() {
		// TODO Auto-generated method stub
		return m_oSensorGatherer;
	}

	@Override
	public void onMove(RemoteControlHelper.Move i_oMove, double i_dblSpeed, double i_dblAngle) {
//		m_oRemoteCtrl.onMove(i_oMove, i_dblSpeed, i_dblAngle);
	}

	@Override
	public void onMove(RemoteControlHelper.Move i_oMove) {
//		m_oRemoteCtrl.onMove(i_oMove);
	}

	@Override
	public void enableControl(boolean i_bEnable) {
//		m_oRemoteCtrl.enableControl(i_bEnable);
		
	}

}
