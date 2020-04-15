package com.zmc.zmcrobot;

//import android.support.v4.app.Fragment;

import java.text.DecimalFormat;

import com.zmc.zmcrobot.ui.RobotSimulateView;

import android.app.Activity;
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
import android.view.WindowManager;
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

public class BLECmdFragment extends DialogFragment  {
	
	private final static String TAG = "BLECmdFragment";
	private View mView;

	
	private String msgs;
	final private String cmdHints = "ST\t\tstop;\nRP[type]\trequire for settings;\n" +
	"SM[0/1]\t\tset simulate mode;\n"+
	"IO[0/1]\tignore obstacles;\n"+
	"RS\t\treset robot;\n" +
	"GO\t\tstart goto goal\n" +
	"GGx,y,theta Goto goal (*100)\n" +
	"MMpwml,pwmr; drive motor 1 sec.\n" +
	"TLpwm,stopCount; +pwm turn right - pwm turn left\n"+
	"CI\t\tCount info\n" +
	"RI\t\tRobot position\n" +
	"SR1r,l; SR2minRPM,maxRPM; robot params\n" +
	"IM0/1,alpha; Use IMU to calculate the theta\n" +
	"IF0/1,alpha; IR distance filter\n" +
	"IRidx0/1; set IR sensor in idx;\n" + 
	"PItype kp,ki,kd; Set PID 2 dir 3 diff 4 turning\n" +
	"SDvw; (set driver goal Binary)\n";

	
	
	private boolean mBeConnected = false;
	
  
	private Button mSendButton, mClearButton;
	
	
	private TextView msgsTextView, hintsTextView;

	EditText editTextCMD;
	
	
    private DecimalFormat mDcmFmt = new DecimalFormat("#0.0000");

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//Settings mSettings = MainActivity.mSettings;//new Settings();
		View rootView = inflater.inflate(R.layout.fragment_blecmd,
				container, false);


		msgsTextView = (TextView)rootView.findViewById(R.id.msgsTextView);
		hintsTextView = (TextView)rootView.findViewById(R.id.hintsTextView);
		hintsTextView.setText(cmdHints );
		
		getDialog().setTitle("BLE Command...");
	
		
		

		editTextCMD = (EditText)rootView.findViewById(R.id.cmdEditText);
		
		

		mSendButton = (Button) rootView.findViewById(R.id.actionButton);
		mClearButton = (Button) rootView.findViewById(R.id.clearButton);
		
		
		mSendButton.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				sendCmd();
				
			}
		});

		mClearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				msgsTextView.setText("");
				msgs = "";
			}
		});


		
		mView = rootView;
		return rootView;
	}

	
	private void sendCmd()
	{
		 MainActivity act = (MainActivity)getActivity();
		 
		 String cmd = editTextCMD.getText().toString();
		 if( cmd.length() < 2 )
			 return;
		 
		 act.SendBLECmd(cmd.getBytes());
	}
	
	
	public void addMessages(String data)
	{
		msgs = msgs + data;
		msgsTextView.setText(msgs);
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

	
	   
	  @Override
	  public void show(FragmentManager manager, String tag)
	  {
		  try{

			  super.show(manager, tag);
			  
//			  FragmentTransaction ft = manager.beginTransaction();
//			  ft.add(this, tag).addToBackStack(null);
//			  ft. commit(); //commitAllowingStateLoss();
		  }catch(Exception e)
		  {
			  Log.e(TAG, "Error in show: " + e);
			  e.printStackTrace();
		  }
		  
//		  this.dismiss()
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

	         String tag = BLECmdFragment.class.getName();
	         BLECmdFragment cmdFragment = BLECmdFragment.getInstance(getActivity().getApplicationContext(), getFragmentManager());
	         cmdFragment.show(fm, tag);

	      }

	  }
	  
	  
		@Override
		public void onPause() {
			super.onPause();
			Log.i(TAG, "onPause");
			mIsStateAlreadySaved = true;
		}
		
	  
	  
	    	
		public static BLECmdFragment getInstance(Context mContext, FragmentManager fm){
			String tag = BLECmdFragment.class.getName();

			FragmentTransaction ft = fm.beginTransaction();
			Fragment fragment = fm.findFragmentByTag( tag );

			if (fragment != null) {
		        ft.remove(fragment);
		    }
		    ft.addToBackStack(null);
		
				fragment = Fragment.instantiate(mContext, tag);
				BLECmdFragment dialogFragment = (BLECmdFragment)fragment;
//				dialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
				dialogFragment.setCancelable(true);
				return dialogFragment;
		}	
	
}
