package org.dobots.robotalk.client.control;

import org.dobots.robotalk.client.ZmqHandler;
import org.dobots.robotalk.client.ZmqSettings;
import org.dobots.robotalk.client.msg.RoboCommands;
import org.dobots.robotalk.client.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.client.msg.RobotMessage;
import org.dobots.robotalk.client.utility.Utils;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

public class CommandHandler {

	private ZContext m_oZContext;
	private ZmqSettings m_oSettings;
	
	// the channel on which messages are sent out
	private Socket m_oCmdPublisher = null;
	// the channel on which messages are coming in
	private Socket m_oCmdSubscriber = null;
	
	// thread handling the message reception
	private CommandReceiveThread m_oRecvThread;
	
	private boolean m_bConnected;
	private ICommandReceiveListener m_oListener;
	
	public CommandHandler(ZmqHandler i_oZmqHandler) {
		m_oZContext = i_oZmqHandler.getContext();
		m_oSettings = i_oZmqHandler.getSettings();
	}
	
	public void setupConnections() {

		m_oCmdPublisher = m_oZContext.createSocket(ZMQ.PUSH);
		m_oCmdSubscriber = m_oZContext.createSocket(ZMQ.SUB);

		// obtain chat ports from settings
		// receive port is always equal to send port + 1
		int nCommandSendPort = m_oSettings.getCommandPort();
		int nCommandRecvPort = nCommandSendPort + 1;
		
		m_oCmdPublisher.connect(String.format("tcp://%s:%d", m_oSettings.getAddress(), nCommandSendPort));
		m_oCmdSubscriber.connect(String.format("tcp://%s:%d", m_oSettings.getAddress(), nCommandRecvPort));

		// subscribe to messages which are targeted at us directly
		m_oCmdSubscriber.subscribe("".getBytes());

		m_oRecvThread = new CommandReceiveThread();
		m_oRecvThread.start();
		
		m_bConnected = true;
		
	}

	public void closeConnections() {

		if (m_oRecvThread != null) {
			m_oRecvThread.bRun = false;
			m_oRecvThread.interrupt();
			m_oRecvThread = null;
		}
		
		if (m_oCmdPublisher != null) {
			m_oCmdPublisher.close();
			m_oCmdPublisher = null;
		}
		
		if (m_oCmdSubscriber != null) {
			m_oCmdSubscriber.close();
			m_oCmdSubscriber = null;
		}

		m_bConnected = false;
	}

	class CommandReceiveThread extends Thread {

		public boolean bRun = true;
		
		@Override
		public void run() {
			while(bRun) {
				try {
					ZMsg oZMsg = ZMsg.recvMsg(m_oCmdSubscriber);
					if (oZMsg != null) {
						// create a chat message out of the zmq message
						RobotMessage oCmdMsg = RobotMessage.fromZMsg(oZMsg);
						
						String strJson = Utils.byteArrayToString(oCmdMsg.data);
						BaseCommand oCmd = RoboCommands.decodeCommand(strJson);
						if (m_oListener != null) {
							m_oListener.onCommandReceived(oCmd);
						}
						
//						if (!oChatMsg.robotName.equals(m_oSettings.getRobotName())) {
//							// double check if the message is for us
//							continue;
//						}
	
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void sendCommand(BaseCommand i_oCmd) {
		if (m_bConnected) {
			String strJSON = i_oCmd.toJSONString();
			RobotMessage oMsg = new RobotMessage(m_oSettings.getRobotName(), strJSON.getBytes());
			ZMsg oZMsg = oMsg.toZmsg();
			oZMsg.send(m_oCmdPublisher);
		}
	}

	public void setReceiveListener(ICommandReceiveListener i_oListener) {
		m_oListener = i_oListener;
	}

}
