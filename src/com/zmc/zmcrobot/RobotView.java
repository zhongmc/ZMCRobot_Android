package com.zmc.zmcrobot;

import com.zmc.zmcrobot.simulator.SlamMap;
import android.annotation.SuppressLint;
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

@SuppressLint("DefaultLocale")
public class RobotView extends View {

	private int mTrailSize = 900;
	private float[][] mTrails = new float[1000][2];
	private int mTrailCount = 0;
		
	private double testPath[][] = {{10,10,0},{50,150,0},{200,200,0}};
    private Paint mPaint  = new Paint();
    private Context mContext;
    private Path mPath = new Path();
    
    private float M = getContext().getResources().getDisplayMetrics().densityDpi / 25.4F;   
    private float H; 
    
    public double width, height;
    
	private SlamMap slamMap = null;


    private final static String TAG="RobotView";
    
    //the robot position
    private double x, y, theta;
	private double transformMatrix[][] = new double[3][3];
	private double[] irDistances;
    
	private double mResultMatrix[][] = new double[15][3];
	private Dimension mDim = new Dimension();
	
	private double targetX = -1, targetY=1;
	private double mScale = 500;
	
	private String targetStr = "";
	
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
	
	public RobotView(Context paramContext, AttributeSet paramAttributeSet)
	  {
	    super(paramContext, paramAttributeSet);
	    
	    this.mContext = paramContext;
	    width = 0;
	    height = 0;
	    
		this.x = 0.2;
		this.y = 0.1;
		this.theta = Math.PI/6;

		getTransformationMatrix(x*mScale, y*mScale, theta , mScale);	    

		ViewTreeObserver vto = getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
			@Override
			public void onGlobalLayout(){ 
			//	getViewTreeObserver().removeGlobalOnLayoutListener(this);
				height =getMeasuredHeight(); 
				width = getMeasuredWidth();

				//transformMatrix = new double[3][3];
				getTransformationMatrix(width/2 + x*mScale, height/2 - y*mScale, theta , mScale);	    
				
			}});
		
		
		
	  }
	
	   @Override  
	    protected void onDraw(Canvas canvas) { 
		   
	        super.onDraw(canvas);  


//	        int cwidth = canvas.getWidth();
//	        int cheight = canvas.getHeight();
	        

	        Paint paint = mPaint; //new Paint();
	      
	        paint.setColor(Color.BLACK);
	        canvas.drawLine((float)0.0, (float)height/2, (float)width, (float)height/2, paint);
	        canvas.drawLine((float)width/2, (float)0, (float)width/2, (float)height, paint);
	        canvas.drawLine((float)0.0, (float)height/2+1, (float)width, (float)height/2+1, paint);
	        canvas.drawLine((float)width/2+1, (float)0, (float)width/2+1, (float)height, paint);
	        
	        double curPos = 0;
	        double step = 0.1*mScale;
	        int idx = 0;
	        double hh = 10;
	        while(true)
	        {
	        	idx++;
	        	curPos = curPos + step;
	        	if( curPos > width/2)
	        		break;

	        	if( idx%10 == 0)
	        	{
	    	        paint.setColor(Color.DKGRAY);
	        		hh = 20;
	        	}
	        	else
	        	{
	    	        paint.setColor(Color.LTGRAY);
	        		hh = 10;
	        	}
	        	canvas.drawLine((float)(width/2+curPos), 0, (float)(width/2+curPos), (float)(height), paint);
		        canvas.drawLine((float)(width/2-curPos), 0, (float)(width/2-curPos), (float)(height), paint);
	        	
//	        	canvas.drawLine((float)(width/2+curPos), (float)(height/2-hh), (float)(width/2+curPos), (float)(height/2), paint);
//		        canvas.drawLine((float)(width/2-curPos), (float)(height/2-hh), (float)(width/2-curPos), (float)(height/2), paint);
	        }
	        
	        
	        
	
	        curPos = 0;
	        step = 0.1*mScale;
	        idx =0;
	        while(true)
	        {
	        	curPos = curPos + step;
	        	if( curPos > height/2)
	        		break;

	        	idx++;
	        	if( idx%10 == 0)
	        	{
	    	        paint.setColor(Color.DKGRAY);
	        		hh = 20;
	        	}
	        	else
	        	{
	    	        paint.setColor(Color.LTGRAY);
	        		hh = 10;
	        	}
	        	
	        	canvas.drawLine(0, (float)(height/2 + curPos), (float)(width), (float)(height/2 + curPos), paint);
		        canvas.drawLine(0, (float)(height/2- curPos), (float)(width), (float)(height/2 - curPos), paint);
//	        	canvas.drawLine((float)(width/2), (float)(height/2 + curPos), (float)(width/2+hh), (float)(height/2 + curPos), paint);
//		        canvas.drawLine((float)(width/2), (float)(height/2- curPos), (float)(width/2+hh), (float)(height/2 - curPos), paint);
	        	
	        }
	        
	        
	        int xIdx = 0, yIdx = 0;
	        float xPos = 0, yPos = 0;
	        float st = (float)(0.05 * mScale);
	        float x0, y0;
	        x0 = (float)width/2;
	        y0 = (float)height/2;
	        
	        
	        if( slamMap != null  )
	        {
	        
		        paint.setColor(Color.MAGENTA);
	
		        while( yPos < height/2 )
		        {
		        	while( xPos < width/2 )
		        	{
		        		if( slamMap.isObstacle(xIdx, yIdx))
		        		{
		        			canvas.drawCircle(x0 + xPos, y0 - yPos, st/2, paint);
		        		}
		        		if( slamMap.isObstacle(-xIdx, yIdx))
		        		{
		        			canvas.drawCircle(x0 - xPos, y0 - yPos, st/2, paint);
		        			
		        		}
		        		
		        		if( slamMap.isObstacle(xIdx, -yIdx))
		        		{
		        			canvas.drawCircle(x0 + xPos, y0 + yPos, st/2, paint);
		        			
		        		}
		        		if( slamMap.isObstacle(-xIdx, -yIdx))
		        		{
		        			canvas.drawCircle(x0 - xPos, y0 + yPos, st/2, paint);
		        			
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
	        
	        //draw the trails of the robot
	        paint.setColor(Color.BLUE);
	        float xs, ys, xe,ye;
	        xs = (float)(width/2 + mTrails[0][0] * mScale);
	        ys = (float)( height/2- mTrails[0][1] * mScale);
	        for(int i=1; i<mTrailCount-1; i++)
	        {
	        	xe = (float)(width/2 + mTrails[i][0] * mScale);
	        	ye = (float)(height/2- mTrails[i][1] * mScale);
	        	canvas.drawLine(xs,ys,xe,ye, paint);
	        	xs = xe;
	        	ys = ye;
	        }
	        

	        paint.setColor(Color.RED);// 设置红色  
	     //   canvas.drawCircle(50, 50, 30, paint);
	        //draw the target trangle;
//	        canvas.drawCircle((float)(width/2 +targetX*mScale), (float)(height/2 - targetY*mScale), 10.0f, paint);
	        
	        paint.setStrokeWidth((float) 3.0);

	        xs = (float)(width/2 +targetX*mScale);
	        ys = (float)(height/2 - targetY*mScale);
        	canvas.drawLine(xs - 20, ys, xs +20, ys, paint);
        	canvas.drawLine(xs, ys -20, xs,  ys + 20, paint);
	        
        	paint.setTextSize(32);
        	canvas.drawText(targetStr, xs+5, ys - 5, paint);
        	
	        paint.setStrokeWidth((float) 1.0);
        	
	        matrixMultip(qb_base_plate, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	        
	//        path = getPathFromMatrix(testPath);
	//        canvas.drawPath(path, paint);
	        
	        paint.setColor(Color.BLACK);
	        
	        matrixMultip(qb_right_wheel, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	        
	        
	        matrixMultip(qb_left_wheel, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);

	        
	        matrixMultip(qb_ir_1, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	        
	        matrixMultip(qb_ir_2, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);

	        matrixMultip(qb_ir_3, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	        
	        matrixMultip(qb_ir_4, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	        
	        matrixMultip(qb_ir_5, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	        
	        paint.setColor(Color.DKGRAY);
	        matrixMultip(qb_bbb, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	        
	        paint.setColor(Color.BLACK);
	        matrixMultip(qb_bbb_rail_l, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);

	        
	        matrixMultip(qb_bbb_rail_r, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	        
	        paint.setColor(Color.LTGRAY);
	        
	        matrixMultip(qb_bbb_eth, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	        
	        matrixMultip(qb_bbb_usb, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	       
	        
	        if( irDistances == null )
	        {
	        	irDistances = new double[5];
	        	irDistances[0] = 0.6;
	        	irDistances[1] = 0.6;
	        	irDistances[2] = 0.6;
	        	irDistances[3] = 0.6;
	        	irDistances[4] = 0.6;
	        }
	        
	        
	        for( int i=0; i<irDistances.length; i++ )
	        {
	        	
	        	if( i > 4 )
	        		break;

//	        	if( irDistances[i] < 0.1 )
//	        		continue;
	        	
	        	double[][] irMt = this.getObstacleMatrix(irDistances[i], i);
	        	if( irDistances[i] > 0.1 )
	        		paint.setColor(Color.LTGRAY);
	        	else
	        		paint.setColor(Color.RED);
	        		
		        matrixMultip(irMt, transformMatrix, mResultMatrix, mDim);
		        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
		        canvas.drawPath(mPath, paint);
        	 
	        	
	        }
	        
//            obj.add_surface(qb_right_wheel, [ 0.15 0.15 0.15 ]);
//            s = obj.add_surface_with_depth(qb_right_wheel_ol, [0.15 0.15 0.15], 1.5);
//            
//            set(s.handle_, 'LineStyle', '--');
//            set(s.handle_, 'FaceColor', 'none');
//            
//            s = obj.add_surface_with_depth(qb_left_wheel_ol, [0.15 0.15 0.15], 1.5);
//            
//            set(s.handle_, 'LineStyle', '--');
//            set(s.handle_, 'FaceColor', 'none');
//%             obj.add_surface_with_alpha(qb_axle, [0.15 0.15 0.15], 0.5);
//            obj.add_surface(qb_left_wheel, [ 0.15 0.15 0.15 ]);
//            
//            obj.add_surface_with_depth(qb_ir_1, [0.1 0.1 0.1], 1.2);
//            obj.add_surface_with_depth(qb_ir_2, [0.1 0.1 0.1], 1.2);
//            obj.add_surface_with_depth(qb_ir_3, [0.1 0.1 0.1], 1.2);
//            obj.add_surface_with_depth(qb_ir_4, [0.1 0.1 0.1], 1.2);
//            obj.add_surface_with_depth(qb_ir_5, [0.1 0.1 0.1], 1.2);
//                       
//                       
//            obj.add_surface_with_depth(qb_bbb, [0.2 0.2 0.2], 1.4);
//            obj.add_surface_with_depth(qb_bbb_rail_l, [0 0 0], 1.5);
//            obj.add_surface_with_depth(qb_bbb_rail_r, [0 0 0], 1.5);
//            obj.add_surface_with_depth(qb_bbb_eth, [0.7 0.7 0.7], 1.5);
//            obj.add_surface_with_depth(qb_bbb_usb, [0.7 0.7 0.7], 1.5);

	        
//	        paint.setColor(Color.DKGRAY);
//	        xs = (float)(width/2 +x*mScale);
//	        ys = (float)(height/2 - y*mScale);
//	        String label = String.format("%.2f,%.2f:%.1f", x, y, 180*theta/Math.PI);
//	        canvas.drawText( label, xs, ys, paint);
	        
	    }  
	
	   
	   //calculate the transform matrix
		private void getTransformationMatrix( double x, double y, double theta, double scale ) {

			transformMatrix[0][0] = Math.cos(theta) * scale;
			transformMatrix[0][1] = -Math.sin(theta) * scale;//-Math.sin(theta) * scale;
			transformMatrix[0][2] = 0;
			transformMatrix[1][0] = -Math.sin(theta) * scale;
			transformMatrix[1][1] = -Math.cos(theta) * scale;//Math.cos(theta) * scale;
			transformMatrix[1][2] = 0;
			transformMatrix[2][0] = x;
			transformMatrix[2][1] = y;
			transformMatrix[2][2] = 1;
			mScale = scale;

		}
		
		
		private boolean getPathFromMatrix(double[][] mt, Path path, int len )
		{
			if( mt.length < len )
				return false;
			path.reset();
			
			path.moveTo((float)mt[0][0], (float)mt[0][1]);
			for(int i=1; i<len; i++)
				path.lineTo((float)mt[i][0], (float)mt[i][1]);
			path.close();
			
			return true;
		}

		private boolean matrixMultip(double[][] m1, double[][] m2, double[][] result, Dimension dm ) {
			int xDim = m2[0].length;
			int yDim = m1.length;
			int kDim = m1[0].length;

//			double[][] res = new double[yDim][xDim];

			dm.x = yDim;
			dm.y = xDim;
			
			if( result.length < yDim )
				return false;
			if( result[0].length < xDim)
				return false;
			
			
			for (int i = 0; i < yDim; i++) {
				for (int j = 0; j < xDim; j++) {
					result[i][j] = 0;
					for (int k = 0; k < kDim; k++)
						result[i][j] = result[i][j] + m1[i][k] * m2[k][j];
				}
			}

			return true;
		}
	   
	   
	   
//	   public void setPosition( double x, double y, double theta, double scale )
//	   {
//			getTransformationMatrix(x, y, theta, scale );
//			this.x = x;
//			this.y = y;
//			this.theta = theta;
//			mScale = scale;
//			this.invalidate();
//	   }	
	   
	   
		//move robot to home
		public void resetRobot()
		{
			mTrailCount = 0;
			
		}

		
		public void setRobotState( RobotState state )
		{
			if( state.sType == 1)
				this.irDistances = state.obstacles;
			
			 double delt1,delt2,delt3;
			 delt1 = Math.abs(state.x - this.x);
			 delt2 = Math.abs(state.y - this.y);
			 delt3 = Math.abs(state.theta - this.theta);
			 
			 if( mTrailCount!=0 && delt1 < 0.001 && delt2<0.001 && delt3<0.001)
			 {
				 if( state.sType == 1)
					 invalidate();
				 
				 return;
			 }
			 
				getTransformationMatrix(width/2 + state.x*mScale, height/2 - state.y*mScale, state.theta, mScale );
				this.x = state.x;
				this.y = state.y;
				this.theta = state.theta;

				if( mTrailCount < mTrailSize )
					mTrailCount++;
					
				else 
					mTrailCount = 0;
				
				mTrails[mTrailCount][0] = (float)x;
				mTrails[mTrailCount][1] = (float)y;
				
				 if( state.sType == 1)
					 updateSlamMap();
				
				this.invalidate();
			 
		
		}
		
		
		public void setIrDistances( double[] value )
		{
			this.irDistances = value;
			this.invalidate();
		}
		
		
	   //使用robot的原始坐标
	 public void setPosition(double x, double y, double theta )
	 {
		 
		 double delt1,delt2,delt3;
		 delt1 = Math.abs(x - this.x);
		 delt2 = Math.abs(y - this.y);
		 delt3 = Math.abs(theta - this.theta);
		 
		 if( mTrailCount!=0 && delt1 < 0.001 && delt2<0.001 && delt3<0.001)
			 return;
		 
			getTransformationMatrix(width/2 + x*mScale, height/2 - y*mScale, theta, mScale );
			this.x = x;
			this.y = y;
			this.theta = theta;

			if( mTrailCount < mTrailSize )
				mTrailCount++;
				
			else 
				mTrailCount = 0;
			
			mTrails[mTrailCount][0] = (float)x;
			mTrails[mTrailCount][1] = (float)y;
			
			updateSlamMap();
			
			this.invalidate();
		 
	 }
	 
	 
	 private void updateSlamMap()
	 {
		 
		 
		 if( slamMap == null )
			 return;
		 
	        double cosTheta = Math.cos( theta);
	        double sinTheta = Math.sin( theta );
	        float xs,ys,xe,ye;

	        
     
	        
	        for( int i=0; i< irDistances.length; i++ )
	        {
	        	if( irDistances[i] >= 0.70 )
	        		continue;
	        	
	        	double irx, iry;
	        	irx = ir_positions[i][0] + irDistances[i]*Math.cos( ir_thetas[i]);
	        	iry = ir_positions[i][1] + irDistances[i]*Math.sin( ir_thetas[i]);
	        	
	        	//width/2 + x*mScale, height/2 - y*mScale,
				xs = (float)(x + irx *cosTheta - iry*sinTheta);
				ys = (float)(y + irx *sinTheta + iry*cosTheta);
	        	
				slamMap.setObstacle(xs, ys);
	        }
		 
	 }

	   public void setScale(double scale )
	   {
		   getTransformationMatrix(width/2 + x*mScale, height/2- y*mScale, theta, scale );
		   mScale = scale;
		   this.invalidate();
	   }
	   
	   public void setTarget(float x, float y)
	   {
		   targetX = x;
		   targetY = y;
		   
		   targetStr = String.format("%.2f,%.2f",targetX, targetY);
		   invalidate();
	   }
	   
		final double qb_base_plate[][] = { { 0.0335, 0.0534, 1 },
				{ 0.0429, 0.0534, 1 }, { 0.0639, 0.0334, 1 },
				{ 0.0686, 0.0000, 1 }, { 0.0639, -0.0334, 1 },
				{ 0.0429, -0.0534, 1 }, { 0.0335, -0.0534, 1 },
				{ -0.0465, -0.0534, 1 }, { -0.0815, -0.0534, 1 },
				{ -0.1112, -0.0387, 1 }, { -0.1112, 0.0387, 1 },
				{ -0.0815, 0.0534, 1 }, { -0.0465, 0.0534, 1 } };

		final double qb_bbb[][] = { { -0.0914, -0.0406, 1 },
				{ -0.0944, -0.0376, 1 }, { -0.0944, 0.0376, 1 },
				{ -0.0914, 0.0406, 1 }, { -0.0429, 0.0406, 1 },
				{ -0.0399, 0.0376, 1 }, { -0.0399, -0.0376, 1 },
				{ -0.0429, -0.0406, 1 } };

		final double qb_bbb_rail_l[][] = { { -0.0429, -0.0356, 1 },
				{ -0.0429, 0.0233, 1 }, { -0.0479, 0.0233, 1 },
				{ -0.0479, -0.0356, 1 } };

		final double qb_bbb_rail_r[][] = { { -0.0914, -0.0356, 1 },
				{ -0.0914, 0.0233, 1 }, { -0.0864, 0.0233, 1 },
				{ -0.0864, -0.0356, 1 } };

		final double qb_bbb_eth[][] = { { -0.0579, 0.0436, 1 },
				{ -0.0579, 0.0226, 1 }, { -0.0739, 0.0226, 1 },
				{ -0.0739, 0.0436, 1 } };

		final double qb_left_wheel[][] = { { 0.0254, 0.0595, 1 },
				{ 0.0254, 0.0335, 1 }, { -0.0384, 0.0335, 1 },
				{ -0.0384, 0.0595, 1 } };

		final double qb_left_wheel_ol[][] = { { 0.0254, 0.0595, 1 },
				{ 0.0254, 0.0335, 1 }, { -0.0384, 0.0335, 1 },
				{ -0.0384, 0.0595, 1 } };

		final double qb_right_wheel_ol[][] = { { 0.0254, -0.0595, 1 },
				{ 0.0254, -0.0335, 1 }, { -0.0384, -0.0335, 1 },
				{ -0.0384, -0.0595, 1 } };

		final double qb_right_wheel[][] = { { 0.0254, -0.0595, 1 },
				{ 0.0254, -0.0335, 1 }, { -0.0384, -0.0335, 1 },
				{ -0.0384, -0.0595, 1 } };


		final double qb_ir_1[][] = { { -0.0732, 0.0534, 1 },
				{ -0.0732, 0.0634, 1 }, { -0.0432, 0.0634, 1 },
				{ -0.0432, 0.0534, 1 } };

		final double qb_ir_2[][] = { { 0.0643, 0.0214, 1 }, { 0.0714, 0.0285, 1 },
				{ 0.0502, 0.0497, 1 }, { 0.0431, 0.0426, 1 } };

		
		final double qb_ir_3[][] = { { 0.0636, -0.015, 1 }, { 0.0636, 0.015, 1 },
				{ 0.0736, 0.015, 1 }, { 0.0736, -0.015, 1 } };
		
//		final double qb_ir_3[][] = { { 0.0636, -0.0042, 1 }, { 0.0636, 0.0258, 1 },
//				{ 0.0736, 0.0258, 1 }, { 0.0736, -0.0042, 1 } };

		final double qb_ir_4[][] = { { 0.0643, -0.0214, 1 },
				{ 0.0714, -0.0285, 1 }, { 0.0502, -0.0497, 1 },
				{ 0.0431, -0.0426, 1 } };

		final double qb_ir_5[][] = { { -0.0732, -0.0534, 1 },
				{ -0.0732, -0.0634, 1 }, { -0.0432, -0.0634, 1 },
				{ -0.0432, -0.0534, 1 } };

		final double qb_bbb_usb[][] = { { -0.0824, -0.0418, 1 },
				{ -0.0694, -0.0418, 1 }, { -0.0694, -0.0278, 1 },
				{ -0.0824, -0.0278, 1 } };
	   
		
//		double ir_positions[][] = {{-0.0582,0.0584}, {0.05725, 0.03555},{0.0686, 0.0},{0.05725, -0.0355},{-0.0582, -0.0584}};
//		double ir_thetas[] = {-Math.PI/2, -Math.PI/4, 0, Math.PI/4, Math.PI/2};
			
		
		final double ir_positions[][] = { { -0.073, 0.066, 1 }, { 0.061, 0.05, 1 }, { 0.072, 0, 1 }, { 0.061, -0.05, 1 },
				{ -0.073, -0.066, 1 } };

		final double ir_thetas[] = {Math.PI/2, Math.PI/4, 0, -Math.PI/4, -Math.PI/2 };
	
		
		double mt[][] = new double[3][3];
		double tt[][] = new double[3][3];
		double res[][] = new double[3][3];

		private double[][] getObstacleMatrix( double distance, int idx)
		{
			mt[1][0] = distance;
			mt[1][1] = distance * 0.0875;  //5deg
			mt[1][2] = 1;
			mt[2][0] = distance;
			mt[2][1] = -mt[1][1];
			mt[2][2] = 1;
			mt[0][0] = 0;
			mt[0][1] = 0;
			mt[0][2] = 1;
			
			tt[0][0] = Math.cos(ir_thetas[idx]);
			tt[0][1] = Math.sin(ir_thetas[idx]);
			tt[0][2] = 0;
			tt[1][0] = -Math.sin(ir_thetas[idx] );
			tt[1][1] = Math.cos(ir_thetas[idx] );
			tt[1][2] = 0;
			tt[2][0] = ir_positions[idx][0];
			tt[2][1] = ir_positions[idx][1];
			tt[2][2] = 1;
			
			this.matrixMultip(mt, tt, res, mDim);
//			res[0][0] = ir_positions[idx][0];
//			res[0][1] = ir_positions[idx][1];
			
			return res;
		}


		/*
		double[][] obp = new double[1][3];
		double[][] obt = new double[1][3];
		
		private double[][] getObstaclePos(double distance, int idx )
		{
			if( distance > 0.7 )
				return null;
			
			obt[0][0] = ir_positions[idx][0] + distance * Math.cos(ir_thetas[idx]);
			obt[0][1] = ir_positions[idx][1] + distance * Math.sin(ir_thetas[idx]);
			obt[0][2] = 1;

			this.matrixMultip(obt, transformMatrix, obp, mDim);
			
			obp[0][0] = obp[0][0] / mScale;
			obp[0][1] = obp[0][1] / mScale;
			
			return obp;
		}
		
		*/
		
		public SlamMap getSlamMap() {
			return slamMap;
		}

		public void setSlamMap(SlamMap slamMap) {
			this.slamMap = slamMap;
		}
		
		
}
