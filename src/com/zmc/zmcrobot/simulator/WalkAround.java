package com.zmc.zmcrobot.simulator;

import com.zmc.zmcrobot.Settings;

import android.util.Log;

public class WalkAround extends Controller {

	private static String TAG = "WalkAround";
	private Vector uAvo = new Vector();
	private Vector u_fw_t = new Vector();

	private double atObstacle;
	private double mThetaAo;
	
	public WalkAround() {
		Kp = 5;
		Ki = 0.01;
		Kd = 0.5;
		lastError = 0;
		lastErrorIntegration = 0;
		atObstacle = 0.2;
		mThetaAo = 0;
	}

	Output output = new Output();


	
	//@Override
	Output execute(AbstractRobot robot, Input input, double dt) {

		double sensor_gains[] = { 0.5, 0.5, 1, 0.5, 0.5 };

		IRSensor[] irSensors = robot.getIRSensors();

		double uao_x = 0, uao_y = 0;
		boolean bAtObstacle = false;
		
		for (int i = 0; i < 5; i++) {
			if( irSensors[i].distance < atObstacle )
				bAtObstacle = true;
			
			uao_x = uao_x + (irSensors[i].xw - robot.x) * sensor_gains[i];
			uao_y = uao_y + (irSensors[i].yw - robot.y) * sensor_gains[i];
		}


		double e_k, e_I, e_D, w, theta_ao;
		theta_ao = mThetaAo;

		if( bAtObstacle )
		{
			
//			if( irSensors[3].distance < irSensors[1].distance)
//			{
//				uao_x = irSensors[3].xw - robot.x;
//				uao_y = irSensors[3].yw - robot.y;
//			}
//			else
//			{
//				uao_x = irSensors[1].xw - robot.x;
//				uao_y = irSensors[1].yw - robot.y;
//				
//			}
//			
//			uao_x = uao_x - (irSensors[2].xw - robot.x);
//			uao_y = uao_y - (irSensors[2].yw - robot.y);
//			
			theta_ao = Math.atan2(uao_y, uao_x);			
			
			mThetaAo = theta_ao;
		}
		
		e_k = theta_ao - robot.theta;

		// this.getWallVector(irSensors);
		// Log.i("AVO", uao_x + ", " + uao_y + ", " + theta_ao + ", " +
		// thetaFw);

		e_k = Math.atan2(Math.sin(e_k), Math.cos(e_k));

		e_I = lastErrorIntegration + e_k * dt;
		e_D = (e_k - lastError) / dt;
		w = Kp * e_k + Ki * e_I + Kd * e_D;
		lastErrorIntegration = e_I;
		lastError = e_k;

		output.v = input.v;
		output.w = w;
		return output;

	}

//	static double maxDistance = 0.5;

	

	@Override
	void reset() {
		lastError = 0;
		lastErrorIntegration = 0;

	}

	@Override
	void getControllorInfo(ControllerInfo state) {
		state.uAoidObstacle = uAvo;
	}

	public void updateSettings(Settings settings)
	{
		super.updateSettings(settings);
		
		atObstacle = settings.atObstacle;
		
	}
	
}
