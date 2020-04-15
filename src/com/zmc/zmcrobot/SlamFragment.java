package com.zmc.zmcrobot;

//import android.support.v4.app.Fragment;

import java.text.DecimalFormat;

import com.zmc.zmcrobot.simulator.SlamMap;
import com.zmc.zmcrobot.ui.RobotSimulateView;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 
 *  
 * @author zhong_zmwo6t7
 *
 */
public class SlamFragment extends Fragment  implements Runnable { //
	
//	private Supervisor supervisor = new Supervisor();
	// private ZMCRobot robot = new ZMCRobot();

    private boolean mBeconnected = false;

//	private AbstractRobot robot = new RearDriveRobot();
	private RobotSimulateView mRobotView;

	private SlamMap slamMap = null;

	private JoystickView mJoystickView;
	private JoystickView mAngleJoystickView;
	private boolean mDualCtrl = false;
	private boolean mGearCtrl = false;

	
	private View mGearCtrlView = null;
	private ImageButton mBrakeButton, mIBSpeed, mIBSpeedR;
	private SteerView mSteerView;
	private TextView mTextViewVW;
	
	private TextView mMessageTextView;
	
	private DecimalFormat fmt = new DecimalFormat("#0.00");
	private CheckBox mGearCtrlCheckBox;

	public boolean mBeSlamMapping = false;
	
	
	private double mV = 0, mW = 0;


	private final static String TAG = "SlamFragment";
	
	private View mView;
	private Button mGoButton, mHomeButton; // , mStopButton;

	private CheckBox  mSimulateCheckBox,  mIgnoreObstacleCheckBox, mUseIMUCheckBox, mDualCtrlCheckBox;
	private EditText goalAngleEditText, velocityEditText, alphaEditText;

//	private ToggleButton mSimulateButton;
	// private Thread speedBrakeThread = null; // = new Thread(this );

	GestureDetector mGestureDector;

	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1f;
	private float mScale = 500;

	// private ClickDetector mClickDector;

	// private float x0, y0, theta0;

	// private float targetX, targetY;
	private boolean inScale = false;

	private boolean isGoing = false;
	private boolean isPause = false;

	private boolean setRoute = false;
	private float[][] mRoutes = new float[2000][2];
	private int routeSize = 0;

	private double home_x = 0, home_y = 0, home_theta = (float) Math.PI / 4;

	private long scaleEndTime = 0;

	private double mGTGVelocity = 0.15;
	private int mGTGTheta = 45;

	private double mAlpha = 0.6;
	
	private double fMaxRobotSpeed = 0.6;

	
	private int speed_f = 0, speed_r = 0;

	private int maxSpeed = 100;
	private static int minSpeed = 0;
	private static int SPEED_STEP = 5;

	private boolean isBraking = false;
	private boolean isSpeeding = false;
	private boolean isRewarding = false;

	private static final int MESSAGE_NEW_SPEED = 1;

	private Thread speedBrakeThread = null; // = new Thread(this );
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.i(TAG, "onCreate View: " + mGearCtrl );

		mView = inflater.inflate(R.layout.fragment_slam, container, false);
		mRobotView = (RobotSimulateView) mView.findViewById(R.id.robot_view);
		
    	mRobotView.setSlamMap(slamMap);

		mRobotView.setRobotPosition(-1, -1, Math.PI / 4, 0);

		
		mSteerView = (SteerView) mView.findViewById(R.id.bSteerView);
		
		mGearCtrlView = (View) mView.findViewById(R.id.GearCtrlView);	
		
	
		//////////2019-09-12
//		robot.setPosition(-1, -1, Math.PI / 4);

		/*
		ObstacleCrossPoint[] ocps = robot.getObstacleCrossPoints();
		mRobotView.setObstacleCrossPoints(ocps);

		try {

			Obstacle obstacle = new Obstacle(obstacle1);
			obstacle.setPosition(-0.8, 0, 0);
			robot.addObstacle(obstacle);
			mRobotView.addObstacle(obstacle);

			obstacle = new Obstacle(obstacle3);
			obstacle.setPosition(-0.5, 0, 0);
			robot.addObstacle(obstacle);
			mRobotView.addObstacle(obstacle);

			obstacle = new Obstacle(obstacle2);
			obstacle.setPosition(-1, -2, 0);
			robot.addObstacle(obstacle);
			mRobotView.addObstacle(obstacle);

			obstacle = new Obstacle(obstacle4); 
			obstacle.setPosition(-2, 1, 0);
			robot.addObstacle(obstacle);
			mRobotView.addObstacle(obstacle);

			obstacle = new Obstacle(border, Color.RED, false);
			obstacle.setPosition(0, 0, 0);
			robot.addObstacle(obstacle);
			mRobotView.addObstacle(obstacle);

		} catch (Exception e) {

		}
		
		*/
		mRobotView.setRoutes(mRoutes, routeSize);

		
		mJoystickView = (JoystickView)	mView.findViewById(R.id.joystickView);

