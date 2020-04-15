package com.zmc.zmcrobot;

import java.text.DecimalFormat;
import java.util.List;

import com.zmc.zmcrobot.simulator.SlamMap;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * 手动驾驶，控制小车
 * @author zhong_zmwo6t7
 *
 */
public class DriverFragment extends Fragment implements Runnable , SensorEventListener{

//	private ImageButton mBrakeButton, mIBSpeed, mIBSpeedR;

	private final static String TAG = "DriverFragment";
//	private SteerView mSteerView;
	
	private JoystickView mJoystickView;
	private JoystickView mAngleJoystickView;
	
	private RobotView mRobotView;
	private CheckBox mSimulateCheckBox, mGravityCheckBox;
	private Button mHomeButton;
	
	private SlamMap slamMap = null;
	
	double m_x_angle, m_y_angle;
	
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    
    private Sensor mGyroscopeSensor;
//    private final float[] mRotationMatrix = new float[16];
	
	private boolean simulateMode = false;
	private boolean gravityMode = false;
	
	// private Sensor mRotationVectorSensor;
	// private final float[] mRotationMatrix = new float[16];

	RobotState mRobotState;

	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1f;
	private float mScale = 500;

	private int tcx, tcy;

	private DecimalFormat fmt = new DecimalFormat("#0.00");

	private long lastDriveTime = System.currentTimeMillis();
	// the drive param
	private double mV = 0, mW = 0;

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

	private boolean inScale = false;
	private TextView mTextViewVW;
	private boolean mConnected = false;
private View mView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.fragment_driver_new,
				container, false);
//		mSteerView = (SteerView) rootView.findViewById(R.id.bSteerView);
		
		
		
		mRobotView = (RobotView) mView.findViewById(R.id.robot_view);

		mRobotView.setSlamMap(slamMap);			

		mJoystickView = (JoystickView)	mView.findViewById(R.id.joystickView);
		mJoystickView.setJoystickEventListener( new JoystickEventListener(){

			@Override
			public void onJoystickAction(JoystickEvent event) {
				// TODO Auto-generated method stub

				double v = 0.5 * event.throttle;
				if( Math.abs( v - mV ) < 0.02 ) //拒绝手抖，避免频繁改变
					return;
                mV = v;
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
				double w = event.angle;
				if( Math.abs( w - mW ) < 0.01 ) //拒绝手抖，避免频繁改变
					return;
				
                if (Math.abs(w) > 0.05)
                    mW = 1 * w;
                else
                    mW = 0;
                driveRobot(mV,mW);
                
//                thetaLabel.setText(String.format("%.2f", mTheta));
//                speedLabel.setText(String.format("%.2f", mSpeed));
//                driveSupervisor.setDriveGoal(mSpeed, mTheta);
				
			}
			
		});		
		
		mRobotView.setOnTouchListener(viewOnTouchListener);

		mHomeButton = (Button)mView.findViewById(R.id.homeButton);
		
        mHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetRobot();
            }
        });
		
		
		mTextViewVW = (TextView) mView.findViewById(R.id.textViewVW);

		mSimulateCheckBox = (CheckBox)mView.findViewById(R.id.SimulateCheckBox);
		mSimulateCheckBox.setEnabled( false );
		mSimulateCheckBox.setChecked( simulateMode );
		mSimulateCheckBox.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				SetSimulateMode(mSimulateCheckBox.isChecked());
				
			}
			
		});
		
		mGravityCheckBox = (CheckBox)mView.findViewById(R.id.GravityCheckBox);
		mGravityCheckBox.setEnabled( false );
		mGravityCheckBox.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				SetGravityMode(mGravityCheckBox.isChecked());
				
			}
			
		});



		if( mConnected )
		{
			mHomeButton.setEnabled(true);
			mSimulateCheckBox.setEnabled(true);
		}
		else
		{
			mHomeButton.setEnabled(false);
			mSimulateCheckBox.setEnabled(false);
			
		}
		
		String label = "[v:" + fmt.format(mV) + ", w:" + fmt.format(mW) + "]";
		mTextViewVW.setText(label);

		MainActivity act = (MainActivity) getActivity();
		// act.setBalanceDriveFragment(this);

		mScaleDetector = new ScaleGestureDetector(this.getActivity()
				.getApplicationContext(), new ScaleListener());

