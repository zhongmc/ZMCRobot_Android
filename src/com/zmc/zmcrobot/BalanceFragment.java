package com.zmc.zmcrobot;

//import android.support.v4.app.Fragment;

import java.text.DecimalFormat;

import com.zmc.zmcrobot.ui.RobotSimulateView;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
//import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class BalanceFragment extends Fragment  {
	
	private final static String TAG = "BalanceFragment";
	private View mView;

	private double bkp = 30, bki = 0.0, bkd = 0.3;
	private double skp = 200, ski = 8, skd = 0;
	private double tkp = 50, tki = 0.14;
	
	private int max_pwm=150;
	private int pwm_zero = 0, pwm_diff = 0;
//	private double angleOff = 0;
//	private double wheelSyncKP = 3.4;
	private double atObstacle = 0.25, unsafe=0.1;
	
	private double wheelRadius = 0.0325, wheelDistance = 0.165;
	
	private boolean  mBeConnected = false;
	
	private boolean haveSpeedLoop = false, haveThetaLoop = false;
	
    ProgressDialog dialog = null;
    private int settingsReceived = 0;
    
//	private RadioGroup angleChoiceGroup;
    
	private Button mUploadPBButton, mResetPBButton,  mUploadPSButton, mResetPSButton, mUploadPMButton, mResetPMButton, mStartStopButton, mLoadButton; // , mStopButton;

	private Button mUploadPTButton, mResetPTButton;
	
	private CheckBox speedCheckBox, thetaCheckBox;
	
	private CheckBox[] curveSelections = new CheckBox[5];
	private double[] curveData = new double[5]; 
	//saCheckBox, kaCheckBox, eaCheckbox,pwmbCheckBox, pwmsCheckbox;
	
	private TextView bkpTextView, bkdTextView; // bkiTextView
	private TextView skpTextView, skiTextView;
	private TextView tkpTextView, tkiTextView;
	
//	private EditText wheelSyncKPEditText;
	
	private EditText wheelRadiusEditText, wheelDistanceEditText;
	
	private EditText atObstacleEditText, unsafeEditText, maxPWMEditText, pwmZeroEditText; //, pwmDiffEditText, angleOffEditText;
	
	private SeekBar bkpSeekBar,bkdSeekBar; // bkiSeekBar, 
	private SeekBar skpSeekBar, skiSeekBar;//, skdSeekBar;
	
	private SeekBar tkpSeekBar, tkiSeekBar;
	
	
    private DecimalFormat mDcmFmt = new DecimalFormat("#0.####");
	
	private boolean isOn = false;
	
	private CurveView mCurveView;
	
	private Settings mBalanceSettings = new Settings();
	
	
//	private double bkp = 30, bki = 0.0, bkd = 0.3;
//	private double skp = 200, ski = 8, skd = 0;
//	private double tkp = 50, tki = 0.14;
	
	private double m_bkp = 30, m_bkd = 0.3;
	private double m_skp = 200, m_ski = 8;
	private double m_tkp = 50, m_tki = 0.14;
	
	private Boolean isCreated = false;
	
//	private Settings mSpeedSettings = new Settings();
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		//Settings mSettings = MainActivity.mSettings;//new Settings();

		isCreated = false;
		
		View rootView = inflater.inflate(R.layout.fragment_balance,
				container, false);

		bkpTextView = (TextView)rootView.findViewById(R.id.bKPTextView);
//		bkiTextView = (TextView)rootView.findViewById(R.id.bKITextView);
		bkdTextView = (TextView)rootView.findViewById(R.id.bKDTextView);

		bkpTextView.setText(mDcmFmt.format(bkp));
//		bkiTextView.setText(mDcmFmt.format(bki));
		bkdTextView.setText(mDcmFmt.format(bkd));
		

		skpTextView = (TextView)rootView.findViewById(R.id.sKPTextView);
		skiTextView = (TextView)rootView.findViewById(R.id.sKITextView);
//		skdTextView = (TextView)rootView.findViewById(R.id.sKDTextView);

		skpTextView.setText(mDcmFmt.format(skp));
		skiTextView.setText(mDcmFmt.format(ski));
