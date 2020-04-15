package com.zmc.zmcrobot.ui;

import android.view.MotionEvent;

public interface ClickListener {

	public void onClicked(float x, float y);
	public void onDoubleClicked(float x, float y);
	public void onLongClicked(float x, float y);
	
}
