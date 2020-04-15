package com.zmc.zmcrobot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class IndicatorBarView extends View {

	private Paint mPaint = new Paint();
	private Context mContext;

	int[] colors;
	float[] positions;
	private float M = getContext().getResources().getDisplayMetrics().densityDpi / 25.4F;
	private float H;

	private String displayStr = "30";

	private int indicatorValue = 30; // 0-99
	
	LinearGradient shader;
	
	public IndicatorBarView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);

		this.mContext = paramContext;
	}

	public IndicatorBarView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		float x = 1.15F; // 1.35

		int cwidth = canvas.getWidth();
		int cheight = canvas.getHeight();

		float fontSize;
		int text_Y;

		fontSize = cheight * 30 / 502;
		text_Y = cheight * 370 / 502;

		mPaint.setColor(Color.RED);

		float height = (cheight - 20 - 40) * indicatorValue / 100;
		
		if( shader == null )
			shader = new LinearGradient(0, 0, cwidth, cheight,Color.RED, Color.GREEN, Shader.TileMode.MIRROR);
		
		mPaint.setShader(shader);		
		
		canvas.drawRect(10, cheight-height-5, cwidth-20, cheight-5, mPaint);

		mPaint.setColor(Color.DKGRAY);
		mPaint.setTextSize(fontSize);
		float w = this.mPaint.measureText(displayStr);
		canvas.drawText(displayStr, (cwidth - w) / 2.0F, 10 + 30, mPaint); // this.height
																			// -
																			// this.meterBitmap.getHeight()
																			// /
																			// 3.5F

		mPaint.setColor(-1);
		mPaint.setTextSize(fontSize);

	}

	public void setIndicatorValue(int value) {
		if (value > 100 || value < 0)
			return;
		indicatorValue = value;
		displayStr = String.valueOf(indicatorValue);
		this.invalidate();

	}

}
