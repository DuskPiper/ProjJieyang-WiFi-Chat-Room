package com.example.duskpiper.projjieyang_wifi_chatroom;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements SocketThread.OnClientListener {
    private EditText inputMessage;
    private EditText inputIP;
    private EditText inputPort;
    private Button sendButton;
    private Button setAddrButton;
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
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        inputMessage = (EditText)findViewById(R.id.message_input);
        sendButton = (Button)findViewById(R.id.send_button);
        debugWindow = (TextView)findViewById(R.id.debug_window);
        inputIP = (EditText)findViewById(R.id.ip_edit);
        inputPort = (EditText)findViewById(R.id.port_edit);
        setAddrButton = (Button)findViewById(R.id.set_addr_button);
        chatWindow = (TextView)findViewById(R.id.chat_window);

        host = "172.31.140.238";
        port = 65525;
        checkWiFiConnection(this);
        output = new StringBuffer();
        inputIP.setText(host);
        inputPort.setText(Integer.toString(port));
        messages = new ArrayList<String>();
        sendButton.setClickable(false);
        socketThread = new SocketThread(this, host, port);
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
                    //send(message);
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
                if (socketThread.isAlive()) {
                    socketThread.disconnect();
                }
                socketThread.start();
                updateDebugWindow("> Socket started.");
                sendButton.setClickable(true);
                //receive();
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
            showMessages.append(eachMessage + "\n");
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketThread.disconnect();
    }

    /*public void send(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket;
                try {
                    // CONNECT
                    Log.i("socket", "wrapping");
                    socket = new Socket(host, port);

                    Log.i("socket", "connected");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDebugWindow("> Sender connection successful.");
                        }
                    });

                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    String utf8Msg = URLEncoder.encode(message, "utf-8");
                    sendTextMsg(out, utf8Msg);
                    // out.close();
                    // socket.close();

                    Log.i("socket", "sent");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDebugWindow("> Message sent.");
                            updateChatWindow("Me: " + message);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    //updateDebugWindow("> Failed to connect");
                }
            }
        }).start();
    }

    public void receive(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Socket socket;
                DataInputStream input;
                byte[] bytes;
                try {
                    socket = new Socket(host, port);
                    Log.i("socket", "connected");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDebugWindow("> Receiver connection successful.");
                        }
                    });
                    while (looperDaemon) {
                        // Log.i("Receiver Loop", "Running");

                        input = new DataInputStream(socket.getInputStream());
                        //long len = input.readLong();
                        //int len = input.readUnsignedShort();
                        int len = input.available();
                        bytes = new byte[(int)len];
                        input.read(bytes);
                        final String receivedMessage = new String(bytes);

                        if (receivedMessage.length() > 1) {
                            Log.e("MSG", receivedMessage);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDebugWindow("> Message received.");
                                    updateChatWindow("Remote: " + receivedMessage);
                                }
                            });
                        } else {
                            // Log.i("MSG", "Empty msg.");
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendTextMsg(DataOutputStream out, String msg) throws IOException {
        // updateDebugWindow("> Sending message...");
        Log.i("socket", "sending");
        byte[] bytes = msg.getBytes();
        long len = bytes.length;
        out.writeLong(len);
        out.write(bytes);
    }*/
}