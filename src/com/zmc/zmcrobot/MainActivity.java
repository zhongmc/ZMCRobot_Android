package com.zmc.zmcrobot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import com.zmc.zmcrobot.simulator.SlamMap;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements ActionBar.TabListener {
	private final static String TAG = "MainActivity";  //MainActivity.class.getSimpleName();

	public static Settings mSettings = new Settings();

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	//ViewPager mViewPager;

	
	private SlamMap slamMap = new SlamMap(10,12, 0.05);
	
	private NoScrollViewPager mViewPager;
	private TextView mSubTitle, mBatteryLabel;
	private ImageView robotTypeView;

	// device state indicator, also used as batter volume display
	private ImageView mDeviceStateView;

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_SETTINGS = 3;

	public static final int MESSAGE_READ = 0;

	// private boolean mServiceBind = false;
	private boolean mConnected = false;

	// private BluetoothGattCharacteristic mNotifyCharacteristic;
	// private BluetoothLeService mBluetoothLeService;

	private BluetoothGattCharacteristic mRobotStateCharact;
	private BluetoothGattCharacteristic mRobotSettingsCharact;
	private BluetoothGattCharacteristic mRobotCMDCharact;

	//the hm-10 ble block 
	private BluetoothGattCharacteristic mBleHM10Charact;
	
	
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice = null;
	private BluetoothGatt mBluetoothGatt = null;

	private String mDeviceName;
	private String mDeviceAddress;

	//小车操控页，手动驾驶、GTG
	private SlamFragment mSlamFragment;
	//goto goal 仿真页
	private SimulatorFragment mSimulateFragment;
	//平衡车页
	private BalanceFragment mBalanceFragment;

	//小车手动驾驶页
//	private DriverFragment mDriverFragment;
//	private GearDriverFragment mGearDriveFragment;
	// private MeterFragment mMeterFragment;
//	private GotoGoalFragment mGotoGoalFragment;

//	//阶跃响应测试页
//	private StepResponseFragment mStepResponseFragment;
	
	private SettingsFragment mSettingsFragment;

	private BLECmdFragment mBLECmdFragment;
	
	private int mCurrentPosition = 0;

	private int voltageVolum = 4; // 0 emp，1 low，2 hight，3 full 4 off
	private double mVoltage = 0;
	private String mVoltageLabelStr = null;
	
	
	private Drawable[] mStateDrawables = new Drawable[5];

	private RobotState mRobotState = new RobotState();

	public int robotType = 0; // 0 3wheel robot 1 2 wheel balance robot

	ProgressDialog dialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.i(TAG, "On create...");
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		LayoutInflater mInflater = LayoutInflater.from(this);

		View mCustomView = mInflater.inflate(R.layout.custom_title, null);
		TextView mTitleTextView = (TextView) mCustomView
				.findViewById(R.id.title_text);
		mTitleTextView.setText("ZMC Robot");

		mSubTitle = (TextView) mCustomView.findViewById(R.id.subtitle_text);

		mSubTitle.setText(R.string.not_connected);
		
		mBatteryLabel = (TextView)mCustomView.findViewById(R.id.battery_text);
		mBatteryLabel.setText("0.0");

		robotTypeView = (ImageView) mCustomView
				.findViewById(R.id.deviceImageView);
		mDeviceStateView = (ImageView) mCustomView
				.findViewById(R.id.deviceStateView);

		mStateDrawables[0] = getResources().getDrawable(R.drawable.bat_empty);
		mStateDrawables[1] = getResources().getDrawable(R.drawable.bat_low);
		mStateDrawables[2] = getResources().getDrawable(R.drawable.bat_hight);
		mStateDrawables[3] = getResources().getDrawable(R.drawable.bat_full);

		mStateDrawables[4] = getResources().getDrawable(R.drawable.device_off);

		robotTypeView.setVisibility(View.INVISIBLE);

		robotTypeView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					robotTypeView.setImageDrawable(getResources().getDrawable(
							R.drawable.carandbalance));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {

					if (robotType == 0) {
						robotTypeView.setImageDrawable(getResources()
								.getDrawable(R.drawable.balance));
						robotType = 1;
					} else {
						robotTypeView.setImageDrawable(getResources()
								.getDrawable(R.drawable.car));
						robotType = 0;
					}

				}

				return true;
			}

		});

		// mStateDrawables[0] = getResources().getDrawable(R.drawable.bat_full);

		actionBar.setCustomView(mCustomView);
		actionBar.setDisplayShowCustomEnabled(true);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (NoScrollViewPager) findViewById(R.id.pager);
		
		mViewPager.setScrollLeft( false );
		mViewPager.setScrollRight( false );
		
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
//		mViewPager
//				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//					@Override
//					public void onPageSelected(int position) {
//						actionBar.setSelectedNavigationItem(position);
//						
//						if( position == 2 || position == 1 || position == 4)
//						{
//							mViewPager.setScrollLeft(false);
//							mViewPager.setScrollRight( false );
//
//						}
//						else
//						{
//							mViewPager.setScrollLeft(true);
//							mViewPager.setScrollRight( true );
//						
//						}
//					}
//				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.i(TAG, "On Resume...");

		// connect(mDeviceAddress);
	}

	@Override
	protected void onPause() {
		super.onPause();

		Log.i(TAG, "On Pause...");

		// disconnect();
		// close();

		// unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.i(TAG, "On Destroy...");

		disconnect();
		close();
	}

	/* connect to the device with specified address */
	public boolean connect(final String deviceAddress) {
		if (mBluetoothAdapter == null || deviceAddress == null)
			return false;
		mDeviceAddress = deviceAddress;
		Log.d(TAG, "Connect to " + deviceAddress + " ......");

		// check if we need to connect from scratch or just reconnect to
		// previous device
		if (mBluetoothGatt != null
				&& mBluetoothGatt.getDevice().getAddress()
						.equals(deviceAddress)) {
			// just reconnect
			Log.d(TAG, "re Connect!");
			return mBluetoothGatt.connect();
		} else {
			// connect from scratch
			// get BluetoothDevice object for specified address
			mBluetoothDevice = mBluetoothAdapter
					.getRemoteDevice(mDeviceAddress);

			Log.d(TAG, "create Connecting ....");

			if (mBluetoothDevice == null) {
				// we got wrong address - that device is not available!
				Log.d(TAG, "Device not found....");
				return false;
			}
			// connect with remote device
			mBluetoothGatt = mBluetoothDevice.connectGatt(this, true,
					mBleCallback);
		}
		return true;
	}

	/*
	 * disconnect the device. It is still possible to reconnect to it later with
	 * this Gatt client
	 */
	public void disconnect() {
		if (mBluetoothGatt != null)
			mBluetoothGatt.disconnect();
	}

	/* close GATT client completely */
	public void close() {
		if (mBluetoothGatt != null)
			mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.d(TAG, "onActivityResult " + resultCode);

		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {

				mDeviceName = data.getStringExtra(EXTRAS_DEVICE_NAME);
				mDeviceAddress = data.getStringExtra(EXTRAS_DEVICE_ADDRESS);

				Log.i(TAG, "BT Device:" + mDeviceName + ", " + mDeviceAddress);

				// Sets up UI references.
				// ((TextView)
				// findViewById(R.id.device_address)).setText(mDeviceAddress);
				// mGattServicesList = (ExpandableListView)
				// findViewById(R.id.gatt_services_list);
				// mGattServicesList.setOnChildClickListener(servicesListClickListner);
				// mConnectionState = (TextView)
				// findViewById(R.id.connection_state);
				// mDataField = (TextView) findViewById(R.id.data_value);

				this.mSubTitle.setText(mDeviceName);

				connect(mDeviceAddress);

				// getActionBar().setTitle(mDeviceName);
				// getActionBar().setDisplayHomeAsUpEnabled(true);

				// Intent gattServiceIntent = new Intent(this,
				// BluetoothLeService.class);
				// bindService(gattServiceIntent, mServiceConnection,
				// BIND_AUTO_CREATE);
				// mServiceBind = true;
				// Log.i(TAG, "Bind the service..." );

			}
			break;

		case REQUEST_SETTINGS:
			if (resultCode == Activity.RESULT_OK) {
				SendSettings();
			}

			break;

		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				// initialize();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		if (mDeviceName != null && !mDeviceName.isEmpty()) {
			if (!mConnected) {
				menu.findItem(R.id.action_connect).setVisible(true);
				menu.findItem(R.id.action_close).setVisible(false);
				menu.findItem(R.id.action_cmd).setVisible( false );
			} else {
				menu.findItem(R.id.action_connect).setVisible(false);
				menu.findItem(R.id.action_close).setVisible(true);
				menu.findItem(R.id.action_cmd).setVisible( true );
			}

		} else {
			menu.findItem(R.id.action_connect).setVisible(false);
			menu.findItem(R.id.action_close).setVisible(false);
			menu.findItem(R.id.action_cmd).setVisible( false );

		}

		if(!mConnected || mViewPager.getCurrentItem() != 0 )
		{
			menu.findItem(R.id.action_slam_start_map ).setVisible(false);
			menu.findItem(R.id.action_slam_stop_map ).setVisible(false);
			menu.findItem(R.id.action_slam_save_map ).setVisible(false);
			menu.findItem(R.id.action_slam_load_map ).setVisible(true);
			return true;
		}
		
		if( mViewPager.getCurrentItem() == 0) // slam fragment
		{
			if( mSlamFragment.mBeSlamMapping )
			{
				menu.findItem(R.id.action_slam_start_map ).setVisible(false);
				menu.findItem(R.id.action_slam_stop_map ).setVisible(true);
				menu.findItem(R.id.action_slam_save_map ).setVisible(true);
				menu.findItem(R.id.action_slam_load_map ).setVisible(true);
				
			}
			else
			{
				menu.findItem(R.id.action_slam_start_map ).setVisible(true);
				menu.findItem(R.id.action_slam_stop_map ).setVisible(false);
				menu.findItem(R.id.action_slam_save_map ).setVisible(false);
				menu.findItem(R.id.action_slam_load_map ).setVisible(true);
				
			}
		}
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {

			startSettingsFragment();
			// startGetSettingsDialog();

			//
			// CustomProgressDialog dialog =new CustomProgressDialog(this,
			// "Getting Settings...", R.anim.frame);
			// dialog.show();
			//
			// byte[] value = new byte[3];
			// value[0]='R';
			// value[1]='P';
			// this.SendBLECmd(value);
			// if( mBluetoothGatt != null && mRobotSettingsCharact != null)
			// {
			// boolean ret =
			// this.mBluetoothGatt.readCharacteristic(mRobotSettingsCharact);
			// Log.i(TAG, "Read settings charact ret:"+ ret);
			// }
			// //waiting for data reading...
			//
			// final Intent intent = new Intent(this, SettingsActivity.class);
			// startActivityForResult(intent, REQUEST_SETTINGS);
			return true;
		} else if (id == R.id.action_cmd) {
			//startBLECmdDialog();
			startBLECmdFragment();
			return true;
		}

		else if (id == R.id.action_device) {

			final Intent intent = new Intent(this, DeviceScanActivity.class);
			startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
			return true;
		} else if (id == R.id.action_connect) {
			connect(this.mDeviceAddress);
			// if( mBluetoothLeService != null )
			// mBluetoothLeService.connect(mDeviceAddress);
		} else if (id == R.id.action_close) {
			this.disconnect();
			// close();
			// if( mBluetoothLeService != null )
			// mBluetoothLeService.disconnect();

		}

		// menu.findItem(R.id.action_slam_start_map ).setVisible(false);
		// menu.findItem(R.id.action_slam_stop_map ).setVisible(true);
		// menu.findItem(R.id.action_slam_save_map ).setVisible(true);
		// menu.findItem(R.id.action_slam_load_map ).setVisible(true);

		else if (id == R.id.action_slam_start_map) {

			mSlamFragment.startSlamMap();

		}

		else if (id == R.id.action_slam_stop_map) {

			mSlamFragment.stopSlamMap();

		}

		else if (id == R.id.action_slam_save_map) {

			mSlamFragment.saveSlamMap();
			// mSlamFragment.mBeSlamMapping = false;

		} else if (id == R.id.action_slam_load_map) {

			mSlamFragment.loadSlamMap();

		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		mCurrentPosition = tab.getPosition();

		updateConnectionState(R.string.connected);
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		protected Fragment mCurrentFragment;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			if (position == 0) {
				Log.i(TAG, "Create the Slam fragment!");
				mSlamFragment = new SlamFragment();
				mSlamFragment.setBeConnected(mConnected);
				mSlamFragment.setSlamMap( slamMap );
				return mSlamFragment;
			} else if (position == 1) {
				Log.i(TAG, "Create the simulate fragment!");
				mSimulateFragment = new SimulatorFragment();
				return mSimulateFragment;
			} else if (position == 2) {
				Log.i(TAG, "Create the balance fragment!");
				mBalanceFragment = new BalanceFragment();
				mBalanceFragment.setBeConnected(mConnected);
				return mBalanceFragment;
			}

			return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			if (object instanceof Fragment)
				mCurrentFragment = (Fragment) object;
			super.setPrimaryItem(container, position, object);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_Slam).toUpperCase(l);
			case 1:
				return getString(R.string.title_simulator).toUpperCase(l);
			case 2:
				return getString(R.string.title_balance_turning).toUpperCase(l);
			}
			return null;
		}
	}

	public void setRobotFragment(GotoGoalFragment fragment) {
//		if (mGotoGoalFragment != fragment) {
//			Log.i(TAG, "Diff robot fragment!");
//			mGotoGoalFragment = fragment;
//			// mRobotFragment.updateBLEState(mConnected);
//		} else {
//			Log.i(TAG, "The same robot fragment!");
//
//		}
	}
