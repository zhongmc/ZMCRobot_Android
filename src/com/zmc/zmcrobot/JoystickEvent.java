package com.zmc.zmcrobot;

public class JoystickEvent {
	protected double angle =0, throttle = 0;
	
	protected JoystickView source;

	public JoystickEvent(JoystickView source)
	{
		this.source = source;
	}
	
	public JoystickEvent(double angle, double throttle)
	{
		this.angle = angle;
		this.throttle = throttle;
	}
}
