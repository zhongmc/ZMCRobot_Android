package com.zmc.zmcrobot.ui;

import java.util.ArrayList;

import com.zmc.zmcrobot.simulator.ControllerInfo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

public class Robot {

    private Paint mPaint  = new Paint();
    private Path mPath = new Path();

    private ObstacleCrossPoint[] mocps;
    
    private ControllerInfo mCtrlInfo;
 //   private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    
    //cavas dimension
    public double width, height;
    private final static String TAG="Robot";
    
    //the robot positionb
    private double x, y, theta;
	private double transformMatrix[][] = new double[3][3];
	private double[] irDistances;
    
	private double mResultMatrix[][] = new double[15][3];
	private Dimension mDim = new Dimension();
	
	private double mScale = 500;
	
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
	
	public Robot()
	  {
		
		width = 0;
	    height = 0;
	    
		this.x = 0.2;
		this.y = 0.1;
		this.theta = Math.PI/6;

		getTransformationMatrix(x*mScale, y*mScale, theta , mScale);	    

	
	  }
	
//	
//	public void addObstacle(Obstacle obs)
//	{
//		obstacles.add( obs );
//		
//	}
	
	public void setCavasDimension(double width, double height)
	{
		getTransformationMatrix(width/2 + x*mScale, height/2 - y*mScale, theta , mScale);
		this.width = width;
		this.height = height;
	}
	
