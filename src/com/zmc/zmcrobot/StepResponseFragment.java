package com.zmc.zmcrobot;

//import android.support.v4.app.Fragment;

import java.text.DecimalFormat;

import com.zmc.zmcrobot.ui.RobotSimulateView;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class StepResponseFragment extends Fragment  {
	
	private final static String TAG = "StepResponseFragment";
	private View mView;

	private int pwm=90;
	
	private Button mStartButton, mStopButton;
	private double[] curveData = new double[5]; 
	
	private EditText pwmEditText;
	
    private DecimalFormat mDcmFmt = new DecimalFormat("#0.00");
	
	private boolean isOn = false;
	
	private CurveView mCurveView;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		//Settings mSettings = MainActivity.mSettings;//new Settings();


		View rootView = inflater.inflate(R.layout.fragment_step_response,
				container, false);

//		colors[0] = Color.CYAN;
//		colors[1] = Color.RED;
//		colors[2] = Color.BLUE;
//		colors[3] = Color.YELLOW;
//		colors[4] = Color.GREEN;
//		colors[5] = Color.WHITE;
		

		

		pwmEditText = (EditText)rootView.findViewById(R.id.PWMEditText);
		
		mCurveView = (CurveView)rootView.findViewById(R.id.curveView);
		
		mCurveView.setCurveCount(5);
		
		
		mStartButton = (Button) rootView.findViewById(R.id.startButton);
		mStartButton.setEnabled(false);

		mStopButton = (Button) rootView.findViewById(R.id.stopButton);
		mStopButton.setEnabled(false);

	
		
		mStartButton.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startStepResponse();
				mStopButton.setEnabled(true);
				mStartButton.setEnabled(false);
				
			}
		});
		mStopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopStepResponse();
				mStopButton.setEnabled(false);
				mStartButton.setEnabled(true);
			}
		});

		mView = rootView;
		return rootView;
	}


	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(TAG, "onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "onCreate");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(TAG, "onActivityCreated");
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i(TAG, "onStop");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.i(TAG, "onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i(TAG, "onDetach");
	}


	private void startStepResponse() {
		byte[] value = new byte[3];

		value[0] = 'S';
		value[1] = 'R';

		 MainActivity act = (MainActivity)getActivity();
		 act.SendBLECmd(value);

	}

	private void stopStepResponse() {
		byte[] value = new byte[3];

		value[0] = 'S';
		value[1] = 'T';

		 MainActivity act = (MainActivity)getActivity();
		 act.SendBLECmd(value);

	}
	

	
	private void intToByte(int value, byte[] buf, int offset) {
		boolean sig = false;
		if (value < 0) {
			value = -value;
			sig = true;
		}
		buf[offset] = (byte) (value & 0xff);
		buf[offset + 1] = (byte) ((value >> 8) & 0x7f);

		if (sig)
			buf[offset + 1] = (byte) (buf[offset + 1] | 0x80);

	}


	public void setRobotState(RobotState state) {
		
		int idx = 0;
		for( int i=0; i<5; i++ )
		{
//			if( curveSelections[i].isChecked())
				this.curveData[idx++] = state.obstacles[i];
		}
		
		this.mCurveView.addData(curveData);
	}


	public void updateBLEState(boolean bConnected) {
		if (bConnected) {
			mStartButton.setEnabled(true);
			mStopButton.setEnabled(false);
			isOn = false;
		} else {
			mStartButton.setEnabled(false);
			mStopButton.setEnabled(false);
			isOn = false;

		}
	}


	

}