//
//	public void setDriveFragment(GravityDriveFragment fragment) {
//		// if(mGravityDriveFragment != fragment )
//		// {
//		// Log.i(TAG, "Diff drive fragment!");
//		// mGravityDriveFragment = fragment;
//		// // mGravityDriveFragment.updateBLEState(mConnected);
//		// }
//		// else
//		// {
//		// Log.i(TAG, "The same drive fragment!");
//		//
//		// }
//
//	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			TextView textView = (TextView) rootView
					.findViewById(R.id.section_label);
			textView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

	// private static IntentFilter makeGattUpdateIntentFilter() {
	// final IntentFilter intentFilter = new IntentFilter();
	// intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
	// intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
	// intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
	// intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
	// return intentFilter;
	// }

	private void updateConnectionState(final int resourceId) {

		cmdToBeSend.clear();
		settingsToBeSend.clear();

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				Log.i(TAG, "Update connection state " + mConnected);
				if (mConnected) {
					mDeviceStateView
							.setImageDrawable(mStateDrawables[voltageVolum]);
					// mDeviceStateView.setImageDrawable(getResources().getDrawable(R.drawable.bat_full));
					robotTypeView.setVisibility(View.VISIBLE);
				} else {
					voltageVolum = 4;
					mDeviceStateView.setImageDrawable(getResources()
							.getDrawable(R.drawable.device_off));
					robotTypeView.setVisibility(View.INVISIBLE);
				}
				if( mSlamFragment != null )
					mSlamFragment.updateBLEState(mConnected);
					
				if (mBalanceFragment != null)
					mBalanceFragment.updateBLEState(mConnected);
				
				if( mSettingsFragment != null )
					mSettingsFragment.updateBLEState(mConnected);

			}
		});
	}

	private void settingsDataReaded(byte[] data) {
	//	Log.i(TAG, "Settings value from ble...");
		
		final Settings settings = new Settings();
		settings.decodeWithSettingsType(data);
		Log.i(TAG, "Settings value from ble..." + settings.settingsType);
		Log.i(TAG, "ble settings:" + settings.toString());

		
//		if( settings.settingsType == 1 )
//		{
//			mSettings.settingsType = 1;
//			mSettings.copyFrom( settings );
//			Log.i(TAG, "main settings:" + mSettings.toString());
//		}
//
//		// mSettings.decode( data );
//		if (settings.settingsType == 5 && dialog != null) {
//			dialog.dismiss();
//			dialog = null;
//
//			mSettings.settingsType = 5; //copy from according to main settings type!!!!!
//			mSettings.copyFrom( settings );
//			Log.i(TAG, "main settings:" + mSettings.toString());
//
//			startSettingsActivity();
//		}
		
		if( mSettingsFragment != null  )
		{
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					mSettingsFragment.updateSettings(settings);
				}
			});			
			
		}
		
		if (mBalanceFragment != null ) //&& (settings.settingsType == 2 || settings.settingsType == 3 || settings.settingsType == 4|| settings.settingsType == 6)) {
		{
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
							mBalanceFragment.updateSettings(settings);
					}
				});
		}
	}
	
	
	private void messageDataReaded(byte[] data ){
		
		final String msg;
		int len = data.length;
		byte[] buf = new byte[len];
		System.arraycopy(data, 1, buf, 0, len-1);
		
		msg = new String(buf);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if( mSlamFragment != null )
					mSlamFragment.addMessage(msg);
				
				if( mBLECmdFragment != null )
				{
					mBLECmdFragment.addMessages(msg );
				}
			}
		});
		
	}

	
	DecimalFormat batFmt = new DecimalFormat("#0.0");
	
	private void updateRobotState(byte[] data) {

		mRobotState.decode(data);

		if( Math.abs(mRobotState.voltage - mVoltage) > 0.1)
		{
			mVoltage = mRobotState.voltage;
			mVoltageLabelStr  = batFmt.format(mRobotState.voltage);
			
		}
		else
			mVoltageLabelStr = null;
		// Log.i(TAG, mRobotState.obstacles[1] + "," + mRobotState.voltage );

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				int batC = 2;
				if (mRobotState.voltage > 9)
					batC = 3;

				int vv;
				if (mRobotState.voltage < (batC * 3.74)) // 20%
				{
					vv = 0;
				} else if (mRobotState.voltage < (batC * 3.82)) // 50%
				{
					vv = 1;
				} else if (mRobotState.voltage < (batC * 3.98)) // 80%
				{
					vv = 2;
				} else {
					vv = 3;
				}
				
				if( mVoltageLabelStr != null &&  mBatteryLabel != null )
					mBatteryLabel.setText(batFmt.format(mRobotState.voltage) );
				
				if (voltageVolum != vv) {
					Log.i(TAG, "voltage:" + mRobotState.voltage);
					voltageVolum = vv;
					mDeviceStateView.setImageDrawable(mStateDrawables[vv]);
				}

				// if( mCurrentPosition == 0 && mMeterFragment != null )
				// mMeterFragment.setRobotState(mRobotState);
				{
					if (mCurrentPosition == 0 && mSlamFragment != null)
						mSlamFragment.setRobotState(mRobotState);

					if (mCurrentPosition == 2 && mBalanceFragment != null)
						mBalanceFragment.setRobotState(mRobotState);
				}
			}
		});

	}

	/* callbacks called for any action on particular Ble Device */
	private final BluetoothGattCallback mBleCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				mConnected = true;
				updateConnectionState(R.string.connected);
				invalidateOptionsMenu();

				Log.i(TAG, "BLE connected!");
				
				try{
//				Thread.currentThread();
					Thread.sleep(500);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
				// in our case we would also like automatically to call for
				// services discovery
				startServicesDiscovery();

				// mUiCallback.uiDeviceConnected(mBluetoothGatt,
				// mBluetoothDevice);
				// // now we can start talking with the device, e.g.
				// mBluetoothGatt.readRemoteRssi();
				// // response will be delivered to callback object!
				// // and we also want to get RSSI value to be updated
				// periodically
				// startMonitoringRssiValue();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				mConnected = false;
				updateConnectionState(R.string.disconnected);
				invalidateOptionsMenu();
				Log.i(TAG, "BLE disconnected!");
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// now, when services discovery is finished, we can call
				// getServices() for Gatt
				getSupportedServices();
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// we got response regarding our request to fetch characteristic
			// value
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// and it success, so we can get the value
				getCharacteristicValue(characteristic);
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			super.onDescriptorWrite(gatt, descriptor, status);
			Log.i(TAG, "descriptor writed! " + descriptor.getCharacteristic().getUuid());
			if( descriptor.getCharacteristic() == mRobotStateCharact ) //mRobotSettingsCharact ) //为了串行执行两个character的enable notify，需要等待第一个完成写入
			{
				enableCharacteristicNotify(mRobotSettingsCharact);
			}
//			else if(descriptor.getCharacteristic() == mBleHM10Charact  )
//			{
//				enableCharacteristicNotify(mBleHM10Charact);
//			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			// Log.i(TAG, "Characteristic changed: " +
			// characteristic.getUuid());
			// characteristic's value was updated due to enabled notification,
			// lets get this value
			// the value itself will be reported to the UI inside
			// getCharacteristicValue
			getCharacteristicValue(characteristic);
			// also, notify UI that notification are enabled for particular
			// characteristic
			// mUiCallback.uiGotNotification(mBluetoothGatt, mBluetoothDevice,
			// mBluetoothSelectedService, characteristic);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {

			// String serviceName =
			// BleNamesResolver.resolveServiceName(characteristic.getService().getUuid().toString().toLowerCase(Locale.getDefault()));
			// String charName =
			// BleNamesResolver.resolveCharacteristicName(characteristic.getUuid().toString().toLowerCase(Locale.getDefault()));
			// String description = "Device: " + deviceName + " Service: " +
			// serviceName + " Characteristic: " + charName;

			Log.i(TAG, "Characteristic writed: " + characteristic.getUuid()
					+ " status: " + status);
				
			if( characteristic == mBleHM10Charact )
			{
				if (!cmdToBeSend.isEmpty()) {
					DoSendBLECmd(cmdToBeSend.poll());
				}
				else if( !settingsToBeSend.isEmpty() )
				{
					
					DoSendSettings(settingsToBeSend.poll());
				}
				if (settingsToBeSend.isEmpty() && cmdToBeSend.isEmpty() ) {
					bleCmdIdle = true;
					bleSettingsIdle = true;
				}
			}
			
			else if (characteristic == mRobotCMDCharact) {
				if (cmdToBeSend.isEmpty()) {
					bleCmdIdle = true;
				} else {
					DoSendBLECmd(cmdToBeSend.poll());
				}
			} else if (characteristic == mRobotSettingsCharact) {
				if (settingsToBeSend.isEmpty()) {
					bleSettingsIdle = true;
				} else {
					DoSendSettings(settingsToBeSend.poll());
				}
			}

			// we got response regarding our request to write new value to the
			// characteristic
			// let see if it failed or not
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// mUiCallback.uiSuccessfulWrite(mBluetoothGatt,
				// mBluetoothDevice, mBluetoothSelectedService, characteristic,
				// description);
			} else {
				// mUiCallback.uiFailedWrite(mBluetoothGatt, mBluetoothDevice,
				// mBluetoothSelectedService, characteristic, description +
				// " STATUS = " + status);
			}
		};

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// we got new value of RSSI of the connection, pass it to the UI
				// mUiCallback.uiNewRssiAvailable(mBluetoothGatt,
				// mBluetoothDevice, rssi);
			}
		};
	};

	private ArrayBlockingQueue<byte[]> cmdToBeSend = new ArrayBlockingQueue<byte[]>( 20);

	private ArrayBlockingQueue<Settings> settingsToBeSend = new ArrayBlockingQueue<Settings>(10);

	private boolean bleCmdIdle = true, bleSettingsIdle = true;

	public void SendBLECmd(byte[] data) {
		if (!mConnected)
			return;
		if (cmdToBeSend.isEmpty() && bleCmdIdle) {
			DoSendBLECmd(data);
		} else {
			try {
				cmdToBeSend.add(data);
			} catch (Exception e) {
				Log.i(TAG, "Failed to add to cmd queue: " + e);
			}
		}
	}

	public void SendSettings() {
		if (mSimulateFragment != null)
			mSimulateFragment.updateSettings(mSettings);

		Settings settings = new Settings();
		settings.settingsType = 1;
		settings.copyFrom(mSettings);

		Log.i(TAG, "mSettings:kp=" + mSettings.kp + ", ki=" + mSettings.ki
				+ ", kd=" + mSettings.kd);
		Log.i(TAG, "copy settings:kp=" + settings.kp + ", ki=" + settings.ki
				+ ", kd=" + settings.kd);

		SendSettings(settings);

		settings = new Settings();
		settings.settingsType = 4;
		settings.copyFrom(mSettings);
		SendSettings(settings);

	}

	public void SendSettings(Settings settings) {
		if (!mConnected)
			return;
		if (settingsToBeSend.isEmpty() && bleSettingsIdle) {
			DoSendSettings(settings);
		} else
			settingsToBeSend.add(settings);
	}

	public void DoSendSettings(Settings settings) {
		if (!mConnected)
			return;
		bleSettingsIdle = false;

		byte[] data = settings.encodeWithSettingsType();

		if (mBluetoothGatt != null && this.mRobotSettingsCharact != null) {
			mRobotSettingsCharact.setValue(data);
			boolean ret = this.mBluetoothGatt
					.writeCharacteristic(mRobotSettingsCharact);
			Log.i(TAG, "Send settings: ret: " + ret);
		}
		else if( mBluetoothGatt != null && mBleHM10Charact != null )
		{

			byte[] dataBuf = new byte[data.length + 1];
			dataBuf[0] = (byte)( data.length-1);
			
			System.arraycopy(data, 0, dataBuf, 1, data.length);
			mBleHM10Charact.setValue(dataBuf);
			this.mBluetoothGatt.writeCharacteristic(mBleHM10Charact);
			
		}
			
	}

	/*
	 * request to discover all services available on the remote devices results
	 * are delivered through callback object
	 */
	public void startServicesDiscovery() {
		if (mBluetoothGatt != null)
			mBluetoothGatt.discoverServices();
	}

	/*
	 * gets services and calls UI callback to handle them before calling
	 * getServices() make sure service discovery is finished!
	 */
	public void getSupportedServices() {

		List<BluetoothGattService> gattServices = mBluetoothGatt.getServices(); // mBluetoothLeService.getSupportedGattServices();

		for (BluetoothGattService gattService : gattServices) {
			String uuid = gattService.getUuid().toString();

			Log.i(TAG, "BLE SVC:" + uuid);
			if (uuid.equals("00003a37-0000-1000-8000-00805f9b34fb")) // the zmc
																		// robot
																		// service
			{
				// we found the service
				Log.i(TAG,
						"found The robot state ble service:"
								+ gattService.toString());

				// the robot state characteristic
				mRobotStateCharact = gattService.getCharacteristic(UUID
						.fromString("00003a3a-0000-1000-8000-00805f9b34fb"));

				enableCharacteristicNotify(mRobotStateCharact);

				mRobotSettingsCharact = gattService.getCharacteristic(UUID
						.fromString("00003a38-0000-1000-8000-00805f9b34fb"));

				mRobotCMDCharact = gattService.getCharacteristic(UUID
						.fromString("00003a39-0000-1000-8000-00805f9b34fb"));

			}
			//the ble hm-10 service
			if( uuid.equals("0000ffe0-0000-1000-8000-00805f9b34fb"))
			{
				// we found the service
				Log.i(TAG, "found The hm-10 ble service:" + gattService.toString());
				mBleHM10Charact = gattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
				if( mBleHM10Charact == null )
				{
					Log.i(TAG, "HM-10 ble charact oxffe1 not found!");
				}
				else
					enableCharacteristicNotify(mBleHM10Charact);
				
			}
		}

	}

	/*
	 * get characteristic's value (and parse it for some types of
	 * characteristics) before calling this You should always update the value
	 * by calling requestCharacteristicValue()
	 */
	
	private byte bleDataBuff[] = new byte[100];
	private int bleBuffOff = 0;
	
	synchronized public void getCharacteristicValue(BluetoothGattCharacteristic ch) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null || ch == null)
			return;

		byte[] rawValue = ch.getValue();
		byte[] dataBuf = new byte[20];
		
		if( mBleHM10Charact != null && ch == mBleHM10Charact ) // .getUuid().equals(mBleHM10Charact.getUuid()))
		{
			int len = rawValue.length;
			if( len + bleBuffOff >= 100 )
			{
				Log.i(TAG, "Error, ble buffer out of range! " + len + ", " + rawValue[0]);
				bleBuffOff = 0;
				return;
			}
			System.arraycopy(rawValue, 0, bleDataBuff, bleBuffOff, len);
			bleBuffOff = bleBuffOff + len;
			while(true) //process package
			{
				len = bleDataBuff[0];
				if( len+1 > bleBuffOff )
					break;
				
				byte pkgType = bleDataBuff[1];
				System.arraycopy(bleDataBuff, 1, dataBuf, 0, len);
				if( bleBuffOff > len+1)
					System.arraycopy(bleDataBuff, len+1, bleDataBuff, 0, bleBuffOff-len-1);
				
				bleBuffOff = bleBuffOff - len - 1;

				if( pkgType == 8 || pkgType == 9) //balance state
				{	
					updateRobotState( dataBuf );
				}
				else if( pkgType == 10 ) //msg package
				{
					messageDataReaded( dataBuf );
				}
				else
				{
					settingsDataReaded( dataBuf );
				}
				if( bleBuffOff < 2)
					break;
				
			}
			
			return;
		}
		
