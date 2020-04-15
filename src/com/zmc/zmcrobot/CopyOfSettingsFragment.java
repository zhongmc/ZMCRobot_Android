package com.zmc.zmcrobot;

//import android.support.v4.app.Fragment;

import java.text.DecimalFormat;

import com.zmc.zmcrobot.ui.RobotSimulateView;

import android.app.Activity;
import android.app.DialogFragment;
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

public class CopyOfSettingsFragment extends DialogFragment  {
	
	private final static String TAG = "SettingsFragment";
	private View mView;

	
//	private double kp = 38, ki = 0.0, kd = 0.58;
	
	private double bkp = 10, bki = 0.20, bkd = 0.1;
	private double skp = 5, ski = 0.5, skd = 0.1;
	private double tkp = 5, tki = 10, tkd = 0.1;
	
	private double m_bkp = 10, m_bki = 0.2, m_bkd = 0.1;
	private double m_skp = 5, m_ski = 0.5, m_skd = 0.1;
	private double m_tkp = 5, m_tki = 10, m_tkd = 0.1;
	

	
	private int max_pwm=150;
	private int pwm_zero = 0, pwm_diff = 0;
	private double angleOff = 0;

	private double atObstacle = 0.25, unsafe=0.1;
	private double wheelRadius = 0.0325, wheelDistance = 0.165;
	private double velocity;
	
	private double max_w; //w limitation; max
	
	private int max_rpm, min_rpm;
	private double dfw;
	
	private boolean mBeConnected = false;
	
    ProgressDialog dialog = null;
    private int settingsReceived = 0;
    
   
	private Button mUploadPBButton, mResetPBButton,  mUploadPSButton, mResetPSButton, mUploadPMButton, mResetPMButton, mUploadPTButton, mResetPTButton;
    
	private Button   mLoadButton; // , mStopButton;

	
	
	private TextView bkpTextView, bkdTextView, bkiTextView;
	private TextView skpTextView, skiTextView, skdTextView;
	private TextView tkpTextView, tkiTextView, tkdTextView;

	private SeekBar bkpSeekBar,bkdSeekBar, bkiSeekBar;
	private SeekBar skpSeekBar, skiSeekBar, skdSeekBar;
	private SeekBar tkpSeekBar, tkiSeekBar, tkdSeekBar;
	
//    private DecimalFormat mDcmFmt = new DecimalFormat("#0.0000");
	//	private TextView kpTextView, kdTextView, kiTextView;

	EditText editTextAtObstacle, editTextUnsafe, editTextDFW;
	EditText editTextMaxW;  //editTextVelocity,
	EditText editTextMaxRPM, editTextMinRPM;
	EditText editTextR, editTextL;
	
//	private SeekBar kpSeekBar,kdSeekBar, kiSeekBar;
	
	
    private DecimalFormat mDcmFmt = new DecimalFormat("#0.0000");
	
	private boolean isOn = false;
	
	private Settings mSettings = new Settings();
	
//	private double m_kp = 38, m_ki =0 , m_kd = 0.58;
	
//	private Settings mSpeedSettings = new Settings();
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//Settings mSettings = MainActivity.mSettings;//new Settings();
		View rootView = inflater.inflate(R.layout.fragment_settings,
				container, false);


		bkpTextView = (TextView)rootView.findViewById(R.id.bKPTextView);
		bkiTextView = (TextView)rootView.findViewById(R.id.bKITextView);
		bkdTextView = (TextView)rootView.findViewById(R.id.bKDTextView);

		bkpTextView.setText(mDcmFmt.format(bkp));
		bkiTextView.setText(mDcmFmt.format(bki));
		bkdTextView.setText(mDcmFmt.format(bkd));
		

		skpTextView = (TextView)rootView.findViewById(R.id.sKPTextView);
		skiTextView = (TextView)rootView.findViewById(R.id.sKITextView);
		skdTextView = (TextView)rootView.findViewById(R.id.sKDTextView);

		skpTextView.setText(mDcmFmt.format(skp));
		skiTextView.setText(mDcmFmt.format(ski));
		skdTextView.setText(mDcmFmt.format(skd));
	
		tkpTextView = (TextView)rootView.findViewById(R.id.tKPTextView);
		tkiTextView = (TextView)rootView.findViewById(R.id.tKITextView);
		tkdTextView = (TextView)rootView.findViewById(R.id.tKDTextView);

		tkpTextView.setText( mDcmFmt.format(tkp) );
		tkiTextView.setText(mDcmFmt.format(tki));
		tkdTextView.setText(mDcmFmt.format(tkd));
		
