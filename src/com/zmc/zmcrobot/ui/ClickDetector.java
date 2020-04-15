package com.zmc.zmcrobot.ui;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;

public class ClickDetector implements Runnable {

	   private Thread clickDetectThread = null; // = new Thread(this );
	   private ClickListener mListener;

	   private float x,y;
	   private int prevX, prevY;
	   private MotionEvent mEvent;
	   private boolean mEventHandled = true; 
	   
	public ClickDetector(ClickListener listener) {
		mListener = listener;
		clickDetectThread = new Thread(this);
		clickDetectThread.start();
		
	}

	public boolean onTouchEvent(MotionEvent event)
	{
        if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP )
        {
			mEventHandled = false;
			if( event.getAction() == MotionEvent.ACTION_UP )
			{
				prevX = (int)x;
				prevY = (int)y;
			}
			x = event.getX();
			y = event.getY();
			mEvent = event;
			clickDetectThread.interrupt();
			
			return true;
        }
        return false;
	}
	
	

	@Override
	public void run() {
	

		long[] mHits = new long[2];
		long lastDownTime = 0;
		while( true )
		{
			if( !mEventHandled )
			{
				try{
		           if(mEvent.getAction() == MotionEvent.ACTION_DOWN)
		           {
		        	   mEventHandled = true;
		        	   lastDownTime = SystemClock.uptimeMillis();
		           }
		           else if( mEvent.getAction() == MotionEvent.ACTION_UP)
		           {
		        	   long touchTime = SystemClock.uptimeMillis() - lastDownTime;
		        	   if( touchTime > 1000 && touchTime < 1500 )
		        	   {
		        		   mHandler.obtainMessage(MESSAGE_LONG_CLICKED, -1, -1).sendToTarget();
		        		   mEventHandled = true;
		        	   }
		        	   else  //check for double click
		        	   {
		        		   int x1,y1;
		        		   x1 = (int)x;
		        		   y1 = (int)y;
		        		   if( x1 == prevX && y1 == prevY )
		        		   {
				        	   System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
				        	   mHits[mHits.length - 1] = SystemClock.uptimeMillis();
				        	   if (mHits[0] >= (SystemClock.uptimeMillis() - 500))  //Ë«»÷ÊÂ¼þ
				        	   {
				        		   mHandler.obtainMessage(MESSAGE_DOUBLE_CLICKED, -1, -1).sendToTarget();
				        		   mEventHandled = true;
				        	   }
		        		   }
		        	   }
		           }
		           else
		           {
		        	   mEventHandled = true;
		           }
	
		           Thread.sleep(500);
		           if( !mEventHandled )
		           {
		        	   mEventHandled = true;
	        		  mHandler.obtainMessage(MESSAGE_CLICKED, -1, -1).sendToTarget();
		           }
		           
				}catch(Exception e)
				{
					
				}
			}
			else
			{
				try{
		           Thread.sleep(1000);
				}catch(Exception e)
				{
					
				}
			
			}
		}
	}
	
	
	   private static final int MESSAGE_CLICKED = 1;
	   private static final int MESSAGE_DOUBLE_CLICKED = 2;
	   private static final int MESSAGE_LONG_CLICKED = 3;

	
	   private final Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {

	        	if( msg.what == MESSAGE_CLICKED )
	        	{
	        		mListener.onClicked(x, y);

	        	}
	        	else if( msg.what == MESSAGE_DOUBLE_CLICKED )
	        	{
	        		mListener.onDoubleClicked(x,y);
        		
	        	}
	        	
	        	else if( msg.what == MESSAGE_LONG_CLICKED )
	        	{
	        		mListener.onLongClicked(x,y);
	        		
	        	}
	        }
	    };
			
}