		mJoystickView.setJoystickEventListener( new JoystickEventListener(){

			@Override
			public void onJoystickAction(JoystickEvent event) {
				// TODO Auto-generated method stub

				double v = 0.5 * event.throttle;
				double w = event.angle;
				
                if (Math.abs(w) > 0.06)
                    w = w;
                else
                    w = 0;

                mV = v;

				if( !mDualCtrl )
					mW = w;
				
                driveRobot(mV,mW);
//                thetaLabel.setText(String.format("%.2f", mTheta));
//                speedLabel.setText(String.format("%.2f", mSpeed));
//                driveSupervisor.setDriveGoal(mSpeed, mTheta);
				
			}
			
		});		
		
		
		mAngleJoystickView = (JoystickView)	mView.findViewById(R.id.angleJoystickView);
		
		mAngleJoystickView.setJoystickEventListener( new JoystickEventListener(){

			@Override
			public void onJoystickAction(JoystickEvent event) {
				// TODO Auto-generated method stub
				
				if( !mDualCtrl )
					return;
				
				double w = event.angle;
//				if( w!= 0 && Math.abs( w - mW ) < 0.01 ) //拒绝手抖，避免频繁改变
//					return;
				
                if (Math.abs(w) > 0.06)
                    mW =  w;
                else
                    mW = 0;
                driveRobot(mV,mW);
                
//                thetaLabel.setText(String.format("%.2f", mTheta));
//                speedLabel.setText(String.format("%.2f", mSpeed));
//                driveSupervisor.setDriveGoal(mSpeed, mTheta);
				
			}
			
		});			
		
		
		
		// mRobotView.setPosition(250, 300, -Math.PI/8, mScale);

		mGoButton = (Button) mView.findViewById(R.id.buttonAction);
		mHomeButton = (Button) mView.findViewById(R.id.homeButton);
		


		goalAngleEditText = (EditText) mView.findViewById(R.id.goalAngleEditText);
		velocityEditText = (EditText) mView.findViewById(R.id.velocityEditText);
		alphaEditText = (EditText)mView.findViewById(R.id.alphaEditText );
				
		velocityEditText.setText(String.valueOf(mGTGVelocity));
		goalAngleEditText.setText(String.valueOf(mGTGTheta));
		alphaEditText.setText(String.valueOf(mAlpha));
		
