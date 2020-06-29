package com.mritunjay.btblog;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

class Piconet {
    private final String TAG = Piconet.class.getSimpleName();

    private static final String PICONET = "PICONET";

    private final BluetoothAdapter bluetoothAdapter;

    private HashMap<String, BluetoothSocket> btSockets;

    private HashMap<String, Thread> btConnectionThreads;

    private UUID uuid;

    private ArrayList<String> btDeviceAddresses;

    private Context context;



    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(context, msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "handleMessage: " + msg.getData().getString("msg"));
                    break;
                default:
                    break;
            }
        }
    };


    public Piconet(Context context) {
        Log.d(TAG, "Piconet: ");
        this.context = context;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btSockets = new HashMap<String, BluetoothSocket>();
        btConnectionThreads = new HashMap<String, Thread>();
        btDeviceAddresses = new ArrayList<String>();

        uuid = UUID.fromString("4e0afe94-df38-4aa6-a850-a1a8795a4b1b");

        Thread connectionProvider = new Thread(new ConnectionProvider());
        connectionProvider.start();
    }


    public void startPiconet(int selectDevice) {
        Log.d(TAG, "startPiconet: ");
        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();

        BluetoothDevice[] btArray = new BluetoothDevice[bt.size()];
        int index = 0;

        if (bt.size() > 0) {
            for (BluetoothDevice device : bt) {
                btArray[index] = device;
                index++;
            }
        }

        BluetoothDevice device = btArray[selectDevice];
        if (device != null) {
            Log.d(TAG, "connectToSlave: -----device" + device.getName() + "found-----");
            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());
            startConnection(remoteDevice);
        } else {
            Log.d(TAG, "connectToSlave: -----device not found-----");
            Toast.makeText(context, "No paired devices found", Toast.LENGTH_SHORT).show();
        }

    }

    public void startConnection(BluetoothDevice device) {
        Log.d(TAG, "startConnection: ");
        if (device != null) {
            Log.d(TAG, "startConnection: -----device" + device.getName() + "found-----");
            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());
            connect(remoteDevice);
        } else {
            Log.d(TAG, "startConnection: -----device not connected-----");
        }
    }

    private void connect(BluetoothDevice device) {
        Log.d(TAG, "connect: ");
        BluetoothSocket bluetoothSocket = null;
        String address = device.getAddress();
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());

        for (int j = 0; j < 1 && bluetoothSocket == null; j++) {
            Log.d(TAG, " ** Trying connection..." + j + " with " + device.getName() + ", uuid " + uuid + "...** ");
            bluetoothSocket = getConnectedSocket(remoteDevice, uuid);

            if (bluetoothSocket == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.e(TAG, "InterruptedException in connect", e);
                }
            }
        }

        if (bluetoothSocket == null) {
            Log.e(TAG, " ** Could not connect ** ");
            return;
        }
        Log.d(TAG, " ** Connection established with " + device.getName() + "! ** " + "uuid:" + Arrays.toString(device.getUuids()));
        btSockets.put(address, bluetoothSocket);
        btDeviceAddresses.add(address);
        Thread mBluetoohConnectionThread = new Thread(new BluetoohConnection(bluetoothSocket));
        mBluetoohConnectionThread.start();
        btConnectionThreads.put(address, mBluetoohConnectionThread);
    }

    private BluetoothSocket getConnectedSocket(BluetoothDevice device, UUID uuid) {
        BluetoothSocket bluetoothsocket;
        try {
            bluetoothsocket = device.createRfcommSocketToServiceRecord(uuid);
            bluetoothsocket.connect();
            return bluetoothsocket;
        } catch (IOException e) {
            Log.e(TAG, "IOException in getConnectedSocket", e);
            e.printStackTrace();
        }
        return null;
    }

    private class BluetoohConnection implements Runnable {

        private String address;

        private final InputStream mmInStream;

        public BluetoohConnection(BluetoothSocket btSocket) {
            Log.d(TAG, "BluetoohConnection: ");
            InputStream tmpIn = null;

            try {
                tmpIn = new DataInputStream(btSocket.getInputStream());
            } catch (IOException e) {
                Log.e(TAG, " ** IOException on create InputStream object ** ", e);
            }
            mmInStream = tmpIn;
        }

        @Override
        public void run() {
            Log.d(TAG, "BluetoothConnection run: ");
            byte[] buffer = new byte[1];
            String message = "";
            while (true) {

                try {
                    int readByte = mmInStream.read();
                    if (readByte == -1) {
                        Log.e(TAG, "Discarting message: " + message);
                        message = "";
                        continue;
                    }
                    buffer[0] = (byte) readByte;

                    if (readByte == 0) { // see terminateFlag on write method
                        onReceive(message);
                        message = "";
                    } else { // a message has been recieved
                        message += new String(buffer, 0, 1);
                    }
                } catch (IOException e) {
                    Log.e(TAG, " ** disconnected ** ", e);
                }

                btDeviceAddresses.remove(address);
                btSockets.remove(address);
                btConnectionThreads.remove(address);
            }
        }
    }

    private void onReceive(String receiveMessage) {
        Log.d(TAG, "onReceive: ");
        if (receiveMessage != null && receiveMessage.length() > 0) {
            Log.i(TAG, " #### " + receiveMessage + " #### ");
            Bundle bundle = new Bundle();
            bundle.putString("msg", receiveMessage);
            Message message = new Message();
            message.what = 1;
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }

    private class ConnectionProvider implements Runnable {

        @Override
        public void run() {
            Log.d(TAG, "ConnectionProvider run: ");
            try {

                BluetoothServerSocket myServerSocket = bluetoothAdapter
                        .listenUsingRfcommWithServiceRecord(PICONET, uuid);
                Log.d(TAG, " ** Opened connection  uuid:" + uuid.toString());

                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, " ** Waiting connection for socket uuid:" + uuid.toString());
                BluetoothSocket myBTsocket = myServerSocket.accept();
                Log.d(TAG, " ** Socket accept  uuid:" + uuid.toString());
                try {
                    // Close the socket now that the
                    // connection has been made.
                    myServerSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, " ** IOException when trying to close serverSocket ** ");
                }

                if (myBTsocket != null) {
                    String address = myBTsocket.getRemoteDevice().getAddress();

                    btSockets.put(address, myBTsocket);
                    btDeviceAddresses.add(address);

                    Thread mBtConnectionThread = new Thread(new BluetoohConnection(myBTsocket));
                    mBtConnectionThread.start();

                    Log.d(TAG, " ** Adding " + address + " in mBtDeviceAddresses ** ");
                    btConnectionThreads.put(address, mBtConnectionThread);
                } else {
                    Log.e(TAG, " ** Can't establish connection ** ");
                }

            } catch (IOException e) {
                Log.e(TAG, " ** IOException in ConnectionService:ConnectionProvider ** ", e);
            }
        }
    }

    public void bluetoothBroadcastMessage(String message) {
//        Log.d(TAG, "bluetoothBroadcastRange: ");
        //send message to all except Id
        Log.d("MasterActivity", "bluetoothBroadcastRange: " + message);
        for (int i = 0; i < btDeviceAddresses.size(); i++) {
            sendMessage(btDeviceAddresses.get(i), message);
        }
    }


    private void sendMessage(String destination, String message) {
        Log.d(TAG, "sendMessage: ");
        Log.d(TAG, "sendMessage: ----destination" + destination);
        BluetoothSocket myBsock = btSockets.get(destination);
        if (myBsock != null) {
            try {
                OutputStream outStream = myBsock.getOutputStream();
                final int pieceSize = 16;
                for (int i = 0; i < message.length(); i += pieceSize) {
                    byte[] send = message.substring(i,
                            Math.min(message.length(), i + pieceSize)).getBytes();
                    outStream.write(send);
                }
                // we put at the end of message a character to sinalize that message
                // was finished
                byte[] terminateFlag = new byte[1];
                terminateFlag[0] = 0; // ascii table value NULL (code 0)
                outStream.write(new byte[1]);
            } catch (IOException e) {
                Log.d(TAG, "sendMessage: e:" + e);
            }
        }
    }
}