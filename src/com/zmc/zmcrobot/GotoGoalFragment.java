package com.zmc.zmcrobot;


//import android.support.v4.app.Fragment;


import com.zmc.zmcrobot.simulator.SlamMap;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * 控制小车页（goto goal ）发送指令，位置；
 * @author zhong_zmwo6t7
 *
 */
public class GotoGoalFragment extends Fragment implements SensorEventListener { // , BTMessageListener {

	
	private RobotView mRobotView;
	
   private SensorManager mSensorManager;
    private Sensor mRotationVectorSensor;
    
	private CheckBox mSimulateCheckBox, mIgnoreObstacleCheckBox;
   
	private boolean simulateMode = false;
	
    private final float[] mRotationMatrix = new float[16];
    
    private TextView speedLabel;
    
    private final static String TAG="GotoGoalFragment";
    private View mView;
    
    private Button mGoButton, mHomeButton; //, mStopButton;
    
    
	private SlamMap slamMap = null;
    
//    private Thread speedBrakeThread = null; // = new Thread(this );

    private boolean mBeconnected = false;
    
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1f;
    private float mScale = 500;
    
    private float targetX, targetY;
    private int tcx,tcy;
    private boolean inScale = false;

    private boolean isGoing = false;
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	
		MainActivity act = (MainActivity)getActivity();
    	act.setRobotFragment(this);
    	
        View rootView = inflater.inflate(R.layout.fragment_goto_goal, container, false);
        mRobotView = (RobotView)rootView.findViewById(R.id.robot_view);
        mRobotView.setSlamMap(slamMap);
        

     //  	mRobotView.setPosition(250, 300, -Math.PI/8, mScale); 

        mGoButton = (Button)rootView.findViewById(R.id.buttonAction);
        mHomeButton = (Button)rootView.findViewById(R.id.uploadButton);
        
		mSimulateCheckBox = (CheckBox)rootView.findViewById(R.id.SimulateCheckBox);
		mSimulateCheckBox.setEnabled( mBeconnected );
		mSimulateCheckBox.setChecked( simulateMode );
		mSimulateCheckBox.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				SetSimulateMode(mSimulateCheckBox.isChecked());
				
			}
			
		});
      
		mIgnoreObstacleCheckBox = (CheckBox)rootView.findViewById(R.id.IgnoreObstacleCheckBox);

		mIgnoreObstacleCheckBox.setEnabled( mBeconnected );
		mIgnoreObstacleCheckBox.setChecked( false );
		mIgnoreObstacleCheckBox.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				SetIgnoreObstacleMode(mIgnoreObstacleCheckBox.isChecked());
				
			}
			
		});
		

		mGoButton.setEnabled( mBeconnected );
		mHomeButton.setEnabled( mBeconnected );
        
        
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
       	
      	
 //      	mRobotView.setOnTouchListener( steerOnTouchListener );
        // Get an instance of the SensorManager
        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        // find the rotation-vector sensor
        mRotationVectorSensor = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ROTATION_VECTOR);
    	
        mSensorManager.registerListener(this, mRotationVectorSensor, 100000);
        
        mScaleDetector = new ScaleGestureDetector(this.getActivity().getApplicationContext(), new ScaleListener());
        
        mView = rootView;
        
        rootView.setOnTouchListener( viewOnTouchListener );
        
        return rootView;
    }

    
    
    @Override        
    public void onAttach(Activity activity) 
    {                
    	super.onAttach(activity);                
    	Log.i(TAG, "onAttach");        
    }        
    
    @Override        
    public void onCreate(Bundle savedInstanceState) 
    {                
    	super.onCreate(savedInstanceState);                
    	Log.i(TAG, "onCreate");        
    }        
    
    @Override        
    public void onActivityCreated(Bundle savedInstanceState) 
    {                
    	super.onActivityCreated(savedInstanceState);                
    	Log.i(TAG, "onActivityCreated");        
    }        
    
    @Override        
    public void onStart() 
    {                
    	super.onStart();                
    	Log.i(TAG, "onStart");        
    }        
    
    @Override        
    public void onResume() 
    {                
    	super.onResume();                
    	Log.i(TAG, "onResume");        
    }        
    
    @Override        
    public void onPause() 
    {                
    	super.onPause();                
    	Log.i(TAG, "onPause");        
    }        
    
    @Override        
    public void onStop() 
    {                
    	super.onStop();                
    	Log.i(TAG, "onStop");        
    }        
    
    @Override        
    public void onDestroyView() 
    {                
    	super.onDestroyView();                
    	Log.i(TAG, "onDestroyView");        
    }        
    
    @Override        
    public void onDestroy() 
    {                
    	super.onDestroy();                
    	Log.i(TAG, "onDestroy");        
    }        
    
    @Override       
    public void onDetach() 
    {                
    	super.onDetach();                
    	Log.i(TAG, "onDetach");        
    }    
    

	@Override
	public void onAccuracyChanged(Sensor sensor, int arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
        // we received a sensor event. it is a good practice to check
        // that we received the proper event
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // convert the rotation-vector to a 4x4 matrix. the matrix
            // is interpreted by Open GL as the inverse of the
            // rotation-vector, which is what we want.
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix , event.values);
         
            //0 X 2*sinQ/2 1 Y 2*sinQ/2
    //        double ang = 2*(180* Math.asin(event.values[0]))/Math.PI;
   //       this.mSteerView.setRotateAngle( (float)ang );
            
       //     Log.i("SENS:",  " ang:" +(180* Math.asin(event.values[0]))/Math.PI + ", " + (180* Math.asin(event.values[1]))/Math.PI + ", " +(180* Math.asin(event.values[2]))/Math.PI);
            
            
        }
        
        if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {

	        float[] values = event.values;
	        float ax = values[0];
	        float ay = values[1];
	
	        double g = Math.sqrt(ax * ax + ay * ay);
	        double cos = ay / g;
	        if (cos > 1) {
	            cos = 1;
	        } else if (cos < -1) {
	            cos = -1;
	        }
	        double rad = Math.acos(cos);
	        
	        double ang = 180*rad /Math.PI;

	//          this.mSteerView.setRotateAngle( (float)ang );
	          	        
	         Log.i(TAG,  rad + ", " + ang );
	        
	        if (ax < 0) {
	            rad = 2 * Math.PI - rad;
	        }
        }
        