		velocityEditText.addTextChangedListener(new TextWatcher() {

			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

				try {
					double velocity = Double.valueOf(velocityEditText.getText().toString());
					if (velocity < 0.1 || velocity > 0.8) {
						velocityEditText.setError("Velocity should between 0.1 to 0.8!");
						return;

					} else {
						mGTGVelocity = velocity;
					}
				} catch (Exception e) {
					velocityEditText.setError("please input proper numeric Velocity between 0.1 to 0.8!");

					// velocityEditText.requestFocus();

				}
			}

		});
		
		
		goalAngleEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

				try {
					int theta = Integer.valueOf(goalAngleEditText.getText().toString());
					if (theta < 0 || theta > 360) {
						goalAngleEditText.setError("angle should between 0 to 360!");
						return;

					} else {
						mGTGTheta = theta;
					}
				} catch (Exception e) {
					goalAngleEditText.setError("please input proper numeric angle between 0 to 360!");

					// velocityEditText.requestFocus();

				}
			}

		});
		
		alphaEditText.addTextChangedListener(new TextWatcher() {

			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

				try {
					double alpha = Double.valueOf(alphaEditText.getText().toString());
					if (alpha < 0 || alpha > 1) {
						alphaEditText.setError("alpha should between 0 to 1.0!");
						return;

					} else {
						mAlpha = alpha;
					}
				} catch (Exception e) {
					alphaEditText.setError("please input proper numeric apha between 0 to 1!");

					// velocityEditText.requestFocus();

				}
			}

		});

		goalAngleEditText.clearFocus();
		velocityEditText.clearFocus();

		mIgnoreObstacleCheckBox = (CheckBox) mView.findViewById(R.id.IgnoreObstacleCheckBox);

		mIgnoreObstacleCheckBox.setChecked(false);
		mIgnoreObstacleCheckBox.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				SetIgnoreObstacleMode(mIgnoreObstacleCheckBox.isChecked());
				
			}

		});
		
		mSimulateCheckBox = (CheckBox) mView.findViewById(R.id.SimulateCheckBox);

		mSimulateCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mSimulateButton.isChecked();
				SetSimulateMode(mSimulateCheckBox.isChecked());
			}
		});

		mUseIMUCheckBox= (CheckBox) mView.findViewById(R.id.IMUCheckBox);
		mUseIMUCheckBox.setChecked(false);
		mUseIMUCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mSimulateButton.isChecked();
				SetUseIMU(mUseIMUCheckBox.isChecked());
			}
		});
		
		mDualCtrlCheckBox =(CheckBox) mView.findViewById(R.id.DualCtrlCheckBox);

		mDualCtrlCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if( mDualCtrlCheckBox.isChecked() )
				{
					mDualCtrl = true;
					mAngleJoystickView.setVisibility(View.VISIBLE);
				}
				else
				{
					mDualCtrl = false;
					mAngleJoystickView.setVisibility(View.INVISIBLE);
				}
			}
		});
		
		
		if( !mDualCtrl )
		{
			mAngleJoystickView.setVisibility(View.INVISIBLE);
			mDualCtrlCheckBox.setChecked( mDualCtrl );
		}
		else
		{
			mAngleJoystickView.setVisibility(View.VISIBLE);
		}
		
		mGearCtrlCheckBox = (CheckBox) mView.findViewById(R.id.GearCtrlCheckBox);

		mGearCtrlCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if( mGearCtrlCheckBox.isChecked() )
				{
					hideViewAnimation(mJoystickView);
					if( mDualCtrl )
						hideViewAnimation(mAngleJoystickView);
					
					mGearCtrlView.setVisibility(View.VISIBLE );
					mGearCtrl = true;
					mJoystickView.setVisibility(View.INVISIBLE);
					mAngleJoystickView.setVisibility(View.INVISIBLE);
					mDualCtrlCheckBox.setVisibility(View.INVISIBLE);

					mGearCtrlView.setVisibility(View.VISIBLE );
					
					showViewAnimation(mGearCtrlView);
				}
				else
				{
					mGearCtrl = false;
					mJoystickView.setVisibility(View.VISIBLE);
					mDualCtrlCheckBox.setVisibility(View.VISIBLE);

					showViewAnimation(mJoystickView);
					if( mDualCtrl )
						showViewAnimation(mAngleJoystickView);
					
					hideViewAnimation(mGearCtrlView);

					
					mGearCtrlView.setVisibility(View.INVISIBLE );
					if( mDualCtrl )
						mAngleJoystickView.setVisibility(View.VISIBLE);
				}
			}
		});
		
		
	
		mGoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

            	if( !isGoing )
            	{
            		startGoToGoal();
            		mGoButton.setText(R.string.stop);
            		isGoing = true;
            	}
            	else
            	{
            		stopRobot();
            		mGoButton.setText(R.string.go);

            		isGoing = false;
            	}
			
			}
		});

		mHomeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetRobot();
			}
		});

		
		
		mGoButton.setEnabled( mBeconnected );
		mHomeButton.setEnabled( mBeconnected );
		mIgnoreObstacleCheckBox.setEnabled(mBeconnected);
		mSimulateCheckBox.setEnabled(mBeconnected);
		mUseIMUCheckBox.setEnabled(mBeconnected);
		
		
		mBrakeButton = (ImageButton) mView.findViewById(R.id.brakeButton);
		mBrakeButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				
				int action = event.getAction();
				
				if (action == MotionEvent.ACTION_DOWN) {
					mBrakeButton.setImageDrawable(getResources().getDrawable(
							R.drawable.brake_touch));

					isBraking = true;
				} else if (action == MotionEvent.ACTION_UP || action ==MotionEvent.ACTION_CANCEL ) {
					mBrakeButton.setImageDrawable(getResources().getDrawable(
							R.drawable.brake));
					isBraking = false;
				}

				speedBrakeThread.interrupt();
				return false;
			}

		});

		mIBSpeed = (ImageButton) mView.findViewById(R.id.speederButton);
		mIBSpeed.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {

				//ACTION_CANCEL
				int action = event.getAction();
				if ( action == MotionEvent.ACTION_DOWN) {

					if (isRewarding || speed_r > 0) {
						isRewarding = false;
						speed_r = 0;
					}
					isSpeeding = true;
					mIBSpeed.setImageDrawable(getResources().getDrawable(
							R.drawable.speed_touch));
				} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
					isSpeeding = false;
					mIBSpeed.setImageDrawable(getResources().getDrawable(
							R.drawable.speed));
				}

				speedBrakeThread.interrupt();
				return true;
			}

		});

		mIBSpeedR = (ImageButton) mView.findViewById(R.id.speeder_rButton);
		mIBSpeedR.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				
				int action = event.getAction();
				
				if (action == MotionEvent.ACTION_DOWN) {

					if (isSpeeding || speed_f > 0) {

						// MeterFragment.this.sendMessage("F0;");
						speed_f = 0;
						isSpeeding = false; // stop speeding

					}
					isRewarding = true;
					mIBSpeedR.setImageDrawable(getResources().getDrawable(
							R.drawable.speed_r_touch));
				} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
					isRewarding = false;
					mIBSpeedR.setImageDrawable(getResources().getDrawable(
							R.drawable.speed_r));
				}

				speedBrakeThread.interrupt();

				return false;
			}

		});

		mBrakeButton.setEnabled( mBeconnected );
		mIBSpeedR.setEnabled(mBeconnected);
		mIBSpeed.setEnabled(mBeconnected);

		mTextViewVW = (TextView) mView.findViewById(R.id.textViewVW);
		
		String label = "[v:" + fmt.format(mV) + ", w:" + fmt.format(mW) + "]";
		mTextViewVW.setText(label);

		mMessageTextView = (TextView)mView.findViewById(R.id.TextViewMsg);
		
		
		mGestureDector = new GestureDetector(this.getActivity().getApplicationContext(), new MyGestureListener());
		mScaleDetector = new ScaleGestureDetector(this.getActivity().getApplicationContext(), new ScaleListener());

	