//		mSteerView.setRotateAngle(0);
//		mSteerView.setOnTouchListener(steerOnTouchListener);

		
        if( speedBrakeThread == null )
        {
        	speedBrakeThread = new  Thread(this);
        	speedBrakeThread.start();
        }
		
        
        // Get an instance of the SensorManager
        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        
        
        //获取当前设备支持的传感器列表        
        
		List<Sensor> allSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		StringBuilder sb = new StringBuilder();
		sb.append("当前设备支持传感器数：" + allSensors.size() + "   分别是：\n\n");
		for (Sensor s : allSensors) {
			switch (s.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				sb.append("加速度传感器(Accelerometer sensor)" + "\n");
				break;
			case Sensor.TYPE_GYROSCOPE:
				sb.append("陀螺仪传感器(Gyroscope sensor)" + "\n");
				break;
			case Sensor.TYPE_LIGHT:
				sb.append("光线传感器(Light sensor)" + "\n");
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				sb.append("磁场传感器(Magnetic field sensor)" + "\n");
				break;
			case Sensor.TYPE_ORIENTATION:
				sb.append("方向传感器(Orientation sensor)" + "\n");
				break;
			case Sensor.TYPE_PRESSURE:
				sb.append("气压传感器(Pressure sensor)" + "\n");
				break;
			case Sensor.TYPE_PROXIMITY:
				sb.append("距离传感器(Proximity sensor)" + "\n");
				break;
			case Sensor.TYPE_TEMPERATURE:
				sb.append("温度传感器(Temperature sensor)" + "\n");
				break;
			default:
				sb.append("其他传感器" + "\n");
				break;
			}
			sb.append("设备名称：" + s.getName() + "\n 设备版本：" + s.getVersion()
					+ "\n 供应商：" + s.getVendor() + "\n\n");
		}

		Log.d("TAG", "sb.toString()----:" + sb.toString());        
        
        // find the rotation-vector sensor
        mAccelerometerSensor = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);
    	
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        
        if( mAccelerometerSensor == null )
        	Log.e(TAG, "No Accelerometer sensor !");

                
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

    	if( gravityMode && mSensorManager != null )
    	{
    		accelerInit = false;
    		mSensorManager.registerListener(this,
    				mAccelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    		
    		mSensorManager.registerListener(this, 
    				mGyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    	}
 		
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
    	if( gravityMode && mSensorManager != null )
    	{
    		mSensorManager.unregisterListener(this);
    	}
		
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
		
       	if( mSensorManager != null )
    	{
    		mSensorManager.unregisterListener(this);
    	}
		

	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i(TAG, "onDetach");
	}

	private OnTouchListener viewOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent event) {

			mScaleDetector.onTouchEvent(event);

			if (event.getActionMasked() > 0xff)
				return true;

			if (inScale)
				return true;

			return true;

		};
	};

	private class ScaleListener implements
			ScaleGestureDetector.OnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();

			// Make sure we don't get too small or too big
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

			mScale = 500 * mScaleFactor;
			mRobotView.setScale(mScale);

			Log.i(TAG, "in onScale, scale factor = " + mScaleFactor);
			// mView.invalidate();
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector arg0) {
			// TODO Auto-generated method stub
			Log.i(TAG, "Begin scale...");

			inScale = true;
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector arg0) {
			// TODO Auto-generated method stub
			inScale = false;
			Log.i(TAG, "scale end.");
		}
	}

	public void setRobotState(RobotState state) {
		mRobotState = state;
		if( state.sType == 1)
			this.mRobotView.setIrDistances(state.obstacles);
		mRobotView.setPosition(state.x, state.y, state.theta);
	}

	
	public void setBeConnected(boolean beConnected )
	{
		mConnected = beConnected;
		if( this.mView != null )
			this.updateBLEState(beConnected);
	}

	public void updateBLEState(boolean bConnected) {
		
		mConnected = bConnected;
		if( mConnected )
		{
			mHomeButton.setEnabled(true);
			mSimulateCheckBox.setEnabled(true);
		}
		else
		{
			mHomeButton.setEnabled(false);
			mSimulateCheckBox.setEnabled(false);
			
		}
		
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
//				mSteerView.setRotateAngle(0);
				// MeterFragment.this.sendMessage("TR0;");
				// MeterFragment.this.sendMessage("F" + speed + ";");
				mW = 0;
				driveRobot(mV, mW);

			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				int cx = (int) event.getX();
				int cy = (int) event.getY();

				int x0 = 0, y0 = 0;
				int[] location = new int[2];
//				mSteerView.getLocationOnScreen(location); // getClipBounds();
//				x0 = mSteerView.getWidth() / 2;
//				y0 = mSteerView.getHeight() / 2;

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
//				mSteerView.setRotateAngle((float) ang);

				if (Math.abs(mW - rad) > 0.01) {
					mW = rad;
					driveRobot(mV, mW);
				}

			}

			return false;
		};
	};

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
	
	
	public void SetSimulateMode( boolean sm )
	{
		Log.i(TAG, "Set simulate mode " + sm);
		byte[] value = new byte[4];
		
		value[0] = 'S';
		value[1] = 'M';
		
		if( sm )
			value[2] = 1;
		else
			value[2] = 0;
		
		
		
		MainActivity act = (MainActivity)getActivity();
		act.SendBLECmd(value);
		
		simulateMode = sm;
	}
	
	public void SetGravityMode( boolean val )
	{
		Log.i(TAG, "Set gravity mode " + val );
		gravityMode = val;
		if( val )
		{
			mSensorManager.unregisterListener( this );

			accelerInit = false;
    		mSensorManager.registerListener(this,
    				mAccelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    		
    		mSensorManager.registerListener(this, 
    				mGyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
			
		}
		else
		{
			mSensorManager.unregisterListener( this );
		}
	}
	
	   private void resetRobot()
	    {
	    	
	    	mRobotView.resetRobot();
	    	byte[] value = new byte[4];
	    	value[0] = 'R';
	    	value[1] = 'S';
			MainActivity act = (MainActivity)getActivity();
			act.SendBLECmd(value);
	    	
	    }


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}


	// Create a constant to convert nanoseconds to seconds.
	private static final float NS2S = 1.0f / 1000000000.0f;
	
	private static final double RAD_TO_DEG = 57.295779513082320876798154814105;
	private float timestamp = 0;	
	
	private float gyroX, gyroY, gyroZ;
	private float aX, aY,aZ;
	
	private boolean accelerInit = false;
	@Override
    public void onSensorChanged(SensorEvent event) {

	       if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {

		        float[] values = event.values;
		         aX = values[0];
		         aY = values[1];
		         aZ = values[2];
		        
		        if( !accelerInit )
		        {
		        	accelerInit = true;
		        	m_x_angle = Math.atan2(aX, aZ) * RAD_TO_DEG;
		        	m_y_angle = Math.atan2(aY, aZ) * RAD_TO_DEG;
        	
		        }
		        
		        if( mGyroscopeSensor == null && accelerInit )
		        {
		        	double xang = Math.atan2(aX, aZ) * RAD_TO_DEG;
		        	double yang = Math.atan2(aY, aZ) * RAD_TO_DEG;
		        	
		        	m_x_angle = 0.8*m_x_angle + 0.2 * xang;
		        	m_y_angle  = 0.8*m_y_angle + 0.2 * yang;
		        	
   		         	double v,w;
   		         	w = m_x_angle;
   		         	v = m_y_angle;
   		        

//   					this.mSteerView.setRotateAngle( (float)w );
   					mW = w;
   					
   					String label = "[v:" + fmt.format(v) + ", w:" + fmt.format(w) + "]";
   					this.mTextViewVW.setText(label);
   		      
		        	
		        }
		        
	       }
	       
	       else if( Sensor.TYPE_GYROSCOPE == event.sensor.getType())
	       {
 		      float axisX = event.values[0];
 		      float axisY = event.values[1];
 		      float axisZ = event.values[2];
	    	   
	    	   if (timestamp != 0 && accelerInit) {
	    		      final float dT = (event.timestamp - timestamp) * NS2S;
	    		      // Axis of the rotation sample, not normalized yet.

	    		      double angle_accX = Math.atan2((double)aX, (double)aZ) * RAD_TO_DEG;
	    		      m_x_angle = estima_cal(m_x_angle, angle_accX, gyroY, dT, 0.05);
	    		      
	    		      double angle_accY = Math.atan2((double)aY, (double)aZ) * RAD_TO_DEG;
	    		      m_y_angle = estima_cal(m_y_angle, angle_accY, gyroX, dT, 0.05);
    		      
	    		      
	    		         double v,w;
	    		         w = m_x_angle;
	    		         v = m_y_angle;
	    		        

//	    					this.mSteerView.setRotateAngle( (float)w );
	    					mW = w;
	    					
	    					String label = "[v:" + fmt.format(v) + ", w:" + fmt.format(w) + "]";
	    					this.mTextViewVW.setText(label);
	    		      
	    	   }
	    	   
	    	   gyroX = axisX;
	    	   gyroY = axisY;
	    	   gyroZ = axisZ;
		       timestamp = event.timestamp;
	       }
	       
	       


    }	
	

	
	double estima_cal(double angle, double g_angle, double gyro, double dt, double KG)
	{
	  double result = KG * g_angle + (1 - KG) * (angle + gyro * dt);
	  return result;
	}	
	
	//@Override
	public void onSensorChanged1(SensorEvent event) {
		
//		Log.i(TAG, "Sensor changed:" + event);
        // we received a sensor event. it is a good practice to check
        // that we received the proper event
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // convert the rotation-vector to a 4x4 matrix. the matrix
            // is interpreted by Open GL as the inverse of the
            // rotation-vector, which is what we want.
//            SensorManager.getRotationMatrixFromVector(
//                    mRotationMatrix , event.values);
         
            //0 X 2*sinQ/2 1 Y 2*sinQ/2
    //        double ang = 2*(180* Math.asin(event.values[0]))/Math.PI;
   //       this.mSteerView.setRotateAngle( (float)ang );
            
       //     Log.i("SENS:",  " ang:" +(180* Math.asin(event.values[0]))/Math.PI + ", " + (180* Math.asin(event.values[1]))/Math.PI + ", " +(180* Math.asin(event.values[2]))/Math.PI);
            
            
        }
        
        if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {

	        float[] values = event.values;
	        float ax = values[0];
	        float ay = values[1];
	        float az = values[2];
	        
//	        double rad2Deg = 180/Math.PI;
//	        double angY = Math.atan2(ay, az)*rad2Deg;
//	        double angX = Math.atan2(ax, az)*rad2Deg;
//	        double angZ = Math.atan2(az, ax)*rad2Deg;

	        
	//         Log.i(TAG,  angX + ", " + angY + "," +  angZ );

       
//	        double g = Math.sqrt(ax * ax + ay * ay);
//	        double cos = ay / g;
//	        if (cos > 1) {
//	            cos = 1;
//	        } else if (cos < -1) {
//	            cos = -1;
//	        }
//	        double rad = Math.acos(cos);
//	        
//	        double ang = 180*rad /Math.PI;

	         double v,w;
	         w = ax;
	         v = ay;
	        
				if(System.currentTimeMillis() - lastDriveTime < 500)
					return;
				if(  Math.abs(w - mW ) < 0.5)
					return;

//				this.mSteerView.setRotateAngle( (float)w );

				lastDriveTime = System.currentTimeMillis();
				mW = w;
				
				String label = "[v:" + fmt.format(v) + ", w:" + fmt.format(w) + "]";
				this.mTextViewVW.setText(label);
				
//				driveRobot(mV, w);
	        	 
	         
	        	 
//	         Log.i(TAG,  rad + ", " + ang );
//	        
//	        if (ax < 0) {
//	            rad = 2 * Math.PI - rad;
//	        }
        }
        
/*        if( event.sensor.getType() == Sensor.TYPE_GYROSCOPE ) 角速度
        {
            this.mSteerView.setRotateAngle( event.values[0] );
        	Log.i("SENS:",  event.values[0] + "," + event.values[1] + "," + event.values[2]);
        }
  */      
  	}

	public SlamMap getSlamMap() {
		return slamMap;
	}

	public void setSlamMap(SlamMap slamMap) {
		this.slamMap = slamMap;
		if( mRobotView != null )
			mRobotView.setSlamMap(slamMap);			
	}	
	
}
