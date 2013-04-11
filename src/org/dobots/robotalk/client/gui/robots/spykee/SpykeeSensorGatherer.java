package org.dobots.robotalk.client.gui.robots.spykee;

import org.dobots.robotalk.client.R;
import org.dobots.robotalk.client.robots.spykee.Spykee;
import org.dobots.robotalk.client.robots.spykee.SpykeeController.DockState;
import org.dobots.robotalk.client.robots.spykee.SpykeeMessageTypes;
import org.dobots.robotalk.video.VideoTypes;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.ScalableImageView;
import org.dobots.utilities.Utils;

import robots.gui.SensorGatherer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SpykeeSensorGatherer extends SensorGatherer {
	
	private Spykee m_oSpykee;

	private boolean m_bVideoEnabled = true;
	private boolean m_bVideoConnected = false;
	private boolean m_bVideoScaled = false;
	
	private TextView m_txtBattery;
	private TextView m_txtDockingState;
	
	private TextView m_lblFPS;
	private boolean m_bDebug = true;
	
	private ProgressBar m_pbLoading;
	private ScalableImageView m_ivVideo;
	
	private FrameLayout m_layCamera;

//	private Socket videoSocket;

    private int m_nFpsCounter = 0;
    private long m_lLastTime = System.currentTimeMillis();

	public SpykeeSensorGatherer(BaseActivity i_oActivity, Spykee i_oSpykee) {
		super(i_oActivity, "SpykeeSensorGatherer");
		m_oSpykee = i_oSpykee;

//		videoSocket = RoboTalkActivity_Client.getZContext().createSocket(ZMQ.PUSH);
//		videoSocket.connect("inproc://video");
		
		setProperties();

		initialize();
		
		start();
	}
	
	public void setProperties() {
		m_lblFPS = (TextView) m_oActivity.findViewById(R.id.lblFPS);
		
		m_txtBattery = (TextView) m_oActivity.findViewById(R.id.txtBattery);
		m_txtDockingState = (TextView) m_oActivity.findViewById(R.id.txtDockingState);
		
		m_pbLoading = (ProgressBar) m_oActivity.findViewById(R.id.pbLoading);
		m_ivVideo = (ScalableImageView) m_oActivity.findViewById(R.id.ivCamera);
		
		m_layCamera = (FrameLayout) m_oActivity.findViewById(R.id.layCamera);
		
	}
	
	public void initialize() {
		m_bVideoConnected = false;
	}

	public void resetLayout() {
		initialize();
		
		m_txtBattery.setText("-");
		m_txtDockingState.setText("-");
		
		showView(m_ivVideo, false);
	}
	
	private void showVideoLoading(final boolean i_bShow) {
		m_oSensorDataUiUpdater.post(new Runnable() {
			@Override
			public void run() {
				showView(m_ivVideo, !i_bShow);
				showView(m_pbLoading, i_bShow);
			}
		});
	}
	
	private void showView(View i_oView, boolean i_bShow) {
		if (i_bShow) {
			i_oView.setVisibility(View.VISIBLE);
		} else {
			i_oView.setVisibility(View.INVISIBLE);
		}
	}

	public void sendMessage(int message, Object data) {
		Utils.sendMessage(m_oSensorDataUiUpdater, message, data);
	}
	
	public void dispatchMessage(Message msg) {
		m_oSensorDataUiUpdater.dispatchMessage(msg);
	}

	/**
	 * Receive messages from the BTCommunicator
	 */
	final Handler m_oSensorDataUiUpdater = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case VideoTypes.SET_FPS:
				if (m_bDebug) {
					final int fps_user = (Integer) msg.obj;
					
//					// update the user's FPS
//					Utils.runAsyncUiTask(new Runnable() {
//						
//		    			@Override
//		    			public void run() {
		    				m_lblFPS.setText("FPS: " + String.valueOf(fps_user));
//		    			}
//		            	
//		            });
				}
				break;
			case SpykeeMessageTypes.BATTERY_LEVEL_RECEIVED:
				updateBatteryLevel(msg.arg1);
				break;
			case SpykeeMessageTypes.DOCKINGSTATE_RECEIVED:
				updateDockingState((DockState)msg.obj);
				break;
			case SpykeeMessageTypes.VIDEO_FRAME_RECEIVED:
				updateVideo((Bitmap)msg.obj);
				break;
			case SpykeeMessageTypes.AUDIO_RECEIVED:
//				if (mMediaPlayer == null) {
//					return;
//				}
//				sNumAudioBuffers += 1;
//				if (sNumAudioBuffers >= DROP_AUDIO_THRESHOLD) {
//					mNumSkips += 1;
//					sNumAudioBuffers -= 1;
//					mPlayingAudioNum += 1;
//					if (mPlayingAudioNum >= MAX_AUDIO_BUFFERS) {
//						mPlayingAudioNum = 0;
//					}
//					Log.d(TAG, "audio skips: " + mNumSkips + " waits: " + mNumWaits);
//				}
//				if (!mMediaPlayer.isPlaying() && sNumAudioBuffers == 1) {
//	    			playNextAudioFile();
//				}
			}
		}
	};
	
	private void updateBatteryLevel(int i_nBattery) {
		m_txtBattery.setText(String.format("%d", i_nBattery));
	}
	
	private void updateDockingState(DockState i_eDockingState) {
		switch (i_eDockingState) {
		case DOCKED:
			m_txtDockingState.setText("Docked");
			break;
		case UNDOCKED:
			m_txtDockingState.setText("Undocked");
			break;
		case DOCKING:
			m_txtDockingState.setText("Docking");
			break;
		}
	}

	
	private void updateVideo(final Bitmap i_bmpFrame) {

		if (m_bVideoEnabled) {
			// if we haven't been connected so far, the reception of a frame
			// means that the video is now connected
			if (!m_bVideoConnected) {
				m_bVideoConnected = true;
				showVideoLoading(false);
			}
            m_ivVideo.setImageBitmap(i_bmpFrame);
		}

        if (m_bDebug) {
            ++m_nFpsCounter;
            long now = System.currentTimeMillis();
            if ((now - m_lLastTime) >= 1000)
            {
            	Message uiMsg = m_oSensorDataUiUpdater.obtainMessage();
	        	uiMsg.what = VideoTypes.SET_FPS;
	        	uiMsg.obj = m_nFpsCounter;
	        	m_oSensorDataUiUpdater.dispatchMessage(uiMsg);
	            
                m_lLastTime= now;
                m_nFpsCounter= 0;
            }
        }
		
//        Utils.runAsyncTask(new Runnable() {
//			
//			@Override
//			public void run() {
//				VideoMessage msg = new VideoMessage(m_oSpykee.getType().toString(), i_bmpFrame);
//				ZMsg zmsg = msg.toZmsg();
//				zmsg.send(videoSocket);
//			}
//		});
		
	}
	
	public void setVideoEnabled(boolean i_bVideoEnabled) {
		
		m_bVideoEnabled = i_bVideoEnabled;
		m_oSpykee.setVideoEnabled(i_bVideoEnabled);
		
		if (i_bVideoEnabled) {
			startVideo();
		} else {
			m_bVideoConnected = false;
			showVideoMsg("Video OFF");
		}
		
	}
	
	private void startVideo() {
		m_bVideoConnected = false;
		showVideoLoading(true);
		m_oSensorDataUiUpdater.postDelayed(new Timeout(), 15000);
	}
	
	private class Timeout implements Runnable {
		@Override
		public void run() {
			if (!m_bVideoConnected) {
				setVideoEnabled(false);
				showVideoLoading(false);
				showVideoMsg("Video Connection Failed");
			}
		}
	}
	
	private void showVideoMsg(String i_strMsg) {
		Bitmap bmp = Bitmap.createBitmap(m_layCamera.getWidth(), m_layCamera.getHeight(), Bitmap.Config.RGB_565);
		Utils.writeToCanvas(m_oActivity, new Canvas(bmp), i_strMsg, true);
		m_ivVideo.setImageBitmap(bmp);
	}

	public void onConnect() {
		setVideoEnabled(m_bVideoEnabled);
	}

	public boolean isVideoEnabled() {
		return m_bVideoEnabled;
	}

	public boolean isVideoScaled() {
		return m_bVideoScaled;
	}

	public void setVideoScaled(boolean i_bScaled) {
		m_bVideoScaled = i_bScaled;
		m_ivVideo.setScale(i_bScaled);
	}

	public void setAudioEnabled(boolean i_bAudioEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}
