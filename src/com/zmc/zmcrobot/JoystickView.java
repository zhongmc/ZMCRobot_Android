package com.zmc.zmcrobot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class JoystickView extends View {

    private float tcx0, tcy0, tcx,tcy;
    private float angle, throttle; //rotateAngle = -20; 

    private boolean inTouch = false;
    
    private Context mContext;
    private Paint mPaint  = new Paint();

    
    private int width, height, d;
    
    private JoystickEventListener joystickListener = null;
    private JoystickEvent joystickEvent = new JoystickEvent(this);
	
	public JoystickView(Context paramContext, AttributeSet paramAttributeSet)
	  {
	    super(paramContext, paramAttributeSet);
	    
	    this.mContext = paramContext;

	    width = 0;
	    height = 0;
//	    steerBitmap =  BitmapFactory.decodeResource(getResources(),  R.drawable.steer);

		ViewTreeObserver vto = getViewTreeObserver();

		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
			@Override
			public void onGlobalLayout(){ 
				height =getMeasuredHeight(); 
				width = getMeasuredWidth();
	        	d = Math.min(width, height) - 4;
			}});
	    
	    
		this.setOnTouchListener(onTouchListener);
	    
	  }
	
	RectF rect = new RectF();
	
	
	   @Override  
	    protected void onDraw(Canvas canvas) { 
	        super.onDraw(canvas);  
	        Paint paint = mPaint; 
	        paint.setColor(Color.rgb(226, 240, 217));
	        paint.setAlpha(200);
	        
	        
	        float x0, y0;
	        float l = (d-4)/2, h = 20;

	        x0 = (width-d)/2;
	        y0 = (height-d)/2;
	        canvas.drawRect(x0, y0, x0+d, y0+d, paint);

	        paint.setStyle(Style.STROKE );
	        paint.setStrokeWidth(2);
	        paint.setColor(Color.rgb(84, 130, 53));
	        paint.setAlpha(150);
	        canvas.drawRect(x0, y0, x0+d, y0+d, paint);

	        paint.setStrokeWidth(1);

	        paint.setStyle(Style.FILL_AND_STROKE);
	        paint.setColor(Color.GRAY);
	        paint.setTextSize( 24 );
	        canvas.drawText("touch/click to use joystic", x0+40, y0+d/2+10, paint);
	        
	        paint.setColor(Color.rgb(255, 230, 153));
	        paint.setAlpha(130);
	        rect.left = x0+2;
	        rect.top = y0+2;
	        rect.right = rect.left + d - 4;
	        rect.bottom = rect.top + h;
	        canvas.drawRoundRect(rect, 10,10, paint);

	        rect.left = x0+2;
	        rect.top = rect.top + h + 4;
	        rect.right = rect.left + d - 4;
	        rect.bottom = rect.top + h;
	        
	        canvas.drawRoundRect(rect, 10, 10, paint);
	        
	        if( !inTouch )
	        	return;

	        paint.setTextSize( 20 );
	        
	        paint.setColor(Color.rgb(197, 224, 180));
	        paint.setAlpha( 130 );
	        canvas.drawCircle(tcx0, tcy0, d/2, paint);

	        paint.setColor(Color.rgb(169,209,142));
	        paint.setAlpha( 130 );
	        canvas.drawCircle(tcx, tcy, d/4, paint);
	        
	        //angle
	        float tx0, angle = -this.angle;

	        
	        x0 = width/2;
	        tx0 = x0;
	        if( angle < 0 )
	        {
	        	tx0 = x0 - 50;
	        	x0 = width/2 + angle*l;
	        }

	        if( angle < 0 )
	        {
	        	paint.setColor(Color.rgb(189,215,238));
	        	
	        }
	        else
	        {
	        	paint.setColor(Color.rgb(248,203,173));
	        }
        	canvas.drawRect(x0, y0+2, x0+l*Math.abs(angle), y0+2+h, paint);
	        paint.setColor(Color.GRAY);
	        canvas.drawText(String.format("%.2f", angle), tx0+5, y0+2 + h - 2, paint);
	        

        	//throttle
	        x0 = width/2;
	        tx0 = x0;
	        if( throttle < 0 )
	        {
	        	tx0 = x0 - 50;
	        	x0 = width/2 + throttle*l;
	        }
	        
	        if( throttle < 0 )
	        {
	        	paint.setColor(Color.rgb(189,215,238));
	        	
	        }
	        else
	        {
	        	paint.setColor(Color.rgb(248,203,173));
	        }
        	canvas.drawRect(x0, y0+2 + 4 + h, x0+l*Math.abs(throttle), y0+2 + 4 + 2*h, paint);
	        paint.setColor(Color.GRAY);
	        canvas.drawText(String.format("%.2f", throttle), tx0+5, y0+2 + 4 + 2*h - 2, paint);
        	
	    }  
	
	   
	   public void setJoystickEventListener(JoystickEventListener listener)
	   {
		   this.joystickListener = listener;
	   }
	   

	   
		private OnTouchListener onTouchListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {

				// Log.i(TAG, "OnTouch:" + event.getX() + "," + event.getY() );

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					
					inTouch = true;
					tcx0 =  event.getX();
					tcy0 =  event.getY();
					tcx = tcx0;
					tcy = tcy0;
					angle = 0;
					throttle = 0;
					invalidate();
					return true; // to continue to receive ACTION_MOVE
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					
					inTouch = false;
					invalidate();
					
					if( joystickListener != null )
					{
						joystickEvent.angle = 0;
						joystickEvent.throttle = 0;
						joystickListener.onJoystickAction(joystickEvent);
					}

				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					tcx = event.getX();
					tcy = event.getY();
					
	                angle = -2 * (tcx - tcx0) /d;
	                if (angle > 1)
	                    angle = 1;
	                else if (angle < -1)
	                    angle = -1;

	                throttle = -2 * (tcy - tcy0) /d;

	                if (throttle < -1)
	                    throttle = -1;
	                else if (throttle > 1)
	                    throttle = 1;
					
	                invalidate();

					if( joystickListener != null )
					{
						joystickEvent.angle = angle;
						joystickEvent.throttle = throttle;
						joystickListener.onJoystickAction(joystickEvent);
					}
	                
				}
				return false;
			};
		};	   
}
