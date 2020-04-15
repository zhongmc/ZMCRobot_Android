package com.zmc.zmcrobot;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoScrollViewPager extends ViewPager {

	public boolean isScrollLeft() {
		return scrollLeft;
	}

	public void setScrollLeft(boolean scrollLeft) {
		this.scrollLeft = scrollLeft;
	}

	public boolean isScrollRight() {
		return scrollRight;
	}

	public void setScrollRight(boolean scrollRight) {
		this.scrollRight = scrollRight;
	}

	private boolean scrollLeft = true;
	private boolean scrollRight = true;
	
	private boolean noScroll = true;

	
	public NoScrollViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public NoScrollViewPager(Context context, AttributeSet attrs) {

		super(context, attrs);

		// TODO Auto-generated constructor stub

	}	
	
	  @Override
	    public boolean onTouchEvent(MotionEvent arg0) {
	        if (noScroll){
	            return false;
	        }else{
	            return super.onTouchEvent(arg0);
	        }
	    }

	    @Override
	    public boolean onInterceptTouchEvent(MotionEvent arg0) {
	        if (noScroll){
	            return false;
	        }else{
	            return super.onInterceptTouchEvent(arg0);
	        }
	    }	
	
	    
/*	
    @Override    
    public boolean onInterceptTouchEvent(MotionEvent ev) {  
    	
    	boolean ret = super.onInterceptTouchEvent( ev );
    	if( scrollLeft && scrollRight )
			return ret;
    	
    	int size = ev.getHistorySize();
    	if( size <= 0)
    		return false;
    	
    	float xH = ev.getHistoricalX( 0 ); 
    	float x = ev.getX();
    	if( scrollLeft && x> xH )
    		return ret;
    	
    	if( scrollRight && x < xH )
    		return ret;
    	
    	return false;
    }     
    
    @Override    
    public boolean onTouchEvent(MotionEvent ev) {        

    	boolean ret = super.onTouchEvent( ev );

    	if( scrollLeft && scrollRight )
			return ret;
    	
    	int size = ev.getHistorySize();
    	if( size <= 0)
    		return false;
    	
    	float xH = ev.getHistoricalX( 0 ); 
    	float x = ev.getX();
    	if( scrollLeft && x> xH )
    		return ret;
    	
    	if( scrollRight && x < xH )
    		return ret;
    	
    	return false;
    }
	*/

}
