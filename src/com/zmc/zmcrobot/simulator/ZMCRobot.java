package com.zmc.zmcrobot.simulator;

import java.util.ArrayList;

import android.util.Log;

import com.zmc.zmcrobot.RobotState;
import com.zmc.zmcrobot.Settings;
import com.zmc.zmcrobot.ui.Obstacle;
import com.zmc.zmcrobot.ui.ObstacleCrossPoint;

public class ZMCRobot {

	private RobotState mState = new RobotState();
	
	double x, y, theta;
	//double vel_l, vel_r;
	double velocity;
	
	double wheel_radius;
	public double wheel_base_length;

	double max_rpm;
	double min_rpm;

	double max_vel, min_vel; //min_vel 代表电机可以移动的最小角速度
	
	double max_v, min_v;
	double max_w;
	double angle;

	int ticks_per_rev;
	double m_per_tick;
	long prev_left_ticks, prev_right_ticks;

	IRSensor irSensors[] = new IRSensor[5];
	double irDistances[] = new double[5];
	
	ObstacleCrossPoint ocps[] = new ObstacleCrossPoint[5];
	

	private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

	private final static String TAG = "ZMCRobot";

	public ZMCRobot() {

		x = 0;
		y = 0;
		theta = 0;
//		vel_l = 0;
//		vel_r = 0;
		prev_left_ticks = 0;
		prev_right_ticks = 0;

		wheel_radius = 0.065 / 2;
		wheel_base_length = 0.13;
		ticks_per_rev = 20;
		m_per_tick = (2 * Math.PI * wheel_radius) / ticks_per_rev;

		max_rpm = 240; // 267
		max_vel = max_rpm * 2 * Math.PI / 60;

		min_rpm = 150; // 113
		min_vel = min_rpm * 2 * Math.PI / 60;

		max_v = max_vel * wheel_radius;
		min_v = min_vel * wheel_radius;
		max_w = (wheel_radius / wheel_base_length) * (max_vel - min_vel);

//		irSensors[0] = new IRSensor(-0.0582,0.0584, 3*Math.PI/2);//Math.PI/2);
//		irSensors[1] = new IRSensor(0.05725, 0.03555,  2*Math.PI-Math.PI/4);//Math.PI/4);
//		irSensors[2] = new IRSensor(0.0686, 0.0, 0);
//		irSensors[3] = new IRSensor(0.05725, -0.0355, Math.PI/4);//-Math.PI/4);
//		irSensors[4] = new IRSensor(-0.0582, -0.0584, Math.PI/2);//-Math.PI/2);

		irSensors[0] = new IRSensor(-0.0582,0.0584, Math.PI/2);
		irSensors[1] = new IRSensor(0.05725, 0.03555, Math.PI/4);
		irSensors[2] = new IRSensor(0.0686, 0.0, 0);
		irSensors[3] = new IRSensor(0.05725, -0.0355, 2*Math.PI -Math.PI/4);
		irSensors[4] = new IRSensor(-0.0582, -0.0584, 2*Math.PI -Math.PI/2);
		
		
	//	double ir_thetas[] = {3*Math.PI/2, 2*Math.PI-Math.PI/4, 0, Math.PI/4, Math.PI/2 };

		ocps[0] = new ObstacleCrossPoint();
		ocps[1] = new ObstacleCrossPoint();
		ocps[2] = new ObstacleCrossPoint();
		ocps[3] = new ObstacleCrossPoint();
		ocps[4] = new ObstacleCrossPoint();
		
//		double ir_positions[][] = {{-0.0582,0.0584}, {0.05725, 0.03555},{0.0686, 0.0},{0.05725, -0.0355},{-0.0582, -0.0584}};
//		double ir_thetas[] = {3*Math.PI/2, 2*Math.PI-Math.PI/4, 0, Math.PI/4, Math.PI/2 };

	}

	public void addObstacle(Obstacle obs) {
		obstacles.add(obs);

	}

	public IRSensor[] getIRSensors() {
		return irSensors;

	}

	 public void setPosition(double x, double y, double theta )
	 {
		 this.x = x;
		 this.y = y;
		 this.theta = theta;
		 this.updateIRDistances();
	 }
	
	public void updateState(long left_ticks, long right_ticks, double dt) {

		// long left_ticks, right_ticks;
		if (prev_right_ticks == right_ticks && prev_left_ticks == left_ticks) {
//			vel_l = 0;
//			vel_r = 0;
			velocity = 0;
			mState.velocity = 0;
			return; // no change
		}
		double d_right, d_left, d_center;

		d_right = (right_ticks - prev_right_ticks) * m_per_tick;
		d_left = (left_ticks - prev_left_ticks) * m_per_tick;

//		vel_l = (d_left / wheel_radius) / dt;
//		vel_r = (d_right / wheel_radius) / dt;

		velocity = (d_right + d_left)/(2*dt);
		
		prev_right_ticks = right_ticks;
		prev_left_ticks = left_ticks;

		d_center = (d_right + d_left) / 2;
		double phi = (d_right - d_left) / wheel_base_length;

		x = x + d_center * Math.cos(theta);
		y = y + d_center * Math.sin(theta);
		theta = theta + phi;
		theta = Math.atan2(Math.sin(theta), Math.cos(theta));

		mState.x = x;
		mState.y = y;
		mState.theta = theta;
		mState.velocity = velocity;
		
		updateIRDistances();

	}

	
	public double[] getIRDistances()
	{
		return irDistances;
	}
	
