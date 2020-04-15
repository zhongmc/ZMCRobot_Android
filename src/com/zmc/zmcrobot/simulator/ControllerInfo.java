package com.zmc.zmcrobot.simulator;

public class ControllerInfo {

	public Vector uGotoGoal;
	public Vector uAoidObstacle;
	public Vector uFollowWall;
	
	public Vector uFwP;
	
	//follow wall 时挑选的wall向量， p0 -> p1
	public Vector p0,p1;
	
	//GotoGoal 为接近目标而设定的第二目标点，以便于调整方向；
	public double ux,uy;
	
	public void reset()
	{
		uGotoGoal = null;
		uAoidObstacle = null;
		uFollowWall = null;
		uFwP = null;
		
		p0 = null;
		ux=0;
		uy=0;
	}
}
