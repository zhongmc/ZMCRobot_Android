package com.zmc.zmcrobot.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.zmc.zmcrobot.Position;
import com.zmc.zmcrobot.RobotState;
import com.zmc.zmcrobot.simulator.ControllerInfo;
import com.zmc.zmcrobot.simulator.SlamMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class RobotSimulateView extends View {

	public boolean 	inScale = false;

	private int mTrailSize = 900;
	private float[][] mTrails = new float[1000][2];
	private int mTrailCount = 0;
	
	private float[][] mRoutes;
	private int routeSize = 0;
	
	private SlamMap slamMap = null;

	private DecimalFormat fmt = new DecimalFormat("#0.00");
	
    private Paint mPaint  = new Paint();
 //   private Path mPath = new Path();
    
    private ControllerInfo mCtrlInfo = null;
    private AbstractRobot robot;
//    private Obstacle obstacle,  bobstacle;
    
    private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    
//    private float M = getContext().getResources().getDisplayMetrics().densityDpi / 25.4F;   
//    private float H; 
    
    //the robot position
    private double x, y, theta;
    private double velocity;
    
    public double width, height;
    
    public double canvas_width, canvas_height;
    private double scroll_x, scroll_y;
    public double x_off = 0, y_off = 0;
//    private double scroll_width, scroll_height; //可滚动的宽度和高度
    
    private final static String TAG="RobotView";
    
    private double recoverX = 0, recoverY = 0;
    
    //the robot positionb
//    private double x, y, theta;
//	private double transformMatrix[][] = new double[3][3];
//	private double[] irDistances;
//    
//	private double mResultMatrix[][] = new double[15][3];

    private Dimension mDim = new Dimension();
	private double targetX = -1, targetY=1;
	private double mScale = 500;
	
	//the canvas dimension
	private double cw = 5.5, ch = 7.5;
	
	public void setRecoverPoint(double x, double y)
	{
		recoverX = x;
		recoverY = y;
	}
	
	public void setRoutes(float[][] routes, int routeSize)
	{
		this.mRoutes = routes;
		this.routeSize = routeSize;
		this.invalidate();
	}
	
	public void setRouteSize( int routeSize )
	{
		this.routeSize = routeSize;
		this.invalidate();		
//		Log.i(TAG, "route size: " + routeSize );

	}
	
	
	public class Dimension {
		public int x, y;
		public Dimension()
		{
			
		}
		public Dimension(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
	};
	
	public RobotSimulateView(Context paramContext, AttributeSet paramAttributeSet)
	  {
	    super(paramContext, paramAttributeSet);
	    
	    try{

	    	robot = new RearDriveRobot();
//	    	obstacle = new Obstacle(obstacle3);
//	    	obstacle.setPosition(1, 1, 0);
//	    	robot.addObstacle( obstacle );
//	    	bobstacle = new Obstacle(obstacle2);
//	    	bobstacle.setPosition(-1, -2, 0);
//	    	robot.addObstacle( bobstacle );
//	    	
	    	robot.setPosition(1,1,0);
	    	
	    	
	    }catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    
	    
	    width = 0;
	    height = 0;
///		getTransformationMatrix(x*mScale, y*mScale, theta , mScale);	    
		ViewTreeObserver vto = getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
			@Override
			public void onGlobalLayout(){ 
			//	getViewTreeObserver().removeGlobalOnLayoutListener(this);
				height =getMeasuredHeight(); 
				width = getMeasuredWidth();
				
			    robot.setPosition(x,y,theta);
				robot.setCavasDimension(width, height );
				
				for(Obstacle obs:obstacles)
				{
					obs.setCavasDimension(width, height);
				}
				
				canvas_width = cw*mScale; 
				canvas_height = ch*mScale;
				
				
				//初始画面居中
				scroll_x = (canvas_width - width)/2;
				scroll_y = (canvas_height - height)/2;
				
//				scroll_width = scroll_x;
//				scroll_height = scroll_y;
				
				if( scroll_x < 0 )
					scroll_x = 0;
				if( scroll_y < 0 )
					scroll_y = 0;
				
				x_off = 0;
				y_off = 0;
				
//				obstacle.setCavasDimension(width, height);
//				bobstacle.setCavasDimension(width,height);
				
				//transformMatrix = new double[3][3];
	//			//getTransformationMatrix(width/2 + x*mScale, height/2 - y*mScale, theta , mScale);	    
				
			}});
		
		
		
	  }
	
	public void addObstacle(Obstacle obs)
	{
		obstacles.add( obs );
		
	}

	
	private Rect r = new Rect();
	
	   @Override  
	    protected void onDraw(Canvas canvas) { 
		   
	        super.onDraw(canvas);  


//	        int cwidth = canvas.getWidth();
//	        int cheight = canvas.getHeight();
	        
	         
	        
	        Paint paint = mPaint; //new Paint();
	        paint.setColor(Color.LTGRAY);
	        
	        if( canvas_width > width )// && inScale)
	        {
	        	paint.setStrokeWidth((float) 5.0);
	        	float scw = (float)(((width-20)/canvas_width)*(width-20));
	        	float scx = (float)(((width-20)/canvas_width)*scroll_x);
		        canvas.drawLine(5+scx, (float)height-5, 5+scx+scw, (float)height - 5, paint);
	        }
	        
	        if( canvas_height > height  ) // && inScale)
	        {
	        	paint.setStrokeWidth((float) 5.0);
	        	float sch = (float)(((height-20)/canvas_height)*(height-20));
	        	float scy = (float)(((height-20)/canvas_height)*scroll_y);
		        canvas.drawLine((float)(width-5), 5+scy, (float)(width-5), 5+scy+sch, paint);
	        	
	        }

        	paint.setStrokeWidth((float) 1.0);
	        
	        double w,h;
	        
	        w =  width;
	        h = height;

	        canvas.translate((float)x_off, (float)y_off);
	        
	        
	        double cw = w, ch = h;
	        if( cw < canvas_width )
	        	cw = canvas_width;
	       
	        if( ch < canvas_height )
	        	ch = canvas_height;
	        
	        paint.setColor(Color.LTGRAY);
	        canvas.drawLine((float)-x_off, (float)h/2, (float)(cw-x_off), (float)h/2, paint);
	        canvas.drawLine((float)w/2, (float)-y_off, (float)w/2, (float)(ch - y_off), paint);
	        
	        
	        double curPos = 0;
	        double step = 0.1*mScale;
	        int idx = 0;
	        double hh = 10;
	        
	        while(true)
	        {
	        	idx++;
	        	curPos = curPos + step;
	        	if( curPos > cw/2) // w/2 && curPos > canvas_width/2 )
	        		break;

	        	if( idx%10 == 0)
	        	{
	    	        paint.setColor(Color.BLACK);
	        		hh = 20;
	        	}
	        	else
	        	{
	    	        paint.setColor(Color.LTGRAY);
	        		hh = 10;
	        	}
	        	
	        	canvas.drawLine((float)(w/2+curPos), (float)(h/2-hh), (float)(w/2+curPos), (float)(h/2), paint);
		        canvas.drawLine((float)(w/2-curPos), (float)(h/2-hh), (float)(w/2-curPos), (float)(h/2), paint);
	        }
	        
	
	        curPos = 0;
	        step = 0.1*mScale;
	        idx =0;
	        
	        while(true)
	        {
	        	curPos = curPos + step;
	        	if( curPos > ch/2) // h/2 && curPos > canvas_height/2 )
	        		break;

	        	idx++;
	        	if( idx%10 == 0)
	        	{
	    	        paint.setColor(Color.BLACK);
	        		hh = 20;
	        	}
	        	else
	        	{
	    	        paint.setColor(Color.LTGRAY);
	        		hh = 10;
	        	}
	        	
	        	canvas.drawLine((float)(w/2), (float)(h/2 + curPos), (float)(w/2+hh), (float)(h/2 + curPos), paint);
		        canvas.drawLine((float)(w/2), (float)(h/2- curPos), (float)(w/2+hh), (float)(h/2 - curPos), paint);
	        	
	        }

	        
	        int xIdx = 0, yIdx = 0;
	        float xPos = 0, yPos = 0;
	        float st = (float)(0.05 * mScale);
	        float x0, y0;
	        x0 = (float)width/2 - st/2;
	        y0 = (float)height/2 - st/2;
	        
	        int ist = (int)st;
	        
	        if( slamMap != null  )
	        {
		        paint.setColor(Color.GRAY);
	
		        while( yPos < ch/2) //height/2 )
		        {
		        	while( xPos < cw/2) //width/2 )
		        	{
		        		if( slamMap.isObstacle(xIdx, yIdx))
		        		{
		        			r.left = (int)(x0 + xPos);
		        			r.top = (int)(y0 - yPos);
		        			r.right = r.left + ist;
		        			r.bottom = r.top + ist;
		        			canvas.drawRect(r, paint);
		        			
//		        			canvas.drawCircle(x0 + xPos, y0 - yPos, st/2, paint);
		        		}
		        		if( slamMap.isObstacle(-xIdx, yIdx))
		        		{
		        			r.left = (int)(x0 - xPos);
		        			r.top = (int)(y0 - yPos);
		        			r.right = r.left + ist;
		        			r.bottom = r.top + ist;
		        			canvas.drawRect(r, paint);
//		        			canvas.drawCircle(x0 - xPos, y0 - yPos, st/2, paint);
		        			
		        		}
		        		
		        		if( slamMap.isObstacle(xIdx, -yIdx))
		        		{
		        			r.left = (int)(x0 + xPos);
		        			r.top = (int)(y0 + yPos);
		        			r.right = r.left + ist;
		        			r.bottom = r.top + ist;
		        			canvas.drawRect(r, paint);
//		        			canvas.drawCircle(x0 + xPos, y0 + yPos, st/2, paint);
		        			
		        		}
		        		if( slamMap.isObstacle(-xIdx, -yIdx))
		        		{
		        			r.left = (int)(x0 - xPos);
		        			r.top = (int)(y0 + yPos);
		        			r.right = r.left + ist;
		        			r.bottom = r.top + ist;
		        			canvas.drawRect(r, paint);
//		        			canvas.drawCircle(x0 - xPos, y0 + yPos, st/2, paint);
		        			
		        		}
		        		xIdx++;
			        	xPos = xPos + st;
		        	}
		        	xIdx = 0;
		        	xPos = 0;
		        	yPos = yPos + st;
		        	yIdx++;
		        }
	
	        }
	        
//	        canvas.translate((float)x_off, (float)y_off);

	        paint.setStrokeWidth((float) 2.0);
	        
	        paint.setColor(Color.RED);// 设置红色  

	        //gtg target
        	canvas.drawLine((float)(width/2 +targetX*mScale-20), (float)(height/2 - targetY*mScale), (float)(width/2 +targetX*mScale+20), (float)(height/2 - targetY*mScale), paint);
        	canvas.drawLine((float)(width/2 +targetX*mScale), (float)(height/2 - targetY*mScale -20), (float)(width/2 +targetX*mScale), (float)(height/2 - targetY*mScale + 20), paint);
	        
        	//gtg a target
        	if( mCtrlInfo != null )
        	{
    	        paint.setColor(Color.GREEN );//   

        	canvas.drawLine((float)(width/2 +mCtrlInfo.ux*mScale-20), (float)(height/2 - mCtrlInfo.uy*mScale), (float)(width/2 +mCtrlInfo.ux*mScale+20), (float)(height/2 - mCtrlInfo.uy*mScale), paint);
        	canvas.drawLine((float)(width/2 +mCtrlInfo.ux*mScale), (float)(height/2 - mCtrlInfo.uy*mScale -20), (float)(width/2 +mCtrlInfo.ux*mScale), (float)(height/2 - mCtrlInfo.uy*mScale + 20), paint);
        	}
//	        canvas.drawCircle((float)(width/2 +targetX*mScale), (float)( height/2 - targetY*mScale), 10.0f, paint);
	        

	        for(Obstacle obs:obstacles)
			{
				obs.Draw(canvas);
			}
	        
	        //draw the trails of the robot
	        paint.setColor(Color.BLUE);
	        float xs, ys, xe,ye;
	        xs = (float)(width/2 + mTrails[0][0] * mScale);
	        ys = (float)(height/2- mTrails[0][1] * mScale);
	        for(int i=1; i<mTrailCount-1; i++)
	        {
	        	xe = (float)( width/2 + mTrails[i][0] * mScale);
	        	ye = (float)( height/2- mTrails[i][1] * mScale);
	        	canvas.drawLine(xs,ys,xe,ye, paint);
	        	xs = xe;
	        	ys = ye;
	        }
	        
	        if( mRoutes != null && routeSize > 0)
	        {
		        //draw the route
		        paint.setColor(Color.CYAN );
		        paint.setStrokeWidth( 3 );
		        
		        xs = (float)(width/2 + mRoutes[0][0] * mScale);
		        ys = (float)(height/2- mRoutes[0][1] * mScale);

	        	paint.setStyle(Paint.Style.STROKE);
	        	paint.setStrokeWidth(3);
		        canvas.drawCircle(xs, ys, 30, paint);
		        
		        for(int i=1; i<routeSize -1; i++)
		        {
		        	xe = (float)( width/2 + mRoutes[i][0] * mScale);
		        	ye = (float)( height/2- mRoutes[i][1] * mScale);
		        	canvas.drawLine(xs,ys,xe,ye, paint);
		        	xs = xe;
		        	ys = ye;
		        }
		        
		        paint.setColor(Color.RED );
		        paint.setStrokeWidth( 2 );

//		        float x0, y0;
		        for(int i=1; i<routeSize -1; i++)
		        {
		        	x0 = (float)( width/2 + mRoutes[i][0] * mScale);
		        	y0 = (float)( height/2- mRoutes[i][1] * mScale);
		        	xs = x0-2;
		        	xe = x0+2;
		        	ys = y0;
		        	ye = y0;
		        	canvas.drawLine(xs,ys,xe,ye, paint);
		        	xs = x0;
		        	xe = x0;
		        	ys = y0 -2;
		        	ye = y0+2;
		        	canvas.drawLine(xs,ys,xe,ye, paint);
		        }
		        
		        
	        }
//	        obstacle.Draw(canvas);
//	        bobstacle.Draw(canvas);

	        robot.Draw(canvas);

	        
	        if( recoverX !=0 && recoverY != 0 )
	        {
		        paint.setColor(Color.RED );
		        paint.setStrokeWidth( 3 );
	        	canvas.drawLine((float)( width/2 + robot.x*mScale), (float)(float)( height/2- robot.y * mScale), (float)( width/2 + recoverX * mScale ), (float)(float)( height/2- recoverY * mScale), paint);
	        }
	        
	        canvas.restore();
	        
//	        paint.setColor(Color.DKGRAY);
//	        paint.setTextSize(36);
//	        int Q = (int)(theta*57.296);
//	        String label = "[" + Q + ", " +fmt.format(velocity)+"]";
//	        canvas.drawText(label, 50, (float) (height-20), paint);
	        
	        
	    }  
	
 

	   
		//move robot to home
		public void resetRobot()
		{
			mTrailCount = 0;
			
		}

		public void setRobotState( RobotState state )
		{
			if( state.sType == 8)
				robot.setIrDistances( state.obstacles );
			
			 double delt1,delt2,delt3;
			 delt1 = Math.abs(state.x - this.x);
			 delt2 = Math.abs(state.y - this.y);
			 delt3 = Math.abs(state.theta - this.theta);
			 
			 if( mTrailCount!=0 && delt1 < 0.001 && delt2<0.001 && delt3<0.001)
			 {
				 if( state.sType == 8)
					 invalidate();
				 
				 return;
			 }
			 
			 	this.x = state.x;
				this.y = state.y;
				this.theta = state.theta;
				this.velocity = state.velocity;

				robot.setPosition(x, y, theta);

				if( mBeSlamMapping && slamMap != null && state.sType == 8)
				 {
					 robot.updateSlamMap( slamMap );
				 }
				
				mTrails[mTrailCount][0] = (float)x;
				mTrails[mTrailCount][1] = (float)y;

				
				if( mTrailCount < mTrailSize )
					mTrailCount++;
				else 
					mTrailCount = 0;

				this.invalidate();
			 
		
		}
				
		
		
		public void setIrDistances( double[] value )
		{
//			this.irDistances = value;
			robot.setIrDistances(value);
//			this.invalidate();
		}
		
		
	   //使用robot的原始坐标
	 public void setRobotPosition(double x, double y, double theta, double velocity )
	 {
		 
		 double delt1,delt2, delt3;
		 delt1 = Math.abs(x - this.x);
		 delt2 = Math.abs(y - this.y);
		 delt3 = Math.abs(theta - this.theta );
		 
		 if( mTrailCount!=0 && delt1 < 0.0001 && delt2<0.0001  && delt3 <0.0001)
			 return;
		 
		 
		 	this.x = x;
			this.y = y;
			this.theta = theta;
			this.velocity = velocity;

			robot.setPosition(x, y, theta);

			if( slamMap != null )
			 {
				 robot.updateSlamMap( slamMap );
			 }
			
			mTrails[mTrailCount][0] = (float)x;
			mTrails[mTrailCount][1] = (float)y;

			
			if( mTrailCount < mTrailSize )
				mTrailCount++;
			else 
				mTrailCount = 0;

			this.invalidate();
/*
			
			if( canvas_width < width && canvas_height < height )
			{
				this.invalidate();
				return;
			}
			double w = width;
			double h = height;
			
			if( w < canvas_width )
				w = canvas_width;
			if( h < canvas_height )
				h = canvas_height;
			
			double px = x*mScale + w/2 - scroll_x;
			double py = h/2 - y*mScale - scroll_y;
			
			if( px < 0 )
			{
				scroll_x = scroll_x + px - 0.2*mScale;
				if( scroll_x < 0 )
					scroll_x = 0;
			}
			
			if( py < 0 )
			{
				scroll_y = scroll_y + py - 0.2*mScale;
				if( scroll_y < 0)
					scroll_y = 0;
			}
			
			if( px > width )
			{
				scroll_x = scroll_x + px-width + 0.2*mScale;
				if(scroll_x > (canvas_width - width ) )
					scroll_x = canvas_width - width;
			}
			
			if( py > height)
			{
				scroll_y = scroll_y + py - height + 0.2*mScale;
				   if( scroll_y > (canvas_height - height) )
					   scroll_y  = canvas_height-height;
			}
			

			
			x_off = (canvas_width - width)/2 - scroll_x;
			y_off = (canvas_height - height)/2 - scroll_y;
			
			this.invalidate();
		 */
	 }

	   public void setScale(double scale )
	   {
		   robot.setScale(scale);
		   for( Obstacle obs:obstacles)
		   {
			   obs.setScale(scale);
		   }
		   
		   
//		   double xc = (scroll_x + width/2)/mScale;
//		   double yc = (scroll_y + height/2)/mScale;
//		   
//			Log.i(TAG, "scale: Y=" + yc);
		   
		   canvas_width = cw*mScale; 
		   canvas_height = ch*mScale;
			
		   
		   //以当前画面中点缩放
			scroll_x = scroll_x*scale/mScale;
			scroll_y = scroll_y*scale/mScale;
		   
			if( scroll_x < 0 || canvas_width < width )
				scroll_x = 0;
			if( scroll_y < 0  || canvas_height < height )
				scroll_y = 0;
			
			if( canvas_width > width)
			{
				x_off = (canvas_width- width)/2 - scroll_x;
			}
			else 
				x_off = 0;
			
			if( canvas_height > height )
				y_off = (canvas_height-width)/2 - scroll_y;
			else
				y_off = 0;

	//		Log.i(TAG, "scale: YOFF=" + scroll_y + ", height=" + height + " cavans height=" + canvas_height);
			
		   mScale = scale;
		   this.invalidate();
	   }
	   
	   

	   //将屏幕坐标的x，y转换为物理坐标
	   public Position getRealPosition(float x, float y)
	   {
		   //转换为物理坐标的
		   double w = width;
		   if( w < canvas_width )
			   w = canvas_width;
		   double h = height;
		   if( h < canvas_height )
			   h = canvas_height;
		  float tx = (float) ((scroll_x + x - w / 2) / mScale );
		  float ty = (float) ((-scroll_y - y + h / 2) /mScale);		   
		   
		   Position p = new Position(tx, ty);
		   return p;
		   
		   
	   }
	   
	   
	   public void setTarget(float x, float y)
	   {
		   targetX = x;
		   targetY = y;
		   invalidate();

		   
	   }
	   
   
	   public void onScroll(float distanceX, float distanceY )
	   {
		   boolean changed = false;
		   
		   if( canvas_width > width && distanceX != 0 )
		   {
			   scroll_x = scroll_x + distanceX;
			   if( scroll_x < 0 )
				   scroll_x = 0;
			   else if( scroll_x > (canvas_width - width ))
				   scroll_x = canvas_width - width;
			   changed = true;
			   
			   x_off = (canvas_width-width)/2 - scroll_x;
		   }
		   if( canvas_height > height  && distanceY != 0 )
		   {
			   scroll_y = scroll_y + distanceY;
			   if( scroll_y < 0 )
				   scroll_y = 0;
			   if( scroll_y > (canvas_height - height) )
				   scroll_y  = canvas_height-height;
			   y_off = (canvas_height - height)/2 - scroll_y;
			   changed = true;
		   }
		   
		   if( changed )
			   invalidate();
	   }
	   
	   
	   public boolean canScrollLeft()
	   {
		   return scroll_x > 0;
	   }
	   
	   public boolean canScrollRight()
	   {
		   return (scroll_x + width) < canvas_width;
	   }
		
//		double ir_positions[][] = {{-0.0582,0.0584}, {0.05725, 0.03555},{0.0686, 0.0},{0.05725, -0.0355},{-0.0582, -0.0584}};
//		double ir_thetas[] = {3*Math.PI/2, 2*Math.PI-Math.PI/4, 0, Math.PI/4, Math.PI/2 };
				

		public void setObstacleCrossPoints(ObstacleCrossPoint[] ocps) {
			robot.setObstacleCrossPoints(ocps);
		}

		public void setControllerInfo(ControllerInfo ctrlInfo) {
			robot.setControllerInfo(ctrlInfo);
			mCtrlInfo = ctrlInfo;			

		}

		public SlamMap getSlamMap() {
			return slamMap;
		}

		public void setSlamMap(SlamMap slamMap) {
			this.slamMap = slamMap;
		}
		
		
		private boolean mBeSlamMapping =false;
		
		public void startSlamMap()
		{
			mBeSlamMapping = true;
		}
		
		public void stopSlamMap()
		{
			mBeSlamMapping = false;
		}
}
