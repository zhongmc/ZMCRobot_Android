package com.zmc.zmcrobot.simulator;


import android.util.Log;


public class GotoGoal extends Controller {

	private static final String TAG = "GTG";
	private double m_xg, m_yg;
	
	private int state = 0; //normal state
	private boolean targetMoified = false;
	
	Output output = new Output();
	private Vector uGtg = new Vector();

	public GotoGoal() {
		// TODO Auto-generated constructor stub
	}
	  

	@Override
	Output execute(AbstractRobot robot, Input input, double dt)
	{
//		return execute2(robot, input, dt);
		
		return dirGoal1(robot, input, dt);
	}
	
	

	
	//到达指定点，转圈的方式定位
	Output dirGoal(AbstractRobot robot, Input input, double dt) {

		double u_x, u_y, e, e_I, e_D, w, theta_g;

		u_x = input.x_g - robot.x;
		u_y = input.y_g - robot.y;

		double d = Math.sqrt(Math.pow(u_x,2) + Math.pow(u_y, 2));

		double thD = robot.theta - input.theta;
		thD = Math.atan2(Math.sin(thD), Math.cos(thD));

		thD = Math.abs( thD);
		
		if( d < 0.4 && state == 0 && thD > Math.PI/6)
		{
			state = 1;
			double th = input.theta - Math.PI/2;


			m_xg = input.x_g + robot.wheel_base_length*Math.cos(th); 
			m_yg = input.y_g + robot.wheel_base_length*Math.sin(th);
			
//			th = Math.atan2(robot.y - m_yg, robot.x - m_xg);
//			m_xg = m_xg + robot.wheel_base_length*Math.cos(th)/2;
//			m_yg = m_yg + robot.wheel_base_length*Math.sin(th)/2;
		
		}
		
	  if( state == 1)
	  {
		  u_x = m_xg - robot.x;
		  u_y = m_yg - robot.y;

		  double d1 = Math.sqrt(Math.pow(u_x,2) + Math.pow(u_y, 2));
		  if( d1 < 0.01 )
		  {
			  u_x = input.x_g - robot.x;
			  u_y = input.y_g - robot.y;
			  d = Math.sqrt(Math.pow(u_x,2) + Math.pow(u_y, 2));
			  state = 2; //recover 
			  lastErrorIntegration = 0;
			  lastError = 0;
			  Log.i(TAG, "recover target!" + thD + ", " + robot.theta);
		  }
			  
	  }
		  
	  if( state == 2 )
	  {
		  output.v = 0;
		  theta_g = input.theta;

		  e = theta_g - robot.theta;
		  e = Math.atan2(Math.sin(e), Math.cos(e));
		  e_I = lastErrorIntegration + e * dt;
		  e_D = (e - lastError )/dt;
		  w = Kp * e + Ki * e_I + Kd * e_D;
		  lastErrorIntegration = e_I;
		  lastError = e;
		  if( w < 0 )
			  output.w = w;
		  else
			  output.w = -w;
		  
		  return output;
		  
	  }
	  
	  output.v = input.v;
	  if( d < 0.01 ) //at goal
	  {
		  output.v = 0;
		  theta_g = input.theta;
	  }
	  else
		  theta_g = Math.atan2(u_y, u_x);
		  
	  uGtg.x = u_x;
	  uGtg.y = u_y;
		  
		  e = theta_g - robot.theta;
		  e = Math.atan2(Math.sin(e), Math.cos(e));
		  e_I = lastErrorIntegration + e * dt;
		  e_D = (e - lastError )/dt;
		  w = Kp * e + Ki * e_I + Kd * e_D;
		  lastErrorIntegration = e_I;
		  lastError = e;
	  
		  output.w = w;
		  
		  return output;
	}	
		

	
	
