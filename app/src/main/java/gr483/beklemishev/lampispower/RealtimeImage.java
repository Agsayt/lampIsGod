package gr483.beklemishev.lampispower;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private int[] tag = new int[16];
    private Handler handler = new Handler();
    private TimerTask timerTask;
    private Timer timer;
    Thread receiveThread;
    private TimerTask timerTask2;
    private Timer timer2;
    private Handler handler2 = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_image);

        Intent i = getIntent();

        tag = (int[]) i.getSerializableExtra("buttons");
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


        ReceivePacket();
        startTimer();

    }

    private void SendPackets() {
        Runnable run = new Runnable() {
            @Override
            public void run() {

                byte[] message = new byte[2];

                try{
                    InetAddress address = InetAddress.getByName(destinationAddress);
                    byte i = 0;
                    while (i < 15){
                        message[0] = 1;
                        message[1] = i++;
                        DatagramPacket packet = new DatagramPacket(message, message.length, address, destinationPort);
                        socket.send(packet);
                        Log.i("SEND", "Sending: " + message + "to btn " + i);
                    }
                }
                catch (Exception e) {
                    Log.i("EXCEPTION", e.getLocalizedMessage());
                    }
                finally
                {
                    Log.i("Sending", "Cycle stopped!");
                }
            }
        };

        Thread secondaryThread = new Thread(run);
        secondaryThread.start();
    }

    private void ReceivePacket() {
        Runnable run = new Runnable() {
            @Override
            public void run() {


                byte[] buffer = new byte[4];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                List<byte[]> colorList = new ArrayList<>();

                try {

                    socket.setBroadcast(true);
                    while (true) {


                        socket.receive(packet);

                        //Packet received

                        if (packet.getData()[1] < 0 || packet.getData()[2] < 0 || packet.getData()[3] < 0)
                            continue;

                        Log.i("RECEIVE" ,"Received: " + Arrays.toString(packet.getData()));

                        colorList.add(packet.getData());

                        runOnUiThread(() -> {
                            try{
                                byte[] localBuffer = colorList.get(0);
                                colorList.remove(0);

                                for(Button b: addedButtons) {
                                    if(Integer.valueOf(b.getTag().toString()).equals((int) localBuffer[0])) {
                                        b.setBackgroundColor(Color.rgb(localBuffer[1], localBuffer[2], localBuffer[3]));
                                        Log.i("UI" ,"Changed button: " + addedButtons.get(localBuffer[0]).getTag());
                                    }
                                }


                            }
                            catch (Exception e){
                                Log.i("UI ERROR", e.getLocalizedMessage());
                            }
                            finally{
                                Log.i("UI", "UI Updated!");
                            }

                        });
                        Thread.sleep(10);

                    }
                }
                catch (IOException | InterruptedException e) {

                    socket.close();
                }
                finally {
                    Log.i("ERROR", "Receiving stopped!");
                    socket.close();
                }
            }
        };

        receiveThread = new Thread(run);
        receiveThread.start();
    }



    @Override
    protected void onResume() {
        super.onResume();


        startTimer();
    }

    public void startTimer() {
        timer = new Timer();

        initializeTimerTask();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                byte[] message = new byte[2];

                try{
                    InetAddress address = InetAddress.getByName(destinationAddress);
                    byte i = 0;
                    while (i <= 15){
                        message[0] = 1;
                        message[1] = (byte) (Byte.parseByte(addedButtons.get(i).getTag().toString()));
                        DatagramPacket packet = new DatagramPacket(message, message.length, address, destinationPort);
                        socket.send(packet);
                        Log.i("SEND", String.valueOf((byte) (Byte.parseByte(addedButtons.get(i).getTag().toString()))) + " - tag " + (i));
                        i++;
                        Thread.sleep(10);
                    }
                }
                catch (Exception e) {
                    Log.i("EXCEPTION", e.getLocalizedMessage());
                }
                finally
                {

                }
            }
        }, 1000, 5000); //

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
            btn.setTag(tag[i]);
            btn.setText("" + (tag[i]));
            btn.setTextColor(Color.BLACK);
            btn.setBackgroundColor(Color.LTGRAY);

            addedButtons.add(btn);
            gl.addView(btn);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.Save: {
                final View customLayout = getLayoutInflater().inflate(R.layout.dialog_imagesave, null);
                AlertDialog.Builder bld = new AlertDialog.Builder(this);
                bld.setTitle("Настройки подключения!");
                bld.setView(customLayout);

                LinearLayout preview = customLayout.findViewById(R.id.previewGridSave);
                EditText imageName = customLayout.findViewById(R.id.etImageNameToSave);

                Button save = customLayout.findViewById(R.id.bSaveImage);
                Button cancel = customLayout.findViewById(R.id.bCancelImageSave);

                Dialog dlg = bld.create();
                dlg.show();

                GridLayout prevGrid = new GridLayout(getApplicationContext());
                prevGrid.setRowCount(gridHeight);
                prevGrid.setColumnCount(gridWidth);
                List<Button> prevButtonsList = new ArrayList<>();
                FillGrid(gridWidth, gridHeight, prevGrid, prevButtonsList);
                for (int i = 0; i < prevButtonsList.size(); i++) {
                    prevButtonsList.get(i).setEnabled(false);
                    prevButtonsList.get(i).setTag(addedButtons.get(i).getTag());
                    prevButtonsList.get(i).setText(addedButtons.get(i).getText());
                    ColorDrawable viewColor = (ColorDrawable) addedButtons.get(i).getBackground();
                    int colorId = viewColor.getColor();
                    prevButtonsList.get(i).setBackgroundColor(colorId);
                }
                preview.addView(prevGrid);

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int[] buffer = new int[prevGrid.getColumnCount() * prevGrid.getRowCount()];
                        for (int i = 0; i < prevButtonsList.size(); i++) {
                            ColorDrawable viewColor = (ColorDrawable) prevButtonsList.get(i).getBackground();
                            int colorId = viewColor.getColor();
                            buffer[i] = colorId;
                        }
                        int nid = StaticDb.database.getMaxIdForSavedImages() + 1;
                        StaticDb.database.addImage(nid, buffer, imageName.getText().toString());

                        Toast.makeText(getApplicationContext(), "Изображение успешно сохранено!", Toast.LENGTH_LONG).show();
                        dlg.cancel();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dlg.cancel();
                    }
                });
            }
            break;
            case R.id.Gallery: {
                Intent i = new Intent(this, StateActivity.class);
                i.putExtra("mode", "gallery");
                startActivityForResult(i, 1);
            }
            break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null)
        {
            Toast.makeText(getApplicationContext(), "Действие успешно", Toast.LENGTH_SHORT);
        }
    }
}