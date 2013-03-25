package org.dobots.robotalk.client.gui.robots.romo;

import org.dobots.robotalk.client.R;
import org.dobots.robotalk.client.gui.robots.SensorGatherer;
import org.dobots.robotalk.client.robots.romo.Romo;
import org.dobots.robotalk.msg.VideoMessage;
import org.dobots.robotalk.video.VideoHandler;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.CameraPreview.CameraPreviewCallback;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import android.widget.TextView;

public class RomoSensorGatherer extends SensorGatherer implements CameraPreviewCallback {

	private ZMQ.Socket m_oVideoSocket;

	private TextView m_lblFPS;

	public static final int SET_FPS				= 1001;
	
	private Romo m_oRomo;
	
	private String m_strVideoAddr;
	
	public RomoSensorGatherer(BaseActivity i_oActivity, Romo i_oRomo) {
		super(i_oActivity, "RomoSensorGatherer");
		m_oRomo = i_oRomo;

		m_strVideoAddr = "inproc://romo_video";

		m_oVideoSocket = ZmqHandler.getInstance().getContext().createSocket(ZMQ.PUSH);
		m_oVideoSocket.bind(m_strVideoAddr);

		VideoHandler.getInstance().publishVideo(m_strVideoAddr);
		
		m_lblFPS = (TextView) i_oActivity.findViewById(R.id.lblFPS);
		
		start();
	}

    private int m_nFpsCounterPartner = 0;
    private long m_lLastTimePartner = System.currentTimeMillis();

    private boolean m_bDebug = true;
    
	@Override
	public void onFrame(byte[] rgb, int width, int height) {

		VideoMessage vmsg = new VideoMessage(m_oRomo.getName(), rgb);
		ZMsg zmsg = vmsg.toZmsg();
		zmsg.send(m_oVideoSocket);
		
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

	@Override
	public void shutDown() {
		VideoHandler.getInstance().unpublishVideo(m_strVideoAddr);
		m_oVideoSocket.close();
	}
	
	

}
