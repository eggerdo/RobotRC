package org.dobots.robotrc;

import org.dobots.communication.control.ZmqRemoteControlHelper;
import org.dobots.communication.control.ZmqRemoteListener;
import org.dobots.communication.video.IFpsListener;
import org.dobots.communication.video.IVideoListener;
import org.dobots.communication.video.VideoDisplayThread;
import org.dobots.communication.zmq.ZmqActivity;
import org.dobots.communication.zmq.ZmqConnectionHelper;
import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.communication.zmq.ZmqConnectionHelper.UseCase;
import org.dobots.utilities.ScalableImageView;
import org.zeromq.ZMQ;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

public class RobotRC_User extends ZmqActivity {

	// video
	private ScalableImageView m_svVideo;
	private TextView lblFPS;
	private ZmqRemoteListener m_oZmqRemoteListener;
	private ZmqRemoteControlHelper m_oRemoteCtrl;
	private ZmqConnectionHelper mZmqConnectionHelper;
	
	private String mRobotID = "";
	private VideoDisplayThread m_oVideoDisplayer;
	private boolean m_bVideoStopped;
	private boolean m_bVideoConnected;
	private FrameLayout m_layVideo;
	
	private VideoHelper mVideoHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setProperties();
        
		mZmqConnectionHelper = new ZmqConnectionHelper(UseCase.USER);
		mZmqConnectionHelper.setup(mZmqHandler, this);

		// don't know the robot id yet
    	m_oZmqRemoteListener = new ZmqRemoteListener("");
		m_oRemoteCtrl = new ZmqRemoteControlHelper(this, m_oZmqRemoteListener);
        m_oRemoteCtrl.setProperties();

		setupVideoDisplay();
		
	}
	
	private void setProperties() {

		setContentView(R.layout.robotrc_user);
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

		m_layVideo = (FrameLayout) findViewById(R.id.layDisplay);

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mZmqConnectionHelper.close();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		stopVideo();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		startVideo();
	}

	@Override
	public void ready() {
		// TODO Auto-generated method stub

	}

	@Override
	public void failed() {
		// TODO Auto-generated method stub

	}

    private void setupVideoDisplay() {

        mVideoHelper = new VideoHelper(this, m_layVideo);
        
        startVideo();
	}
    
    ZMQ.Socket oVideoRecvSocket;
    private void startVideo() {

		oVideoRecvSocket = ZmqHandler.getInstance().obtainVideoRecvSocket();
		oVideoRecvSocket.subscribe(mRobotID.getBytes());
		
		// start a video display thread which receives video frames from the socket and displays them
		m_oVideoDisplayer = new VideoDisplayThread(ZmqHandler.getInstance().getContext().getContext(), oVideoRecvSocket);
		m_oVideoDisplayer.setVideoListener(mVideoHelper);
		m_oVideoDisplayer.setFPSListener(mVideoHelper);
		m_oVideoDisplayer.start();
    }
    
    private void stopVideo() {
		if (m_oVideoDisplayer != null) {
			m_oVideoDisplayer.close();
			m_oVideoDisplayer = null;
		}
		oVideoRecvSocket.close();
		oVideoRecvSocket = null;
    }

}