	public double getObstacleDistance()
	{
		  double d = irDistances[1];
		  if ( d > irDistances[2])
		    d =  irDistances[2];
		  if ( d >  irDistances[3])
		    d =  irDistances[3];
		  return d;		
	}
	
	public ObstacleCrossPoint[] getObstacleCrossPoints()
	{
		return ocps;
	}
	
	private void updateIRDistances() {

		if (irDistances == null)
			irDistances = new double[5];

		double cosTheta, sinTheta;
		cosTheta = Math.cos( theta );
		sinTheta = Math.sin( theta );
		
		double x0, y0, theta0;
		for (int i = 0; i < 5; i++) {
			ocps[i].distance = 1000;
			irDistances[i] = 1000;
			IRSensor irSensor = irSensors[i];
//			xs = (float)(x + ir_positions[i][0] *cosTheta + ir_positions[i][1]*sinTheta);
//			ys = (float)(y - ir_positions[i][0] *sinTheta + ir_positions[i][1]*cosTheta);
	
			x0 = x + irSensor.x_s *cosTheta - irSensor.y_s*sinTheta;
			y0 = y + irSensor.x_s *sinTheta + irSensor.y_s*cosTheta;
			
			theta0 = irSensor.theta_s + theta;
			
			double theta1 = Math.atan2(Math.sin(theta0), Math.cos(theta0));
			
	//		Log.i(TAG, "Get Distance:[" + x0 + ", " + y0 + "," + theta0 + ", " + theta);
			
			double d = 100;
			for (Obstacle obs : obstacles) {
				obs.getDistance(x0, y0, theta0, ocps[i]);
				if (ocps[i].distance < irDistances[i])
					irDistances[i] = ocps[i].distance;

			}
			 //4-30cm ir sensor
			if (irDistances[i] > 0.3)
				irDistances[i] = 0.3;
			if (irDistances[i] < 0.04)
				irDistances[i] = 0.04;

	//		Log.i(TAG, "Distance:[" +irDistances[i] + "] ");
			
			irSensor.setDistance(irDistances[i]);
			irSensor.applyGeometry(x, y, theta);
		}
	}

	public void reset(long left_ticks, long right_ticks) {
		prev_left_ticks = left_ticks;
		prev_right_ticks = right_ticks;
	}

	public Vel uni_to_diff(double v, double w) {
		Vel vel = new Vel();
		vel.vel_r = (2 * v + w * wheel_base_length) / (2 * wheel_radius);
		vel.vel_l = (2 * v - w * wheel_base_length) / (2 * wheel_radius);
		return vel;
	}

	public Output diff_to_uni(double vel_l, double vel_r) {
		Output out = new Output();
		if (vel_l + vel_r == 0) {
			Log.i(TAG, "div by o...in robot 1");
			out.v = 0.5;
			return out;
		} else
			out.v = wheel_radius / 2 * (vel_l + vel_r);

		if (vel_r - vel_l == 0) {
			Log.i(TAG, "div by o...in robot 2");
			out.w = Math.PI / 2;
		} else
			out.w = wheel_radius / wheel_base_length * (vel_r - vel_l);

		return out;
	}

	// IRSensor **getIRSensors();

	// void updateSettings(SETTINGS settings);

	// 根据电机参数，限速度与拐弯角速度
	
