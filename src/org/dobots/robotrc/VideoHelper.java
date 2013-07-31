package org.dobots.robotrc;

import org.dobots.communication.video.IFpsListener;
import org.dobots.communication.video.IVideoListener;
import org.dobots.utilities.ScalableImageView;
import org.dobots.utilities.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class VideoHelper implements IVideoListener, IFpsListener {
	
	private Handler mHandler = new Handler();

	private boolean m_bVideoConnected;

	private boolean m_bVideoStopped;

	private ScalableImageView m_ivVideo;
	private ProgressBar m_pbLoading;
	private TextView m_lblFps;
	private FrameLayout m_layCamera;

	private Activity m_oActivity;

	public VideoHelper(Activity activity, ViewGroup container) {
		m_oActivity = activity;
		
    	LayoutInflater.from(activity).inflate(R.layout.videoview, container);
    	m_layCamera = (FrameLayout) container.findViewById(R.id.layCamera);
    	m_ivVideo = (ScalableImageView) container.findViewById(R.id.ivCamera);
    	m_pbLoading = (ProgressBar) container.findViewById(R.id.pbLoading);
    	m_lblFps = (TextView) container.findViewById(R.id.lblFPS);
    	
    	initialize();
	}

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
		m_bVideoConnected = false;
		m_bVideoStopped = false;
		showVideoLoading(true);
		mHandler.postDelayed(m_oTimeoutRunnable, 15000);
	}

	protected void onStopVideo() {
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
		
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Utils.writeToCanvas(m_oActivity, new Canvas(bmp), i_strMsg, true);
		m_ivVideo.setImageBitmap(bmp);
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

			m_ivVideo.setImageBitmap(bmp);
		}
	}
    
}
