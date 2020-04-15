package com.zmc.zmcrobot;

//import android.support.v4.app.Fragment;

import com.zmc.zmcrobot.simulator.AbstractRobot;
import com.zmc.zmcrobot.simulator.ControllerInfo;
import com.zmc.zmcrobot.simulator.RearDriveRobot;
import com.zmc.zmcrobot.simulator.Supervisor;
import com.zmc.zmcrobot.simulator.TTRobot;
import com.zmc.zmcrobot.simulator.Vector;

import com.zmc.zmcrobot.ui.Obstacle;
import com.zmc.zmcrobot.ui.ObstacleCrossPoint;
import com.zmc.zmcrobot.ui.RobotSimulateView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

/**
 * goto goal ����ҳ��
 * 
 * @author zhong_zmwo6t7
 *
 */
public class SimulatorFragment extends Fragment implements Runnable { // ,
																		// BTMessageListener
																		// {

	private Supervisor supervisor = new Supervisor();
	// private ZMCRobot robot = new ZMCRobot();

	private AbstractRobot robot = new RearDriveRobot();

	private RobotSimulateView mRobotView;

	private int mMode = 0; // gotogoal mode
	// private Settings mSettings;

	private final static String TAG = "SimulatorFragment";
	private View mView;

	private CheckBox mIgnoreObstacleCheckBox;

	private Button mGoButton, mHomeButton; // , mStopButton;

	private EditText goalAngleEditText, velocityEditText;

	private ToggleButton mSimulateButton;
	// private Thread speedBrakeThread = null; // = new Thread(this );

	GestureDetector mGestureDector;

	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1f;
	private float mScale = 500;

	// private ClickDetector mClickDector;

	// private float x0, y0, theta0;

	// private float targetX, targetY;
	private boolean inScale = false;

	private boolean isGoing = false;
	private boolean isPause = false;

	private boolean setRoute = false;
	private float[][] mRoutes = new float[2000][2];
	private int routeSize = 0;

	private double home_x = 0, home_y = 0, home_theta = (float) Math.PI / 4;

	private long scaleEndTime = 0;

	private Thread timmerThread = null; // = new Thread(this );
	private boolean mStopTimer = false;

	private static int MSG_TIMMER = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		supervisor.setRobot(robot);
		supervisor.updateSettings(robot.getSettings());

		View rootView = inflater.inflate(R.layout.fragment_simulator, container, false);
		mRobotView = (RobotSimulateView) rootView.findViewById(R.id.robot_view);
		mRobotView.setRobotPosition(-1, -1, Math.PI / 4, 0);
		robot.setPosition(-1, -1, Math.PI / 4);

		ObstacleCrossPoint[] ocps = robot.getObstacleCrossPoints();
		mRobotView.setObstacleCrossPoints(ocps);

		try {

			Obstacle obstacle = new Obstacle(obstacle1);
			obstacle.setPosition(-0.8, 0, 0);
			robot.addObstacle(obstacle);
			mRobotView.addObstacle(obstacle);

			obstacle = new Obstacle(obstacle3);
			obstacle.setPosition(-0.5, 0, 0);
			robot.addObstacle(obstacle);
			mRobotView.addObstacle(obstacle);

			obstacle = new Obstacle(obstacle2);
			obstacle.setPosition(-1, -2, 0);
			robot.addObstacle(obstacle);
			mRobotView.addObstacle(obstacle);

			obstacle = new Obstacle(obstacle4); // ����һ��С�ϰ���
			obstacle.setPosition(-2, 1, 0);
			robot.addObstacle(obstacle);
			mRobotView.addObstacle(obstacle);

			obstacle = new Obstacle(border, Color.RED, false);
			obstacle.setPosition(0, 0, 0);
			robot.addObstacle(obstacle);
			mRobotView.addObstacle(obstacle);

		} catch (Exception e) {

		}
		mRobotView.setRoutes(mRoutes, routeSize);

		// mRobotView.setPosition(250, 300, -Math.PI/8, mScale);

		mGoButton = (Button) rootView.findViewById(R.id.buttonAction);
		mHomeButton = (Button) rootView.findViewById(R.id.uploadButton);

		goalAngleEditText = (EditText) rootView.findViewById(R.id.goalAngleEditText);
		velocityEditText = (EditText) rootView.findViewById(R.id.velocityEditText);

		velocityEditText.setText(String.valueOf(supervisor.getVelocity()));

		velocityEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

