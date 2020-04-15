package com.zmc.zmcrobot.simulator;


import android.util.Log;

import com.zmc.zmcrobot.simulator.ZMCRobot;
public class GotoGoalVelocityCtrl extends Controller {

	private static final String TAG = "GTG-V";
	
	Output output = new Output();

	public GotoGoalVelocityCtrl() {
		// TODO Auto-generated constructor stub
	}
	 
	
	@Override
	Output execute(AbstractRobot robot, Input input, double dt) {
		output.v = input.v;
		double u_x, u_y, e, e_I, e_D;

		u_x = input.x_g - robot.x;
		u_y = input.y_g - robot.y;
		e = Math.sqrt(Math.pow(u_x,2) + Math.pow(u_y, 2));
		if( e > 0.2 )
		 return output;
		
		  e_I = lastErrorIntegration + e * dt;
		  e_D = (e - lastError )/dt;
		  double w = Kp * e;// + Ki * e_I + Kd * e_D;
		  lastErrorIntegration = e_I;
		  lastError = e;
	  
		  output.v = w;
		  
		  return output;
		
		
		
	}


	@Override
	void getControllorInfo(ControllerInfo state) {
		
	}


	
	

	
	
}
