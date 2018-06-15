package com.huskar.scarcontrol;

import android.Manifest;
import android.content.pm.PackageManager;
import android.renderscript.Byte3;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = false;

    public static final int REC_DATA = 2;
    public static final int CONNECTED_DEVICE_NAME = 4;
    public static final int BT_TOAST = 5;
    public static final int MAIN_TOAST = 6;

    // 标志字符串常量
    public static final String DEVICE_NAME = "device name";
    public static final String TOAST = "toast";

    // 意图请求码
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;


    private TextView RecDataView;
    private Button ClearWindow, pauseButton, sendButton;
    private Button upButton, downButton, leftButton, rightButton, speedupButton, rotateButton, danceButton;
    private Button Button01, Button02, Button03, Button04, Button05;
    private StringBuffer Str2Send1 = new StringBuffer("1"),
            Str2Send2 = new StringBuffer("2"),
            Str2Send3 = new StringBuffer("3"),
            Str2Send4 = new StringBuffer("4"),
            Str2Send5 = new StringBuffer("5");
    private RadioGroup rgRec, rgSend;
    private EditText sendContent, period;
    private CheckBox setPeriod;
    // 已连接设备的名字
    private String mConnectedDeviceName = null;
    //蓝牙连接服务对象
    private BluetoothAdapter mBluetoothAdapter = null;

    private BluetoothService mConnectService = null;
    static boolean isHEXsend = false, isHEXrec = false;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void saveConfig() {
        String filename = Environment.getExternalStorageDirectory().getPath() + "/scarcontrol.config";
        File f = new File(filename);
        FileOutputStream fOut;
        try {
            f.createNewFile();
            fOut = new FileOutputStream(f);
            fOut.write(Button01.getText().toString().getBytes());
            fOut.write('\0');
            fOut.write(Str2Send1.toString().getBytes());
            fOut.write('\0');
            fOut.write(Button02.getText().toString().getBytes());
            fOut.write('\0');
            fOut.write(Str2Send2.toString().getBytes());
            fOut.write('\0');
            fOut.write(Button03.getText().toString().getBytes());
            fOut.write('\0');
            fOut.write(Str2Send3.toString().getBytes());
            fOut.write('\0');
            fOut.write(Button04.getText().toString().getBytes());
            fOut.write('\0');
            fOut.write(Str2Send4.toString().getBytes());
            fOut.write('\0');
            fOut.write(Button05.getText().toString().getBytes());
            fOut.write('\0');
            fOut.write(Str2Send5.toString().getBytes());
            fOut.write('\0');
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restoreConfig() {
        String filename = Environment.getExternalStorageDirectory().getPath() + "/scarcontrol.config";
        File f = new File(filename);
        FileInputStream fIn;
        try {
            fIn = new FileInputStream(f);
            byte[] bs = new byte[1024];
            fIn.read(bs);
            String s = new String(bs);
            String[] ss = s.split("\0");
            Button01.setText(ss[0]);
            Str2Send1.append(ss[1]);
            Button02.setText(ss[2]);
            Str2Send2.append(ss[3]);
            Button03.setText(ss[4]);
            Str2Send3.append(ss[5]);
            Button04.setText(ss[6]);
            Str2Send4.append(ss[7]);
            Button05.setText(ss[8]);
            Str2Send5.append(ss[9]);
            fIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClickButton(View view) {
        switch (view.getId()) {
            case R.id.lightupbutton:
                sendMessage("d");
                break;
            case R.id.lightdownbutton:
                sendMessage("x");
                break;
        }
    }

    private void sendMessage(String Str2Send) {
        Log.d("huskar",Str2Send);
        if (mConnectService == null || mConnectService.getState() != BluetoothService.CONNECTED) {
            // Toast.makeText(this, "未连接到任何蓝牙设备", Toast.LENGTH_SHORT).show();
            mHandler.sendEmptyMessage(MAIN_TOAST);
            return;
        } else if (Str2Send == null || mConnectService == null || Str2Send.equals(""))
        {
            return;
        } else {
            byte[] bs = Str2Send.getBytes();
            mConnectService.write(bs);
        }
    }
//    timeTask = new timeThread(1000);
//    //启动定时任务
//                timeTask.start();
//} else timeTask.interrupt();
    private class touchListener implements OnTouchListener{
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                if(timeTask!=null){
                    timeTask.interrupt();
                    timeTask = null;
                }
                return false;
            };
            String str = "";
            int time = 0;
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if(timeTask!=null){
                    timeTask.interrupt();
                    timeTask = null;
                }
                switch (view.getId()) {
                    case R.id.upbutton://f
                        time = 800;
                        str = "f";
                        break;
                    case R.id.leftbutton://l
                        time = 800;
                        str = "l";
                        break;
                    case R.id.rightbutton://r
                        time = 800;
                        str = "r";
                        break;
                    case R.id.downbutton://d
                        time = 800;
                        str = "b";
                        break;
                    case R.id.speedupbutton://u 2
                        time = 1500;
                        str = "u";
                        break;
                    case R.id.rotatebutton://s
                        time = 3000;
                        str = "s";
                        break;
                    case R.id.dancebutton://t
                        time = 3000;
                        str = "t";
                        break;
                }
                timeTask = new timeThread(time,str);
                timeTask.start();
            }
            return false;
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scar_main);
        touchListener t = new touchListener();
        upButton = (Button) findViewById(R.id.upbutton);
        upButton.setOnTouchListener(t);
        leftButton = (Button) findViewById(R.id.leftbutton);
        leftButton.setOnTouchListener(t);
        rightButton = (Button) findViewById(R.id.rightbutton);
        rightButton.setOnTouchListener(t);
        downButton = (Button) findViewById(R.id.downbutton);
        downButton.setOnTouchListener(t);
        speedupButton = (Button) findViewById(R.id.speedupbutton);
        speedupButton.setOnTouchListener(t);
        rotateButton = (Button) findViewById(R.id.rotatebutton);
        rotateButton.setOnTouchListener(t);
        danceButton = (Button) findViewById(R.id.dancebutton);
        danceButton.setOnTouchListener(t);
        // setContentView(R.layout.activity_main);
//        Button01 = (Button) findViewById(R.id.Button01);
//        Button02 = (Button) findViewById(R.id.Button02);
//        Button03 = (Button) findViewById(R.id.Button03);
//        Button04 = (Button) findViewById(R.id.Button04);
//        Button05 = (Button) findViewById(R.id.Button05);
//        RecDataView = (TextView) findViewById(R.id.Rec_Text_show);
//        ClearWindow = (Button) findViewById(R.id.ClearWindow);
//        pauseButton = (Button) findViewById(R.id.pauseButton);
//        sendContent = (EditText) findViewById(R.id.sendContent);
//        period = (EditText) findViewById(R.id.period);
//        sendButton = (Button) findViewById(R.id.sendButton);
//        setPeriod = (CheckBox) findViewById(R.id.setPeriod);
//        rgRec = (RadioGroup) findViewById(R.id.rgRec);
//        rgSend = (RadioGroup) findViewById(R.id.rgSend);
//        setupListener();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }//savaData();
//        this.setTitle("蓝牙遥控(未连接)");
//        init_hex_string_table();
//        verifyStoragePermissions(this);
//        restoreConfig();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) Log.i(TAG, "++ ON START ++");
        // 查看请求打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } //否则创建蓝牙连接服务对象
        else if (mConnectService == null) {
            mConnectService = new BluetoothService(mHandler);
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (mConnectService != null) {
            if (mConnectService.getState() == BluetoothService.IDLE) {
                //监听其他蓝牙主设备
                mConnectService.acceptWait();
            }
        }
    }

    /**
     * 自定义按键长按监听方法，进入定义按键的对话框
     */
    private OnLongClickListener ButtonLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.Button01:
                    new defineButtonDialog(MainActivity.this, Button01, Str2Send1).show();
                    break;
                case R.id.Button02:
                    new defineButtonDialog(MainActivity.this, Button02, Str2Send2).show();
                    break;
                case R.id.Button03:
                    new defineButtonDialog(MainActivity.this, Button03, Str2Send3).show();
                    break;
                case R.id.Button04:
                    new defineButtonDialog(MainActivity.this, Button04, Str2Send4).show();
                    break;
                case R.id.Button05:
                    new defineButtonDialog(MainActivity.this, Button05, Str2Send5).show();
                    break;
            }
            return false;
        }

    };
    /**
     * 所有按键的监听方法，
     * 分别根据按键ID处理其相应的事件
     */
    private OnClickListener ButtonClickListener = new OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Button01:
                    sendMessage(Button01, Str2Send1.toString());
                    break;
                case R.id.Button02:
                    sendMessage(Button02, Str2Send2.toString());
                    break;
                case R.id.Button03:
                    sendMessage(Button03, Str2Send3.toString());
                    break;
                case R.id.Button04:
                    sendMessage(Button04, Str2Send4.toString());
                    break;
                case R.id.Button05:
                    sendMessage(Button05, Str2Send5.toString());
                    break;
                case R.id.ClearWindow:
                    RecDataView.setText("");
                    break;
                case R.id.sendButton:
                    sendMessage(sendButton, sendContent.getText().toString());
                    break;
                case R.id.pauseButton:
                    if (BluetoothService.allowRec) pauseButton.setText("继续");
                    else pauseButton.setText("暂停");
                    BluetoothService.allowRec = !BluetoothService.allowRec;
                    break;
            }
        }
    };
    /**
     * 定时选择框组件监听方法
     * 开启相应的时间任务
     */
    private OnCheckedChangeListener checkBoxListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//            if (isChecked) {
