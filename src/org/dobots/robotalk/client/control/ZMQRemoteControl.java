package org.dobots.robotalk.client.control;

import org.dobots.robotalk.client.ZmqHandler;
import org.dobots.robotalk.client.msg.RoboCommands;
import org.dobots.robotalk.client.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.client.msg.RoboCommands.DriveCommand;
import org.dobots.robotalk.client.msg.RobotMessage;
import org.dobots.robotalk.client.robots.IRobotDevice;
import org.dobots.robotalk.client.utility.Utils;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import android.app.Activity;

public class ZMQRemoteControl extends RemoteControlHelper {
	
	private ZMQ.Socket m_oCmdReceiver;
	
	private CommandReceiveThread m_oReceiver;
	
	public ZMQRemoteControl(Activity i_oActivity, IRobotDevice i_oRobot,
			IRemoteControlListener i_oListener) {
		super(i_oActivity, i_oRobot, i_oListener);
		
		m_oCmdReceiver = ZmqHandler.getInstance().createSocket(ZMQ.PULL);
		m_oCmdReceiver.connect(String.format("ipc://%s/remotectrl", ZmqHandler.getInstance().getIPCpath()));
//		m_oCmdReceiver.connect("inproc://remote");
		
		m_oReceiver = new CommandReceiveThread();
		m_oReceiver.start();
	}

	class CommandReceiveThread extends Thread {

		public boolean bRun = true;
		
		@Override
		public void run() {
			while(bRun) {
				try {
					ZMsg oZMsg = ZMsg.recvMsg(m_oCmdReceiver);
					if (oZMsg != null) {
						// create a chat message out of the zmq message
						RobotMessage oCmdMsg = RobotMessage.fromZMsg(oZMsg);
						
						String strJson = Utils.byteArrayToString(oCmdMsg.data);
						BaseCommand oCmd = RoboCommands.decodeCommand(strJson);
						
						if (oCmd instanceof DriveCommand) {
							DriveCommand oDriveCmd = (DriveCommand)oCmd;
							m_oRemoteControlListener.onMove(oDriveCmd.eMove, oDriveCmd.dblSpeed, oDriveCmd.dblAngle);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	

}
