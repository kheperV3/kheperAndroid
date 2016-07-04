package khp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Float.parseFloat;
import khp.R;

import static java.lang.Integer.getInteger;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

/**
 * Created by ros on 21/05/2015.
 */
public class   KheperSettingsActivity extends Activity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_STATUS = "DEVICE_STATUS";
    private String mDeviceName;
    private String mDeviceAddress;
    private khp.BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothAdapter mBluetoothAdapter;
    static boolean initParse =false;
    private boolean mScanning;
    // Stops scanning after 3 seconds.
    private static final long SCAN_PERIOD = 6000;
    private Handler mHandler;


    List<BluetoothDevice> deviceList;
    TextView nameAff;
    TextView passwordAff;
//    ImageView okVoyAff;
    ImageButton upBut;
    ImageView iv;
    boolean connected = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((khp.BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (khp.BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (khp.BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;

//                Toast.makeText(KheperSettingsActivity.this, "Please see your BT connection settings3", Toast.LENGTH_LONG).show();


                invalidateOptionsMenu();

                clearUI();
            } else if (khp.BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mConnected = true;


                invalidateOptionsMenu();
            } else if (khp.BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                String data = intent.getStringExtra(khp.BluetoothLeService.EXTRA_DATA);
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemClock.sleep(1000);
        setContentView(R.layout.settings_layout);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        nameAff = (TextView) this.findViewById(R.id.name);
        passwordAff = (TextView) this.findViewById(R.id.password);
        upBut = (ImageButton) this.findViewById(R.id.uploadBut);
/*
        passwordAff.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    loginClick();
                    handled = true;
                }
                return handled;
            }
        });

        */
        iv = (ImageView) this.findViewById(R.id.swipeView1);

        Intent gattServiceIntent = new Intent(this, khp.BluetoothLeService.class);
        Log.d("KheperSettingsActivity", "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if(initParse == false) {
            Parse.initialize(this, "ebmAvXTYQBwP4eFJyoy4qKmFaPa5nleQuVZGRMaZ", "AguluO1MbzzbYIEmjhFstkt9PZCaQgOSMxyD99ot ");
            initParse = true;
        }
        iv.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                final Intent intent = new Intent(KheperSettingsActivity.this, DeviceScanActivity.class);
                setContentView(R.layout.settings_layout);
                startActivity(intent);
                finish();
            }

            public void onSwipeLeft() {
                final Intent intent = new Intent(KheperSettingsActivity.this, DeviceControlActivity.class);

                setContentView(R.layout.settings_layout);intent.putExtra(KheperSettingsActivity.EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(KheperSettingsActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                intent.putExtra(KheperSettingsActivity.EXTRAS_STATUS, "newConnection");

                startActivity(intent);
                finish();
            }


        });
        if (mDeviceAddress == null)  {
            // Use this check to determine whether BLE is supported on the device.  Then you can
            // selectively disable BLE-related features.
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                finish();
            }

            // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
            // BluetoothAdapter through BluetoothManager.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            // Checks if Bluetooth is supported on the device.
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            deviceList = new ArrayList<BluetoothDevice>();
            deviceList.clear();
            scanLeDevice(true);
            Toast.makeText(this, "Scanning...", Toast.LENGTH_LONG).show();
        }
    }

    private void clearUI() {
        //mDataField.setText(R.string.no_data);
    }

    /**
     * Detects left and right swipes across a view.
     */
    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context context) {
            gestureDetector = new GestureDetector(context, new GestureListener());
        }

        public void onSwipeLeft() {
        }

        public void onSwipeRight() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_DISTANCE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0)
                        onSwipeRight();
                    else
                        onSwipeLeft();
                    return true;
                }
                return false;
            }
        }
    }


    void scanLeDevice(final boolean enable)
    {
        if (enable) {
            // Stops scanning after a pre-defined scan period.

            mHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    mScanning= false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    if((deviceList.size() == 0)){
                        Toast.makeText(KheperSettingsActivity.this,"Please see your BT connection settings1",Toast.LENGTH_LONG).show();
                        return;
                    }

                    if((deviceList.size() > 1)||(deviceList.size()==0)){
                        final Intent intent2 = new Intent(KheperSettingsActivity.this, DeviceScanActivity.class);
                        setContentView(R.layout.listitem_device);
                        startActivity(intent2);
                        finish();
                        return;
                    }
                    BluetoothDevice dev = deviceList.get(0);
                    mDeviceName = dev.getName();
                    mDeviceAddress = dev.getAddress();
                    mBluetoothLeService.connect(mDeviceAddress);
                }

            },SCAN_PERIOD);


            mScanning = true;
            //F000E0FF-0451-4000-B000-000000000000
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);      }
    }
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean deviceFound = false;
                            for(BluetoothDevice listDev:deviceList){
                                if(listDev.getAddress().equals(device.getAddress())){
                                    deviceFound=true;
                                    break;
                                }
                            }
                            String prefix ;
                            if (device.getName() != null)  prefix = device.getName().substring(0,3); else prefix = "DUM ";
                            if(!deviceFound &&((prefix.equals("KHP"))||(prefix.equals("HMS")))){
                                deviceList.add(device);
                            }
                        }
                    });
                }
            };








    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(khp.BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(khp.BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(khp.BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }


    @Override

    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onResume() {
        super.onResume();


        }



    @Override
    protected void onPause() {
        super.onPause();

       if(mConnected) {
           mBluetoothLeService.disconnect();
           unregisterReceiver(mGattUpdateReceiver);
           unbindService(mServiceConnection);
       }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
        if(mConnected) {
            mBluetoothLeService.disconnect();
            this.unregisterReceiver(mGattUpdateReceiver);
            unbindService(mServiceConnection);
            if (mBluetoothLeService != null) {
                mBluetoothLeService.close();
                mBluetoothLeService = null;
            }

        }
        */
    }


    public void uploadClick(View view) {

        String label1, label2, label3;
        ParseUser user;


        ParseUser.logOut();
        upBut.setBackground(getResources().getDrawable(R.drawable.uploading));

        ParseUser.logInInBackground(nameAff.getText().toString(), passwordAff.getText().toString(),new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException pe) {
                if (user != null) {

                    connected = true;
//                 okVoyAff.setBackground(getResources().getDrawable(R.drawable.vert));
                    upBut.setBackground(getResources().getDrawable(R.drawable.uploading));
                } else {
                    // Signup failed. Look at the ParseException to see what happened.

                    connected = false;
                    switch (pe.getCode()) {
                        case ParseException.USERNAME_TAKEN:
                            Log.d("Testing", "Sorry, this username has already been taken.");
                            break;
                        case ParseException.USERNAME_MISSING:
                            Log.d("Testing", "Sorry, you must supply a username to register.");
                            Toast.makeText(KheperSettingsActivity.this, "Sorry, you must supply a username", Toast.LENGTH_LONG).show();
                            break;
                        case ParseException.PASSWORD_MISSING:
                            Log.d("Testing", "Sorry, you must supply a password to register.");
                            Toast.makeText(KheperSettingsActivity.this, "Sorry, you must supply a password ", Toast.LENGTH_LONG).show();
                            break;
                        case ParseException.OBJECT_NOT_FOUND:
                            Log.d("Testing", "Sorry, those credentials were invalid.");
                            break;
                        case ParseException.CONNECTION_FAILED:
                            Log.d("Testing", "Internet connection was not found. Please see your connection settings.");
                            Toast.makeText(KheperSettingsActivity.this, "Please, see your internet connection settings", Toast.LENGTH_LONG).show();
                            finish();
                            break;
                        default:
                            Log.d("Testing", pe.getMessage());
                            break;
                    }
                    upBut.setBackground(getResources().getDrawable(R.drawable.signin));
                    Toast.makeText(KheperSettingsActivity.this, "Wrong username or password, try again", Toast.LENGTH_LONG).show();
                    return;
                }


               SystemClock.sleep(2000);

                if (connected == false) {
                    Toast.makeText(KheperSettingsActivity.this, "Wrong username or password, try again", Toast.LENGTH_LONG).show();
                    upBut.setBackground(getResources().getDrawable(R.drawable.signin));
                    return;
                }
                if (mConnected == false) {
                    Toast.makeText(KheperSettingsActivity.this, "Please see your BT connection settings", Toast.LENGTH_LONG).show();
                    upBut.setBackground(getResources().getDrawable(R.drawable.signin));
                    return;
                }


                try {
                    user = ParseUser.getCurrentUser().fetch();
                } catch (ParseException pp) {
                    Toast.makeText(KheperSettingsActivity.this, "Wrong username or password, try again", Toast.LENGTH_LONG).show();

                    return;
                }


// init
                mBluetoothLeService.WriteValue("P9933333;");
               SystemClock.sleep(500);


                String st = user.getString("stator");
                int stator = parseInt(st) * 100;
//////////////////////////
// Courbe 1
/////////////////////////
                mBluetoothLeService.WriteValue("E" + user.getString("descrid1") + ";");
                Log.d("L1", user.getString("descrid1"));
                SystemClock.sleep(500);
 //               mBluetoothLeService.WriteValue("Q" + user.getString("revlimit1") + ";");
                mBluetoothLeService.WriteValue(String.format("Q%05d;", user.getInt("revlimit1")));
                Log.d("Limit1",String.format("%05d", user.getInt("revlimit1")));
 //               Log.d("R1", user.getString("revlimit1"));
                SystemClock.sleep(500);
                JSONArray Curve = user.getJSONArray("Curve1");

                for (int i = 0; i < Curve.length(); i++) {
                    try {
                        String s = Curve.getString(i);
                        Log.d("Curve1I",s);
                        int d = s.lastIndexOf(",") + 2;
                        int p = s.lastIndexOf(".");
                        int e = s.lastIndexOf("]") - 1;
                        String dec = s.substring(p + 1, e);
                        if (dec.length() == 1) dec = dec + "0";
                        int v = stator - (parseInt(s.substring(d, p)) * 100 + parseInt(dec));
                        if (v < 0) v = 0;
                        String com = String.format("P%02d%05d;", i, v);
                        mBluetoothLeService.WriteValue(com);
                        /*
                        try {
                            Thread.sleep(50);
                        }catch(InterruptedException xx){}
*/
                        SystemClock.sleep(50);
                        Log.d("Curve1", com);
                    } catch (JSONException pp) {

                    }
                }
                /*
                try {
                    Thread.sleep(300);
                }catch(InterruptedException xx){}
*/

                SystemClock.sleep(300);
                mBluetoothLeService.WriteValue("w1;");
                SystemClock.sleep(2500);
/*
                try {
                    Thread.sleep(1500);
                }catch(InterruptedException xx){}
                */
                Log.d("Write1","OK");
//////////////////////////
// Courbe 2
/////////////////////////
                mBluetoothLeService.WriteValue("E" + user.getString("descrid2") + ";");

                Log.d("L2", user.getString("descrid2"));
                SystemClock.sleep(500);
//                mBluetoothLeService.WriteValue("Q" + user.getString("revlimit2") + ";");
                mBluetoothLeService.WriteValue(String.format("Q%05d;",user.getInt("revlimit2")));
                Log.d("Limit2", String.format("%05d", user.getInt("revlimit2")));

                SystemClock.sleep(500);
                Curve = user.getJSONArray("Curve2");

                for (int i = 0; i < Curve.length(); i++) {
                    try {
                        String s = Curve.getString(i);
                        Log.d("Curve2I",s);
                        int d = s.lastIndexOf(",") + 2;
                        int p = s.lastIndexOf(".");
                        int e = s.lastIndexOf("]") - 1;
                        String dec = s.substring(p + 1, e);
                        if (dec.length() == 1) dec = dec + "0";
                        int v = stator - (parseInt(s.substring(d, p)) * 100 + parseInt(dec));
                        if (v < 0) v = 0;
                        String com = String.format("P%02d%05d;", i, v);
                        mBluetoothLeService.WriteValue(com);
                        /*
                        try {
                            Thread.sleep(50);
                        }catch(InterruptedException xx){}
                        */
                        SystemClock.sleep(50);
                        Log.d("Curve2", com);
                    } catch (JSONException pp) {

                    }
                }
/*
                try {
                    Thread.sleep(300);
                }catch(InterruptedException xx){}
*/
                SystemClock.sleep(300);
                mBluetoothLeService.WriteValue("w2;");
                SystemClock.sleep(1500);
                /*
                try {
                    Thread.sleep(1500);
                }catch(InterruptedException xx){}
                */
                Log.d("Write2","OK");

//////////////////////////
// Courbe 3
/////////////////////////
                mBluetoothLeService.WriteValue("E" + user.getString("descrid3") + ";");
                Log.d("L3", user.getString("descrid3"));
                SystemClock.sleep(500);
                mBluetoothLeService.WriteValue(String.format("Q%05d;", user.getInt("revlimit3")));
                Curve = user.getJSONArray("Curve3");
                Log.d("Limit3",String.format("%05d",user.getInt("revlimit3")));
                SystemClock.sleep(500);
                for (int i = 0; i < Curve.length(); i++) {
                    try {
                        String s = Curve.getString(i);
                        Log.d("Curve3I",s);
                        int d = s.lastIndexOf(",") + 2;
                        int p = s.lastIndexOf(".");
                        int e = s.lastIndexOf("]")-1;
                        String dec = s.substring(p + 1, e);
                        if (dec.length() == 1) dec = dec + "0";
                        int v = stator - (parseInt(s.substring(d, p)) * 100 + parseInt(dec));
                        if (v < 0) v = 0;
                        String com = String.format("P%02d%05d;", i, v);
                        mBluetoothLeService.WriteValue(com);
                        /*
                        try {
                            Thread.sleep(50);
                        }catch(InterruptedException xx){}
                        */
                        SystemClock.sleep(50);
                        Log.d("Curve3", com);
                    } catch (JSONException pp) {

                    }
                }
                /*
                try {
                    Thread.sleep(300);
                }catch(InterruptedException xx){}
*/

                SystemClock.sleep(300);
                mBluetoothLeService.WriteValue("w3;");

                /*
                try {
                    Thread.sleep(1500);
                }catch(InterruptedException xx){}
                */
                SystemClock.sleep(1500);
                Log.d("Write3","OK");



                upBut.setBackground(getResources().getDrawable(R.drawable.done));
                Toast.makeText(KheperSettingsActivity.this, "Curve #1 :  " + user.getString("descrid1") + " uploaded", Toast.LENGTH_LONG).show();
                Toast.makeText(KheperSettingsActivity.this, "Curve #2 :  " + user.getString("descrid2") + " uploaded", Toast.LENGTH_LONG).show();
                Toast.makeText(KheperSettingsActivity.this, "Curve #3 :  " + user.getString("descrid3") + " uploaded", Toast.LENGTH_LONG).show();
                upBut.setBackground(getResources().getDrawable(R.drawable.done));

            }
        });
        }
}