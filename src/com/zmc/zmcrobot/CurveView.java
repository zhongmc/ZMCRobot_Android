package com.zmc.zmcrobot;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class CurveView extends View {

	private int mTrailSize = 900;
	private float[][] mTrails; // = new float[1000][2];
	private int mTrailCount = 0;
		
    private Paint mPaint  = new Paint();
    private Context mContext;
    
    public int width = 0, height;
  
    private int xStep = 5;
    
    private int startIdx = 0;
   
    private DecimalFormat fmt = new DecimalFormat("#0.00");
    
    private boolean mShowCurve[] = new boolean[5];
    

    private final static String TAG="CurveView";
    
    private float maxValue = 0; 
    private int curveCount = 1; 
    
	public void addData(double[] data)
	{
		if( mTrails == null ) //not ready ignore the data
			return;
		
		float value;
		int idx = mTrailCount;
		if( mTrailCount == mTrailSize-1 )
		{
			idx = startIdx;
			startIdx++;
			if( startIdx >= mTrailSize )
				startIdx = 0;
		}
		for( int i=0; i<curveCount; i++ )
		{
			if( i < data.length )
				value = (float)data[i];
			else
				value = 0;
			
			mTrails[i][idx] = value;
			if( this.mShowCurve[i] &&  Math.abs(value) > maxValue )
				maxValue = (float)1.2*Math.abs( value );
		}
		
		if( mTrailCount < (mTrailSize-1))
			mTrailCount++;
		
		this.invalidate();

	}
	
	public void setCurveCount(int value )
	{
		curveCount = value;
		if( width != 0 )
		{
			mTrails = new float[curveCount][(int)width/xStep];
		}
		mTrailCount = 0;
		startIdx = 0;
		this.invalidate();
	}
	
	public void showCurve(boolean[] showCurve )
	{
		mShowCurve = showCurve;
	}
	
	public void setCurveColor(int cols[])
	{
		for( int i=0; i<cols.length; i++)
			colors[i] = cols[i];
		
	}
	
	public CurveView(Context paramContext, AttributeSet paramAttributeSet)
	  {
	    super(paramContext, paramAttributeSet);
	    
	    this.mContext = paramContext;
	    width = 0;
	    height = 0;

	    ViewTreeObserver vto = getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
			@Override
			public void onGlobalLayout(){ 
			//	getViewTreeObserver().removeGlobalOnLayoutListener(this);
				height =getMeasuredHeight(); 
				width = getMeasuredWidth();
				if( mTrails == null )
				{
					mTrails = new float[curveCount][(int)width/xStep];
					mTrailSize = width/xStep;
				}
			}});
	
		colors[0] = Color.CYAN;
		colors[1] = Color.RED;
		colors[2] = Color.BLUE;
		colors[3] = Color.YELLOW;
		colors[4] = Color.GREEN;
		colors[5] = Color.WHITE;
		
		mShowCurve[0] = true;
		mShowCurve[1] = true;
		mShowCurve[2] = true;
		mShowCurve[3] = true;
		mShowCurve[4] = true;
		
		
		
	  }
	
	int colors[] = new int[6];
	
	   @Override  
	    protected void onDraw(Canvas canvas) { 
		   
	        super.onDraw(canvas);  

	        Paint paint = mPaint; //new Paint();

	        paint.setColor(Color.BLACK);
	        canvas.drawRect(0, 0, width, height, paint);

	        
	        double curPos = 0;
	        double step = 20;
	        int idx = 0;
	        while(true)
	        {
	        	if( idx%5 == 0)
	        	{
	    	        paint.setColor(Color.WHITE);
	        	}
	        	else
	        	{
	    	        paint.setColor(Color.DKGRAY);
	        	}
	        	canvas.drawLine((float)(width/2+curPos), 0, (float)(width/2+curPos), (float)(height), paint);
		        canvas.drawLine((float)(width/2-curPos), 0, (float)(width/2-curPos), (float)(height), paint);

		        idx++;
	        	curPos = curPos + step;
	        	if( curPos > width/2)
	        		break;
	        	
	        }
	        
	        float mScale = ((height-20)/2)/maxValue;
	        
	        float labelValue = 0;
	        
		       paint.setTextSize(30);
		       paint.setColor(Color.DKGRAY);

		       String label = fmt.format( labelValue );

		   //    canvas.drawText(label, 5, 35, paint);
		   //    canvas.drawText(label, 5, height - 5, paint);
	
	        curPos = 0;
	        step = 20;
	        idx =0;
	        while(true)
	        {
	        	if( idx%5 == 0)
	        	{
	        		
	    	        paint.setColor(Color.DKGRAY);
	        		label = fmt.format( labelValue );
	        		if( idx == 0 )
	        		{
	 	 		       canvas.drawText(label, 5, (float)(height/2 + curPos-5), paint);
	        			
	        		}
	        		else
	        		{
		 	 		       canvas.drawText("-"+label, 5, (float)(height/2 + curPos -5), paint);
					       canvas.drawText(label, 5, (float)(height/2- curPos - 5), paint);
	        			
	        		}
	    	        paint.setColor(Color.WHITE);
	        	}
	        	else
	        	{
	    	        paint.setColor(Color.DKGRAY);
	        	}
	        	
	        	canvas.drawLine(0, (float)(height/2 + curPos), (float)(width), (float)(height/2 + curPos), paint);
		        canvas.drawLine(0, (float)(height/2- curPos), (float)(width), (float)(height/2 - curPos), paint);
        	
	        	curPos = curPos + step;
	        	if( curPos > height/2)
	        		break;
	        	labelValue = (float) (labelValue + step/mScale);
	        	idx++;
	        }
		        
	        paint.setColor(Color.LTGRAY);
	        canvas.drawLine(0, 1, width, 1, paint);
	        canvas.drawLine(0, height-2, width, height-2, paint);
	      //  canvas.drawLine(0, height/2, width, height/2, paint);
	        

	        
	        if( mTrails == null )
	        	return;

//	        if( maxValue == 0 )
//	        	maxValue = 1;
	        

	        maxValue = 0;
	        
	        int count = 0;
	        //draw the trails of the robot
	        paint.setColor(Color.BLUE);
	        float xs, ys, xe,ye;
	       for( int k =0; k<curveCount; k++)
	       {
	    	   if( mShowCurve[k] == false )
	    		   continue;
	    	   
	    	    count = 1;
		        paint.setColor(colors[k%6]);
		        int i = startIdx;
		        xs = 0;
		        ys = (float)( height/2- mTrails[k][0] * mScale);
		        if( maxValue < Math.abs( mTrails[k][i]))
		        	maxValue = (float)1.2*Math.abs( mTrails[k][i]);
		        i++;
		        if( i >= mTrailSize )
		        	i=0;
		        while( count <= mTrailCount )
		        {	
		        	xe = xs+xStep;
		        	ye = (float)(height/2- mTrails[k][i] * mScale);
		        	canvas.drawLine(xs,ys,xe,ye, paint);
		        	xs = xe;
		        	ys = ye;
			        if( maxValue < Math.abs( mTrails[k][i]))
			        	maxValue = Math.abs( mTrails[k][i]);
			        i++;
			        if( i >= mTrailSize )
			        	i=0;
			        count++;
		        }
	       }
	       
	    }  		
}