	Output execute2(AbstractRobot robot, Input input, double dt) {

		 double u_x, u_y, e, e_I, e_D, w, theta_g;
		  
		 double dd = 2.5*robot.wheel_base_length;
		 
		  output.v = input.v;
		  
		 if( state == 0 ) // first set the goal to abc
		 {
			  state = 1;

			  u_x = input.x_g - robot.x;
			  u_y = input.y_g - robot.y;

			  double tg = Math.atan2(u_y, u_x);
			  double tDif = tg - input.theta;
			  if( tDif > Math.PI )
				  tDif = tDif - 2*Math.PI;
			  else if( tDif < -Math.PI )
				  tDif = tDif + 2*Math.PI;
			  if(tDif > 2.8  || tDif < -2.8)
			  {		
				  Log.i(TAG, "reverse enter: " + tDif); 
				  m_xg = input.x_g - dd*Math.cos(input.theta - Math.PI/4);
				  m_yg = input.y_g - dd*Math.sin(input.theta - Math.PI/4);	
			  }
			  else
			  {
				  m_xg = input.x_g - dd*Math.cos(input.theta);
				  m_yg = input.y_g - dd*Math.sin(input.theta);				  
			  }
		 }
		 
		  u_x = input.x_g - robot.x;
		  u_y = input.y_g - robot.y;
		 
		 
		 if( state == 1 )
		 {

			  u_x = m_xg - robot.x;
			  u_y = m_yg - robot.y;

			  double d1 = Math.sqrt(Math.pow(u_x,2) + Math.pow(u_y, 2));
			  
			  if( !targetMoified && d1 < 0.3 ) //如果接近角大于120度，应该将目标稍微偏一点？
			  {
		  
				  double tg = Math.atan2(u_y, u_x);
				  Log.i(TAG, "enter theta: "  + 180*tg/3.14 + " target: " + m_xg + ", " + m_yg);
		  
				  double tDif = tg - input.theta;
			  
				  Log.i(TAG, String.format("theta dif: %.4f", tDif)); 
				  if( tDif > Math.PI )
					  tDif = tDif - 2*Math.PI;
				  else if( tDif < -Math.PI )
					  tDif = tDif + 2*Math.PI;

				  Log.i(TAG, "theta dif1: " + tDif); 
				  if(tDif > Math.PI/2  || tDif < -Math.PI/2)
				  {
					  double xpg, ypg, xmg, ymg, thetaP, thetaM, thetaG, e1, e2;
					  xpg = input.x_g + dd*Math.cos(Math.PI + input.theta + 0.3);
					  ypg = input.y_g + dd*Math.sin(Math.PI + input.theta + 0.3);
					  
					  thetaP = Math.atan2(ypg - robot.y, xpg - robot.x); //the target theta
					  thetaG = Math.atan2(input.y_g - ypg, input.x_g - xpg);
					  e1 = thetaG - thetaP;
					  e1 = Math.atan2(Math.sin(e1), Math.cos(e1));
					  
					  
					  
					  xmg = input.x_g + dd*Math.cos(Math.PI + input.theta - 0.3);
					  ymg = input.y_g + dd*Math.sin(Math.PI + input.theta - 0.3);
					  thetaM = Math.atan2(ymg - robot.y, xmg - robot.x);

					  thetaG = Math.atan2(input.y_g - ymg, input.x_g - xmg);
					  e2 = thetaG - thetaM;
					  e2 = Math.atan2(Math.sin(e2), Math.cos(e2));
					  
					  Log.i(TAG, "e plus:" + e1 + "; e minu:" + e2);
					  
					  if( e1 < 0 )
					  {
						  m_xg = xpg;
						  m_yg = ypg;
						  targetMoified = true;
						  Log.i(TAG, "Change to plus!");
					  }
					  else if( e2 > 0)
					  {
						  m_xg = xmg;
						  m_yg = ymg;
						  targetMoified = true;
						  Log.i(TAG, "Change to Minus!");
						  
					  }
					  else
					  {
						  m_xg = xpg;
						  m_yg = ypg;
						  targetMoified = true;
						  Log.i(TAG, "failed to choice!, Change to plus!");
						  
					  }
					  
				  }
				  
//				  if( tDif > Math.PI/2)
//				  {
//						  m_xg = input.x_g + dd*Math.cos(Math.PI + input.theta + 0.3);
//						  m_yg = input.y_g + dd*Math.sin(Math.PI + input.theta + 0.3);
//					  targetMoified = true;
//					  
//				  }
//				  else if( tDif < -Math.PI/2)
//				  {
//						  m_xg = input.x_g + dd*Math.cos(Math.PI + input.theta - 0.3);
//						  m_yg = input.y_g + dd*Math.sin(Math.PI + input.theta - 0.3);
//					  targetMoified = true;
//					  
//				  }
					  
			  }
				  
			  if( d1 < 0.03 )
			  {
				  u_x = input.x_g - robot.x;
				  u_y = input.y_g - robot.y;
				  
				  lastError = 0;
				  lastErrorIntegration = 0;
				  state = 2; //recover 
				  
			  }
			  
			  if( d1 < 0.1 )
				  output.v = this.Kp * d1;
			  			 
			 
		 }
		 
		 
	

		  double d = Math.sqrt(Math.pow(u_x,2) + Math.pow(u_y, 2));
	  
		  if( d < 0.01 ) //at goal
		  {
			  output.v = 0;
			  theta_g = input.theta;
		  }
		  else
			  theta_g = Math.atan2(u_y, u_x);

		  uGtg.x = u_x;
		  uGtg.y = u_y;
		  
		  e = theta_g - robot.theta;
		  e = Math.atan2(Math.sin(e), Math.cos(e));
		  e_I = lastErrorIntegration + e * dt;
		  e_D = (e - lastError )/dt;
		  w = Kp * e + Ki * e_I + Kd * e_D;
		  lastErrorIntegration = e_I;
		  lastError = e;
	  
		  if( state == 2 && Math.abs(e) > 0.1)
			  output.v = 0;
		  
		  output.w = w;
		  
		  return output;
	}	
	
	
	//到达指定点，转圈的方式定位
	Output dirGoal1(AbstractRobot robot, Input input, double dt) {

		double u_x, u_y, e, e_I, e_D, w, theta_g;

		u_x = input.x_g - robot.x;
		u_y = input.y_g - robot.y;

		theta_g = Math.atan2(u_y, u_x);

		double d = Math.sqrt(Math.pow(u_x,2) + Math.pow(u_y, 2));
		
		output.v = input.v;
		
		
		  if( d < 0.02 ) //at goal
		  {
			  if(state == 0 )
			  {
				  Log.i(TAG, "Zero V, cirle!");
				  lastError = 0;
				  lastErrorIntegration = 0;
				  state = 1;
			  }
			  output.v = 0;
			  theta_g = input.theta;
		  }
		  else if( d < 0.1 )  //slow down
		  {
			  output.v = 4 * d;
		  }
		  else
		  {
			state = 0;
			output.v = input.v / (1 + 10*Math.abs( theta_g - robot.theta));
		  }

//		output.v = 0;
//		theta_g = input.theta;
		
		

		  uGtg.x = u_x;
		  uGtg.y = u_y;
		  
		  e = theta_g - robot.theta;
		  e = Math.atan2(Math.sin(e), Math.cos(e));
		  e_I = lastErrorIntegration + e * dt;
		  e_D = (e - lastError )/dt;
		  w = Kp * e + Ki * e_I + Kd * e_D;
		  lastErrorIntegration = e_I;
		  lastError = e;
		  output.w = w;
		
		  if( Math.abs(e) < 0.01 )
		  {
			output.w = 0;  
		  }
		  
		  return output;
		
		
	}

