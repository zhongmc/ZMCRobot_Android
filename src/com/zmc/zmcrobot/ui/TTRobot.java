package com.zmc.zmcrobot.ui;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

public class TTRobot extends AbstractRobot {

//	List<ComponentInfo> components;
	
	
	public TTRobot(){
		super();
		ComponentInfo component;
		component = new ComponentInfo(Color.BLUE, qb_base_plate);
		addComponent(component);

		component = new ComponentInfo(Color.BLACK, qb_right_wheel);
		addComponent(component);
		
		component = new ComponentInfo(Color.BLACK, qb_left_wheel);
		addComponent(component);

		addArduino(0, 0);
		addTwoCellBattery(-0.06, 0);

/*		component = new ComponentInfo(Color.DKGRAY, qb_bbb);
		addComponent(component);
		
		component = new ComponentInfo(Color.BLACK, qb_bbb_rail_l);
		addComponent(component);

		component = new ComponentInfo(Color.BLACK, qb_bbb_rail_r);
		addComponent(component);

		component = new ComponentInfo(Color.LTGRAY, qb_bbb_eth);
		addComponent(component);
		
		component = new ComponentInfo(Color.LTGRAY, qb_bbb_usb);
		addComponent(component);
	*/
		
		component  = new ComponentInfo(Color.RED, this.irDim );
		for( int i=0; i<this.ir_thetas.length; i++)
		{
			addComponent(ir_positions[i][0], ir_positions[i][1], this.ir_thetas[i], component);
		}
		
	}

	

	final double qb_base_plate[][] = { 
			{ 0.047, 0.062, 1 },
			{ 0.076, 0.03, 1 }, { 0.076, -0.03, 1 },
			{ 0.047, -0.062, 1 }, { -0.035, -0.063, 1 },
			{ -0.035, -0.093, 1 }, { -0.065, -0.08, 1 },
			{ -0.065, -0.063, 1 }, { -0.15, -0.063, 1 },
			{ -0.15, -0.063, 1 }, 
			
			{ -0.167,  -0.077, 1}, { -0.173,  -0.064, 1}, { -0.16,  -0.044, 1},
			{ -0.16,  0.044, 1},  { -0.173,  0.064, 1}, { -0.167,  0.077, 1},
			
			{-0.15,0.063,1}, { -0.065, 0.063, 1 },
			{ -0.065, 0.08, 1 }, { -0.035, 0.093, 1 },
			{ -0.035, 0.063, 1 }};
   
   
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


	final double qb_left_wheel[][] = { { 0.0325, 0.090, 1 },
			{ 0.0325, 0.069, 1 }, { -0.0325, 0.069, 1 },
			{ -0.0325, 0.090, 1 } };


	final double qb_right_wheel[][] = { { 0.0325, -0.090, 1 },
			{ 0.0325, -0.069, 1 }, { -0.0325, -0.069, 1 },
			{ -0.0325, -0.090, 1 } };
	
	
	final double qb_left_wheel_ol[][] = { { 0.0254, 0.0595, 1 },
			{ 0.0254, 0.0335, 1 }, { -0.0384, 0.0335, 1 },
			{ -0.0384, 0.0595, 1 } };

	final double qb_right_wheel_ol[][] = { { 0.0254, -0.0595, 1 },
			{ 0.0254, -0.0335, 1 }, { -0.0384, -0.0335, 1 },
			{ -0.0384, -0.0595, 1 } };



//	final double qb_ir_1[][] = { { -0.126, 0.063, 1 },
//			{ -0.096, 0.063, 1 }, { -0.096, 0.053, 1 },
//			{ -0.126, 0.053, 1 } };
//	
//
//	final double qb_ir_2[][] = { { 0.044, 0.052, 1 }, { 0.051, 0.059, 1 },
//			{ 0.071, 0.036, 1 }, { 0.064, 0.029, 1 } };
//	
//	final double qb_ir_3[][] = { { 0.066, -0.015, 1 }, { 0.066, 0.015, 1 },
//			{ 0.076, 0.015, 1 }, { 0.076, -0.015, 1 } };
//	
//
//	final double qb_ir_4[][] = { { 0.044, -0.052, 1 }, { 0.051, -0.059, 1 },
//			{ 0.071, -0.036, 1 }, { 0.064, -0.029, 1 } };
//
//	final double qb_ir_5[][] = { { -0.126, -0.063, 1 },
//			{ -0.096, -0.063, 1 }, { -0.096, -0.053, 1 },
//			{ -0.126, -0.053, 1 } };
//	
	
	final double qb_bbb_usb[][] = { { -0.0824, -0.0418, 1 },
			{ -0.0694, -0.0418, 1 }, { -0.0694, -0.0278, 1 },
			{ -0.0824, -0.0278, 1 } };
   	

	
	
//	double ir_positions[][] = {{-0.0582,0.0584}, {0.05725, 0.03555},{0.0686, 0.0},{0.05725, -0.0355},{-0.0582, -0.0584}};
	final double ir_positions[][] = {{-0.11,0.063}, {0.062, 0.045},{0.076, 0.0},{0.062, -0.045},{-0.11, -0.063}};
	
	final double ir_thetas[] = {3*Math.PI/2, 2*Math.PI-Math.PI/4, 0, Math.PI/4, Math.PI/2 };
//	double ir_thetas[] = {Math.PI/2, Math.PI/4, 0, -Math.PI/4, -Math.PI/2 };


	@Override
	double[][] getIrPositions() {
		// TODO Auto-generated method stub
		return ir_positions;
	}



	@Override
	double[] getIrThetas() {
		// TODO Auto-generated method stub
		return ir_thetas;
	}




	double irDim[][] = 
		{
			{-0.005, -0.015, 1},
			{-0.005, 0.015, 1},
			{0.005, 0.015, 1},
			{0.005, -0.015, 1}
		};
	

}
