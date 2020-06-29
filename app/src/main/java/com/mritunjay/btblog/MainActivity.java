package com.mritunjay.btblog;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private Button btnDiscover, btnSubmit;
    private Button btnMessage;
    private final String TAG = MainActivity.class.getSimpleName();
    private Spinner dropDownPairedDevices, dropDownSelectRange;
    private TextView tvMasterLocation;

    ArrayList<String> listSelectDevice = new ArrayList<String>();
    List<String> listSelectRange = new ArrayList<String>();
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;
    String[] deviceName, deviceAssress;

    int selectDevice;

    private Piconet piconet;

    int REQUEST_ENABLE_BLUETOOTH = 1;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        piconet = new Piconet(getApplicationContext());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
//            Log.d(TAG, "onCreate: !bluetoothAdapter.isEnabled()" + !bluetoothAdapter.isEnabled());
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }
        dropDownPairedDevices = findViewById(R.id.rangeDropDown);
        btnMessage = (Button) findViewById(R.id.btnMessage);
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                piconet.bluetoothBroadcastMessage("Hello World");
            }
        });
        btnSubmit = findViewById(R.id.btnSubmit);
        btnDiscover = findViewById(R.id.btnDiscover);
        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();

                btArray = new BluetoothDevice[bt.size()];
                int index = 0;

                if (bt.size() > 0) {
                    for (BluetoothDevice device : bt) {
                        btArray[index] = device;
                        index++;
                    }
                }
                Toast.makeText(MainActivity.this,
                        "Conneted to " + dropDownPairedDevices.getSelectedItem(),
                        Toast.LENGTH_SHORT).show();

                selectDevice = listSelectDevice.indexOf(dropDownPairedDevices.getSelectedItem());
//                Log.d(TAG, "onClick: selectDevice" + selectDevice);
//                Log.d(TAG, "onClick: getSelectedItem: " + dropDownPairedDevices.getSelectedItem());
                piconet.startPiconet(selectDevice);
                String deviceName = String.valueOf(dropDownPairedDevices.getSelectedItem()).replace(" ", "-");
                String message = "Hello World";

                piconet.bluetoothBroadcastMessage(message);
            }

        });
        selectDeviceDropDownMenu();
    }
    private void selectDeviceDropDownMenu() {
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listSelectDevice);
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
                deviceName = new String[bt.size()];
                deviceAssress = new String[bt.size()];
                btArray = new BluetoothDevice[bt.size()];
                int index = 0;

                if (bt.size() > 0) {
                    for (BluetoothDevice device : bt) {
                        btArray[index] = device;
                        deviceName[index] = device.getName();
                        deviceAssress[index] = device.getAddress();
                        index++;
                    }
                }

                for (int i = 0; i < bt.size(); i++) {
                    if (!listSelectDevice.contains(deviceName[i])) {
                        listSelectDevice.add(deviceName[i]);
                    }
                }

                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dropDownPairedDevices.setAdapter(dataAdapter);
            }
        });

    }
}
