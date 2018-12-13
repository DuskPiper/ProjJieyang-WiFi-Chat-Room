package com.example.duskpiper.projjieyang_wifi_chatroom;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements SocketThread.OnClientListener {
    private EditText inputMessage;
    private EditText inputIP;
    private EditText inputPort;
    private Button sendButton;
    private Button setAddrButton;
    private Button cancelConnectionButton;
    private TextView debugWindow;
    private TextView chatWindow;
    private ArrayList<String> debugInfo;
    private ArrayList<String> messages;
    private int debugMaxLength = 6;
    private StringBuffer output;
    private String message;
    private String host;
    private int port;
    private boolean looperDaemon = true;
    private SocketThread socketThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // Allow NetWork On Main Thread

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        inputMessage = (EditText)findViewById(R.id.message_input);
        sendButton = (Button)findViewById(R.id.send_button);
        debugWindow = (TextView)findViewById(R.id.debug_window);
        inputIP = (EditText)findViewById(R.id.ip_edit);
        inputPort = (EditText)findViewById(R.id.port_edit);
        setAddrButton = (Button)findViewById(R.id.set_addr_button);
        cancelConnectionButton = (Button)findViewById(R.id.disconnect_button);
        chatWindow = (TextView)findViewById(R.id.chat_window);

        host = "172.31.129.73";
        port = 999;
        checkWiFiConnection(this);
        output = new StringBuffer();
        inputIP.setText(host);
        inputPort.setText(Integer.toString(port));
        messages = new ArrayList<String>();
        sendButton.setClickable(false);
        socketThread = new SocketThread(this, host, port);
        cancelConnectionButton.setVisibility(View.INVISIBLE); // WE ARE NOT YET USING THIS UNSTABLE FEATURE
        updateDebugWindow("> System initialized.");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // COLLECT AND SEND MESSAGE
                message = inputMessage.getText().toString();
                if (message.length() < 1) {
                    updateDebugWindow("> Aborted: message too short.");
                    Toast.makeText(ChatActivity.this, "Message should not be empty!", Toast.LENGTH_LONG).show();
                } else if (!checkWiFiConnection(ChatActivity.this)) {
                    // NO WIFI
                } else {
                    inputMessage.setText("");
                    // SEND THROUGH SOCKET
                    socketThread.sendMessage(message);
                }
            }
        });

        setAddrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                host = inputIP.getText().toString();
                port = Integer.valueOf(inputPort.getText().toString());
                updateDebugWindow("> Set new addr = " + host + ":" + Integer.toString(port));
                /*if (socketThread.isAlive()) {
                    socketThread.disconnect();
                }*/
                socketThread.start();
                updateDebugWindow("> Socket started.");
                sendButton.setClickable(true);
                setAddrButton.setClickable(false);
            }
        });

        cancelConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socketThread != null) {
                    socketThread.sendMessage("{quit}");
                    socketThread.disconnect();
                    socketThread.interrupt();
                    setAddrButton.setClickable(true);
                    sendButton.setClickable(false);
                }
            }
        });
    }

    private void updateDebugWindow(String info) {
        if (debugInfo == null) {
            debugInfo = new ArrayList<String>();
        } else {
            while (debugInfo.size() >= debugMaxLength) {
                debugInfo.remove(0);
            }
        }
        debugInfo.add(info);

        output = new StringBuffer();
        for (String infoLine : debugInfo) {
            output.append(infoLine + "\n");
        }
        debugWindow.setText(output);
    }

    private void updateChatWindow(String newMessage) {
        messages.add(newMessage);
        StringBuffer showMessages = new StringBuffer();
        for (String eachMessage : messages) {
            showMessages.append(">" + eachMessage + "\n\n");
        }
        chatWindow.setText(showMessages);
    }

    private boolean checkWiFiConnection(Context context){
        ConnectivityManager connectManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(networkInfo.isConnected()){
            updateDebugWindow("> WiFi check: Connected.");
            return true;
        }
        else{
            updateDebugWindow("> WiFi failure: NOT connected to WiFi.");
            Toast.makeText(ChatActivity.this, "Please connect to WiFi!", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onNewMessage(String msg) {
        final String newMsg = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateDebugWindow("New message");
                updateChatWindow(newMsg);
            }
        });
        if (checkMsg(msg)) {
            notifyByVibrator();
            notifyByFlashlight();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketThread.disconnect();
    }

    private void notifyByFlashlight() {
        try {
            CameraManager cameraManager = (CameraManager)this.getSystemService(Context.CAMERA_SERVICE);
            cameraManager.setTorchMode("0", true);
            cameraManager.setTorchMode("0", false);
            cameraManager.setTorchMode("0", true);
            cameraManager.setTorchMode("0", false);
            cameraManager.setTorchMode("0", true);
            cameraManager.setTorchMode("0", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyByVibrator() {
        Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {300, 100, 200, 500, 300, 100};
        vibrator.vibrate(pattern, -1);
    }

    private boolean checkMsg(String msg) {
        if (msg == null) {
            return false;
        } else if (msg.length() < 3) {
            return false;
        } else if (msg.charAt(0) == 'W' && msg.charAt(1) == 'e') {
            return false;
        } else {
            return true;
        }
    }
}