				try {
					double velocity = Double.valueOf(velocityEditText.getText().toString());
					if (velocity < 0.1 || velocity > 0.5) {
						velocityEditText.setError("Velocity should between 0.1 to 0.5!");
						return;

					} else {
						supervisor.setVelocity(velocity);
					}
				} catch (Exception e) {
					velocityEditText.setError("please input proper numeric Velocity between 0.2 to 0.8!");

					// velocityEditText.requestFocus();

				}
			}

		});

		mIgnoreObstacleCheckBox = (CheckBox) rootView.findViewById(R.id.IgnoreObstacleCheckBox);

		mIgnoreObstacleCheckBox.setEnabled(true);
		mIgnoreObstacleCheckBox.setChecked(false);
		mIgnoreObstacleCheckBox.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				supervisor.setIgnoreObstacle(mIgnoreObstacleCheckBox.isChecked());

				// SetIgnoreObstacleMode(mIgnoreObstacleCheckBox.isChecked());

			}

		});

		mSimulateButton = (ToggleButton) rootView.findViewById(R.id.toggleButtonSimulate);
		// mStopButton = (Button)rootView.findViewById(R.id.buttonStop);

		mGoButton.setEnabled(true);
		mHomeButton.setEnabled(true);
		mSimulateButton.setEnabled(false);

		mSimulateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mSimulateButton.isChecked();
				SetSimulateMode(mSimulateButton.isChecked());
			}
		});

		mGoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isGoing) {
					startRobot();
					// startGoToGoal();
					// mGoButton.setText(R.string.stop);
					// supervisor.reset();
					// isGoing = true;
				} else {
					stopRobot();
					// mGoButton.setText(R.string.go);
					// isGoing = false;
				}
			}
		});

		mHomeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetRobot();
			}
		});

		RadioGroup modeChoiceGroup = (RadioGroup) rootView.findViewById(R.id.ModeChoice);
		modeChoiceGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int idx = 0;
				if (checkedId == R.id.GotoGoalButton) {
					idx = 0;
				} else if (checkedId == R.id.AvoidObstacleButton) {
					idx = 1;
				} else if (checkedId == R.id.TraceRouteButton) {
					idx = 2;
					stopRobot();
				}

				isPause = false;
				mMode = idx;
				supervisor.setMode(idx);
				Log.i(TAG, "Change mode to: " + mMode);
				// setMode(idx);

			}

		});

		mGestureDector = new GestureDetector(this.getActivity().getApplicationContext(), new MyGestureListener());
		mScaleDetector = new ScaleGestureDetector(this.getActivity().getApplicationContext(), new ScaleListener());

		mView = rootView;
