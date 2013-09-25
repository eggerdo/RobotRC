/**
* 456789------------------------------------------------------------------------------------------------------------120
*
* @brief: Robot RC application
* @file: RobotRCActivity.java
*
* @desc:  	Main Activity for the Robot RC application. choose between robot and user mode.
*
*
* Copyright (c) 2013 Dominik Egger <dominik@dobots.nl>
*
* @author:		Dominik Egger
* @date:		30.08.2013
* @project:		RobotRC
* @company:		Distributed Organisms B.V.
*/
package org.dobots.robotrc;


import org.dobots.communication.zmq.ZmqActivity;
import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.communication.zmq.ZmqSettings;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.RTFUtils;
import org.dobots.utilities.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.BufferType;

public class RobotRCActivity extends ZmqActivity {

	private static final String TAG = "RoboTalk";

	private static Activity CONTEXT;

	// general

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setProperties();

        CONTEXT = this;
        Utils.setContext(CONTEXT);
        
        fillText();
        writeChangeLog();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	mZmqHandler.onDestroy();
    }

    private void setProperties() {
        setContentView(R.layout.main);

		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		ImageButton btnRobot = (ImageButton) findViewById(R.id.btnRobot);
		btnRobot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RobotRCActivity.this, RobotRC_Robot.class);
				startActivity(intent);
			}
		});
		
		ImageButton btnUser = (ImageButton) findViewById(R.id.btnUser);
		btnUser.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RobotRCActivity.this, RobotRC_User.class);
				startActivity(intent);
			}
		});
    }

	public static Activity getActivity() {
		return CONTEXT;
	}
	
//	@Override
//    public Dialog onCreateDialog(int id) {
//    	return m_oZmqHandler.getSettings().onCreateDialog(this, id);
//    }
//    
//	@Override
//	protected void onPrepareDialog(int id, Dialog dialog) {
//		m_oZmqHandler.getSettings().onPrepareDialog(id, dialog);
//	}

	private void fillText() {
		String[] rgUsecases = getResources().getStringArray(R.array.usecases);
		
		SpannableString title = new SpannableString(getResources().getString(R.string.intro) + "\n\n");
		title.setSpan(new RelativeSizeSpan(1.3f), 0, title.length(), 0);
		CharSequence text = title;
		
		text = RTFUtils.recursive(text, rgUsecases);
		
		TextView intro = (TextView) findViewById(R.id.txtIntro);
		intro.setText(text, BufferType.SPANNABLE);
	}
	
	private void writeChangeLog() {
		String strVersion;
		try {
			strVersion = "Version " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			strVersion = "?";
		}
		String[] rgstrChangelog = getResources().getStringArray(R.array.changelog);
		
		// write title together with the version and increase the text size slightly
		SpannableString title = new SpannableString("Changelog " + strVersion + "\n\n");
		title.setSpan(new RelativeSizeSpan(1.3f), 0, title.length(), 0);
		CharSequence text = title;
	
		// assemble the rest of the changelog
		text = RTFUtils.recursive(text, rgstrChangelog);

		// write everything into the textview
        TextView changelog = (TextView) findViewById(R.id.lblChangeLog);
        changelog.setText(text, BufferType.SPANNABLE);
	}

	@Override
	public void onZmqReady() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onZmqFailed() {
		showToast("Failed to set-up ZeroMQ, make sure your settings are correct!", Toast.LENGTH_LONG);
	}

}