		getDialog().setTitle("Settings...");
	
		
		bkpSeekBar = (SeekBar)rootView.findViewById(R.id.bKPSeekBar);
		bkpSeekBar.setMax(200);
		bkpSeekBar.setProgress( (int)(bkp*10));
		
		bkiSeekBar = (SeekBar)rootView.findViewById(R.id.bKISeekBar);
		bkiSeekBar.setMax(500);
		bkiSeekBar.setProgress( (int)(bki*100));

		bkdSeekBar = (SeekBar)rootView.findViewById(R.id.bKDSeekBar);
		bkdSeekBar.setMax(100);
		bkdSeekBar.setProgress( (int)(bkd*100));
		

		skpSeekBar = (SeekBar)rootView.findViewById(R.id.sKPSeekBar);
		skpSeekBar.setMax(200);
		skpSeekBar.setProgress( (int)(skp*10));

		skiSeekBar = (SeekBar)rootView.findViewById(R.id.sKISeekBar);
		skiSeekBar.setMax(500);
		skiSeekBar.setProgress( (int)(ski*100));

		skdSeekBar = (SeekBar)rootView.findViewById(R.id.sKDSeekBar);
		skdSeekBar.setMax(100);
		skdSeekBar.setProgress( (int)(skd*100));
		
		tkpSeekBar = (SeekBar)rootView.findViewById(R.id.tKPSeekBar);
		tkpSeekBar.setMax(200);
		tkpSeekBar.setProgress( (int)(tkp*10));
		
		tkiSeekBar = (SeekBar)rootView.findViewById(R.id.tKISeekBar);
		tkiSeekBar.setMax(2000);
		tkiSeekBar.setProgress( (int)(tki*100));

		tkdSeekBar = (SeekBar)rootView.findViewById(R.id.tKDSeekBar);
		tkdSeekBar.setMax(100);
		tkdSeekBar.setProgress( (int)(tkd*100));
		
		bkpSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
		bkiSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
		bkdSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
		
		skpSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
		skiSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
		skdSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );

		tkpSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
		tkiSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );
		tkdSeekBar.setOnSeekBarChangeListener( mOnSeekBarChangeListener );

		
		

		editTextAtObstacle = (EditText)rootView.findViewById(R.id.editTextObstacle);
		editTextUnsafe = (EditText)rootView.findViewById(R.id.editTextUnsafe);
		editTextDFW = (EditText)rootView.findViewById(R.id.editTextDFW);
//		editTextVelocity = (EditText)rootView.findViewById(R.id.editTextVelocity);
		editTextMaxW = (EditText)rootView.findViewById(R.id.editTextMaxW);
		

		editTextMaxRPM = (EditText)rootView.findViewById(R.id.editTextMaxRPM);
		editTextMinRPM = (EditText)rootView.findViewById(R.id.editTextMinRPM);
		
		editTextR = (EditText)rootView.findViewById(R.id.editTextR);
		editTextL = (EditText)rootView.findViewById(R.id.editTextL);
		
		editTextAtObstacle.setText(String.valueOf(mSettings.atObstacle));
		editTextUnsafe.setText(String.valueOf(mSettings.unsafe));
		editTextDFW.setText(String.valueOf(mSettings.dfw));