//		rootView.setOnTouchListener(viewOnTouchListener);
		mRobotView.setOnTouchListener(viewOnTouchListener);
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(TAG, "onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		timmerThread = new Thread(this);
		timmerThread.start();
		Log.i(TAG, "onCreate");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(TAG, "onActivityCreated");
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i(TAG, "onStop");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.i(TAG, "onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		mStopTimer = true;
		timmerThread.interrupt();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i(TAG, "onDetach");
	}

	long[] mHits = new long[2];

	private OnTouchListener viewOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent event) {

			mScaleDetector.onTouchEvent(event);

			if (event.getActionMasked() > 0xff)
				return true;
			if (inScale)
				return true;

			if (setRoute) {
				if (event.getAction() == MotionEvent.ACTION_UP) {

					Log.i(TAG, "action up");
					float x, y;
					x = event.getX();
					y = event.getY();
					Position p = mRobotView.getRealPosition(x, y);

					setRoute = false; // end set route

					updateViewScroll();

					mRoutes[routeSize][0] = p.x;
					mRoutes[routeSize][1] = p.y;
					routeSize++;

					mRobotView.setRouteSize(routeSize);

					curveRoute(mRoutes, routeSize);

					supervisor.setRoute(mRoutes, routeSize);
					return true;

				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {

					Log.i(TAG, "action move");

					if (routeSize > 1990)
						return true;
					float x, y;
					x = event.getX();
					y = event.getY();
					Position p = mRobotView.getRealPosition(x, y);

					mRoutes[routeSize][0] = p.x;
					mRoutes[routeSize][1] = p.y;
					routeSize++;
					mRobotView.setRouteSize(routeSize);

				} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					Log.i(TAG, "action cancel");
					setRoute = false;
				}

				return true;
			}

			return mGestureDector.onTouchEvent(event);

		};
	};

	private void startRobot() {
		mRobotView.setRecoverPoint(0, 0);
		if (isPause) {
			isPause = false;
			mGoButton.setText(R.string.stop);
			// supervisor.reset();
			isGoing = true;
			return;

		}

		if (this.mMode == 2) // trace route mode
		{
			if (routeSize < 5) {
				new AlertDialog.Builder(this.getActivity()).setTitle("��ʾ")
						.setMessage("Pless long press and touch move to set the route first!")
						.setPositiveButton("OK", null).show();
				return;
			}

			float x, y;
			x = mRoutes[0][0];
			y = mRoutes[0][1];

			double u_x = mRoutes[1][0] - mRoutes[0][0];
			double u_y = mRoutes[1][1] - mRoutes[0][1];
			// double d = Math.sqrt(Math.pow(u_x,2) + Math.pow(u_y, 2));
			double theta_g = Math.atan2(u_y, u_x);

			mRobotView.resetRobot();
			robot.setPosition(x, y, (float) theta_g);
			double irDistances[] = robot.getIRDistances();
			mRobotView.setIrDistances(irDistances);
			ObstacleCrossPoint[] ocps = robot.getObstacleCrossPoints();
			mRobotView.setObstacleCrossPoints(ocps);
			mRobotView.setRobotPosition(x, y, (float) theta_g, 0); // (float)

			supervisor.setRoute(mRoutes, routeSize);

			home_x = x;
			home_y = y;
			home_theta = theta_g;

		}

		mGoButton.setText(R.string.stop);
		// supervisor.reset();
		isGoing = true;

	}

	private void stopRobot() {

		isPause = true;

		mGoButton.setText(R.string.go);
		// supervisor.reset();
		isGoing = false;

	}

	// private void startGoToGoal() {
	// byte[] value = new byte[4];
	//
	// value[0] = 'G';
	// value[1] = 'O';
	//
	// // MainActivity act = (MainActivity)getActivity();
	// // act.SendBLECmd(value);
	//
	// }

	private void resetRobot() {

		mRobotView.resetRobot();
		robot.setPosition(home_x, home_y, (float) home_theta);
		double irDistances[] = robot.getIRDistances();
		mRobotView.setIrDistances(irDistances);

		mRobotView.setRobotPosition(home_x, home_y, (float) home_theta, 0);
		supervisor.reset();

		isPause = false;
		// byte[] value = new byte[4];
		// value[0] = 'R';
		// value[1] = 'S';
		// MainActivity act = (MainActivity)getActivity();
		// act.SendBLECmd(value);

	}

	private void SendGoToGoalCmd(float targetX, float targetY) {

		supervisor.setGoal(targetX, targetY, Math.PI);

		Log.i(TAG, "Go to Goal: " + targetX + ", " + targetY);
		byte[] value = new byte[10];

		value[0] = 'G';
		value[1] = 'G';

		int tmp = (int) (targetX * 100.0);

		intToByte(tmp, value, 2);

		tmp = (int) (targetY * 100.0);
		intToByte(tmp, value, 4);

		tmp = 0;
		value[6] = (byte) (tmp & 0xff);
		value[7] = (byte) (tmp >> 8);

		MainActivity act = (MainActivity) getActivity();
		act.SendBLECmd(value);
	};

	private void intToByte(int value, byte[] buf, int offset) {
		boolean sig = false;
		if (value < 0) {
			value = -value;
			sig = true;
		}
		buf[offset] = (byte) (value & 0xff);
		buf[offset + 1] = (byte) ((value >> 8) & 0x7f);

		if (sig)
			buf[offset + 1] = (byte) (buf[offset + 1] | 0x80);

	}

	private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();

			// Make sure we don't get too small or too big
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

			mScale = 500 * mScaleFactor;
			mRobotView.setScale(mScale);

			// Log.i(TAG, "in onScale, scale factor = " + mScaleFactor);
			// mView.invalidate();
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector arg0) {
			// TODO Auto-generated method stub
			Log.i(TAG, "Begin scale...");

			mRobotView.inScale = true;

			inScale = true;
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector arg0) {
			// TODO Auto-generated method stub
			scaleEndTime = SystemClock.uptimeMillis();

			mRobotView.inScale = false;

			inScale = false;

			updateViewScroll();

			Log.i(TAG, "scale end.");
		}
	}

	public void setRobotState(RobotState state) {
		this.mRobotView.setIrDistances(state.obstacles);
		mRobotView.setRobotPosition(state.x, state.y, state.theta, state.velocity);
	}

	public void SetSimulateMode(boolean sm) {
		Log.i(TAG, "Set simulate mode " + sm);
		byte[] value = new byte[4];

		value[0] = 'S';
		value[1] = 'M';

		if (sm)
			value[2] = 1;
		else
			value[2] = 0;

		// MainActivity act = (MainActivity)getActivity();
		// act.SendBLECmd(value);

	}

	public void updateBLEState(boolean bConnected) {
		if (bConnected) {
			mGoButton.setEnabled(true);
			mHomeButton.setEnabled(true);
			mSimulateButton.setEnabled(true);
		} else {
			mGoButton.setEnabled(false);
			mHomeButton.setEnabled(false);
			mSimulateButton.setEnabled(false);

		}
	}

	// final double obstacle1[][] = { { -0.5, 0, 1 }, { 0.5, 0, 1 },
	// { 0.5, -0.5, 1 }, { 0, -0.5, 1 } };
	double obstacle1[][] = { { 0, 0, 1 }, { 0.35, 0, 1 }, { 0.35, -0.30, 1 }, { 0, -0.30, 1 } };

	// ����
	final double obstacle2[][] = { { 0, 0, 1 }, { 0, 0.8, 1 }, { 0.4, 0.8, 1 }, { 0.4, 0.4, 1 }, { 1.8, 0.4, 1 },
			{ 1.8, 0, 1 }

	};

	// ����
	final double obstacle3[][] = { { 0, 1, 1 }, { 0, 2.2, 1 }, { 2.4, 2.2, 1 }, { 2.4, 0, 1 }, { 1.2, 0, 1 },
			{ 1.2, 0.4, 1 }, { 2, 0.4, 1 }, { 2, 1.8, 1 }, { 0.4, 1.8, 1 }, { 0.4, 1, 1 } };

	final double obstacle4[][] = { { 0, 0, 1 }, { 0.3, 0, 1 }, { 0.3, 0.4, 1 }, { 0, 0.4, 1 } };

	final double border[][] = { { -3, -3.5, 1 }, { -3, 3.5, 1 }, { 3, 3.5, 1 }, { 3, -3.5, 1 } };

	@Override
	public void run() {

		while (true) {
			if (mStopTimer) // stop
				break;

			try {

				Thread.sleep(100);
				mTimmerHandler.obtainMessage(MSG_TIMMER, -1, -1).sendToTarget();
			} catch (Exception e) {

			}
		}
	}

	private final Handler mTimmerHandler = new Handler() {
		// private int count = 0;

		@Override
		public void handleMessage(Message msg) {

			if (msg.what == MSG_TIMMER) {
				if (isGoing && !supervisor.atGoal()) {
					supervisor.execute(0, 0, 0.1);
					// count++;
					// if (count > 2) {
					RobotState state = robot.getState();
					double irDistances[] = robot.getIRDistances();
					mRobotView.setIrDistances(irDistances);
					ControllerInfo ctrlInfo = supervisor.getControllerInfo();
					mRobotView.setControllerInfo(ctrlInfo);

					mRobotView.setRobotPosition(state.x, state.y, state.theta, state.velocity);
					// count = 0;
					// }
				}
			}
		}
	};

	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
		public MyGestureListener() {
			super();
		}

		@Override
		public boolean onDoubleTap(MotionEvent event) {
			// Log.e(TAG, "onDoubleTap");
			float x, y;
			x = event.getX();
			y = event.getY();

			Position p = mRobotView.getRealPosition(x, y);
			home_x = p.x;
			home_y = p.y;

			home_theta = (float) Math.PI / 6;
			if (goalAngleEditText != null) {
				int in = Integer.valueOf(goalAngleEditText.getText().toString());

				if (in <= 180)
					home_theta = (in * Math.PI) / 180;
				else {
					in = in - 360;
					home_theta = (in * Math.PI) / 180;
				}
			}

			mRobotView.resetRobot();
			robot.setPosition(p.x, p.y, home_theta);
			double irDistances[] = robot.getIRDistances();
			mRobotView.setIrDistances(irDistances);
			ObstacleCrossPoint[] ocps = robot.getObstacleCrossPoints();
			mRobotView.setObstacleCrossPoints(ocps);
			mRobotView.setRobotPosition(p.x, p.y, home_theta, 0);

			Vector pp = supervisor.getRecoverPoint();
			mRobotView.setRecoverPoint(pp.x, pp.y);

			supervisor.reset();

			return true;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent event) {
			// Log.e(TAG, "onDoubleTapEvent");

			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			// Log.e(TAG, "onSingleTapConfirmed");

			float x, y;
			x = event.getX();
			y = event.getY();

			Position p = mRobotView.getRealPosition(x, y);

			mRobotView.setTarget(p.x, p.y);

			double angle = Math.PI / 2;
			if (goalAngleEditText != null) {
				int in = Integer.valueOf(goalAngleEditText.getText().toString());

				if (in <= 180)
					angle = (in * Math.PI) / 180;
				else {
					in = in - 360;
					angle = (in * Math.PI) / 180;
				}

			}

			Log.i(TAG, "Set target to:" + p.x + "," + p.y + ": " + angle);

			supervisor.setGoal(p.x, p.y, angle);

			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			Log.e(TAG, "onDown");
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// Log.e(TAG, "onShowPress");
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			// Log.e(TAG, "onSingleTapUp");

			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			Log.i(TAG, "onScroll, x=" + distanceX + ", y=" + distanceY);
			mRobotView.onScroll(distanceX, distanceY);

//			updateViewScroll();

			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			Log.i(TAG, "onLongPress");
			float x, y;
			x = e.getX();
			y = e.getY();
			Position p = mRobotView.getRealPosition(x, y);

			setRoute = true;

			MainActivity act = (MainActivity) getActivity();
			act.setViewScroll(false, false);

			routeSize = 1;
			mRoutes[0][0] = p.x;
			mRoutes[0][1] = p.y;

			mRobotView.setRoutes(mRoutes, routeSize);

		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			// Log.e(TAG, "onFling");
			return true;
		}
	}

	private void updateViewScroll() {

		MainActivity act = (MainActivity) getActivity();
		boolean scrollLeft = !this.mRobotView.canScrollLeft();
		boolean scrollRight = !this.mRobotView.canScrollRight();
		act.setViewScroll(scrollLeft, scrollRight);
	}

	public void updateSettings(Settings settings) {
		supervisor.updateSettings(settings);
	}

	public Settings getSettings() {
		return supervisor.getSettings();
	}

	private void curveRoute(float[][] mRoutes, int routeSize) {
		// y= ax+c;

		float[][] routes = new float[2000][2];

		double a, b, c;
		int idx = 0;
		double x1, y1, x2, y2, sqAB, x0, y0, d = 0;

		int k = 0;

		Log.i(TAG, "cs:" + routeSize);

		routes[k][0] = mRoutes[idx][0];
		routes[k][1] = mRoutes[idx][1];
		k = 1;

		while (idx < routeSize - 2) {
			x1 = mRoutes[idx][0];
			y1 = mRoutes[idx][1];
			x2 = mRoutes[idx + 1][0];
			y2 = mRoutes[idx + 1][1];

			if (x2 - x1 != 0) {
				a = (y2 - y2) / (x2 - x1);
				b = 1.0;
				c = y1 - a * x1;
				sqAB = Math.sqrt(a * a + 1);
			} else {
				a = 1;
				b = (x1 - x2) / (y1 - y2);
				c = x1 - b * y1;
				sqAB = Math.sqrt(b * b + 1);
			}

			Log.i(TAG, idx + ": " + x1 + ", " + y1 + "; " + x2 + ", " + y2 + "; a=" + a + " c=" + c + "; d=" + d);

			idx = idx + 2;
			while (idx < routeSize) {
				x0 = mRoutes[idx][0];
				y0 = mRoutes[idx][1];
				d = (a * x0 + b * y0 - c) / sqAB;
				if (Math.abs(d) > 0.08) {
					routes[k][0] = mRoutes[idx - 1][0];
					routes[k][1] = mRoutes[idx - 1][1];
					k++;
					routes[k][0] = mRoutes[idx][0];
					routes[k][1] = mRoutes[idx][1];
					k++;
					break;
				}
				idx++;
			}

			idx--;
		}

		routes[k][0] = mRoutes[idx + 1][0];
		routes[k][1] = mRoutes[idx + 1][1];
		k++;

		mRoutes = routes;
		routeSize = k;
		// Log.i(TAG, idx + ": " + mRoutes[idx][0] + ", " + mRoutes[idx][1]);

	}

}
