package com.zmc.zmcrobot;

//import android.support.v4.app.Fragment;

import java.text.DecimalFormat;

import com.zmc.zmcrobot.ui.RobotSimulateView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
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

public class SettingsFragment extends DialogFragment  {
	
	private final static String TAG = "SettingsFragment";
	private View mView;

	
//	private double kp = 38, ki = 0.0, kd = 0.58;
	
	private double bkp = 10, bki = 0.20, bkd = 0.1;
	private double skp = 5, ski = 0.5, skd = 0.1;
	private double tkp = 5, tki = 10, tkd = 0.1;
	private double dkp = 0.4, dki = 0.6, dkd = 0.001;
	
	
	private double m_bkp = 10, m_bki = 0.2, m_bkd = 0.1;
	private double m_skp = 5, m_ski = 0.5, m_skd = 0.1;
	private double m_tkp = 5, m_tki = 10, m_tkd = 0.1;
	private double m_dkp = 0.4, m_dki = 0.6, m_dkd = 0.001;
	

	
	private int max_pwm=150;
	private int pwm_zero = 0, pwm_diff = 0;
	private double angleOff = 0;

	private double atObstacle = 0.25, unsafe=0.1;
	private double wheelRadius = 0.0325, wheelDistance = 0.165;
	private double velocity;
	
	private double max_w; //w limitation; max
	
	private int max_rpm, min_rpm;
	private double dfw;
	
	private boolean mBeUseIMU = false, mBeIRFilter = false;
	private double imuAlpha = 0.7, irAlpha = 0.5;
	
	
	private boolean mBeConnected = false;
	
    ProgressDialog dialog = null;
    private int settingsReceived = 0;
    
   
	private Button mUploadPBButton, mResetPBButton,  mUploadPSButton, mResetPSButton,
		mUploadPMButton, mResetPMButton, mUploadPTButton, mResetPTButton,mUploadPDButton, mResetPDButton;
    
	private Button   mLoadButton; // , mStopButton;


	EditText editTextBKP, editTextBKI, editTextBKD;
	EditText editTextSKP, editTextSKI, editTextSKD;
	EditText editTextTKP, editTextTKI, editTextTKD;
	EditText editTextDKP, editTextDKI, editTextDKD;
	

//    private DecimalFormat mDcmFmt = new DecimalFormat("#0.0000");
	//	private TextView kpTextView, kdTextView, kiTextView;

	EditText editTextAtObstacle, editTextUnsafe, editTextDFW;
	EditText editTextMaxW;  //editTextVelocity,
	EditText editTextMaxRPM, editTextMinRPM;
	EditText editTextR, editTextL;
	
	EditText editTextIMUAlpha, editTextIRAlpha;
	CheckBox useIMUCheckBox, irFilterCheckBox;
	
//	private SeekBar kpSeekBar,kdSeekBar, kiSeekBar;
	
	
    private DecimalFormat mDcmFmt = new DecimalFormat("#0.####");
	
	private boolean isOn = false;
	
	private Settings mSettings = new Settings();
	
//	private double m_kp = 38, m_ki =0 , m_kd = 0.58;
	
//	private Settings mSpeedSettings = new Settings();
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Log.i(TAG, "Create the view...");
		
		//Settings mSettings = MainActivity.mSettings;//new Settings();
		View rootView = inflater.inflate(R.layout.fragment_settings_new,
				container, false);


		
		editTextBKP = (EditText)rootView.findViewById(R.id.editTextBKP);
		editTextBKI = (EditText)rootView.findViewById(R.id.editTextBKI);
		editTextBKD = (EditText)rootView.findViewById(R.id.editTextBKD);

		editTextBKP.setText(mDcmFmt.format(bkp));
		editTextBKI.setText(mDcmFmt.format(bki));
		editTextBKD.setText(mDcmFmt.format(bkd));
		

		editTextSKP = (EditText)rootView.findViewById(R.id.editTextPKP);
		editTextSKI = (EditText)rootView.findViewById(R.id.editTextPKI);
		editTextSKD = (EditText)rootView.findViewById(R.id.editTextPKD);