//		rootView.setOnTouchListener(viewOnTouchListener);

		mRobotView.setOnTouchListener(viewOnTouchListener);
		
		
		mSteerView.setRotateAngle(0);
		mSteerView.setOnTouchListener(steerOnTouchListener);

		
        if( speedBrakeThread == null )
        {
        	speedBrakeThread = new  Thread(this);
        	speedBrakeThread.start();
        }
		

        //recover the user selection
		mDualCtrlCheckBox.setChecked(mDualCtrl );
		
		Log.i(TAG, "update gear checkbox: " + mGearCtrl);
		mGearCtrlCheckBox.setChecked( mGearCtrl );
		
		Log.i(TAG, "After: " + mGearCtrlCheckBox.isChecked() );
        
		if( mGearCtrl )
		{
			mAngleJoystickView.setVisibility(View.INVISIBLE);
			mAngleJoystickView.setVisibility(View.INVISIBLE);
			mDualCtrlCheckBox.setVisibility(View.INVISIBLE);
			mGearCtrlView.setVisibility(View.VISIBLE );
			
		}
		else
		{
			mAngleJoystickView.setVisibility(View.VISIBLE);
			mDualCtrlCheckBox.setVisibility(View.VISIBLE);
			
			if( mDualCtrl )
				mAngleJoystickView.setVisibility(View.VISIBLE);
			else
				mAngleJoystickView.setVisibility(View.INVISIBLE);
				
			mGearCtrlView.setVisibility(View.INVISIBLE );
		}
		
        
		
		return mView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(TAG, "onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "onCreate:" + mGearCtrl);
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
		
	       //recover the user selection
			mDualCtrlCheckBox.setChecked(mDualCtrl );
			
			Log.i(TAG, "update gear checkbox: " + mGearCtrl);
			mGearCtrlCheckBox.setChecked( mGearCtrl );
			
			Log.i(TAG, "After: " + mGearCtrlCheckBox.isChecked() );
	        
			if( mGearCtrl )
			{
				mJoystickView.setVisibility(View.INVISIBLE);
				mAngleJoystickView.setVisibility(View.INVISIBLE);
				mDualCtrlCheckBox.setVisibility(View.INVISIBLE);
				mGearCtrlView.setVisibility(View.VISIBLE );
				
			}
			else
			{
				mJoystickView.setVisibility(View.VISIBLE);
				mDualCtrlCheckBox.setVisibility(View.VISIBLE);
				
				if( mDualCtrl )
					mAngleJoystickView.setVisibility(View.VISIBLE);
				else
					mAngleJoystickView.setVisibility(View.INVISIBLE);
					
				mGearCtrlView.setVisibility(View.INVISIBLE );
			}		
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		this.updateBLEState( mBeconnected );
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
	
	
	public void SetIgnoreObstacleMode(boolean igm)
	{
		Log.i(TAG, "Set ignore obstacle mode " + igm);
		byte[] value = new byte[4];
		
		value[0] = 'I';
		value[1] = 'O';
		
		if( igm )
			value[2] = 1;
		else
			value[2] = 0;
		
		
		
		MainActivity act = (MainActivity)getActivity();
		act.SendBLECmd(value);
		
	}


	long[] mHits = new long[2];

	private OnTouchListener viewOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent event) {

			mScaleDetector.onTouchEvent(event);

			if (event.getActionMasked() > 0xff)
				return true;
			if (inScale)
				return true;

			if (setRoute) {
				if (event.getAction() == MotionEvent.ACTION_UP) {

					Log.i(TAG, "action up");
					float x, y;
					x = event.getX();
					y = event.getY();
					Position p = mRobotView.getRealPosition(x, y);

					setRoute = false; // end set route

					updateViewScroll();

					mRoutes[routeSize][0] = p.x;
					mRoutes[routeSize][1] = p.y;
					routeSize++;

					mRobotView.setRouteSize(routeSize);

					curveRoute(mRoutes, routeSize);

					return true;

				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {

					Log.i(TAG, "action move");

					if (routeSize > 1990)
						return true;
					float x, y;
					x = event.getX();
					y = event.getY();
					Position p = mRobotView.getRealPosition(x, y);

					mRoutes[routeSize][0] = p.x;
					mRoutes[routeSize][1] = p.y;
					routeSize++;
					mRobotView.setRouteSize(routeSize);

				} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					Log.i(TAG, "action cancel");
					setRoute = false;
				}

				return true;
			}

			return mGestureDector.onTouchEvent(event);

		};
	};


	
    private void startGoToGoal()
    {
		byte[] value = new byte[4];
		
		value[0] = 'G';
		value[1] = 'O';
		
//		int tmp = (int) (targetX * 100.0);
//		
//		value[2] =  (byte)(tmp&0xff);
//		value[3] = (byte) (tmp>>8);
//
//		tmp =  (int) (targetY * 100.0);
//		value[4] =  (byte)(tmp&0xff);
//		value[5] = (byte) (tmp>>8);
//		
//		tmp = 90;
//		value[6] =  (byte)(tmp&0xff);
//		value[7] = (byte) (tmp>>8);
		
		MainActivity act = (MainActivity)getActivity();
		act.SendBLECmd(value);
    	
    }
    
    private void stopRobot()
    {
		byte[] value = new byte[4];
		
		value[0] = 'S';
		value[1] = 'T';

		MainActivity act = (MainActivity)getActivity();
		act.SendBLECmd(value);
    	
    }
	
    private void resetRobot()
    {
    	
    	mRobotView.resetRobot();
    	byte[] value = new byte[4];
    	value[0] = 'R';
    	value[1] = 'S';
		MainActivity act = (MainActivity)getActivity();
		act.SendBLECmd(value);
		if( slamMap != null )
			slamMap.reset();
    	
    }
    
	private void SendGoToGoalCmd(float targetX, float targetY)
	{
	
		Log.i(TAG,
				"Go to Goal: " + 
						targetX + 
						", " + targetY + ","  + mGTGTheta + "," + mGTGVelocity);
		byte[] value = new byte[11];
		
		value[0] = 'G';
		value[1] = 'G';
		
		int tmp = (int) (targetX * 100.0);
		intToByte(tmp, value, 2);
		
		tmp =  (int) (targetY * 100.0);
		intToByte(tmp, value, 4);
		
		tmp = mGTGTheta;
		value[6] =  (byte)(tmp&0xff);
		value[7] = (byte) (tmp>>8);

		tmp = (int)(mGTGVelocity * 100.0);
		intToByte(tmp, value, 8);
		
		MainActivity act = (MainActivity)getActivity();
		act.SendBLECmd(value);
	};
	

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

	private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();

			// Make sure we don't get too small or too big
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

			mScale = 500 * mScaleFactor;
			mRobotView.setScale(mScale);

			// Log.i(TAG, "in onScale, scale factor = " + mScaleFactor);
			// mView.invalidate();
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector arg0) {
			// TODO Auto-generated method stub
			Log.i(TAG, "Begin scale...");

			mRobotView.inScale = true;

			inScale = true;
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector arg0) {
			// TODO Auto-generated method stub
			scaleEndTime = SystemClock.uptimeMillis();

			mRobotView.inScale = false;

			inScale = false;

			updateViewScroll();

			Log.i(TAG, "scale end.");
		}
	}

	public void setRobotState(RobotState state) {
		
		mRobotView.setRobotState(state);
	}
	


	public void SetSimulateMode(boolean sm) {
		Log.i(TAG, "Set simulate mode " + sm);
		byte[] value = new byte[4];

		value[0] = 'S';
		value[1] = 'M';

		if (sm)
			value[2] = 1;
		else
			value[2] = 0;

		MainActivity act = (MainActivity)getActivity();
		act.SendBLECmd(value);

	}
	
	public void SetUseIMU(boolean val )
	{
		MainActivity act = (MainActivity)getActivity();
		
		String cmd;
		if( val )
			cmd = "IM1," + mAlpha;
		else
			cmd = "IM0," +  + mAlpha;
		act.SendBLECmd( cmd.getBytes() );

		Log.i(TAG, "Set use IMU, cmd:" + cmd );
		
	}

	
	public void setBeConnected(boolean beConnected )
	{
		mBeconnected = beConnected;
		if( this.mView != null )
			this.updateBLEState(beConnected);
	}

	
	public void updateBLEState(boolean bConnected) {
		
		Log.i(TAG, "Update connection state: " + bConnected);
		mBeconnected = bConnected;
		mGoButton.setEnabled( mBeconnected );
		mHomeButton.setEnabled( mBeconnected );
		mIgnoreObstacleCheckBox.setEnabled(mBeconnected);
		mSimulateCheckBox.setEnabled(mBeconnected);
		mUseIMUCheckBox.setEnabled(mBeconnected);
		
		mBrakeButton.setEnabled( mBeconnected );
		mIBSpeedR.setEnabled(mBeconnected);
		mIBSpeed.setEnabled(mBeconnected);
		

	}

	// final double obstacle1[][] = { { -0.5, 0, 1 }, { 0.5, 0, 1 },
	// { 0.5, -0.5, 1 }, { 0, -0.5, 1 } };
	double obstacle1[][] = { { 0, 0, 1 }, { 0.35, 0, 1 }, { 0.35, -0.30, 1 }, { 0, -0.30, 1 } };

	// 锟斤拷锟斤拷
	final double obstacle2[][] = { { 0, 0, 1 }, { 0, 0.8, 1 }, { 0.4, 0.8, 1 }, { 0.4, 0.4, 1 }, { 1.8, 0.4, 1 },
			{ 1.8, 0, 1 }

	};

	// 锟斤拷锟斤拷 
	final double obstacle3[][] = { { 0, 1, 1 }, { 0, 2.2, 1 }, { 2.4, 2.2, 1 }, { 2.4, 0, 1 }, { 1.2, 0, 1 },
			{ 1.2, 0.4, 1 }, { 2, 0.4, 1 }, { 2, 1.8, 1 }, { 0.4, 1.8, 1 }, { 0.4, 1, 1 } };

	final double obstacle4[][] = { { 0, 0, 1 }, { 0.3, 0, 1 }, { 0.3, 0.4, 1 }, { 0, 0.4, 1 } };

	final double border[][] = { { -3, -3.5, 1 }, { -3, 3.5, 1 }, { 3, 3.5, 1 }, { 3, -3.5, 1 } };


	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
		public MyGestureListener() {
			super();
		}

		@Override
		public boolean onDoubleTap(MotionEvent event) {
			// Log.e(TAG, "onDoubleTap");
			float x, y;
			x = event.getX();
			y = event.getY();

			Position p = mRobotView.getRealPosition(x, y);
			home_x = p.x;
			home_y = p.y;

			home_theta = (float) Math.PI / 6;
			if (goalAngleEditText != null) {
				int in = Integer.valueOf(goalAngleEditText.getText().toString());

				if (in <= 180)
					home_theta = (in * Math.PI) / 180;
				else {
					in = in - 360;
					home_theta = (in * Math.PI) / 180;
				}
			}

			mRobotView.resetRobot();
			//////////2019-09-12
//			robot.setPosition(p.x, p.y, home_theta);
//			double irDistances[] = robot.getIRDistances();
//			mRobotView.setIrDistances(irDistances);
//			ObstacleCrossPoint[] ocps = robot.getObstacleCrossPoints();
//			mRobotView.setObstacleCrossPoints(ocps);

			mRobotView.setRobotPosition(p.x, p.y, home_theta, 0);

//			Vector pp = supervisor.getRecoverPoint();
//			mRobotView.setRecoverPoint(pp.x, pp.y);
//
//			supervisor.reset();

			return true;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent event) {
			// Log.e(TAG, "onDoubleTapEvent");

			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			// Log.e(TAG, "onSingleTapConfirmed");

			float x, y;
			x = event.getX();
			y = event.getY();

			Position p = mRobotView.getRealPosition(x, y);

			
			p.x = (float) ((float)Math.round(p.x*10)/10.0);
			p.y = (float) ((float)Math.round(p.y*10)/10.0);
			
			mRobotView.setTarget(p.x, p.y);

			double angle = Math.PI / 2;
			if (goalAngleEditText != null) {
				int in = Integer.valueOf(goalAngleEditText.getText().toString());

				if (in <= 180)
					angle = (in * Math.PI) / 180;
				else {
					in = in - 360;
					angle = (in * Math.PI) / 180;
				}

			}

			Log.i(TAG, "Set target to:" + p.x + "," + p.y + ": " + angle);

			SendGoToGoalCmd(p.x, p.y);
//			supervisor.setGoal(p.x, p.y, angle);

			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			Log.e(TAG, "onDown");
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// Log.e(TAG, "onShowPress");
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			// Log.e(TAG, "onSingleTapUp");

			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			Log.i(TAG, "onScroll, x=" + distanceX + ", y=" + distanceY);
			mRobotView.onScroll(distanceX, distanceY);

			updateViewScroll();

			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			Log.i(TAG, "onLongPress");
			float x, y;
			x = e.getX();
			y = e.getY();
			Position p = mRobotView.getRealPosition(x, y);

			setRoute = true;

			MainActivity act = (MainActivity) getActivity();
			act.setViewScroll(false, false);

			routeSize = 1;
			mRoutes[0][0] = p.x;
			mRoutes[0][1] = p.y;

			mRobotView.setRoutes(mRoutes, routeSize);

		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			// Log.e(TAG, "onFling");
			return true;
		}
	}

	private void updateViewScroll() {

		MainActivity act = (MainActivity) getActivity();
		boolean scrollLeft = !this.mRobotView.canScrollLeft();
		boolean scrollRight = !this.mRobotView.canScrollRight();
		act.setViewScroll(scrollLeft, scrollRight);
	}

	public void updateSettings(Settings settings) {
//		supervisor.updateSettings(settings);
	}

	public Settings getSettings() {
//		return supervisor.getSettings();
		return null;
	}

	private void curveRoute(float[][] mRoutes, int routeSize) {
		// y= ax+c;

		float[][] routes = new float[2000][2];

		double a, b, c;
		int idx = 0;
		double x1, y1, x2, y2, sqAB, x0, y0, d = 0;

		int k = 0;

		Log.i(TAG, "cs:" + routeSize);

		routes[k][0] = mRoutes[idx][0];
		routes[k][1] = mRoutes[idx][1];
		k = 1;

		while (idx < routeSize - 2) {
			x1 = mRoutes[idx][0];
			y1 = mRoutes[idx][1];
			x2 = mRoutes[idx + 1][0];
			y2 = mRoutes[idx + 1][1];

			if (x2 - x1 != 0) {
				a = (y2 - y2) / (x2 - x1);
				b = 1.0;
				c = y1 - a * x1;
				sqAB = Math.sqrt(a * a + 1);
			} else {
				a = 1;
				b = (x1 - x2) / (y1 - y2);
				c = x1 - b * y1;
				sqAB = Math.sqrt(b * b + 1);
			}

			Log.i(TAG, idx + ": " + x1 + ", " + y1 + "; " + x2 + ", " + y2 + "; a=" + a + " c=" + c + "; d=" + d);

			idx = idx + 2;
			while (idx < routeSize) {
				x0 = mRoutes[idx][0];
				y0 = mRoutes[idx][1];
				d = (a * x0 + b * y0 - c) / sqAB;
				if (Math.abs(d) > 0.08) {
					routes[k][0] = mRoutes[idx - 1][0];
					routes[k][1] = mRoutes[idx - 1][1];
					k++;
					routes[k][0] = mRoutes[idx][0];
					routes[k][1] = mRoutes[idx][1];
					k++;
					break;
				}
				idx++;
			}

			idx--;
		}

		routes[k][0] = mRoutes[idx + 1][0];
		routes[k][1] = mRoutes[idx + 1][1];
		k++;

		mRoutes = routes;
		routeSize = k;
		// Log.i(TAG, idx + ": " + mRoutes[idx][0] + ", " + mRoutes[idx][1]);

	}
	
	
	private String[] msgs = new String[4];
	private int msgCount = 0;


	public void addMessage(String msg )
	{
		if( msgCount < 4 )
			msgs[msgCount++] = msg;
		else
		{
			for( int i=0; i<3; i++)
				msgs[i] = msgs[i+1];
			msgs[3] = msg;
		}
		
		String msgStr = msgs[0]; 
		for( int i=1; i<msgCount; i++)
		{
			
			msgStr = msgStr + "; " + msgs[i];
		}
		if(mMessageTextView != null )
			mMessageTextView.setText( msgStr );
	}
	
	
	private void driveRobot(double v, double w) {

		String label = "[v:" + fmt.format(v) + ", w:" + fmt.format(w) + "]";
		this.mTextViewVW.setText(label);

		// Log.i(TAG, label);

		mV = v;
		mW = w;

		// if( !isGoing )
		// return;

		byte[] value = new byte[6]; // SD v, w

		value[0] = 'S';
		value[1] = 'D';

		// double wInDeg = 2*Math.PI*mW/360;

		doubleToByte(mV, value, 2, 100);
		doubleToByte(mW, value, 4, 100);

		MainActivity act = (MainActivity) getActivity();
		act.SendBLECmd(value);

	}

	private void doubleToByte(double value, byte[] data, int offset, int scale) {
		double val = value * scale;
		int intVal = (int) val;

		if (val < 0)
			intVal = -intVal;

		data[offset] = (byte) (intVal & 0xff);

		if (val >= 0)
			data[offset + 1] = (byte) ((intVal >> 8) & 0xff);
		else
			data[offset + 1] = (byte) (((intVal >> 8) & 0x7f) | 0x80);

	}
	
	
	

	public SlamMap getSlamMap() {
		return slamMap;
	}



	public void setSlamMap(SlamMap slamMap) {
		this.slamMap = slamMap;
        if(mRobotView!= null )
        	mRobotView.setSlamMap(slamMap);

	}
	
	
	@Override
	public void run() {

		while (true) {
			try {

				boolean speedChanged = false;
				if (this.isBraking) {
					if (speed_f > minSpeed) {
						speed_f -= SPEED_STEP;
						speed_f -= SPEED_STEP;
						if (speed_f < minSpeed)
							speed_f = minSpeed;
						speedChanged = true;
					}

					if (speed_r > minSpeed) {
						speed_r -= SPEED_STEP;
						speed_r -= SPEED_STEP;
						if (speed_r < minSpeed)
							speed_r = minSpeed;
						speedChanged = true;
					}
				} else if (this.isSpeeding) {
					if (speed_f < maxSpeed) {
						speed_f += SPEED_STEP;
						if (speed_f > maxSpeed)
							speed_f = maxSpeed;
						speedChanged = true;
					}

				} else if (this.isRewarding) {
					if (speed_r < maxSpeed) {
						speed_r += SPEED_STEP;
						if (speed_r > maxSpeed)
							speed_r = maxSpeed;
						speedChanged = true;
					}

				}

				if (speedChanged) {
					// Give the new state to the Handler so the UI Activity can
					// update
					mHandler.obtainMessage(MESSAGE_NEW_SPEED, -1, -1)
							.sendToTarget();

					// Log.i(TAG, "Speed:" + speed);
				}
				Thread.sleep(200);

			} catch (Exception e) {

			}
		}

	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if (msg.what == MESSAGE_NEW_SPEED) {

				int speed = 0;
				String speedStr = "0";
				if (speed_f > minSpeed) {
					speedStr = "F " + speed_f;
					speed = speed_f;
				} else if (speed_r > minSpeed) {
					speedStr = "R " + speed_r;
					speed = -speed_r;
				}

				
				mV = fMaxRobotSpeed * speed/maxSpeed;
				
				driveRobot(mV, mW);
				

			}
		}
	};
	
	
	private int tcx, tcy;
	

	private OnTouchListener steerOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent event) {

			// Log.i(TAG, "OnTouch:" + event.getX() + "," + event.getY() );

			if (event.getAction() == MotionEvent.ACTION_DOWN) {

				tcx = (int) event.getX();
				tcy = (int) event.getY();
				return true; // to continue to receive ACTION_MOVE
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				// reset the geer
				mSteerView.setRotateAngle(0);
				// MeterFragment.this.sendMessage("TR0;");
				// MeterFragment.this.sendMessage("F" + speed + ";");
				mW = 0;
				driveRobot(mV, mW);

			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				int cx = (int) event.getX();
				int cy = (int) event.getY();

				int x0, y0;
				int[] location = new int[2];
				mSteerView.getLocationOnScreen(location); // getClipBounds();

				x0 = mSteerView.getWidth() / 2;
				y0 = mSteerView.getHeight() / 2;

				int x, y;
				x = x0 - tcx;
				y = y0 - tcy;
				double b = Math.sqrt(x * x + y * y);
				x = x0 - cx;
				y = y0 - cy;
				double c = Math.sqrt(x * x + y * y);

				x = cx - tcx;
				y = cy - tcy;
				double a = Math.sqrt(x * x + y * y);

				if (b * c == 0)
					return false;

				double cos = (b * b + c * c - a * a) / (2 * b * c);
				double rad = Math.acos(cos);
				int ang = (int) (180 * rad / Math.PI);

				// Log.i(TAG, x0 + "," + y0 +";" + tcx + ", " + tcy + "; " + cx
				// + ", " + cy );
				// Log.i(TAG, a + ", " + b + ", " + c);
				// Log.i(TAG, "Touch Ang:" + ang );

				rad = -rad;

				if (tcx < x0 && cy > tcy) {
					ang = -1 * ang;
					rad = -rad;
				} else if (tcx > x0 && cy < tcy) {
					ang = -1 * ang;
					rad = -rad;
				}
				mSteerView.setRotateAngle((float) ang);

				if (Math.abs(mW - rad) > 0.01) {
					mW = rad;
					driveRobot(mV, mW);
				}

			}

			return false;
		};
	};	
	
	
	private void hideViewAnimation(View view) {
		
		/**         
		* 构造方法如下         
		* fromXType、toXType、fromYType、toYType(Animation.ABSOLUTE,、Animation.RELATIVE_TO_SELF、Animation.RELATIVE_TO_PARENT)         
		* 当type为Animation.ABSOLUTE时，这个个值为具体的像素值，当type为Animation.RELATIVE_TO_SELF或Animation.RELATIVE_TO_PARENT，这个个值为比例值，取值范围是[0f, 1.0f]         
		*public TranslateAnimation(int fromXType, float fromXValue, int toXType, float toXValue,int fromYType, float fromYValue, int toYType, float toYValue) {}              
		*/    
		
		int w = view.getWidth();
		int h = view.getHeight();
		
		TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,                
				Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);        
				animation.setDuration(800);        
				//动画重复次数-1表示不停重复        
				//animation.setRepeatCount(-1);
				//动画结束后View停留在结束位置
				//animation.setFillAfter(true);        
				view.startAnimation(animation); 		
	}

	private void showViewAnimation(View view ){
		int w = view.getWidth();
		int h = view.getHeight();
		
		TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.6f,                
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f);        
				animation.setDuration(800);        
				//动画重复次数-1表示不停重复        
				//animation.setRepeatCount(-1);
				//动画结束后View停留在结束位置
				//animation.setFillAfter(true);        
				view.startAnimation(animation); 		
		
	}
	
	
	public void startSlamMap()
	{
		mBeSlamMapping = true;
		mRobotView.startSlamMap();
	}
	
	public void stopSlamMap()
	{
		mBeSlamMapping = false;
		mRobotView.stopSlamMap();
		
	}
	
	public void saveSlamMap()
	{
		
	}

	public void loadSlamMap()
	{
		mBeSlamMapping = false;
	}
}
