package com.zmc.zmcrobot.ui;

import java.util.ArrayList;
import java.util.List;

import com.zmc.zmcrobot.simulator.ControllerInfo;
import com.zmc.zmcrobot.simulator.IRSensor;
import com.zmc.zmcrobot.simulator.SlamMap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

public abstract class AbstractRobot {

    private Paint mPaint  = new Paint();
    private Path mPath = new Path();

    private ObstacleCrossPoint[] mocps;
    
    private ControllerInfo mCtrlInfo;
 //   private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    
    
	List<ComponentInfo> components = new ArrayList<ComponentInfo> ();
    
    //cavas dimension
    public double width, height;
    private final static String TAG="Robot";
    
    //the robot positionb
    public double x, y, theta;
	private double transformMatrix[][] = new double[3][3];
	private double[] irDistances;
    
	private double mResultMatrix[][] = new double[25][3];
	private Dimension mDim = new Dimension();
	
	private double mScale = 500;
	
	protected double xl, yl, xr, yr; //robot 的左下角、右上角左边
	
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
	
	
	
	public AbstractRobot()
	  {
		
		width = 0;
	    height = 0;
	    
		this.x = 0.2;
		this.y = 0.1;
		this.theta = Math.PI/6;

		getTransformationMatrix(x*mScale, y*mScale, theta , mScale);	    

//		double[][] irDims = this.getIrDimention();
	
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
	
	
	
	public void addComponent(ComponentInfo component )
	{
		this.components.add( component );
	}
	
	public void addComponent(double x, double y, ComponentInfo component )
	{
		ComponentInfo component1 = component.offset(x, y);
		this.components.add( component1 );
	}
	
	public void addComponent(double x, double y, double theta, ComponentInfo component )
	{
		ComponentInfo component1 = component.transform(x, y, theta);
		this.components.add( component1 );
		
	}
	
	
	    public void Draw(Canvas canvas) { 

	        Paint paint = mPaint; //new Paint();
	      
	        
	        for( ComponentInfo comp:components)
	        {
	        	paint.setColor( comp.color );
	        	matrixMultip(comp.path, transformMatrix, mResultMatrix, mDim);
	        	getPathFromMatrix(mResultMatrix, mPath, mDim.x);
	        	canvas.drawPath(mPath, paint);
	        }
	        

	        
	        
	        
	    //    draw the irSensors
//	        double[] irThetas = this.getIrThetas();
//	        double[][] irDimention = this.getIrDimention();
//    		paint.setColor(Color.CYAN);
//	        
//	        for(int i=0; i<irThetas.length; i++)
//	        {
//	        	double[][] irComponent = this.getIRSensorMatrix(irDimention, i);
//		        matrixMultip(irComponent, transformMatrix, mResultMatrix, mDim);
//		        getPathFromMatrix(mResultMatrix, mPath, mDim.x);
//		        canvas.drawPath(mPath, paint);
//	        }
	        
	   
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
	        	if( irDistances[i] >= IRSensor.maxDistance )
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

        	double[][] ir_positions = this.getIrPositions();
        	
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
	        	
	        	if( mCtrlInfo.uFwP != null )
	        	{
	    	        paint.setColor(Color.BLUE );
		        	xe = (float) (width/2 + mScale*(x+mCtrlInfo.uFwP.x));
		        	ye = (float) (height/2 - mScale*(y+mCtrlInfo.uFwP.y));

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
	    
	    
	    public void updateSlamMap( SlamMap slamMap )
	    {
	    	if( slamMap == null )
	    		return;
	    	
	        if( irDistances == null )
	        	return;
	        
	        double cosTheta = Math.cos( theta);
	        double sinTheta = Math.sin( theta );
        	float xs,ys,xe,ye;

        	double[][] ir_positions = this.getIrPositions();
        	double[] ir_thetas = this.getIrThetas();
        	
        
	        
	        for( int i=0; i< irDistances.length; i++ )
	        {
	        	if( irDistances[i] >= 0.7 )
	        		continue;
	        	
	        	double irx, iry;
	        	irx = ir_positions[i][0] + irDistances[i]*Math.cos( ir_thetas[i]);
	        	iry = ir_positions[i][1] + irDistances[i]*Math.sin( ir_thetas[i]);
	        	
	        	//width/2 + x*mScale, height/2 - y*mScale,
				xs = (float)(x + irx *cosTheta - iry*sinTheta);
				ys = (float)(y + irx *sinTheta + iry*cosTheta);
	        	
				slamMap.setObstacle(xs, ys);
	        }
	        
	        //clear the robot position's obstacle
	        double grid = slamMap.getGrid();
	        
	        for( double x1 = xl; x1<xr; x1+=grid)
	        {
	        	for(double y1=yl; y1<yr; y1+=grid)
	        	{
	        		xs = (float)(x + x1 *cosTheta - y1*sinTheta);
					ys = (float)(y + x1 *sinTheta + y1*cosTheta);
		        	
					slamMap.clearObstacle(xs, ys);
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
		
		
	   //使用robot的原始坐标
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
	   



		public void setIrDistances( double[] value )
		{
			this.irDistances = value;
		}
		
		double mt[][] = new double[3][3];
		double tt[][] = new double[3][3];
		double res[][] = new double[3][3];

		private double[][] getObstacleMatrix( double distance, int idx)
		{
			
        	double[][] ir_positions = this.getIrPositions();
        	double[] ir_thetas = this.getIrThetas();

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

		public void setObstacleCrossPoints(ObstacleCrossPoint[] ocps) {
			mocps = ocps;
		}

		public void setControllerInfo(ControllerInfo ctrlInfo) {
			mCtrlInfo = ctrlInfo;			
		}
		
		/**
		 * 所有IR传感器的位置坐标
		 * @return
		 */
		abstract double[][] getIrPositions();
		
		/**
		 * 所有传感器的角度
		 * @return
		 */
		abstract double[] getIrThetas();
		
		
		//添加arduino 主板 （公共定义的）
		protected void addArduino(double x, double y ) 
		{
			ComponentInfo component;
			component = new ComponentInfo(Color.CYAN, offset(arduino1, x, y));
			addComponent( component );

			component = new ComponentInfo(Color.DKGRAY, offset(arduino2, x, y));
			addComponent( component );

			component = new ComponentInfo(Color.DKGRAY, offset(arduino3, x, y));
			addComponent( component );
			component = new ComponentInfo(Color.DKGRAY, offset(arduino4, x, y));
			addComponent( component );
			component = new ComponentInfo(Color.LTGRAY, offset(arduino5, x, y));
			addComponent( component );
			component = new ComponentInfo(Color.DKGRAY, offset(arduino6, x, y));
			addComponent( component );
			component = new ComponentInfo(Color.DKGRAY, offset(arduino7, x, y));
			addComponent( component );
		}
		
		//添加两个电芯的电池盒
		protected void addTwoCellBattery(double x, double y )
		{
			ComponentInfo component;
			component = new ComponentInfo(Color.BLACK, offset(bat1, x, y));
			addComponent( component );

			component = new ComponentInfo(Color.GREEN, offset(bat2, x, y));
			addComponent( component );

			component = new ComponentInfo(Color.GREEN, offset(bat3, x, y));
			addComponent( component );
			
		}

		private double[][] offset(double path[][], double xs, double ys)
		{
			int len1 = path.length;
			for( int i=0; i<len1; i++)
			{
				path[i][0] = xs + path[i][0];
				path[i][1] = ys + path[i][1];
			}
			return path;
		}
	

		//arduino 主板
		double arduino1[][] = {
				{-0.027, -0.035, 1}, {-0.027, 0.035, 1}, 
				{0.027, 0.035,1}, {0.027, -0.035, 1}
		};


		//arduino 左接口
		double arduino2[][] = {
				{-0.0248, -0.0316,1}, {-0.0248, 0.0074, 1}, 
				{-0.022, 0.0074,1}, {-0.022, -0.0316,1}
		};
		
		
		//arduino 上芯片
		double arduino3[][] = {
				{-0.014, 0.008, 1}, {-0.014, 0.018,1},
				{-0.004, 0.018,1}, {-0.004, 0.008, 1}
		};
		
		//arduino 电源插口
		double arduino4[][] = {
				{-0.022, 0.032,1}, {-0.022, 0.038, 1},
				{-0.016, 0.038, 1}, {-0.016, 0.032, 1}
		};
		
		
		//arduino USB
		double arduino5[][] = {
				{0.010,0.035,1}, {0.010,0.041, 1},
				{0.022, 0.041, 1}, {0.022, 0.035, 1}
		};
		
		//arduino 右接口
		double arduino6[][] = {
				{0.020, -0.032, 1}, {0.020, 0.016, 1},
				{0.0228, 0.016, 1}, {0.0228, -0.032, 1}
		};
		
		//arduino 主芯片
		double arduino7[][] = {
				{-0.012, -0.015, 1}, {-0.012, 0.003, 1},
				{0.006, 0.003, 1}, {0.006, -0.015,1}
		};
		
		double bat1[][] = {
				{-0.025, -0.038, 1}, {-0.025, 0.038, 1},
				{0.025, 0.038, 1}, {0.025, -0.038, 1}
		};
		
		double bat2[][] = {
				{-0.02,-0.034, 1}, {-0.02, 0.034, 1}, 
				{-0.002, 0.034, 1}, {-0.002, -0.034, 1}
		};
		
		double bat3[][] = {
				{0.0018, -0.034,1}, {0.0018, 0.034,1},
				{0.0198, 0.034, 1}, {0.0198, -0.034,1}
		};
		
}
