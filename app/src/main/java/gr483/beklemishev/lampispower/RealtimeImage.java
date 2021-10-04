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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RealtimeImage extends AppCompatActivity {


    int gridWidth;
    int gridHeight;
    int destinationPort;
    String destinationAddress;

    int receivePeriod = 5000;
    int sendPeriod = 1000;

    boolean sendingOnRun = true;
    boolean receivingOnRun = true;

    GridLayout gl;
    LinearLayout layForGrid;
    DatagramSocket socket;
    DatagramSocket dsocket;


    float receiveInterval = 0.5f;

    private List<Button> addedButtons = new ArrayList<>();
    private int[] tag = new int[16];
    private Timer timer;
    private Timer timer2;


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

        EditText send = findViewById(R.id.etSend);
        EditText receive = findViewById(R.id.etReceive);

        send.setText(String.valueOf(sendPeriod));
        receive.setText(String.valueOf(receivePeriod));

        send.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged (CharSequence charSequence,int i, int i1, int i2){

            }

                @Override
                public void onTextChanged (CharSequence charSequence,int i, int i1, int i2){

            }


            @Override
            public void afterTextChanged(Editable editable) {
                    sendingOnRun = false;

                    if(timer != null)
                        timer.cancel();

                    sendPeriod = Integer.parseInt(editable.toString());
                    startSending();
            }
        });
        receive.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                receivingOnRun = false;

                if(timer2 != null)
                    timer2.cancel();

                receivePeriod = Integer.parseInt(editable.toString());
                startReceiving();
            }
        });

        WebView webView = findViewById(R.id.webViewLive);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.loadUrl("http://node00.ddns.net:8080/flow/recv.html");
        webView.setInitialScale(500);



    }

    public void stopTimers(){
        if (timer != null) {
            sendingOnRun = false;
            timer.cancel();
        }

        if (timer2 != null)
        {
            receivingOnRun = false;
            timer2.cancel();
        }
    }

    public void resumeTimers(){
        startReceiving();
        startSending();
    }


    @Override
    protected void onResume() {
        super.onResume();
        startSending();
        startReceiving();
    }

    public void startReceiving(){
        timer2 = new Timer();
        receivingOnRun = true;

        timer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                byte[] buffer = new byte[4];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                List<byte[]> colorList = new ArrayList<>();

                try {

                    socket.setBroadcast(true);
                    while (receivingOnRun) {


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
                }
            }
        }, 100, receivePeriod);

    }

    public void startSending() {
        timer = new Timer();
        receivingOnRun = true;

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
            }
        }, 100, sendPeriod); //



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
        play = menu.findItem(R.id.play);
        pause = menu.findItem(R.id.pause);

        return true;
    }

    MenuItem play, pause;

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
            case R.id.play: {
                play.setVisible(false);
                pause.setVisible(true);


                    resumeTimers();
                    item.setIcon(getResources().getDrawable(android.R.drawable.ic_media_play));

            }
            break;
            case R.id.pause:{
                pause.setVisible(false);
                play.setVisible(true);
                stopTimers();
                item.setIcon(getResources().getDrawable(android.R.drawable.ic_media_pause));
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