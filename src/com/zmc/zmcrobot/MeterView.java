package com.zmc.zmcrobot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class MeterView extends View {

    private Paint mPaint  = new Paint();
    
    private Bitmap meterBitmap;
    private Bitmap needleBitmap;
    private Bitmap wheelBitmap;
    
    private Context mContext;
    
    private int meterValue = 300;
    private int meterMin = 0, meterMax = 1000;
    
    private float rotateAngle = 40; 
    
    private float M = getContext().getResources().getDisplayMetrics().densityDpi / 25.4F;   
    private float H; 
    
    private String displayStr = "200";
    
    private int width, height;
	
	public MeterView(Context paramContext, AttributeSet paramAttributeSet)
	  {
	    super(paramContext, paramAttributeSet);
	    
	    this.mContext = paramContext;
	    width = 0;
	    height = 0;
	    
	    meterBitmap =  BitmapFactory.decodeResource(getResources(),  R.drawable.meter_sound);
		needleBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.needle_sound);
		wheelBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wheel);
		
	  }
	
	
	   @Override  
	    protected void onDraw(Canvas canvas) { 
		   
	        super.onDraw(canvas);  
	        
	        
	        
	        if( meterBitmap == null || needleBitmap == null || wheelBitmap == null  )
	        {
	 		   mPaint.setColor(-1);
			   mPaint.setTextSize(16.5F);
		        
			 //  float H = 20; //mPaint¡£measureText("  rpm");
			   displayStr = "Meter View";
			   canvas.drawText(displayStr, (this.width - this.mPaint.measureText(displayStr)) / 2.0F, this.height/2.5F, mPaint);
	        	
	        	return;
	        }
	        
			   float x = 1.15F; //1.35
		   
	        int cwidth = canvas.getWidth();
	        int cheight = canvas.getHeight();
	        
	        float fontSize;
	        int text_Y;

	        fontSize = cheight*30/502;
	        text_Y = cheight*370/502;
	        
	        if( cwidth != width || cheight != height ) // size changed???
	        {
	        	float scaleX, scaleY;
	        	scaleX = (float)cwidth / (float)meterBitmap.getWidth();
	        	scaleY = (float)cheight / (float)meterBitmap.getHeight();
	        	width = cwidth;
	        	height = cheight;
	        	float scale = Math.min(scaleX, scaleY);
	        	
	        	meterBitmap = Bitmap.createScaledBitmap(meterBitmap, (int)(scale * this.meterBitmap.getWidth()), (int)(scale * this.meterBitmap.getHeight()), false);
	            needleBitmap = Bitmap.createScaledBitmap(this.needleBitmap, (int)(scale * this.needleBitmap.getWidth()), (int)(scale * this.needleBitmap.getHeight()), false);
	            wheelBitmap = Bitmap.createScaledBitmap(this.wheelBitmap, (int)(scale * this.wheelBitmap.getWidth()), (int)(scale * this.wheelBitmap.getHeight()), false);
	            mPaint.setTextSize(4.0F * this.M * x);
	           // H = this.mPaint.measureText("  rpm");
	        }
	        
		   canvas.drawBitmap(this.meterBitmap, (this.width - this.meterBitmap.getWidth()) / 2, (this.height - this.meterBitmap.getHeight()) / 2, null);
		   canvas.save();
		   canvas.rotate(rotateAngle, this.width/2, this.height/2);
		   canvas.drawBitmap(this.needleBitmap, (this.width - this.needleBitmap.getWidth()) / 2, (this.height - this.needleBitmap.getHeight()) / 2, null);
		   canvas.restore();
		   canvas.drawBitmap(this.wheelBitmap, (this.width - this.wheelBitmap.getWidth()) / 2, (this.height - this.wheelBitmap.getHeight()) / 2, null);
		   
		   
		   mPaint.setColor(-1);
		   mPaint.setTextSize(fontSize);
	        
		   float y0 =  (this.height - this.meterBitmap.getHeight()) / 2;
		//   float H = 20; //mPaint¡£measureText("  rpm");
		   float w = this.mPaint.measureText(displayStr);
		   canvas.drawText(displayStr, (this.width - w )/ 2.0F, y0 + text_Y, mPaint); //this.height - this.meterBitmap.getHeight() / 3.5F
		//   mPaint.setTextSize(3.0F * this.M * x);
		//   canvas.drawText("  rpm", (this.width - (w + H) )/ 2.0F + w, y0 + this.height - this.meterBitmap.getHeight() / 3.5F, mPaint);
	 		   
	    }  
	
	   
	   public void setMeterBitmapId(int id )
	   {
		   width = 0; //reset
		   height = 0;
		   meterBitmap =  BitmapFactory.decodeResource(getResources(), id);
	   }
	   
	   public void setNeedleBitmapId(int id )
	   {
		   width = 0; //reset
		   height = 0;
		   needleBitmap =  BitmapFactory.decodeResource(getResources(), id);
		   
	   }
	   
	   public void setWheelBitmapId(int id )
	   {
		   width = 0; //reset
		   height = 0;
		   wheelBitmap = BitmapFactory.decodeResource(getResources(), id);
	   }
	   
	   public void setMeterValue( int value )
	   {
		   this.meterValue = value;
		//   this.displayStr = "" + value;
		   
		   rotateAngle = 250*(value-meterMin )/(meterMax - meterMin);
		   invalidate();
	   }
	   
	   public void setMeterMin(int value )
	   {
		   meterMin = value;
	   }
	   
	   public void setMeterMax( int value )
	   {
		   meterMax = value;
	   }
	   
	   
	   public void setDisplayStr(String value )
	   {
		   displayStr = value;
	   }
}