//		editTextVelocity.setText(String.valueOf(mSettings.velocity));
		editTextMaxW.setText(String.valueOf(mSettings.max_w));
		
		editTextMaxRPM.setText(String.valueOf(mSettings.max_rpm));
		editTextMinRPM.setText(String.valueOf(mSettings.min_rpm));
		
		editTextR.setText( String.valueOf(mSettings.wheelRadius));
		editTextL.setText(String.valueOf(mSettings.wheelDistance));
		
		
		

		mUploadPBButton = (Button) rootView.findViewById(R.id.uploadPBButton);
		mResetPBButton = (Button) rootView.findViewById(R.id.resetPBButton);
		mUploadPBButton.setEnabled(false);
		mResetPBButton.setEnabled(true);

		mUploadPSButton = (Button) rootView.findViewById(R.id.uploadPSButton);
		mResetPSButton = (Button) rootView.findViewById(R.id.resetPSButton);
		mUploadPSButton.setEnabled(false);
		
		
		
		mUploadPTButton = (Button) rootView.findViewById(R.id.uploadPTButton);
		mResetPTButton = (Button) rootView.findViewById(R.id.resetPTButton);
		mUploadPTButton.setEnabled(false);

		
		mUploadPMButton = (Button) rootView.findViewById(R.id.uploadPMButton);
		mResetPMButton = (Button) rootView.findViewById(R.id.resetPMButton);
		mUploadPMButton.setEnabled(false);
		
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
		
		
//		mUploadPMButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				sendSettings(5);
//			}
//		});
//
//		mResetPMButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				resetSettings(5);
//			}
//		});

		
	
	
		
		mUploadPMButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSettings(5);
			}
		});

		mResetPMButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSettings(5);
			}
		});


		if (mBeConnected) {
			
			mUploadPBButton.setEnabled(true);
			mUploadPSButton.setEnabled(true);

			mUploadPTButton.setEnabled(true);
			mUploadPMButton.setEnabled(true);
			mLoadButton.setEnabled(true);

//			mResetPBButton.setEnabled(true);
//			mResetPSButton.setEnabled(true);
//			mResetPTButton.setEnabled(true);
//			mResetPMButton.setEnabled(true);
			
			
		} else {

			mUploadPBButton.setEnabled(false);
			mUploadPSButton.setEnabled(false);
			mUploadPTButton.setEnabled(false);
			mUploadPMButton.setEnabled(false);
			mLoadButton.setEnabled(false);


		}		

		
		mView = rootView;
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
			else if(seekBar == bkiSeekBar  )
			{
				bki = seekBar.getProgress() / 100.0;
				bkiTextView.setText(mDcmFmt.format( bki));
				
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

			else if(seekBar == skdSeekBar  )
			{
				skd = seekBar.getProgress() / 100.0;
				skdTextView.setText(mDcmFmt.format( skd));
				
			}
			else if(seekBar == tkpSeekBar  )
			{
				tkp = seekBar.getProgress()/10.0;
				tkpTextView.setText(mDcmFmt.format( tkp));
				
			}
			else if( seekBar == tkiSeekBar )
			{
				tki = seekBar.getProgress()/100.0;
				tkiTextView.setText(mDcmFmt.format( tki));
			}
			else if(seekBar == tkdSeekBar  )
			{
				tkd = seekBar.getProgress() / 100.0;
				tkdTextView.setText(mDcmFmt.format( tkd));
				
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
			 settings.kd = tkd;
			 
		 }
		 else
		 {
			 
				settings.atObstacle = Double.valueOf( editTextAtObstacle.getText().toString());
				settings.unsafe = Double.valueOf( editTextUnsafe.getText().toString());
				settings.dfw = Double.valueOf( editTextDFW.getText().toString());
//				settings.velocity = Double.valueOf( editTextVelocity.getText().toString());
				settings.max_w = Double.valueOf( editTextMaxW.getText().toString());
				
				settings.max_rpm = Integer.valueOf(editTextMaxRPM.getText().toString());
				settings.min_rpm = Integer.valueOf(editTextMinRPM.getText().toString());

				settings.wheelRadius = Double.valueOf( editTextR.getText().toString());
				settings.wheelDistance = Double.valueOf( editTextL.getText().toString());
			 
				atObstacle = settings.atObstacle;
				unsafe = settings.unsafe;
				dfw = settings.dfw;
				velocity = settings.velocity;
				max_w = settings.max_w;
				max_rpm = settings.max_rpm;
				min_rpm = settings.min_rpm;
				wheelRadius = settings.wheelRadius;
				wheelDistance = settings.wheelDistance;
			 
		 }
		 
		 act.SendSettings(settings);
	}
	
//	private void loadPIDSettings(){
//		 MainActivity act = (MainActivity)getActivity();
//		byte[] value = new byte[3];
//		value[0]='R';
//		value[1]='P';
//		value[2]=1;
//		act.SendBLECmd(value);
//	}

