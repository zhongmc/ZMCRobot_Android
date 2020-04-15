package com.zmc.zmcrobot.simulator;

import java.util.ArrayList;

import android.util.Log;

import com.zmc.zmcrobot.RobotState;
import com.zmc.zmcrobot.Settings;
import com.zmc.zmcrobot.ui.Obstacle;
import com.zmc.zmcrobot.ui.ObstacleCrossPoint;

public abstract class AbstractRobot {

	private RobotState mState = new RobotState();
	
	protected Settings settings = new Settings();
	
	double x, y, theta; //位置
	double w;
	
	double velocity;
	
	double wheel_radius;  //轮子半径
	public double wheel_base_length;  //轮距

	//最大和最小轮子每分钟圈数
	double max_rpm;
	double min_rpm;

	double max_vel, min_vel; //min_vel 代表电机可以移动的最小角速度
	
	double max_v, min_v;
	double min_w, max_w;
//	double angle;

	int ticks_per_rev_l, ticks_per_rev_r ; //每圈的脉冲数
	double m_per_tick_l, m_per_tick_r;  //每个脉冲转过的距离
	long prev_left_ticks, prev_right_ticks;

	IRSensor irSensors[] = new IRSensor[5];
	double irDistances[] = new double[5];
	
	ObstacleCrossPoint ocps[] = new ObstacleCrossPoint[5];
	private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

	private final static String TAG = "ZMCRobot";

	public AbstractRobot() {
	}
	
	public String toString()
	{
		return x + ", " + y + ":" + theta;
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

		d_left = (left_ticks - prev_left_ticks) * m_per_tick_l;
		d_right = (right_ticks - prev_right_ticks) * m_per_tick_r;

//		vel_l = (d_left / wheel_radius) / dt;
//		vel_r = (d_right / wheel_radius) / dt;

		velocity = (d_right + d_left)/(2*dt);
		
		prev_right_ticks = right_ticks;
		prev_left_ticks = left_ticks;

		d_center = (d_right + d_left) / 2;
		double phi = (d_right - d_left) / wheel_base_length;
		w = phi/dt;

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
	
	
	
	//获取IR 指定距离的向量
	public Vector getIRSensorVector(int idx, double distance )
	{
		Vector p = new Vector();
		IRSensor irSensor = irSensors[idx].clone();
		irSensor.setDistance( distance );
		irSensor.applyGeometry(x, y, theta);
		
		p.x = irSensor.xw;
		p.y = irSensor.yw;
		return p;
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
			if (irDistances[i] > IRSensor.maxDistance )
				irDistances[i] = IRSensor.maxDistance;
			if (irDistances[i] < IRSensor.minDistance)
				irDistances[i] = IRSensor.minDistance;

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


	
	
	public abstract Vel ensure_w(double v, double w);
	
	

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
	
	


public abstract double vel_l_to_pwm( double vel);

public abstract double vel_r_to_pwm( double vel);



public abstract double pwm_to_ticks_r(double pwm, double dt);

public abstract double pwm_to_ticks_l(double pwm, double dt);


/**
 * 针对不同的电机、robot，给不同的参数设定
 * @return
 */
	public Settings getSettings()
	
	{
		return settings;
	}

	

	void updateSettings(Settings settings) {
		
		this.settings.settingsType = 1;
		this.settings.copyFrom(settings); //update PID param
		this.settings.settingsType = 5;
		this.settings.copyFrom(settings);  //update 
		
		wheel_radius = settings.wheelRadius;
		wheel_base_length = settings.wheelDistance;
		
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
