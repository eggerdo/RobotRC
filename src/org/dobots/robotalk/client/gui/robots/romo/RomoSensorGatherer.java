package org.dobots.robotalk.client.gui.robots.romo;

import org.dobots.robotalk.client.R;
import org.dobots.robotalk.client.gui.robots.SensorGatherer;
import org.dobots.robotalk.video.VideoMessage;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.dobots.utilities.CameraPreview.CameraPreviewCallback;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import android.widget.TextView;

public class RomoSensorGatherer extends SensorGatherer implements CameraPreviewCallback {

	private ZMQ.Socket videoSocket;

	private TextView m_lblFPS;

	public static final int SET_FPS				= 1001;
	
	public RomoSensorGatherer(BaseActivity i_oActivity) {
		super(i_oActivity);

		videoSocket = ZmqHandler.getInstance().getContext().createSocket(ZMQ.PUSH);
		videoSocket.connect("inproc://video");
		
		m_lblFPS = (TextView) i_oActivity.findViewById(R.id.lblFPS);
	}

    private int m_nFpsCounterPartner = 0;
    private long m_lLastTimePartner = System.currentTimeMillis();

    private boolean m_bDebug = true;
    
	@Override
	public void onFrame(byte[] rgb, int width, int height) {

		VideoMessage vmsg = new VideoMessage("Spykee", rgb);
		ZMsg zmsg = vmsg.toZmsg();
		zmsg.send(videoSocket);
		
        if (m_bDebug) {
            ++m_nFpsCounterPartner;
            long now = System.currentTimeMillis();
            if ((now - m_lLastTimePartner) >= 1000)
            {
            	final int nFPS = m_nFpsCounterPartner;
            	
//	        	Message uiMsg = m_oUiHandler.obtainMessage();
//	        	uiMsg.what = SET_FPS;
//	        	uiMsg.obj = m_nFpsCounterPartner;
//				m_oUiHandler.dispatchMessage(uiMsg);
				
				Utils.runAsyncUiTask(new Runnable() {
					
					@Override
					public void run() {
						m_lblFPS.setText("FPS: " + String.valueOf(nFPS));
					}
				});
	            
                m_lLastTimePartner = now;
                m_nFpsCounterPartner = 0;
            }
        }
	}
	
	

}
