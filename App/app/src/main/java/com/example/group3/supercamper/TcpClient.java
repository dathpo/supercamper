package com.example.group3.supercamper;

import android.util.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by aidanhowie on 07/02/18.
 */
public class TcpClient {

    private String serverMessage;
    private String messageToSend;
    public String serverIp = "192.168.4.1"; //server IP address
    public static final int SERVER_PORT = 100;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;

    private PrintWriter out = null;
    private BufferedReader in = null;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(final OnMessageReceived listener, String ipAddressOfServerDevice) {
        mMessageListener = listener;
        serverIp = ipAddressOfServerDevice;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (out != null && !out.checkError()) {
                    System.out.println("Message: " + message);
                    out.print(message);
                    out.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        mRun = false;
    }

    public void run() {
        mRun = true;
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(serverIp);
            Log.e("TCP SI Client", "SI: Connecting...");
            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVER_PORT);
            try {
                //sends the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                Log.e("TCP SI Client", "SI: Sent.");
                Log.e("TCP SI Client", "SI: Done.");
                //receives the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();
                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                        Log.e("RESPONSE FROM SERVER", "S: Received Message: " + serverMessage + "'");
                    }
                    serverMessage = null;
                }
            } catch (Exception e) {
                Log.e("TCP SI Error", "SI: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }
        } catch (Exception e) {
            Log.e("TCP SI Error", "SI: Error", e);
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}