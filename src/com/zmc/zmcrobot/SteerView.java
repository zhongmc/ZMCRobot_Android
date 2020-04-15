package com.zmc.zmcrobot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class SteerView extends View {

	
    private Paint mPaint  = new Paint();
    
    private Bitmap steerBitmap;
    
    private Context mContext;
    
    
    private float rotateAngle = -20; 
    
    private float M = getContext().getResources().getDisplayMetrics().densityDpi / 25.4F;   
    private float H; 
    
    private int width, height;
	
	public SteerView(Context paramContext, AttributeSet paramAttributeSet)
	  {
	    super(paramContext, paramAttributeSet);
	    
	    this.mContext = paramContext;
	    width = 0;
	    height = 0;
	    steerBitmap =  BitmapFactory.decodeResource(getResources(),  R.drawable.steer);

		ViewTreeObserver vto = getViewTreeObserver();

		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
			@Override
			public void onGlobalLayout(){ 
				height =getMeasuredHeight(); 
				width = getMeasuredWidth();
	
				
	        	float scaleX, scaleY;
	        	scaleX = (float)width / (float)steerBitmap.getWidth();
	        	scaleY = (float)height / (float)steerBitmap.getHeight();

	        	float scale = Math.min(scaleX, scaleY);
	        	steerBitmap = Bitmap.createScaledBitmap(steerBitmap, (int)(scale * steerBitmap.getWidth()), (int)(scale * steerBitmap.getHeight()), false);
				
			}});
	    
	    
	    
	  }
	
	
	   @Override  
	    protected void onDraw(Canvas canvas) { 
		   
	        super.onDraw(canvas);  
	        
	        if( steerBitmap == null  )
	        {
	 		   mPaint.setColor(-1);
			   mPaint.setTextSize(16.5F);
		        
			   float H = 20; //mPaint¡£measureText("  rpm");
			   String displayStr = "Steer View";
			   canvas.drawText(displayStr, this.width - (this.mPaint.measureText(displayStr) + H) / 2.0F, this.height/2.5F, mPaint);
	        	
	        	return;
	        }
	        
//			   float x = 1.15F; //1.35
		   
//	        int cwidth = canvas.getWidth();
//	        int cheight = canvas.getHeight();
//	        if( cwidth != width || cheight != height ) // size changed???
//	        {
//	        	float scaleX, scaleY;
//	        	scaleX = (float)cwidth / (float)steerBitmap.getWidth();
//	        	scaleY = (float)cheight / (float)steerBitmap.getHeight();
//	        	width = cwidth;
//	        	height = cheight;
//	        	float scale = Math.min(scaleX, scaleY);
//	        	
//	        	steerBitmap = Bitmap.createScaledBitmap(steerBitmap, (int)(scale * this.steerBitmap.getWidth()), (int)(scale * this.steerBitmap.getHeight()), false);
//	        }
	        
//		   canvas.drawBitmap(this.meterBitmap, (this.width - this.meterBitmap.getWidth()) / 2, (this.height - this.meterBitmap.getHeight()) / 2, null);
//		   canvas.save();
		   canvas.rotate(rotateAngle, this.width/2, this.height/2);
		   canvas.drawBitmap(this.steerBitmap, (this.width - this.steerBitmap.getWidth()) / 2, (this.height - this.steerBitmap.getHeight()) / 2, null);
		   canvas.restore();
	 		   
	    }  
	
	   
	   public void setSteerBitmapId(int id )
	   {
		   steerBitmap =  BitmapFactory.decodeResource(getResources(), id);
		   width = 0;
	   }
	   
	   
	   public void setRotateAngle( float value )
	   {
		   this.rotateAngle = value;
		   this.invalidate();
		 //  this.
	   }	
	
}