//	private void loadSettings() {
//
//		this.startGetSettingsDialog();
//		settingsReceived = 0;
//		
//		 MainActivity act = (MainActivity)getActivity();
//		byte[] value = new byte[3];
//		value[0]='R';
//		value[1]='P';
//		value[2]=1;
//		act.SendBLECmd(value);
//
//		value = new byte[3];
//		value[0]='R';
//		value[1]='P';
//		value[2]=5;
//		act.SendBLECmd(value);
//
//	}

	
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
		value[2]=5;
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


	public void updateBLEState(boolean bConnected) {

		mBeConnected = bConnected;

		if( mResetPBButton == null )
			return;
		

		if (mBeConnected) {
			
			mUploadPBButton.setEnabled(true);
			mUploadPSButton.setEnabled(true);

			mUploadPTButton.setEnabled(true);
			mUploadPMButton.setEnabled(true);
			mLoadButton.setEnabled(true);

//			mResetPBButton.setEnabled(true);
//			mResetPSButton.setEnabled(true);
//			mResetPTButton.setEnabled(true);
//			mResetPMButton.setEnabled(true);
			
			
		} else {

			mUploadPBButton.setEnabled(false);
			mUploadPSButton.setEnabled(false);
			mUploadPTButton.setEnabled(false);
			mUploadPMButton.setEnabled(false);
			mLoadButton.setEnabled(false);


		}		
		
	}



	public void updateSettings(Settings settings ) {

		settingsReceived++;
		if( settingsReceived >= 4 )
		{
			if( dialog != null )
			{
				dialog.dismiss();
				dialog = null;
			}
		}
		Log.i(TAG, "Update settings: " + settings.settingsType);
		if( settings.settingsType == 5 || settings.settingsType == 0)
		{
			mSettings.settingsType = 5;
			mSettings.copyFrom( settings );
			
		}
		
		if( settings.settingsType == 2 )
		{
			m_bkp = settings.kp;
			m_bki = settings.ki;
			m_bkd = settings.kd;
		}
		else if( settings.settingsType == 3 )
		{
			m_skp = settings.kp;
			m_ski = settings.ki;
			m_skd = settings.kd;
			
		}
		else if( settings.settingsType == 4 )
		{
			m_tkp = settings.kp;
			m_tki = settings.ki;
			m_tkd = settings.kd;
			
		}
		
//		if( settings.settingsType == 1 || settings.settingsType == 0)
//		{
//			mSettings.settingsType = 1;
//			mSettings.copyFrom( settings );
//			
//			m_kp = settings.kp;
//			m_ki = settings.ki;
//			m_kd = settings.kd;
//		}
		
		resetSettings(settings.settingsType);
		
//		Settings mSettings = MainActivity.mSettings;//new Settings();
	}
	
	private void resetSettings(int settingsType)
	{
		if( settingsType == 2 )
		{
			bkp =  m_bkp; 
			bkd = m_bkd;//0.5;
			bki = m_bki;

			bkpTextView.setText(mDcmFmt.format(bkp));
			bkiTextView.setText(mDcmFmt.format(bki));
			bkdTextView.setText(mDcmFmt.format(bkd));

			bkpSeekBar.setProgress( (int)(bkp*10));
			bkiSeekBar.setProgress( (int)(bki*100));
			bkdSeekBar.setProgress( (int)(bkd*100));
			
			
		}
		else if( settingsType == 3 )
		{
			skp = m_skp;//5;
			ski = m_ski;//0.1;
			skd = m_skd;
			
			skpTextView.setText(mDcmFmt.format(skp));
			skiTextView.setText(mDcmFmt.format(ski));
			skdTextView.setText(mDcmFmt.format(skd));

			skpSeekBar.setProgress( (int)(skp*10));
			skiSeekBar.setProgress( (int)(ski*100));
			skdSeekBar.setProgress( (int)(skd*100));
			
			Log.i(TAG, "Update settings:" + skp);
		}
		else if( settingsType == 4 )
		{
			tkp = m_tkp;//5;
			tki = m_tki;
			tkd = m_tkd;
			
			tkpTextView.setText(mDcmFmt.format(tkp));
			tkiTextView.setText(mDcmFmt.format(tki));
			tkdTextView.setText(mDcmFmt.format(tkd));

			tkpSeekBar.setProgress( (int)(tkp*10));
			tkiSeekBar.setProgress( (int)(tki*100));
			tkdSeekBar.setProgress( (int)(tkd*100));
			
			Log.i(TAG, "Update settings:" + tkp);
		}
		
		else if( settingsType == 5 || settingsType == 0)
		{

			atObstacle = mSettings.atObstacle;
			unsafe = mSettings.unsafe;
			dfw = mSettings.dfw;
			velocity = mSettings.velocity;
			max_w = mSettings.max_w;
			max_rpm = mSettings.max_rpm;
			min_rpm = mSettings.min_rpm;
			wheelRadius = mSettings.wheelRadius;
			wheelDistance = mSettings.wheelDistance;
			
			if( mView != null )
			{
			editTextAtObstacle.setText(String.valueOf(mSettings.atObstacle));
			editTextUnsafe.setText(String.valueOf(mSettings.unsafe));
			editTextDFW.setText(String.valueOf(mSettings.dfw));
//			editTextVelocity.setText(String.valueOf(mSettings.velocity));
			editTextMaxW.setText(String.valueOf(mSettings.max_w));

			
			editTextMaxRPM.setText(String.valueOf(mSettings.max_rpm));
			editTextMinRPM.setText(String.valueOf(mSettings.min_rpm));
			
			editTextR.setText( String.valueOf(mSettings.wheelRadius));
			editTextL.setText(String.valueOf(mSettings.wheelDistance));
			}
		}
	
		
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