//		skdTextView.setText(mDcmFmt.format(skd));
	
		tkpTextView = (TextView)rootView.findViewById(R.id.tKPTextView);
		tkpTextView.setText( mDcmFmt.format(tkp) );
		
		tkiTextView = (TextView)rootView.findViewById(R.id.tKITextView);
		tkiTextView.setText(mDcmFmt.format(tki));
		
		
		speedCheckBox = (CheckBox)rootView.findViewById(R.id.speedCheckBox);
		thetaCheckBox = (CheckBox)rootView.findViewById(R.id.thetaCheckBox);
		
		curveSelections[0] = (CheckBox)rootView.findViewById(R.id.SACheckBox);
		curveSelections[1] = (CheckBox)rootView.findViewById(R.id.KACheckBox);
		curveSelections[2] = (CheckBox)rootView.findViewById(R.id.EACheckBox);
		curveSelections[3] = (CheckBox)rootView.findViewById(R.id.PWMBCheckBox);
		curveSelections[4] = (CheckBox)rootView.findViewById(R.id.PWMSCheckBox);
		
		
		curveSelections[0].setTextColor(Color.CYAN );
		curveSelections[1].setTextColor(Color.RED );
		curveSelections[2].setTextColor(Color.BLUE );
		curveSelections[3].setTextColor(Color.YELLOW );
		curveSelections[4].setTextColor(Color.GREEN );
		
		
		OnClickListener curSelectionListener = new OnClickListener(){
			public void onClick(View arg0) {
				setCurShow();
			}
		};
		
		for( int i=0; i<5; i++)
		{
			curveSelections[i].setChecked(true);
			curveSelections[i].setOnClickListener(curSelectionListener);
			
		}
		
//		colors[0] = Color.CYAN;
//		colors[1] = Color.RED;
//		colors[2] = Color.BLUE;
//		colors[3] = Color.YELLOW;
//		colors[4] = Color.GREEN;
//		colors[5] = Color.WHITE;
		
//		for( int i=0; i<5; i++)
//		{
//			curveSelections[i].setChecked( true );
//			curveSelections[i].setOnCheckedChangeListener(mCheckedChangelistener);
//		}
	
		speedCheckBox.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				SetSpeedLoop(speedCheckBox.isChecked());
				
			}
			
		});
		
			
		thetaCheckBox.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				SetThetaLoop(thetaCheckBox.isChecked());
				
			}
			
		});
		
		speedCheckBox.setEnabled( false );
		thetaCheckBox.setEnabled( false );
		
	
		bkpSeekBar = (SeekBar)rootView.findViewById(R.id.bKPSeekBar);
		bkpSeekBar.setMax(600);
		bkpSeekBar.setProgress( (int)(bkp*10));
		
//		bkiSeekBar = (SeekBar)rootView.findViewById(R.id.bKISeekBar);
//		bkiSeekBar.setMax(100);
//		bkiSeekBar.setProgress( (int)(bki*10));

		bkdSeekBar = (SeekBar)rootView.findViewById(R.id.bKDSeekBar);
		bkdSeekBar.setMax(100);
		bkdSeekBar.setProgress( (int)(bkd*100));
		

		skpSeekBar = (SeekBar)rootView.findViewById(R.id.sKPSeekBar);
		skpSeekBar.setMax(3000);
		skpSeekBar.setProgress( (int)(skp*10));

		skiSeekBar = (SeekBar)rootView.findViewById(R.id.sKISeekBar);
		skiSeekBar.setMax(3000);
		skiSeekBar.setProgress( (int)(ski*100));

		tkpSeekBar = (SeekBar)rootView.findViewById(R.id.tKPSeekBar);
		tkpSeekBar.setMax(300);
		tkpSeekBar.setProgress( (int)(tkp));
		
		tkiSeekBar = (SeekBar)rootView.findViewById(R.id.tKISeekBar);
		tkiSeekBar.setMax(150);
		tkiSeekBar.setProgress( (int)(tki*10));
		
		bkpSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
//		bkiSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
		bkdSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
		
		skpSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
		skiSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
//		skdSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );

		tkpSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
		
		tkiSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
//		atObstacleEditText, unsafeEditText, maxPWMEditText,
		
		maxPWMEditText = (EditText)rootView.findViewById(R.id.maxPWMEditText);
		maxPWMEditText.setText(String.valueOf(max_pwm));
		
		pwmZeroEditText = (EditText)rootView.findViewById(R.id.PWMZeroEditText);
		pwmZeroEditText.setText(String.valueOf(pwm_zero));

