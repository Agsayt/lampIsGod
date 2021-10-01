package gr483.beklemishev.lampispower;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RealtimeImage extends AppCompatActivity {


    int gridWidth;
    int gridHeight;
    int destinationPort;
    String destinationAddress;

    GridLayout gl;
    LinearLayout layForGrid;
    DatagramSocket socket;
    DatagramSocket dsocket;


    float receiveInterval = 0.5f;

    private List<Button> addedButtons = new ArrayList<>();
    private Handler handler = new Handler();
    private TimerTask timerTask;
    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_image);

        Intent i = getIntent();

        gridHeight = i.getIntExtra("height", 0);
        gridWidth = i.getIntExtra("width", 0);
        destinationPort = i.getIntExtra("port", 0);
        destinationAddress = i.getStringExtra("address");

        gl = new GridLayout(this);

        gl.setColumnCount(gridWidth);
        gl.setRowCount(gridHeight);
        gl.setUseDefaultMargins(true);
        gl.setAlignmentMode(GridLayout.ALIGN_BOUNDS);

        layForGrid = findViewById(R.id.layForLiveGrid);

        layForGrid.addView(gl);

        FillGrid(gridWidth,gridWidth, gl, addedButtons);

        try {
            socket = new DatagramSocket(null);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {
            dsocket = new DatagramSocket(2000, InetAddress.getByName("0.0.0.0"));
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        startTimer();
        ReceivePacket();
    }

    private void SendPackets() {
        Runnable run = new Runnable() {
            @Override
            public void run() {

                byte[] message = new byte[2];

                try{
                    InetAddress address = InetAddress.getByName(destinationAddress);
                    byte i = 0;
                    while (true){
                        message[0] = 1;
                        message[1] = i++;
                        DatagramPacket packet = new DatagramPacket(message, message.length, address, destinationPort);
                        socket.send(packet);
                        Log.i("SEND", "Sending: " + message);
                        if (i > 15)
                            break;
                    }
                }
                catch (Exception e) {return;}
            }
        };

        Thread secondaryThread = new Thread(run);
        secondaryThread.start();
    }

    private void ReceivePacket() {
        Runnable run = new Runnable() {
            @Override
            public void run() {


                byte[] buffer = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                try {
                    //Keep a socket open to listen to all the UDP trafic that is destined for this port
                    socket.setBroadcast(true);
                    while (true) {

                        //Receive a packet
                        socket.receive(packet);  //<<<<<<<<<<<< This is where it hangs

                        //Packet received
                        Log.i("TEST" ,"Packet received from: " + packet.getData());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addedButtons.get(packet.getData()[0]).setBackgroundColor(Color.rgb(packet.getData()[1], packet.getData()[2], packet.getData()[3]));
                            }
                        });

                    }
                }
                catch (IOException e) {
                    Log.i("ERROR", "Receiving stopped!");
                    socket.close();
                }


            }
        };

        Thread thread = new Thread(run);
        thread.start();
    }



    @Override
    protected void onResume() {
        super.onResume();

        //onResume we start our timer so it can start when the app comes from the background
        startTimer();
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void stoptimertask(View v) {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                    handler.post(new Runnable() {
                    public void run() {
                        SendPackets();
//
                    }
                });
            }
        };
    }

    private void FillGrid(int width, int height, GridLayout gl, List<Button> addedButtons) {
        addedButtons.clear();
        int size = width * height;
        for (int i = 0; i < size; i++)
        {
            Button btn = new Button(this);
            btn.setTag(i);
            btn.setText("" + i);
            btn.setTextColor(Color.BLACK);
            btn.setBackgroundColor(Color.LTGRAY);

            addedButtons.add(btn);
            gl.addView(btn);
        }
    }
}