	    public void Draw(Canvas canvas) { 

	        Paint paint = mPaint; //new Paint();
	      
	        paint.setColor(Color.RED);//聽璁剧疆绾㈣壊聽聽
	        matrixMultip(qb_base_plate, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	        
	//        path = getPathFromMatrix(testPath);
	//        canvas.drawPath(path, paint);
	        
	        paint.setColor(Color.BLACK);
	        
	        matrixMultip(qb_right_wheel, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	        
	        paint.setColor(Color.BLUE);
	        
	        matrixMultip(qb_left_wheel, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);

	        paint.setColor(Color.CYAN);
	        matrixMultip(qb_ir_1, transformMatrix, mResultMatrix, mDim);
	        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        canvas.drawPath(mPath, paint);
	        
	        paint.setColor(Color.BLACK);
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
	        	irDistances[0] = 0.15;
	        	irDistances[1] = 0.6;
	        	irDistances[2] = 0.2;
	        	irDistances[3] = 0.1;
	        	irDistances[4] = 0.7;
	        }
	        
	        
	        for( int i=0; i<irDistances.length; i++ )
	        {
	        	

//	        	if( irDistances[i] < 0.1 )
//	        		continue;
	        	
	        	double[][] irMt = this.getObstacleMatrix(irDistances[i], i);
	        	if( irDistances[i] > 0.1 )
	        		paint.setColor(Color.LTGRAY);
	        	else
	        		paint.setColor(Color.CYAN);
	        		
		        matrixMultip(irMt, transformMatrix, mResultMatrix, mDim);
		        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
		        canvas.drawPath(mPath, paint);
        	
	        	
	        }
	        
	        if( mocps == null )
	        	return;
       
	        paint.setColor(Color.BLACK );
	        
	        
	        double cosTheta = Math.cos( theta);
	        double sinTheta = Math.sin( theta );
        	float xs,ys,xe,ye;

	        for( int i=0; i<irDistances.length; i++ )
	        {
	        	
	        	if( mocps[i].distance > 5 )
	        		continue;
	        	
	        	
	        	//width/2 + x*mScale, height/2 - y*mScale,
				xs = (float)(x + ir_positions[i][0] *cosTheta - ir_positions[i][1]*sinTheta);
				ys = (float)(y + ir_positions[i][0] *sinTheta + ir_positions[i][1]*cosTheta);
        	
//				Log.i(TAG, "IR pos:" + xs + ", " + ys + " ; crosAt:" + mocps[i].x +", " +mocps[i].y + "; d=" + mocps[i].distance );
				
	        	xs = (float) (width/2 + mScale* xs);
	        	ys = (float) (height/2 - mScale*ys);
	        	
	        	
	        	xe = (float) (width/2 + mScale*mocps[i].x);
	        	ye = (float) (height/2 - mScale*mocps[i].y);
	        
	        	
	        	canvas.drawLine(xs,ys,xe,ye ,paint);
	        }
	        
        	xs = (float) (width/2 + mScale* x);
        	ys = (float) (height/2 - mScale*y);

	        if( mCtrlInfo != null )
	        {
	        	if( mCtrlInfo.uAoidObstacle != null )
	        	{
	    	        paint.setColor(Color.GREEN );
		        	xe = (float) (width/2 + mScale*(x+mCtrlInfo.uAoidObstacle.x));
		        	ye = (float) (height/2 - mScale*(y+mCtrlInfo.uAoidObstacle.y));

		        	canvas.drawLine(xs,ys,xe,ye ,paint);
	    	        
	        	}
	        	
	        	if( mCtrlInfo.uFollowWall != null )
	        	{
	    	        paint.setColor(Color.RED );
		        	xe = (float) (width/2 + mScale*(x+mCtrlInfo.uFollowWall.x));
		        	ye = (float) (height/2 - mScale*(y+mCtrlInfo.uFollowWall.y));

		        	canvas.drawLine(xs,ys,xe,ye ,paint);
	        		
	        	}

	        	if( mCtrlInfo.uGotoGoal != null )
	        	{
	    	        paint.setColor(Color.CYAN );
		        	xe = (float) (width/2 + mScale*(x+mCtrlInfo.uGotoGoal.x));
		        	ye = (float) (height/2 - mScale*(y+mCtrlInfo.uGotoGoal.y));

		        	canvas.drawLine(xs,ys,xe,ye ,paint);
	        		
	        	}
	        	if( mCtrlInfo.p0 != null )
	        	{
	    	        paint.setColor(Color.CYAN );
		        	xs = (float) (width/2 + mScale*(mCtrlInfo.p0.x));
		        	ys = (float) (height/2 - mScale*(mCtrlInfo.p0.y));

	    	        xe = (float) (width/2 + mScale*(mCtrlInfo.p1.x));
		        	ye = (float) (height/2 - mScale*(mCtrlInfo.p1.y));

		        	canvas.drawLine(xs,ys,xe,ye ,paint);
	        		
	        	}

	        }
	        
	    }  
	
	   
	   //calculate the transform matrix
		private void getTransformationMatrix( double x, double y, double theta, double scale ) {

			transformMatrix[0][0] = Math.cos(theta) * scale;
			transformMatrix[0][1] = -Math.sin(theta) * scale; //-Math.sin(theta) * scale;
			transformMatrix[0][2] = 0;
			transformMatrix[1][0] = -Math.sin(theta) * scale;
			transformMatrix[1][1] = -Math.cos(theta) * scale; //Math.cos(theta) * scale;
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
		
		}
		
		
	   //浣跨敤robot鐨勫師濮嬪潗鏍�
	 public void setPosition(double x, double y, double theta )
	 {
		 
//		 double delt1,delt2,delt3;
//		 delt1 = Math.abs(x - this.x);
//		 delt2 = Math.abs(y - this.y);
//		 delt3 = Math.abs(theta - this.theta);
//		 
//		 if( delt1 < 0.001 && delt2<0.001 && delt3<0.001)
//			 return;
		 
		 
			this.x = x;
			this.y = y;
			this.theta = theta;
		getTransformationMatrix(width/2 + x*mScale, height/2 - y*mScale, theta, mScale );

//		updateIRDistances();
			
	 }

	   public void setScale(double scale )
	   {
		   getTransformationMatrix(width/2 + x*mScale, height/2- y*mScale, theta, scale );
		   mScale = scale;
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
//		double ir_thetas[] = {3*Math.PI/2, 2*Math.PI-Math.PI/4, 0, Math.PI/4, Math.PI/2 };

		final double ir_positions[][] = { { -0.045, 0.050 }, { 0.08, 0.04 }, { 0.162, 0.0 }, { 0.08, -0.04 },
				{ -0.045, -0.050 } };
		final double ir_thetas[] = { Math.PI / 2, Math.PI / 4, 0, -Math.PI / 4, -Math.PI / 2 };
		
		
/*		
		private void updateIRDistances()
		{
			
	        if( irDistances == null )
	        	irDistances = new double[5];
			
			double x0, y0, theta0;
			for( int i=0; i<ir_positions.length; i++ )
			{
				irDistances[i] = 1000;
				
				x0 = x + ir_positions[i][0];
				y0 = y + ir_positions[i][1];
				theta0 = theta + ir_thetas[i];
				double d;
				for(Obstacle obs:obstacles)
				{
					d = obs.getDistance(x0, y0, theta0);
					if( d < irDistances[i])
						irDistances[i] = d;
						
				}
				
				Log.i(TAG, "IRDIS:" + i + ", [" + ir_positions[i][0] + ", " +ir_positions[i][1] + "]:" + irDistances[i] );
				
				if( irDistances[i] > 0.5 )
					irDistances[i] = 0.5;
				if( irDistances[i]< 0.04)
					irDistances[i] = 0.04;
			}
		}
		
		
	*/
		
		public void setIrDistances( double[] value )
		{
			this.irDistances = value;
		}
		
		double mt[][] = new double[3][3];
		double tt[][] = new double[3][3];
		double res[][] = new double[3][3];

		private double[][] getObstacleMatrix( double distance, int idx)
		{
			mt[0][0] = 0;
			mt[0][1] = 0;
			mt[0][2] = 1;
			mt[1][0] = distance;
			mt[1][1] = distance * 0.0875;  //5deg
			mt[1][2] = 1;
			mt[2][0] = distance;
			mt[2][1] = -mt[1][1];
			mt[2][2] = 1;
			
			tt[0][0] = Math.cos(ir_thetas[idx]);
			tt[0][1] = -Math.sin(ir_thetas[idx]);
			tt[0][2] = 0;
			tt[1][0] = Math.sin(ir_thetas[idx] );
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

		public void setObstacleCrossPoints(ObstacleCrossPoint[] ocps) {
			mocps = ocps;
		}

		public void setControllerInfo(ControllerInfo ctrlInfo) {
			mCtrlInfo = ctrlInfo;			
		}
		
		
}
