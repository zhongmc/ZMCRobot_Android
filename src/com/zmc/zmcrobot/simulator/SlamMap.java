package com.zmc.zmcrobot.simulator;

import android.util.Log;

public class SlamMap {
	
	private final static String TAG = "SlamMap";

	private double width, height; //
	private int iw,ih;
	private int lineLen;
	
	private double grid;
	
	private byte[] obstacleMatric;
	
	public SlamMap(double width, double height, double grid)
	{
		iw = (int)(width/grid);
		ih = (int)(height/grid);
		
		lineLen = iw/8 + 1;
			
		
		int length = lineLen * (ih+1);
		obstacleMatric = new byte[length + 10];
		int idx = lineLen * ih/2 + iw/16;
		
		obstacleMatric[ idx ] = (byte)0x0ff;
		idx = idx + lineLen *5 - 1;
		obstacleMatric[idx ] = (byte)0x0ff;
		this.width = width;
		this.height = height;
		this.grid = grid;
		setObstacle(1,1);
		setObstacle(-1, 1.5);
		setObstacle(-1.5, -1.5);
		setObstacle(1,-1.2);
	}
	public double getGrid()
	{
		return grid;
	}
	
	public void reset()
	{
		obstacleMatric = new byte[lineLen * (ih+1)+10];
	}
	
	public void setObstacle(double x, double y )
	{
		if( Math.abs(x) > width/2 || Math.abs(y) > height/2 )
		{
			Log.e(TAG, "Out of bound: " + x + ", " + y);
			return;
		}
		
		int w,h;
		w = (int)((width/2 + x )/grid);
		h = (int)((height/2 + y)/grid);
		
		int idx = h * lineLen + w/8;
		int off = w%8;
		byte mask = 1;
		mask <<= off;
		if( idx < obstacleMatric.length )
			obstacleMatric[idx]|= mask;
		
//		Log.e(TAG, String.format("Set Obs:%.3f,%.3f; %.3f,%.3f; %d,%d;",width, height, x, y, w, h));
	}
	
	

	
	public void clearObstacle(double x, double y)
	{
		if( Math.abs(x) > width/2 || Math.abs(y) > height/2 )
		{
			Log.e(TAG, "Out of bound: " + x + ", " + y);
			return;
		}
		
		int w,h;
		w = (int)((width/2 + x )/grid);
		h = (int)((height/2 + y)/grid);
		
		int idx = h * lineLen + w/8;
		int off = w%8;
		byte mask = 1;
		mask <<= off;
		mask =(byte)~mask;
		if( off == 7)
			mask = 0x7f;
		if( idx < obstacleMatric.length )
			obstacleMatric[idx]&= mask;
		
	}
	
	public boolean isObstacle(double x, double y )
	{
		if( obstacleMatric == null )
			return false;
		if( Math.abs(x) > width/2 || Math.abs(y) > height/2 )
		{
			Log.e(TAG, "get OBS Out of bound: " + x + ", " + y);
			return false;
		}
		
		int w,h;
		w = (int)((width/2 + x )/grid);
		h = (int)((height/2 + y)/grid);
		
		int idx = h * lineLen + w/8;
		
		int off = w%8;
		byte mask = 1;
		mask <<= off;
		
		return (obstacleMatric[idx] & mask) != 0;
		
	}
	
	public boolean isObstacle(int x, int y )
	{
		if( Math.abs( x ) > iw/2 || Math.abs(y) > ih/2 )
			return false;
		
		int idx = (ih/2 + y) * lineLen + (iw/2 + x)/8;
		
		int off = (iw/2 + x)%8;
		byte mask = 1;
		mask <<= off;
		try{
		return (obstacleMatric[idx] & mask) != 0;
		}catch(Exception e)
		{
			e.printStackTrace();
			Log.e(TAG, "failed to get obst:" + iw + "," + ih + ", " + x + ", " + y + "; idx:" + idx);
		}
		return false;
	}
	
}