//		 Log.i(TAG, "Ble Data Avalable:" + ch.getUuid() + "; data length:" +
//		 rawValue.length );

		if (ch == mRobotStateCharact) //.getUuid().equals(mRobotStateCharact.getUuid())) {
			updateRobotState(rawValue);
			// mBluetoothGatt.readCharacteristic( ch );
		 else if (ch ==mRobotSettingsCharact) // .getUuid().equals(mRobotSettingsCharact.getUuid())) {
			settingsDataReaded(rawValue);

	}

	private void enableCharacteristicNotify(
			BluetoothGattCharacteristic characteristic) {

		if (characteristic == null)
			return;

		Log.i(TAG, "Enable notify for ch: " + characteristic.getUuid());

		final int charaProp = characteristic.getProperties();
		if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
			Log.i(TAG, "Enable the notify...");

			mBluetoothGatt.setCharacteristicNotification(characteristic, true);

			BluetoothGattDescriptor config = characteristic.getDescriptor(UUID
					.fromString("00002902-0000-1000-8000-00805f9b34fb"));
			if (config != null) {
				int wt = characteristic.getWriteType();
				characteristic.setWriteType(2);
				Log.i(TAG, "Enable the remote notify..." + wt);
				boolean ret = config
						.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				Log.i(TAG, "set desc ret:" + ret);
				ret = mBluetoothGatt.writeDescriptor(config); // 远程开启
				Log.i(TAG, "write desc ret:" + ret);
				// ret =
				// config.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
				// Log.i(TAG, "set desc ret:" + ret);
				// ret=mBluetoothGatt.writeDescriptor(config); //远程开启
				// Log.i(TAG, "write desc ret:" + ret);
				characteristic.setWriteType(wt);
			}

		}

	}

	public void SetRobotType(int robotType) {
		Log.i(TAG, "Set robot type " + robotType);
		byte[] value = new byte[4];

		value[0] = 'R';
		value[1] = 'T';
		value[2] = (byte) robotType;
		SendBLECmd(value);

	}

	public void DoSendBLECmd(byte[] value) {

		Log.i(TAG, "Send cmd...");
		if (!mConnected)
			return;

		bleCmdIdle = false;
		if (mRobotCMDCharact != null) {
			mRobotCMDCharact.setValue(value);

			boolean ret = this.mBluetoothGatt
					.writeCharacteristic(mRobotCMDCharact);

			Log.i(TAG, "Send cmd. " + ret);
		}
		else if(mBleHM10Charact != null )
		{
			byte[] dataBuf = new byte[value.length + 2];
			dataBuf[0] = (byte) value.length;
			dataBuf[1] = 0; //cmd
			System.arraycopy(value, 0, dataBuf, 2, value.length);
			mBleHM10Charact.setValue(dataBuf);
			this.mBluetoothGatt.writeCharacteristic(mBleHM10Charact);
			
		}
	}

	public boolean isBLEConnected() {
		return mConnected;
	}

	
	private void startBLECmdDialog() {
		
		
	    final EditText editText = new EditText(MainActivity.this);
	    AlertDialog.Builder inputDialog = 
	        new AlertDialog.Builder(MainActivity.this);
	    inputDialog.setTitle("BLE Command").setView(editText);
	    
	    inputDialog.setNeutralButton("Send", 
	            new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	
	            	String cmd = editText.getText().toString();
	            	if(cmd.length() <2 )
	            	{
	    	            Toast.makeText(MainActivity.this,
	    	    	            "Please input proper command!", 
	    	    	            Toast.LENGTH_SHORT).show();	            		
	            		
	            	}
	            	else
	            	{
	            		SendBLECmd(cmd.getBytes());
	            		Log.i(TAG, "Send BleCMD: " + cmd);
	            	}
	            }
	        });

	    
	    inputDialog.setPositiveButton("确定", 
	        new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
//	            Toast.makeText(MainActivity.this,
//	            editText.getText().toString(), 
//	            Toast.LENGTH_SHORT).show();
	        }
	    });
	    
	    
	    
	    inputDialog.show();
		
		
	}
	
	
	private void startSettingsFragment()
	{

		String tag = SettingsFragment.class.getName();
		mSettingsFragment = SettingsFragment.getInstance(getApplicationContext(), getFragmentManager());

	    if( this.mSimulateFragment != null )
	    {
	    	Settings settings = mSimulateFragment.getSettings();
	    	settings.settingsType = 0;
	    	mSettingsFragment.updateSettings(settings);
	    }
	    mSettingsFragment.updateBLEState( mConnected );
		mSettingsFragment.show(getFragmentManager(), tag);
	}
	
	
	private void startBLECmdFragment()
	{
		
		String tag = BLECmdFragment.class.getName();
		mBLECmdFragment = BLECmdFragment.getInstance(getApplicationContext(), getFragmentManager());
		mBLECmdFragment.show(getFragmentManager(), tag);
		
//	    FragmentTransaction ft = getFragmentManager().beginTransaction();
//	    Fragment prev = getFragmentManager().findFragmentByTag("BLE Cmd Dlg");
//	    if (prev != null) {
//	        ft.remove(prev);
//	    }
//	    ft.addToBackStack(null);
//
//	    // Create and show the dialog.
//	    BLECmdFragment newFragment = new BLECmdFragment();
//	    mBLECmdFragment = newFragment;
//	    newFragment.show(ft, "BLE Cmd Dlg");
		
	}
	
	
	private void startGetSettingsDialog() {

		if (dialog != null)
			return;

		byte[] value = new byte[3];
		value[0] = 'R';
		value[1] = 'P';
		value[2] = 1;
		this.SendBLECmd(value);

		value = new byte[3];
		value[0] = 'R';
		value[1] = 'P';
		value[2] = 4;
		this.SendBLECmd(value);

		if( mSimulateFragment != null )
		{
			Settings settings = mSimulateFragment.getSettings();
			mSettings.settingsType = 4;
			mSettings.copyFrom(settings );
			mSettings.settingsType = 0;
			mSettings.copyFrom(settings );
		}
		
		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
		dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
		dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
		dialog.setIcon(R.drawable.ic_launcher);//
		// 设置提示的title的图标，默认是没有的，如果没有设置title的话只设置Icon是不会显示图标的
		dialog.setTitle("提示");
		// dismiss监听
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				// dialog = null;
			}
		});

		// 监听cancel事件
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				
				Settings settings = mSimulateFragment.getSettings();
				mSettings.settingsType = 4;
				mSettings.copyFrom(settings );
				mSettings.settingsType = 0;
				mSettings.copyFrom(settings );
				
				startSettingsActivity();
				dialog = null;
			}
		});
		// //设置可点击的按钮，最多有三个(默认情况下)
		// dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定",
		// new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// // TODO Auto-generated method stub
		//
		// }
		// });
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// dialog = null;
						startSettingsActivity();
					}
				});

		dialog.setMessage("Loading settings...");
		dialog.show();

	}

	public void startSettingsActivity() {
		if (dialog != null)
			dialog.dismiss();

		dialog = null;
		final Intent intent = new Intent(this, SettingsActivity.class);
		startActivityForResult(intent, REQUEST_SETTINGS);

	}

	public void setViewScroll(boolean scrollLeft, boolean scrollRight )
	{
//		mViewPager.setScrollLeft(scrollLeft);
//		mViewPager.setScrollRight( scrollRight );

		
	}
	
	
}