/*        if( event.sensor.getType() == Sensor.TYPE_GYROSCOPE ) 角速度
        {
            this.mSteerView.setRotateAngle( event.values[0] );
        	Log.i("SENS:",  event.values[0] + "," + event.values[1] + "," + event.values[2]);
        }
  */      
  	}	

	public void setBeConnected(boolean beConnected )
	{
		mBeconnected = beConnected;
		if( this.mView != null )
			this.updateBLEState(beConnected);
	}
	
	
	private OnTouchListener viewOnTouchListener = new OnTouchListener()
	{
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			
			mScaleDetector.onTouchEvent(event);

			
			if( event.getActionMasked() > 0xff)
				return true;

			if( inScale )
				return true;
			
			//	Log.i(TAG,  "OnTouch:"  +  event.getX() + "," + event.getY() );
	           if(event.getAction() == MotionEvent.ACTION_DOWN)
	           {
	        	   	tcx = (int) event.getX();  
	                tcy = (int) event.getY();  
	                return true;  //to continue to receive ACTION_MOVE
	           }
	           else if( event.getAction() == MotionEvent.ACTION_UP)
	           {
	        	   //reset the geer
	                
	        	   	float x, y;
	        	   	x = event.getX(); 
	                y = event.getY();	                
	                
	                if( tcx == (int)x && tcy == (int)y)
	                {
//	                	if(isGoing)  
//	                		return true;
	                	
						Log.i(TAG, "Set target to:" + x + "," + y);
						
						double width,height;
						width = mRobotView.width;
						height = mRobotView.height;
						
						//change to meter
						targetX = (float)((x - width/2)/500/mScaleFactor);
						targetY = (float)((height/2 - y)/500/mScaleFactor);
						
						targetX = (float) ((float)Math.round(targetX*10)/10.0);
						targetY = (float) ((float)Math.round(targetY*10)/10.0);
						
						Log.i(TAG, "Set target to(in meter):" + targetX + "," + targetY);
		                
						mRobotView.setTarget(targetX, targetY);
						SendGoToGoalCmd(targetX, targetY);
						
		         		return true;
	                }
	                return false;
	           }
			
	           return false;
			
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
    	
    }
    
	private void SendGoToGoalCmd(float targetX, float targetY)
	{
	
		Log.i(TAG,"Go to Goal: " + targetX + ", "+ targetY);
		byte[] value = new byte[10];
		
		value[0] = 'G';
		value[1] = 'G';
		
		int tmp = (int) (targetX * 100.0);

		intToByte(tmp, value, 2);
		
		tmp =  (int) (targetY * 100.0);
		intToByte(tmp, value, 4);
		
		//theta
		tmp = 0;
		value[6] =  (byte)(tmp&0xff);
		value[7] = (byte) (tmp>>8);
		
		tmp = 12; //v= 0.12
		intToByte(tmp, value, 8);
		
		MainActivity act = (MainActivity)getActivity();
		act.SendBLECmd(value);
	};
	

	
	private void intToByte(int value, byte[] buf, int offset)
	{
		boolean sig = false;
		if( value < 0 )
		{
			value = -value;
			sig = true;
		}
		buf[offset] =  (byte)(value&0xff);
		buf[offset+1] = (byte) ((value>>8)&0x7f);

		if( sig )
			buf[offset+1] = (byte)(buf[offset+1]|0x80);
		
	}
	
	
	   private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {
			@Override
	        public boolean onScale(ScaleGestureDetector detector) {
				mScaleFactor *= detector.getScaleFactor();

				// Make sure we don't get too small or too big
				mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

				mScale = 500 * mScaleFactor;
				mRobotView.setScale( mScale );
				
				Log.i(TAG, "in onScale, scale factor = " + mScaleFactor);
				//mView.invalidate();
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
	   
		public void setRobotState( RobotState state )
		{
			mRobotView.setRobotState(state);
		}

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
		
		public void updateBLEState(boolean bConnected )
		{
			
			Log.i(TAG, "Update connect state " + bConnected );
			mBeconnected = bConnected;
			mGoButton.setEnabled( bConnected );
			mHomeButton.setEnabled( bConnected );
			mIgnoreObstacleCheckBox.setEnabled( bConnected );
			mSimulateCheckBox.setEnabled( bConnected );
			mView.invalidate();
		}



		public SlamMap getSlamMap() {
			return slamMap;
		}



		public void setSlamMap(SlamMap slamMap) {
			this.slamMap = slamMap;
	        if(mRobotView!= null )
	        	mRobotView.setSlamMap(slamMap);

		}
	
}

