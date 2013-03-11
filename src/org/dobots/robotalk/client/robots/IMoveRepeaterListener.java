package org.dobots.robotalk.client.robots;

import org.dobots.robotalk.client.robots.MoveRepeater.MoveCommand;

public interface IMoveRepeaterListener {
	
	void onDoMove(MoveCommand i_eMove, double i_dblSpeed);
	void onDoMove(MoveCommand i_eMove, double i_dblSpeed, int i_nRadius);

}
