package org.dobots.robotalk.client.gui.robots.romo;

import org.dobots.robotalk.client.VideoMessage;
import org.dobots.robotalk.client.ZmqHandler;
import org.dobots.robotalk.client.gui.robots.BaseActivity;
import org.dobots.robotalk.client.gui.robots.SensorGatherer;
import org.dobots.utilities.CameraPreview.CameraPreviewCallback;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class RomoSensorGatherer extends SensorGatherer implements CameraPreviewCallback {

	private ZMQ.Socket videoSocket;

	public RomoSensorGatherer(BaseActivity i_oActivity) {
		super(i_oActivity);

		videoSocket = ZmqHandler.getInstance().getContext().createSocket(ZMQ.PUSH);
		videoSocket.connect("inproc://video");
	}

	@Override
	public void onFrame(byte[] rgb, int width, int height) {

		VideoMessage vmsg = new VideoMessage("Spykee", rgb);
		ZMsg zmsg = vmsg.toZmsg();
		zmsg.send(videoSocket);
		
	}
	
	

}
