package org.dobots.robotrc;

import org.dobots.communication.video.IFpsListener;
import org.dobots.communication.video.IRawVideoListener;
import org.dobots.communication.video.IVideoListener;
import org.dobots.utilities.ScalableImageView;
import org.dobots.utilities.ScalableSurfaceView;
import org.dobots.utilities.Utils;

import robots.gui.VideoSurfaceView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class VideoHelper implements IVideoListener, IFpsListener, IRawVideoListener {
	
	private static final int TIMEOUT = 5000; // 5 seconds
	private static final int WATCHDOG_INTERVAL = 1000; // 1 second
	
	private Handler mHandler = new Handler();

	private boolean m_bVideoConnected;

	private boolean m_bVideoStopped;

	private VideoSurfaceView m_ivVideo;
	private ProgressBar m_pbLoading;
	private TextView m_lblFps;
	private FrameLayout m_layCamera;

	private Activity m_oActivity;
	
	private long lastFrameRecv = 0;

	public VideoHelper(Activity activity, ViewGroup container) {
		m_oActivity = activity;
		
    	LayoutInflater.from(activity).inflate(R.layout.videoview, container);
    	m_layCamera = (FrameLayout) container.findViewById(R.id.layCamera);
    	m_ivVideo = (VideoSurfaceView) container.findViewById(R.id.ivCamera);
    	m_pbLoading = (ProgressBar) container.findViewById(R.id.pbLoading);
    	m_lblFps = (TextView) container.findViewById(R.id.lblFPS);
    	
    	m_ivVideo.setScale(true);
    	
    	initialize();
    	
    	mHandler.postDelayed(m_oWatchdog, WATCHDOG_INTERVAL);
	}
	
	protected Runnable m_oWatchdog = new Runnable() {
		
		public void run() {
			if (m_bVideoConnected && !m_bVideoStopped) {
				if (System.currentTimeMillis() - lastFrameRecv > TIMEOUT) {
					m_bVideoConnected = false;
					showVideoLoading(true);
				}
			}
			mHandler.postDelayed(this, WATCHDOG_INTERVAL);
		}
	};

	protected Runnable m_oTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			if (!m_bVideoConnected) {
				m_bVideoStopped = true;
				showVideoLoading(false);
				showVideoMsg("Video Connection Failed");
			}
		}
	};

	public void resetLayout() {
		initialize();
		
		Utils.showView(m_ivVideo, false);
	}
	
	private void initialize() {
		m_bVideoConnected = false;
	}

	public void onStartVideo() {
		onStartVideo(true);
	}
	
	public void onStartVideo(boolean i_bTimeout) {
		m_bVideoConnected = false;
		m_bVideoStopped = false;
		showVideoLoading(true);
		if (i_bTimeout) {
			mHandler.postDelayed(m_oTimeoutRunnable, 15000);
		}
	}

	public void onStopVideo() {
		m_bVideoStopped = true;
		showVideoMsg("Video OFF");
	}
	
	protected void showVideoLoading(final boolean i_bShow) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Utils.showView(m_ivVideo, !i_bShow);
				Utils.showView(m_pbLoading, i_bShow);
			}
		});
	}

	protected void showVideoMsg(String i_strMsg) {
		int width = m_layCamera.getWidth();
		int height = m_layCamera.getHeight();
		
		if (width == 0 || height == 0) {
			width = 380;
			height = 240;
		}
		
		Utils.writeToSurfaceView(m_oActivity, m_ivVideo, i_strMsg, true);
	}

	@Override
	public void onFPS(final int i_nFPS) {
		Utils.runAsyncUiTask(new Runnable() {

			@Override
			public void run() {
				if (!m_bVideoStopped) {
					m_lblFps.setText("FPS: " + String.valueOf(i_nFPS));
				}
			}
		});
		
	}

	@Override
	public void onFrame(Bitmap bmp, int rotation) {
		if (!m_bVideoStopped) {
			if (!m_bVideoConnected) {
				mHandler.removeCallbacks(m_oTimeoutRunnable);
				m_bVideoConnected = true;
				showVideoLoading(false);
			}
			
			lastFrameRecv = System.currentTimeMillis();
			m_ivVideo.onFrame(bmp, 0);
		}
	}

	@Override
	public void onFrame(byte[] rgb, int rotation) {
		if (!m_bVideoStopped) {
			Utils.runAsyncUiTask(new Runnable() {
				
				@Override
				public void run() {
					if (!m_bVideoConnected) {
						mHandler.removeCallbacks(m_oTimeoutRunnable);
						m_bVideoConnected = true;
						showVideoLoading(false);
					}
				}
			});

			lastFrameRecv = System.currentTimeMillis();
			m_ivVideo.onFrame(rgb, rotation);

		}
	}
    
}