//		pwmDiffEditText = (EditText)rootView.findViewById(R.id.PWMDiffEditText);
//		pwmDiffEditText.setText(String.valueOf(pwm_diff));
//		
//		angleOffEditText = (EditText)rootView.findViewById(R.id.AngleOffEditText);
//		angleOffEditText.setText(mDcmFmt.format(angleOff));
		
		wheelRadiusEditText = (EditText)rootView.findViewById(R.id.editTextR);
		wheelRadiusEditText.setText(mDcmFmt.format(wheelRadius));
		wheelDistanceEditText = (EditText)rootView.findViewById(R.id.editTextL);
		wheelDistanceEditText.setText(mDcmFmt.format(wheelDistance));
//		wheelSyncKPEditText = (EditText)rootView.findViewById(R.id.WheelSyncKPEditText);
//		wheelSyncKPEditText.setText(mDcmFmt.format(wheelSyncKP));
		
		atObstacleEditText  = (EditText)rootView.findViewById(R.id.atObstacleEditText);
		atObstacleEditText.setText(mDcmFmt.format(atObstacle));
	
		unsafeEditText = (EditText)rootView.findViewById(R.id.unsafeEditText);
		unsafeEditText.setText(mDcmFmt.format(atObstacle));
		
		mCurveView = (CurveView)rootView.findViewById(R.id.curveView);
		
		mCurveView.setCurveCount(5);
		
		
		mStartStopButton = (Button) rootView.findViewById(R.id.StartButton);
		
		
		mStartStopButton.setEnabled(false);

		mUploadPBButton = (Button) rootView.findViewById(R.id.uploadPBButton);
		mResetPBButton = (Button) rootView.findViewById(R.id.resetPBButton);
		mUploadPBButton.setEnabled(false);
		mResetPBButton.setEnabled(true);

		mUploadPSButton = (Button) rootView.findViewById(R.id.uploadPSButton);
		mResetPSButton = (Button) rootView.findViewById(R.id.resetPSButton);
		mUploadPSButton.setEnabled(false);
		
		
		mUploadPMButton = (Button) rootView.findViewById(R.id.uploadPMButton);
		mResetPMButton = (Button) rootView.findViewById(R.id.resetPMButton);
		mUploadPMButton.setEnabled(false);

		
		mUploadPTButton = (Button) rootView.findViewById(R.id.uploadPTButton);
		mResetPTButton = (Button) rootView.findViewById(R.id.resetPTButton);
		mUploadPTButton.setEnabled(false);
		
		mLoadButton = (Button)rootView.findViewById(R.id.LoadSettingsButton);
		mLoadButton.setEnabled( false );
		
		
		mLoadButton.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				loadSettings();
				
			}
		});
		mUploadPBButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSettings(2);
			}
		});

		mResetPBButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSettings(2);
			}
		});
		
		
		mUploadPSButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSettings(3);
			}
		});

		mResetPSButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSettings(3);
			}
		});
		
		mUploadPTButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSettings(4);
			}
		});

		mResetPTButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSettings(4);
			}
		});
		
		
		mUploadPMButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSettings(6);
			}
		});

		mResetPMButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSettings(6);
			}
		});


		
		mStartStopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isOn) {
					startRobot();
					mStartStopButton.setText(R.string.stop);
					isOn = true;
				} else {
					stopRobot();
					mStartStopButton.setText(R.string.startLabel);
					isOn = false;
				}
			}
		});


		if (mBeConnected) {
			mStartStopButton.setEnabled(true);

			if( isOn )
				mStartStopButton.setText(R.string.stop);
			else
				mStartStopButton.setText(R.string.startLabel);
			
			mResetPBButton.setEnabled(true);
			mUploadPBButton.setEnabled(true);
			mResetPMButton.setEnabled(true);
			mUploadPMButton.setEnabled(true);
			mLoadButton.setEnabled(true);
			speedCheckBox.setEnabled(true);
			thetaCheckBox.setEnabled( true );
			mUploadPTButton.setEnabled(true);
			
			
		} else {
			mStartStopButton.setEnabled(false);

			if( isOn )
				mStartStopButton.setText(R.string.stop);
			else
				mStartStopButton.setText(R.string.startLabel);

			mResetPBButton.setEnabled(true);
			mUploadPBButton.setEnabled(false);
			mUploadPMButton.setEnabled(false);
			mLoadButton.setEnabled(false);
			speedCheckBox.setEnabled(false);
			thetaCheckBox.setEnabled( false );
			mUploadPTButton.setEnabled(false);


		}		
		
		mView = rootView;
		
		isCreated = true;
		updateBLEState( mBeConnected );
		
		return rootView;
	}

	private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int arg1, boolean arg2) {
			
			if( seekBar == bkpSeekBar )
			{
				bkp = seekBar.getProgress()/10.0; // / 10.0;
				bkpTextView.setText(mDcmFmt.format( bkp));
				
			}
			else if(seekBar == bkdSeekBar  )
			{
				bkd = seekBar.getProgress() / 100.0;
				bkdTextView.setText(mDcmFmt.format( bkd));
				
			}
	
			else if( seekBar == skpSeekBar )
			{
				skp = seekBar.getProgress()/10.0;// / 10.0;
				skpTextView.setText(mDcmFmt.format( skp));
				
			}
			else if(seekBar == skiSeekBar  )
			{
				ski = seekBar.getProgress()/100.0;// / 100.0;
				skiTextView.setText(mDcmFmt.format( ski));
			}
			else if(seekBar == tkpSeekBar  )
			{
				tkp = seekBar.getProgress();
				tkpTextView.setText(mDcmFmt.format( tkp));
				
			}
			else if( seekBar == tkiSeekBar )
			{
				tki = seekBar.getProgress()/10.0;
				tkiTextView.setText(mDcmFmt.format( tki));
			}
			
						
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}
		
		
	};
	
	
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


	private void startRobot() {
		byte[] value = new byte[3];

		value[0] = 'G';
		value[1] = 'B';

		 MainActivity act = (MainActivity)getActivity();
		 act.SendBLECmd(value);

	}

	
	//stop balance
	private void stopRobot() {
		byte[] value = new byte[3];

		value[0] = 'B';
		value[1] = 'S';

		 MainActivity act = (MainActivity)getActivity();
		 act.SendBLECmd(value);

	}
	
	private void sendSettings(int settingsType )
	{
		 MainActivity act = (MainActivity)getActivity();
		 
		 Settings settings = new Settings();
		 settings.settingsType = settingsType;
		 
		 if( settingsType == 2 )
		 {
			 settings.kp = bkp;
			 settings.ki = bki;
			 settings.kd = bkd;
		 }
		 else if( settingsType == 3)
		 {
			 settings.kp = skp;
			 settings.ki = ski;
			 settings.kd = skd;

		 }
		 else if( settingsType == 4)
		 {
			 settings.kp = tkp;
			 settings.ki = tki;
			 settings.kd = 0;
			 
		 }
		 else
		 {
			 max_pwm = Integer.valueOf( maxPWMEditText.getText().toString());
			 pwm_zero = Integer.valueOf(pwmZeroEditText.getText().toString());
//			 pwm_diff = Integer.valueOf(pwmDiffEditText.getText().toString());
			 atObstacle = Double.valueOf(atObstacleEditText.getText().toString());
			 unsafe = Double.valueOf(unsafeEditText.getText().toString());
			 
//			 angleOff = Double.valueOf(angleOffEditText.getText().toString());
//			 wheelSyncKP = Double.valueOf(wheelSyncKPEditText.getText().toString());

			 wheelRadius = Double.valueOf(wheelRadiusEditText.getText().toString());
			 wheelDistance = Double.valueOf(wheelDistanceEditText.getText().toString());
			 
			 settings.pwm_diff = pwm_diff;
			 settings.max_pwm = max_pwm;
			 settings.atObstacle = atObstacle;
			 settings.unsafe = unsafe;
			 settings.pwm_zero = pwm_zero;
//			 settings.angleOff = angleOff;
			 settings.wheelRadius = wheelRadius;
			 settings.wheelDistance = wheelDistance;
//			 settings.wheelSyncKP = wheelSyncKP;
		 }
		 
		 act.SendSettings(settings);
	}

	private void loadSettings() {

		this.startGetSettingsDialog();
		settingsReceived = 0;
		
		 MainActivity act = (MainActivity)getActivity();
		byte[] value = new byte[3];
		value[0]='R';
		value[1]='P';
		value[2]=2;
		act.SendBLECmd(value);

		value = new byte[3];
		value[0]='R';
		value[1]='P';
		value[2]=3;
		act.SendBLECmd(value);

		value = new byte[3];
		value[0]='R';
		value[1]='P';
		value[2]=4;
		act.SendBLECmd(value);
		
		value = new byte[3];
		value[0]='R';
		value[1]='P';
		value[2]=6;
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
		if( !isCreated )
			return;
		mCurveView.addData(state.obstacles);
		
	}
	
	
	private void setCurShow()
	{
		boolean curShow[] = new boolean[5];
		for( int i =0; i<5; i++)
			curShow[i] = curveSelections[i].isChecked();
		
		mCurveView.showCurve(curShow);
		
	}	



	public void SetSpeedLoop(boolean val) {
		Log.i(TAG, "Set speed loop to " + val);
		byte[] value = new byte[4];

		value[0] = 'C';
		value[1] = 'L';
		value[2] = 0;
		if (val)
			value[3] = 1;
		else
			value[3] = 0;

		 MainActivity act = (MainActivity)getActivity();
		 act.SendBLECmd(value);
		 haveSpeedLoop = val;
	}

	public void SetThetaLoop(boolean val){
		Log.i(TAG, "Set theta loop to " + val);
		byte[] value = new byte[4];

		value[0] = 'C';
		value[1] = 'L';
		value[2] = 1;
		if (val)
			value[3] = 1;
		else
			value[3] = 0;

		 MainActivity act = (MainActivity)getActivity();
		 act.SendBLECmd(value);
		 haveThetaLoop = val;
		
	}
	
	public void setBeConnected(boolean beConnected )
	{
		mBeConnected = beConnected;
		if( this.mView != null )
			this.updateBLEState(beConnected);
	}

	
	public void updateBLEState(boolean bConnected) {
		
		mBeConnected = bConnected;
		
		if( !isCreated )
			return;
		
		if( mStartStopButton == null )
			return;
		
		if (bConnected) {
			mStartStopButton.setEnabled(true);

			if( isOn )
				mStartStopButton.setText(R.string.stop);
			else
				mStartStopButton.setText(R.string.startLabel);
			
			mResetPBButton.setEnabled(true);
			mUploadPBButton.setEnabled(true);

			mResetPSButton.setEnabled(true);
			mUploadPSButton.setEnabled(true);

			mUploadPTButton.setEnabled(true);

			mResetPMButton.setEnabled(true);
			mUploadPMButton.setEnabled(true);
			mLoadButton.setEnabled(true);
			speedCheckBox.setEnabled(true);
			thetaCheckBox.setEnabled( true );
			
			
		} else {
			mStartStopButton.setEnabled(false);

			if( isOn )
				mStartStopButton.setText(R.string.stop);
			else
				mStartStopButton.setText(R.string.startLabel);

			mResetPBButton.setEnabled(true);
			mUploadPBButton.setEnabled(false);

			mUploadPSButton.setEnabled(false);

			mUploadPMButton.setEnabled(false);
			mLoadButton.setEnabled(false);
			speedCheckBox.setEnabled(false);
			thetaCheckBox.setEnabled( false );
			mUploadPTButton.setEnabled(false);

		}
	}



	public void updateSettings(Settings settings ) {

		settingsReceived++;
		if( settingsReceived >= 3 )
		{
			if( dialog != null )
			{
				dialog.dismiss();
				dialog = null;
			}
		}
		Log.i(TAG, "Update settings: " + settings.settingsType);
		if( settings.settingsType == 6)
		{
			mBalanceSettings.settingsType = settings.settingsType;
			mBalanceSettings.copyFrom( settings );
			
		}
		
		else if( settings.settingsType == 2)
		{
			m_bkp = settings.kp;
			m_bkd = settings.kd;
		}
		
		else if( settings.settingsType == 3)
		{
			m_skp = settings.kp;
			m_ski = settings.ki;
		}
		else if( settings.settingsType == 4)
		{
			m_tkp = settings.kp;
			m_tki = settings.ki;
		}
		
		resetSettings(settings.settingsType);
		
//		Settings mSettings = MainActivity.mSettings;//new Settings();
	}
	
	private void resetSettings(int settingsType)
	{
		if( settingsType == 2 )
		{
			bkp =  m_bkp; 
			bkd = m_bkd;//0.5;

			bkpTextView.setText(mDcmFmt.format(bkp));
			bkdTextView.setText(mDcmFmt.format(bkd));

			bkpSeekBar.setProgress( (int)(bkp*10));
			bkdSeekBar.setProgress( (int)(bkd*100));
			
			
		}
		else if( settingsType == 3 )
		{
			skp = m_skp;//5;
			ski = m_ski;//0.1;
			
			skpTextView.setText(mDcmFmt.format(skp));
			skiTextView.setText(mDcmFmt.format(ski));
			skpSeekBar.setProgress( (int)(skp*10));
			skiSeekBar.setProgress( (int)(ski*100));
			
			Log.i(TAG, "Update settings:" + skp);
		}
		else if( settingsType == 4 )
		{
			tkp = m_tkp;//5;
			tki = m_tki;
			
			tkpTextView.setText(mDcmFmt.format(tkp));

			tkpSeekBar.setProgress( (int)(tkp));
			
			tkiTextView.setText(mDcmFmt.format(tki));
			tkiSeekBar.setProgress( (int)(tki*10));
			
			Log.i(TAG, "Update settings:" + tkp);
		}
		
		if( settingsType == 6 || settingsType == 0)
		{
			max_pwm = mBalanceSettings.max_pwm;
			pwm_zero = mBalanceSettings.pwm_zero;
			pwm_diff = mBalanceSettings.pwm_diff;
			
			atObstacle = mBalanceSettings.atObstacle;
			unsafe = mBalanceSettings.unsafe;
			
			wheelRadius = mBalanceSettings.wheelRadius;
			wheelDistance = mBalanceSettings.wheelDistance;
			
//			angleOff = mBalanceSettings.angleOff;

			maxPWMEditText.setText(String.valueOf(max_pwm));
			pwmZeroEditText.setText(String.valueOf(pwm_zero));
//			pwmDiffEditText.setText(String.valueOf(pwm_diff));
			
			atObstacleEditText.setText(mDcmFmt.format(atObstacle));
			unsafeEditText.setText(mDcmFmt.format(unsafe));
			
//			angleOffEditText.setText(mDcmFmt.format(angleOff));
			
			wheelRadiusEditText.setText(mDcmFmt.format(wheelRadius));
			wheelDistanceEditText.setText(mDcmFmt.format(wheelDistance));
			
			this.speedCheckBox.setChecked( mBalanceSettings.speedLoop );
			this.thetaCheckBox.setChecked( mBalanceSettings.thetaLoop );
//			this.simulateCheckBox.setChecked( mBalanceSettings.simulateMode );
		}
	
		
	}


	static int colors[] = {Color.CYAN,Color.RED,Color.BLUE,Color.YELLOW, Color.GREEN  };

	
	private android.widget.CompoundButton.OnCheckedChangeListener mCheckedChangelistener = new android.widget.CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {

			int curveCount = 0;
			int cols[] = new int[5];
			
			for( int i=0; i<5; i++)
			{
				if( curveSelections[i].isChecked() )
				{
					cols[curveCount] = colors[i];
					curveCount++;
				}
			}
			mCurveView.setCurveCount( curveCount);
			mCurveView.setCurveColor(cols);
		}
		
	};
	

	public void setAngleToUse(int val )
	{
		Log.i(TAG, "change angle type to: " + val);
		byte[] value = new byte[4];
		
		value[0] = 'A';
		value[1] = 'S';
		value[2] = (byte)val;
		
		MainActivity act = (MainActivity)getActivity();
		act.SendBLECmd(value);		
	}
	
	public void SetCheckBattleMode( boolean sm )
	{
		Log.i(TAG, "Set check battle  mode " + sm);
		byte[] value = new byte[4];
		
		value[0] = 'C';
		value[1] = 'B';
		
		if( sm )
			value[2] = 1;
		else
			value[2] = 0;
		
		
		
		MainActivity act = (MainActivity)getActivity();
		act.SendBLECmd(value);
		
	}
	
	
    private void startGetSettingsDialog()
    {
    
    	if( dialog != null )
    		return;
    	
	
    	dialog = new ProgressDialog(getActivity());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
        dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        dialog.setIcon(R.drawable.ic_launcher);//
        // 设置提示的title的图标，默认是没有的，如果没有设置title的话只设置Icon是不会显示图标的
        dialog.setTitle("提示");
        // dismiss监听
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                // TODO Auto-generated method stub
            	//dialog = null;
            }
        });

        // 监听cancel事件
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
  //          	startSettingsActivity();
            	dialog = null;
            }
        });
//        //设置可点击的按钮，最多有三个(默认情况下)
//        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定",
//                new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // TODO Auto-generated method stub
//
//                    }
//                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	dialog = null;
//                    	startSettingsActivity();
                    }
                });

        dialog.setMessage("Loading settings...");
        dialog.show();
    	
    	
    }
    	
	
}
