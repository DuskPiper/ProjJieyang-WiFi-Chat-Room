package com.example.duskpiper.projjieyang_wifi_chatroom;

import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;

public class SocketThread extends Thread {

    public interface OnClientListener {
        void onNewMessage(String msg);
    }

    private OnClientListener onClientListener;

    public void setOnClientListener(OnClientListener onClientListener) {
        this.onClientListener = onClientListener;
    }

    private Socket socket;
    private boolean isConnected = false;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String host;
    private int port;

    public SocketThread(OnClientListener onClientListener, String host, int port) {
        this.onClientListener = onClientListener;
        this.host = host;
        this.port = port;
    }

    public void disconnect() {
        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {
        super.run();
        try {
            socket = new Socket(host, port);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            Log.i("Socket", "Connected");
            isConnected = true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (isConnected) {
            try {
                while (isConnected) {
                    byte[] bytedMsg = new byte[dataInputStream.available()];
                    dataInputStream.read(bytedMsg);
                    String receivedMsg = new String(bytedMsg);
                    if (receivedMsg.length() > 1) {
                        Log.i("MSG", ">>>" + receivedMsg + "<<<");
                        if (onClientListener != null) {
                            onClientListener.onNewMessage(receivedMsg);
                        }
                    }
                }
            } catch (EOFException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (dataInputStream != null)
                        dataInputStream.close();
                    if (dataOutputStream != null)
                        dataOutputStream.close();
                    if (socket != null) {
                        socket.close();
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void sendMessage(String message) {
        try {
            String encodedMsg = URLEncoder.encode(message, "utf-8");
            byte[] bytedMsg = encodedMsg.getBytes();
            dataOutputStream.write(bytedMsg);
            Log.d("Socket", "> Message sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
