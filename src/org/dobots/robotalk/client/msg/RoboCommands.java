package org.dobots.robotalk.client.msg;

import org.dobots.robotalk.client.control.RemoteControlHelper;
import org.dobots.robotalk.client.msg.RoboCommandTypes.Request;
import org.json.JSONException;
import org.json.JSONObject;

public class RoboCommands {

	public static int TRANSACTION_ID = 0;
	private static RoboCommands INSTANCE = null;
	
	public static RoboCommands getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new RoboCommands();
		}
		return INSTANCE;
	}
	
	public static BaseCommand decodeCommand(String i_strJson) {
		
		JSONObject oJSON;
		try {
			oJSON = new JSONObject(i_strJson);
			JSONObject header = oJSON.getJSONObject(RoboCommandTypes.S_HEADER);
			
			switch(header.getInt(RoboCommandTypes.F_HEADER_ID)) {
			case RoboCommandTypes.DRIVE_COMMAND:
				return getInstance().new DriveCommand(oJSON);
			default: 
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public abstract class BaseCommand {
		int nHeaderID;
		int nTransactionID;
		long lTimeStamp;
		
		public BaseCommand(int i_nID) {
			nHeaderID = i_nID;
			nTransactionID = TRANSACTION_ID++;
			lTimeStamp = System.currentTimeMillis();
		}
		
		public BaseCommand(JSONObject i_oObj) throws JSONException {
			JSONObject header = i_oObj.getJSONObject(RoboCommandTypes.S_HEADER);
			nHeaderID 		= header.getInt(RoboCommandTypes.F_HEADER_ID);
			nTransactionID 	= header.getInt(RoboCommandTypes.F_TID);
			lTimeStamp 		= header.getLong(RoboCommandTypes.F_TIMESTAMP);
		}
		
		public JSONObject toJSON() {
			JSONObject obj = new JSONObject();
			JSONObject header = new JSONObject();
			try {
				header.put(RoboCommandTypes.F_HEADER_ID, 	nHeaderID);
				header.put(RoboCommandTypes.F_TID, 		nTransactionID);
				header.put(RoboCommandTypes.F_TIMESTAMP, 	lTimeStamp);
				obj.put(RoboCommandTypes.S_HEADER, header);
				return obj;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		public String toJSONString() {
			return toJSON().toString();
		}
		
	}
	
	public class DriveCommand extends BaseCommand {
		
		public RemoteControlHelper.Move eMove;
		public double dblSpeed;
		public double dblAngle;
		
		public DriveCommand(RemoteControlHelper.Move i_eMove, double i_dblSpeed, double i_dblAngle) {
			super(RoboCommandTypes.DRIVE_COMMAND);
			
			this.eMove = i_eMove;
			this.dblSpeed = i_dblSpeed; 
			this.dblAngle = i_dblAngle;
		}
		
		public DriveCommand(JSONObject i_oObj) throws JSONException {
			super(i_oObj);
			
			JSONObject data = i_oObj.getJSONObject(RoboCommandTypes.S_DATA);
			eMove 		= RemoteControlHelper.Move.valueOf(data.getString(RoboCommandTypes.F_MOVE));
			dblSpeed 	= data.getDouble(RoboCommandTypes.F_SPEED);
			dblAngle 	= data.getDouble(RoboCommandTypes.F_ANGLE);
		}
		
		public JSONObject toJSON() {
			JSONObject obj = super.toJSON();
			JSONObject data = new JSONObject();
			try {
				data.put(RoboCommandTypes.F_MOVE, 	eMove.toString());
				data.put(RoboCommandTypes.F_SPEED, 	dblSpeed);
				data.put(RoboCommandTypes.F_ANGLE, 	dblAngle);
				obj.put(RoboCommandTypes.S_DATA, data);
				return obj;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	
	}
	
	public static DriveCommand createDriveCommand(RemoteControlHelper.Move i_eMove, double i_dblSpeed, double i_dblAngle) {
		return getInstance().new DriveCommand(i_eMove, i_dblSpeed, i_dblAngle);
	}
	
	public static DriveCommand createDriveCommand(RemoteControlHelper.Move i_eMove, double i_dblSpeed) {
		return getInstance().new DriveCommand(i_eMove, i_dblSpeed, 0);
	}
	
	public class CameraCommand extends BaseCommand {

		int nCameraID;
		boolean bOn;
		
		public CameraCommand(int i_nCameraID, boolean i_bOn) {
			super(RoboCommandTypes.CAMERA_COMMAND);
			
			nCameraID = i_nCameraID;
			bOn = i_bOn;
		}
		
		public CameraCommand(JSONObject i_oObj) throws JSONException {
			super(i_oObj);
			
			JSONObject data = i_oObj.getJSONObject(RoboCommandTypes.S_DATA);
			nCameraID 	= data.getInt(RoboCommandTypes.F_CAMERA_ID);
			bOn 		= data.getBoolean(RoboCommandTypes.F_ON);
		}
		
		public JSONObject toJSON() {
			JSONObject obj = super.toJSON();
			JSONObject data = new JSONObject();
			try {
				data.put(RoboCommandTypes.F_CAMERA_ID, 	nCameraID);
				data.put(RoboCommandTypes.F_ON, 		bOn);
				obj.put(RoboCommandTypes.S_DATA, data);
				return obj;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static CameraCommand createCameraCommand(int i_nCameraID, boolean i_bOn) {
		return getInstance().new CameraCommand(i_nCameraID, i_bOn);
	}
	
	public class ControlCommand extends BaseCommand {
		
		Request eRequest;
		
		public ControlCommand(Request i_eRequest) {
			super(RoboCommandTypes.CONTROL_COMMAND);
			
			eRequest = i_eRequest;
		}
		
		public ControlCommand(JSONObject i_oObj) throws JSONException {
			super(i_oObj);
			
			JSONObject data = i_oObj.getJSONObject(RoboCommandTypes.S_DATA);
			eRequest = (Request)data.get(RoboCommandTypes.F_REQUEST);
		}
		
		public JSONObject toJSON() {
			JSONObject obj = super.toJSON();
			JSONObject data = new JSONObject();
			try {
				data.put(RoboCommandTypes.F_REQUEST, eRequest);
				obj.put(RoboCommandTypes.S_DATA, data);
				return obj;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	}

}