	/*
	public Vel ensure_w(double v, double w) {
		Vel vel = uni_to_diff(v, w);
		double vel_l, vel_r;

		double vel_min, vel_max;
		vel_min = vel.vel_l;
		vel_max = vel.vel_r;
		if( vel_min > vel_max )
		{
			vel_min = vel.vel_r;
			vel_max = vel.vel_l;
		}

//		min_vel = 0;
//		if( Math.abs( w ) < 2 )
//			min_vel = 5;
		
		vel_r = vel.vel_r;
		vel_l = vel.vel_l;
		if( vel_min < 0 )
		{
			vel_r = vel.vel_r - vel_min;
			vel_l = vel.vel_l - vel_min;
		}
		
		vel_max = Math.max( vel_r, vel_l);
		
		if( Math.max(vel_r, vel_l) > max_vel)
		{
			vel_r = vel_r -( vel_max - max_vel);
			vel_l = vel_l - ( vel_max - max_vel);
			if( vel_r < 0 )
				vel_r = 0;
			if( vel_l < 0 )
				vel_l = 0;
		}
		
		vel_max = Math.max( vel_r, vel_l);
		if( vel_max < min_vel)
		{
			if( vel_r > vel_l )
			{
				vel_r = min_vel;
//				double dif = vel_r - vel_l;
//				vel_l = vel_r - dif;
			}
			else
			{
				vel_l = min_vel;
//				double dif = vel_l - vel_r;
//				vel_r = vel_l - dif;
			}
		}


		vel.vel_l = vel_l;
		vel.vel_r = vel_r;
		
		return vel;
	}

*/
	
	
	Vel ensure_w(double v, double w)
	{
		if ( w > max_w )
		    w = max_w;
		  else if ( w < 0 && Math.abs(w) > max_w )
		    w = -max_w;

		  Vel vel = uni_to_diff(v, w);
		  double vel_l, vel_r;

		  double vel_min, vel_max;
		  vel_min = vel.vel_l;
		  vel_max = vel.vel_r;

		  if ( vel_min > vel.vel_r )
		  {
		    vel_min = vel.vel_r;
		    vel_max = vel.vel_l;
		  }

		  // stop one motor to support large angle turning
		  double minVel = 0;
		  if ( Math.abs( w ) < 0.2 )
		    minVel = min_vel;

		  if ( vel_max > max_vel )
		  {
		    vel_r = vel.vel_r - (vel_max - max_vel);
		    vel_l = vel.vel_l - (vel_max - max_vel);

		  }
		  else if ( vel_min < minVel )
		  {
		    vel_r = vel.vel_r + (minVel - vel_min);
		    vel_l = vel.vel_l + (minVel - vel_min);

		  }
		  else
		  {
		    vel_r = vel.vel_r;
		    vel_l = vel.vel_l;
		  }

		      if( vel_l < minVel )
		        vel_l = minVel;
		       else if( vel_l > max_vel )
		        vel_l = max_vel;

		      if( vel_r < minVel )
		        vel_r = minVel;
		       else if( vel_r > max_vel )
		        vel_r = max_vel;
		  

		  vel.vel_l = vel_l;
		  vel.vel_r = vel_r;
		  return vel;
}
	
	

Vel ensure_w_old(double v, double w)
{
//  if ( w > max_w )
//    w = max_w;
//  else if ( w < 0 && abs(w) > max_w )
//    w = -max_w;

  Vel vel = uni_to_diff(v, w);
  double vel_l, vel_r;

  double vel_min, vel_max;
  vel_min = vel.vel_l;
  vel_max = vel.vel_r;

  if ( vel_min > vel.vel_r )
  {
    vel_min = vel.vel_r;
    vel_max = vel.vel_l;
  }

  vel_l = vel.vel_l;
  vel_r = vel.vel_r;
  if( vel_min < 0 )
  {
      vel_l = vel.vel_l - vel_min;
      vel_r = vel.vel_r - vel_min;  
  }
  vel_max = Math.max( vel_l, vel_r );
  if( Math.max(vel_l, vel_r) > max_vel )
  {
    vel_l = vel_l - ( vel_max - max_vel);
    vel_r = vel_r - (vel_max - max_vel );
    if( vel_l < 0 )
      vel_l = 0;
    if( vel_r < 0 )
      vel_r = 0;  
  }
  vel_max = Math.max(vel_l, vel_r );
  if( vel_max < min_vel )
  {
    if( vel_r > vel_l )
      vel_r = min_vel;
    else
      vel_l = min_vel;
  }
  vel.vel_l = vel_l;
  vel.vel_r = vel_r;
  return vel;

}	
	
	


double vel_l_to_pwm( double vel)
{
  //ax^2+bx+c
  double nvel = Math.abs( vel );
  double retVal = 6.393*nvel + 13.952;

  if( vel >= 0 )
    return retVal;
  else
    return -retVal;
}


double vel_r_to_pwm( double vel)
{
  //ax^2+bx+c
  double nvel = Math.abs( vel );
  double retVal = 6.2798*nvel + 18.787;

  if( vel >= 0 )
    return retVal;
  else
    return -retVal;
 
}



double pwm_to_ticks_r(double pwm, double dt)
{
	double npwm = Math.abs(pwm);
	if( npwm < 20 )
		return 0;
 
  double ticks = dt*(0.5084*npwm - 9.7666);
  if( pwm > 0 )
    return ticks;
  else
   return -ticks;
 }


double pwm_to_ticks_l(double pwm, double dt)
{
	double npwm = Math.abs(pwm);
	if( npwm < 14)
		return 0;
	
  double ticks = dt*(0.4975*npwm -6.9066);
  if( pwm > 0 )
    return ticks;
  else
   return -ticks;

}
	
	
	

	void updateSettings(Settings settings) {
		max_rpm = settings.max_rpm; // 267
		max_vel = max_rpm * 2 * Math.PI / 60;

		min_rpm = settings.min_rpm; // 113
		min_vel = min_rpm * 2 * Math.PI / 60;

		max_v = max_vel * wheel_radius;
		min_v = min_vel * wheel_radius;
		max_w = (wheel_radius / wheel_base_length) * (max_vel - min_vel);

		Log.i(TAG, "UpdateSettings: mrpm:" + max_rpm + ", min_rpm:" + min_rpm );
	}

	public RobotState getState() {
		return mState;
	}

}
