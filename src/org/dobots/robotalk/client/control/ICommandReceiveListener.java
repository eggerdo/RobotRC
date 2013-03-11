package org.dobots.robotalk.client.control;

import org.dobots.robotalk.client.msg.RoboCommands.BaseCommand;

public interface ICommandReceiveListener {
	
	public void onCommandReceived(BaseCommand i_oCmd);
	
}