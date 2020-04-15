package com.zmc.zmcrobot;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.os.Build;

public class SettingsActivity extends Activity {

	Button mOKButton, mCancelButton;
	
	EditText editTextKP, editTextKI, editTextKD;
	EditText editTextAtObstacle, editTextUnsafe, editTextDFW;
	EditText editTextVelocity;
	EditText editTextMaxRPM, editTextMinRPM;
	EditText editTextR, editTextL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		Settings settings = MainActivity.mSettings;
		
		Log.i("MainActivity", "Settings from main:" + settings);
		
		editTextKP = (EditText)findViewById(R.id.editTextKP);
		editTextKI = (EditText)findViewById(R.id.editTextKI);
		editTextKD = (EditText)findViewById(R.id.editTextKD);
		editTextAtObstacle = (EditText)findViewById(R.id.editTextObstacle);
		editTextUnsafe = (EditText)findViewById(R.id.editTextUnsafe);
		editTextDFW = (EditText)findViewById(R.id.editTextDFW);
		editTextVelocity = (EditText)findViewById(R.id.editTextVelocity);

		editTextMaxRPM = (EditText)findViewById(R.id.editTextMaxRPM);
		editTextMinRPM = (EditText)findViewById(R.id.editTextMinRPM);
		
		editTextR = (EditText)findViewById(R.id.editTextR);
		editTextL = (EditText)findViewById(R.id.editTextL);
		
		editTextKP.setText(String.valueOf(settings.kp));
		editTextKI.setText(String.valueOf(settings.ki));
		editTextKD.setText(String.valueOf(settings.kd));
		editTextAtObstacle.setText(String.valueOf(settings.atObstacle));
		editTextUnsafe.setText(String.valueOf(settings.unsafe));
		editTextDFW.setText(String.valueOf(settings.dfw));
		editTextVelocity.setText(String.valueOf(settings.velocity));

		
		editTextMaxRPM.setText(String.valueOf(settings.max_rpm));
		editTextMinRPM.setText(String.valueOf(settings.min_rpm));
		
		editTextR.setText( String.valueOf(settings.wheelRadius));
		editTextL.setText(String.valueOf(settings.wheelDistance));
		
		mOKButton = (Button)this.findViewById(R.id.uploadButton);
		mCancelButton = (Button) findViewById(R.id.StartButton);
		
		
		
		mOKButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				Settings settings = MainActivity.mSettings;
				
				settings.kp = Double.valueOf( editTextKP.getText().toString());
				settings.ki = Double.valueOf( editTextKI.getText().toString());
				settings.kd = Double.valueOf( editTextKD.getText().toString());
				settings.atObstacle = Double.valueOf( editTextAtObstacle.getText().toString());
				settings.unsafe = Double.valueOf( editTextUnsafe.getText().toString());
				settings.dfw = Double.valueOf( editTextDFW.getText().toString());
				settings.velocity = Double.valueOf( editTextVelocity.getText().toString());
				
				settings.max_rpm = Integer.valueOf(editTextMaxRPM.getText().toString());
				settings.min_rpm = Integer.valueOf(editTextMinRPM.getText().toString());

				settings.wheelRadius = Double.valueOf( editTextR.getText().toString());
				settings.wheelDistance = Double.valueOf( editTextL.getText().toString());
				
				// Set result and finish this Activity
		        setResult(Activity.RESULT_OK, null);
		        finish();
				
			}
			
		});
		
		mCancelButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
		         // Set result and finish this Activity
		        setResult(Activity.RESULT_CANCELED, null);
		        finish();
			}
			
		});

		
	}

}