	Output execute1(AbstractRobot robot, Input input, double dt) {

		 double u_x, u_y, e, e_I, e_D, w, theta_g;
		  
		 
		  u_x = input.x_g - robot.x;
		  u_y = input.y_g - robot.y;
		 
		  output.v = input.v;

		  double d = Math.sqrt(Math.pow(u_x,2) + Math.pow(u_y, 2));
	  
		  double thD;
		  if( robot.theta < 0 )
			  thD = 2*Math.PI + robot.theta - input.theta;
		  else
			  thD = robot.theta - input.theta;
		  thD = Math.abs( thD);
		  
		  	if( d < 0.4 && state == 0 && thD > Math.PI/6)
		  	{
		  		if( thD < Math.PI/4 || (thD > 2*Math.PI - Math.PI/4))
		  		{
					  Log.i(TAG, "change target 1..." + thD + ", " + robot.theta );
		  			
					  double th = input.theta + Math.PI;
					  m_xg = input.x_g + 0.15*Math.cos(th);
					  m_yg = input.y_g + 0.15*Math.sin(th);
		  			
		  		}
		  		else if( thD > (Math.PI - 0.16) && thD < (Math.PI + 0.16))
		  		{
					  double th = input.theta + Math.PI;
					  if( robot.theta < 0 ) //&& thD > Math.PI/4 )
						  th = th - Math.PI/8;
					  else if( robot.theta > 0 ) //&& thD > Math.PI/4 )
						  th = th + Math.PI/8;
				
					  Log.i(TAG, "change target 2..." + thD + ", " + robot.theta  + ", " + th);
					  
					  m_xg = input.x_g + 0.25*Math.cos(th);  //0.25
					  m_yg = input.y_g + 0.25*Math.sin(th);
		  			
		  		}
		  		else
		  		{
					  Log.i(TAG, "change target 3..." + thD + ", " + robot.theta );
		  			
		  			
					  double th = input.theta + Math.PI;
					  m_xg = input.x_g + 0.2*Math.cos(th); //0.25
					  m_yg = input.y_g + 0.2*Math.sin(th);
					  
					  double Q1, Q2, Q3;
					  
					  Q2 = robot.theta;
					  if( robot.theta < 0 )
					  {
						  Q2 = 2*Math.PI + Q2;
					  }
					  
					  Q3 = input.theta + Math.PI/2;
					  if( Q3 > 2*Math.PI )
						  Q3 = Q3 - 2*Math.PI;
					 
					  Q1 = Math.abs(Q3-Q2);
					//  if( Q1 > Math.PI)
					//	  Q1 = Q1-Math.PI;
					  if( Q1 < Math.PI/2)
					  {
						  Q3 = input.theta - Math.PI/2;
						  Log.i(TAG, "-Pi/2 " + Q1 + ", " + Q2 + ", " + Q3);
					  }
					  else
						  Log.i(TAG, "+Pi/2" + Q1 + ", " + Q2 + ", " + Q3);
						  
		
					  m_xg = m_xg  + 0.08*Math.cos(Q3);
					  m_yg = m_yg  + 0.08*Math.sin(Q3);	 //0.08	  			
		  			
		  		}

		  		state = 1; //change to another goal
		  		
		  		
		  	}
		  
		 
		  
		  if( state == 1)
		  {
			  u_x = m_xg - robot.x;
			  u_y = m_yg - robot.y;

			  double d1 = Math.sqrt(Math.pow(u_x,2) + Math.pow(u_y, 2));
			  if( d1 < 0.03 )
			  {
				  u_x = input.x_g - robot.x;
				  u_y = input.y_g - robot.y;
				  d = Math.sqrt(Math.pow(u_x,2) + Math.pow(u_y, 2));
				  state = 2; //recover 
				  Log.i(TAG, "recover target!" + thD + ", " + robot.theta);
			  }
			  
			  if( d1 < 0.1 )
				  output.v = this.Kp * d1;
			  
		  }
		  
		  
		  if( d < 0.01 ) //at goal
		  {
			  output.v = 0;
			  theta_g = input.theta;
		  }
		  else
			  theta_g = Math.atan2(u_y, u_x);

		  uGtg.x = u_x;
		  uGtg.y = u_y;
		  
		  e = theta_g - robot.theta;
		  e = Math.atan2(Math.sin(e), Math.cos(e));
		  e_I = lastErrorIntegration + e * dt;
		  e_D = (e - lastError )/dt;
		  w = Kp * e + Ki * e_I + Kd * e_D;
		  lastErrorIntegration = e_I;
		  lastError = e;
	  
		  if( state == 2 && Math.abs(e) > 0.1)
			  output.v = 0;
		  
		  output.w = w;
		  
		  return output;
	}	
		
	
	
	@Override
	void reset() {
		  lastError = 0;
		  lastErrorIntegration = 0;
		  state = 0;
		  targetMoified = false;
		
	}

	@Override
	void getControllorInfo(ControllerInfo state) {
		state.uGotoGoal = uGtg;
		state.ux = this.m_xg;
		state.uy = this.m_yg;
	}

	
	
}