		editTextSKP.setText(mDcmFmt.format(skp));
		editTextSKI.setText(mDcmFmt.format(ski));
		editTextSKD.setText(mDcmFmt.format(skd));


		editTextTKP = (EditText)rootView.findViewById(R.id.editTextTKP);
		editTextTKI = (EditText)rootView.findViewById(R.id.editTextTKI);
		editTextTKD = (EditText)rootView.findViewById(R.id.editTextTKD);

		editTextTKP.setText(mDcmFmt.format(tkp));
		editTextTKI.setText(mDcmFmt.format(tki));
		editTextTKD.setText(mDcmFmt.format(tkd));

		editTextDKP = (EditText)rootView.findViewById(R.id.editTextDKP);
		editTextDKI = (EditText)rootView.findViewById(R.id.editTextDKI);
		editTextDKD = (EditText)rootView.findViewById(R.id.editTextDKD);

		editTextDKP.setText(mDcmFmt.format(dkp));
		editTextDKI.setText(mDcmFmt.format(dki));
		editTextDKD.setText(mDcmFmt.format(dkd));
		
		editTextIMUAlpha = (EditText)rootView.findViewById(R.id.editTextAlpha);
		editTextIRAlpha = (EditText)rootView.findViewById(R.id.editTextFilterAlpha);
		useIMUCheckBox  = ( CheckBox )rootView.findViewById(R.id.IMUCheckBox);
		irFilterCheckBox = ( CheckBox )rootView.findViewById(R.id.IRfilterCheckBox);

		useIMUCheckBox.setEnabled( false );
		irFilterCheckBox.setEnabled( false );
		
		useIMUCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mSimulateButton.isChecked();
				SetUseIMU(useIMUCheckBox.isChecked());
			}
		});

		irFilterCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mSimulateButton.isChecked();
				SetIRFilter(irFilterCheckBox.isChecked());
			}
		});
		
		getDialog().setTitle("Settings...");
			

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

		mUploadPDButton = (Button) rootView.findViewById(R.id.uploadPDButton);
		mResetPDButton = (Button) rootView.findViewById(R.id.resetPDButton);
		mUploadPDButton.setEnabled(false);
		
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
				sendSettings(1);
			}
		});

		mResetPBButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSettings(1);
			}
		});
		
		
		mUploadPSButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSettings(2);
			}
		});

		mResetPSButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSettings(2);
			}
		});
		
		mUploadPTButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSettings(3);
			}
		});

		mResetPTButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSettings(3);
			}
		});

		mUploadPDButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSettings(4);
			}
		});

		mResetPDButton.setOnClickListener(new View.OnClickListener() {
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


		mUploadPBButton.setEnabled(mBeConnected);
		mUploadPSButton.setEnabled(mBeConnected);

		mUploadPTButton.setEnabled(mBeConnected);
		mUploadPDButton.setEnabled(mBeConnected);
		mUploadPMButton.setEnabled(mBeConnected);
		mLoadButton.setEnabled(mBeConnected);

		useIMUCheckBox.setEnabled( mBeConnected );
		irFilterCheckBox.setEnabled( mBeConnected );

		
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
		 
		 if( settingsType == 1 )
		 {
			 bkp = Double.valueOf(editTextBKP.getText().toString() );
			 bki = Double.valueOf(editTextBKI.getText().toString() );
			 bkd = Double.valueOf(editTextBKD.getText().toString() );
			 
			 m_bkp = bkp;
			 m_bki = bki;
			 m_bkd = bkd;
			 
			 settings.kp = bkp;
			 settings.ki = bki;
			 settings.kd = bkd;
		 }
		 else if( settingsType == 2)
		 {
			 skp = Double.valueOf(editTextSKP.getText().toString() );
			 ski = Double.valueOf(editTextSKI.getText().toString() );
			 skd = Double.valueOf(editTextSKD.getText().toString() );
			 
			 m_skp = skp;
			 m_ski = ski;
			 m_skd = skd;

			 settings.kp = skp;
			 settings.ki = ski;
			 settings.kd = skd;

		 }
		 if( settingsType == 3 )
		 {
			 tkp = Double.valueOf(editTextTKP.getText().toString() );
			 tki = Double.valueOf(editTextTKI.getText().toString() );
			 tkd = Double.valueOf(editTextTKD.getText().toString() );
			 
			 m_tkp = tkp;
			 m_tki = tki;
			 m_tkd = tkd;
			 
			 settings.kp = tkp;
			 settings.ki = tki;
			 settings.kd = tkd;
		 }
		 else if( settingsType == 4)
		 {
			 dkp = Double.valueOf(editTextDKP.getText().toString() );
			 dki = Double.valueOf(editTextDKI.getText().toString() );
			 dkd = Double.valueOf(editTextDKD.getText().toString() );
			 
			 m_dkp = dkp;
			 m_dki = dki;
			 m_dkd = dkd;

			 settings.kp = dkp;
			 settings.ki = dki;
			 settings.kd = dkd;
			 
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
		value[2]=1;
		act.SendBLECmd(value);

		value = new byte[3];
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
		
		mUploadPBButton.setEnabled(mBeConnected);
		mUploadPSButton.setEnabled(mBeConnected);

		mUploadPTButton.setEnabled(mBeConnected);
		mUploadPDButton.setEnabled(mBeConnected);
		mUploadPMButton.setEnabled(mBeConnected);
		mLoadButton.setEnabled(mBeConnected);

		useIMUCheckBox.setEnabled( mBeConnected );
		irFilterCheckBox.setEnabled( mBeConnected );
		
	}



	public void updateSettings(Settings settings ) {

		settingsReceived++;
		if( settingsReceived >= 5 )
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
		
		if( settings.settingsType == 1 )
		{
			m_bkp = settings.kp;
			m_bki = settings.ki;
			m_bkd = settings.kd;
		}
		else if( settings.settingsType == 2 )
		{
			m_skp = settings.kp;
			m_ski = settings.ki;
			m_skd = settings.kd;
			
		}
		else if( settings.settingsType == 3 )
		{
			m_tkp = settings.kp;
			m_tki = settings.ki;
			m_tkd = settings.kd;
			
		}
		else if( settings.settingsType == 4 )
		{
			m_dkp = settings.kp;
			m_dki = settings.ki;
			m_dkd = settings.kd;

			mBeUseIMU = settings.beUseIMU;
			mBeIRFilter = settings.beIRFilter;
			imuAlpha = settings.imuAlpha;
			irAlpha = settings.irFilter;
		}
		
		resetSettings(settings.settingsType);
		
//		Settings mSettings = MainActivity.mSettings;//new Settings();
	}
	
	private void resetSettings(int settingsType)
	{
		if( settingsType == 1 )
		{
			bkp =  m_bkp; 
			bkd = m_bkd;//0.5;
			bki = m_bki;

			editTextBKP.setText(mDcmFmt.format(bkp));
			editTextBKI.setText(mDcmFmt.format(bki));
			editTextBKD.setText(mDcmFmt.format(bkd));
			
		
			
		}
		else if( settingsType == 2 )
		{
			skp = m_skp;//5;
			ski = m_ski;//0.1;
			skd = m_skd;
			
			editTextSKP.setText(mDcmFmt.format(skp));
			editTextSKI.setText(mDcmFmt.format(ski));
			editTextSKD.setText(mDcmFmt.format(skd));
			
//			Log.i(TAG, "Update settings:" + skp);
		}
		else if( settingsType == 3 )
		{
			tkp = m_tkp;//5;
			tki = m_tki;
			tkd = m_tkd;
			
			editTextTKP.setText(mDcmFmt.format(tkp));
			editTextTKI.setText(mDcmFmt.format(tki));
			editTextTKD.setText(mDcmFmt.format(tkd));
			
//			Log.i(TAG, "Update settings:" + tkp);
		}

		else if( settingsType == 4 )
		{
			dkp = m_dkp;//5;
			dki = m_dki;
			dkd = m_dkd;
			
			editTextDKP.setText(mDcmFmt.format(dkp));
			editTextDKI.setText(mDcmFmt.format(dki));
			editTextDKD.setText(mDcmFmt.format(dkd));

			
			editTextIMUAlpha.setText( mDcmFmt.format(imuAlpha));
			editTextIRAlpha.setText( mDcmFmt.format(irAlpha));

			useIMUCheckBox.setChecked(mBeUseIMU );
			irFilterCheckBox.setChecked( mBeIRFilter );
	
		
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

	
	private void SetUseIMU(boolean val)
	{
		imuAlpha = Double.parseDouble(editTextIMUAlpha.getText().toString());
		mBeUseIMU = val;

		String cmdStr = "IM";
		if( val )
			cmdStr = cmdStr + "1,";
		else 
			cmdStr = cmdStr+"0,";
		cmdStr = cmdStr + editTextIMUAlpha.getText();
		
		MainActivity act = (MainActivity)getActivity();
		act.SendBLECmd(cmdStr.getBytes());

		Log.i(TAG, cmdStr);
	}
	
	private void SetIRFilter(boolean val )
	{
		irAlpha = Double.parseDouble(editTextIRAlpha.getText().toString());
		mBeIRFilter = val;

		String cmdStr = "IF";
		if( val )
			cmdStr = cmdStr + "1,";
		else 
			cmdStr = cmdStr+"0,";
		cmdStr = cmdStr + editTextIRAlpha.getText();
		
		MainActivity act = (MainActivity)getActivity();
		act.SendBLECmd(cmdStr.getBytes());

		Log.i(TAG, cmdStr);

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
    
    
  @Override
  public void show(FragmentManager manager, String tag)
  {
	  try{
		  super.show(manager, tag);
//		  FragmentTransaction ft = manager.beginTransaction();
//		  ft.add(this, tag).addToBackStack(null);
//		  ft.commit(); //commitAllowingStateLoss();
	  }catch(Exception e)
	  {
		  Log.i(TAG, "Exception in show:" + e);
		  
		  e.printStackTrace();
	  }
  }
  
  boolean mIsStateAlreadySaved = false;
  boolean mPendingShowDialog = false;
  
  @Override
  public void onResume(){
	Log.i(TAG, "onResume");
	  onResumeFragments();
	  super.onResume();
  }
  
  
  public void onResumeFragments(){
	  mIsStateAlreadySaved = false;
	  if( mPendingShowDialog ){
		  mPendingShowDialog = false;
		  
		  ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
	        params.width = WindowManager.LayoutParams.MATCH_PARENT;
	        params.height = WindowManager.LayoutParams.MATCH_PARENT;
	        getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);
        
		  showDialog();
	  }
	  
  }
  
  
  
  private void showDialog() {

      if (mIsStateAlreadySaved) {

          mPendingShowDialog = true;

      } else {
          FragmentManager fm = getFragmentManager();

         String tag = SettingsFragment.class.getName();
  		SettingsFragment settingsFragment = SettingsFragment.getInstance(getActivity().getApplicationContext(), getFragmentManager());
  		settingsFragment.show(fm, tag);

      }

  }
  
  
	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
		mIsStateAlreadySaved = true;
	}
	
  
  
    	
	public static SettingsFragment getInstance(Context mContext, FragmentManager fm){
		String tag = SettingsFragment.class.getName();
	    FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment = fm.findFragmentByTag( tag );

		if (fragment != null) {
	        ft.remove(fragment);
	    }
	    ft.addToBackStack(null);
		
			fragment = Fragment.instantiate(mContext, tag);
			SettingsFragment dialogFragment = (SettingsFragment)fragment;
//			dialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
			dialogFragment.setCancelable(true);
			return dialogFragment;
	}
}