//                String s = period.getText().toString();
//                //新建时间任务
//                if (s.length() != 0) {
//                    timeTask = new timeThread(Integer.valueOf(s));
//                } else timeTask = new timeThread(1000);
//                //启动定时任务
//                timeTask.start();
//            } else timeTask.interrupt();
        }
    };
    /**
     * RadioGroup的监听方法
     * 根据RadioButton的ID处理相应事件
     */
    private RadioGroup.OnCheckedChangeListener rgListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.receiveASCII:
                    isHEXrec = false;
                    break;
                case R.id.receiveHEX:
                    isHEXrec = true;
                    break;
                case R.id.sendASCII:
                    isHEXsend = false;
                    break;
                case R.id.sendHEX:
                    isHEXsend = true;
                    break;
            }
        }
    };

    /**
     * 设置自定按键及其他固定按键的监听方法
     */
    private void setupListener() {
        Button01.setOnClickListener(ButtonClickListener);
        Button02.setOnClickListener(ButtonClickListener);
        Button03.setOnClickListener(ButtonClickListener);
        Button04.setOnClickListener(ButtonClickListener);
        Button05.setOnClickListener(ButtonClickListener);
        Button01.setOnLongClickListener(ButtonLongClickListener);
        Button02.setOnLongClickListener(ButtonLongClickListener);
        Button03.setOnLongClickListener(ButtonLongClickListener);
        Button04.setOnLongClickListener(ButtonLongClickListener);
        Button05.setOnLongClickListener(ButtonLongClickListener);
        ClearWindow.setOnClickListener(ButtonClickListener);
        pauseButton.setOnClickListener(ButtonClickListener);
        sendButton.setOnClickListener(ButtonClickListener);
        setPeriod.setOnCheckedChangeListener(checkBoxListener);
        rgRec.setOnCheckedChangeListener(rgListener);
        rgSend.setOnCheckedChangeListener(rgListener);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (DEBUG) Log.i(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG) Log.i(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.e(TAG, "onDestroy");
        // Stop the Bluetooth connection
        if (mConnectService != null) mConnectService.cancelAllBtThread();
        if (timeTask != null) timeTask.interrupt();
        // saveConfig();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 按键触发发送字符串
     *
     * @param Str2Send 欲发送的字符串.
     */
    private void sendMessage(Button callButton, String Str2Send) {
        if (callButton != null) {
            if (Str2Send.length() == 0) {
                if (callButton != sendButton) {
                    Toast.makeText(this, "请先长按配置按键", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            if (mConnectService == null || mConnectService.getState() != BluetoothService.CONNECTED) {
                Toast.makeText(this, "未连接到任何蓝牙设备", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (Str2Send == null || mConnectService == null || Str2Send.equals("")) return;
        byte[] bs;
        if (!isHEXsend) {
            bs = Str2Send.getBytes();
            mConnectService.write(bs);
        } else {
            for (char c : Str2Send.toCharArray()) {
                if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || c == ' ')) {
                    Toast.makeText(this, "发送内容含非法字符", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            String[] ss = Str2Send.split(" ");
            bs = new byte[1];
            for (String s : ss) {
                if (s.length() != 0) {
                    bs[0] = (byte) (int) Integer.valueOf(s, 16);
                    mConnectService.write(bs);
                }
            }
        }
    }

    timeThread timeTask = null;

    private class timeThread extends Thread {
        private int sleeptime;
        private String sendStr;
        timeThread(int militime,String sendstr) {
            super();
            sendStr = sendstr;
            sleeptime = militime;
        }
		/*byte[] buffer={'a','v','c','d','f','a','v','c','d','f','a','v','c',
				'a','v','c','d','f','a','v','c','d','f','a','v','c',
				'a','v','c','d','f','a','v','c','d','f','a','v','c',
				'a','v','c','d','f','a','v','c','d','f','a','v','c'
				,'d','f','a','v','c','d','f','a','v','c','d','f','\n'};*/

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (DEBUG) Log.i("huskar", "timeThread start");
                // sendMessage(null, sendContent.getText().toString());
                sendMessage(sendStr);
                //mHandler.obtainMessage(MainActivity.REC_DATA,buffer.length,-1,buffer).sendToTarget();
                try {
                    Thread.sleep(sleeptime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            if (DEBUG) Log.i("huskar", "timeThread end");
        }
    }

    String[] hex_string_table = new String[256];

    private void init_hex_string_table() {
        for (int i = 0; i < 256; i++) {
            if (i < 16) {
                hex_string_table[i] = " 0" + Integer.toHexString(i).toUpperCase();
            } else {
                hex_string_table[i] = " " + Integer.toHexString(i).toUpperCase();
            }
        }
    }

    private int align_num = 0;//对齐字节数
    // 用于从线程获取信息的Handler对象
    private final Handler mHandler = new Handler() {
        StringBuffer sb = new StringBuffer();
        byte[] bs;
        float sWidth;
        int b, i, lineWidth = 0, align_i = 0;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REC_DATA:
//                    sb.setLength(0);
//                    if (isHEXrec) {
//                        bs = (byte[]) msg.obj;
//                        for (i = 0; i < msg.arg1; i++) {
//                            b = (bs[i] & 0xff);
//                            sb.append(hex_string_table[b]);
//                            sWidth = RecDataView.getPaint().measureText(hex_string_table[b]);
//                            lineWidth += sWidth;
//                            if (lineWidth > RecDataView.getWidth() || (align_num != 0 && align_num == align_i)) {
//                                lineWidth = (int) sWidth;
//                                align_i = 0;
//                                sb.insert(sb.length() - 3, '\n');
//                            }
//                            align_i++;
//                        }
//                    } else {
//                        bs = (byte[]) msg.obj;
//                        char[] c = new char[msg.arg1];
//                        for (i = 0; i < msg.arg1; i++) {
//                            c[i] = (char) (bs[i] & 0xff);
//                            sWidth = RecDataView.getPaint().measureText(c, i, 1);
//                            lineWidth += sWidth;
//                            if (lineWidth > RecDataView.getWidth()) {
//                                lineWidth = (int) sWidth;
//                                sb.append('\n');
//                            }
//                            if (c[i] == '\n') lineWidth = 0;
//                            sb.append(c[i]);
//                        }
//                    }
//                    RecDataView.append(sb);
                    break;
                case CONNECTED_DEVICE_NAME:
                    // 提示已连接设备名
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "已连接到"
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    MainActivity.this.setTitle("蓝牙遥控(已连接)");
                    break;
                case BT_TOAST:
//                    if (mConnectedDeviceName != null)
//                        Toast.makeText(getApplicationContext(), "与" + mConnectedDeviceName +
//                                msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
//                    else Toast.makeText(getApplicationContext(), "与" + target_device_name +
//                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    MainActivity.this.setTitle("蓝牙遥控(未连接)");
                    mConnectedDeviceName = null;
                    break;
                case MAIN_TOAST:
                    Toast.makeText(getApplicationContext(), "未连接到任何蓝牙设备", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private String target_device_name = null;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult");
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    // 提取蓝牙地址数据
                    String address = data.getExtras().getString(DeviceListActivity.DEVICE_ADDRESS);
                    // 获取设备
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    target_device_name = device.getName();
                    if (target_device_name.equals(mConnectedDeviceName)) {
                        Toast.makeText(this, "已连接" + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 提示正在连接设备
                    Toast.makeText(this, "正在连接" + target_device_name, Toast.LENGTH_SHORT).show();
                    // 连接设备
                    mConnectService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // 请求打开蓝牙被用户拒绝时提示
                if (resultCode == Activity.RESULT_OK) {
                    mConnectService = new BluetoothService(mHandler);
                } else {
                    Toast.makeText(this, "拒绝打开蓝牙", Toast.LENGTH_SHORT).show();
                    //finish();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Connect:
                // 查看请求打开蓝牙
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    return true;
                }
                // 打开设备蓝牙设备列表活动
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discoverable:
                // 请求打开本地蓝牙可见性
                if (mBluetoothAdapter.getScanMode() !=
                        BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                }
                return true;
//            case R.id.RecAlign:
//                new setAlignDialog(this, new setAlignDialog.DialogCallback() {
//                    @Override
//                    public void DialogReturn(int i) {
//                        align_num = i;
//                    }
//                }).show();
        }
        return false;
    }

}
