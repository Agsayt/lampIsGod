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
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    GridLayout gl;
    LinearLayout mainLay;
    LinearLayout layForGrid;

    InetAddress address;
    DatagramSocket socket;
    DatagramPacket packet;

    Boolean IsAnimation = false;

    String destinationAddress = "node00.ddns.net";
    int destinationPort = 2000;
    int gridWidth = 4;
    int gridHeight = 4;

    // Лист добавленных кнопок
    List<Button> addedButtons = new ArrayList<>();

    // Листы для настроек сети
    ListView loadLV;
    ArrayList<NetworkSettings> lstNetwork = new ArrayList<NetworkSettings>();
    ArrayAdapter<NetworkSettings> adpNetwork;
    //

    // Листы для разметок
    ListView loadGL;
    ArrayList<GridLayoutCombination> lstLayout = new ArrayList<GridLayoutCombination>();
    ArrayAdapter<GridLayoutCombination> adpLayout;

    // Листы для сохранения картинок
    ListView imagesLV;
    ArrayList<NetworkSettings> lstImages = new ArrayList<NetworkSettings>();
    ArrayAdapter<NetworkSettings> adpImages;
    //


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StaticDb.database = new DataBaseClass(this,"lampDb5.db",null,5);

        gl = new GridLayout(this);

        gl.setColumnCount(gridWidth);
        gl.setRowCount(gridHeight);
        gl.setUseDefaultMargins(true);
        gl.setAlignmentMode(GridLayout.ALIGN_BOUNDS);


        Button saveLayout = findViewById(R.id.bSaveLayout);
        saveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveLayout(view);
            }
        });

        Button loadLayout = findViewById(R.id.bLoadLayout);
        loadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadLayout(view);
            }
        });

        mainLay = findViewById(R.id.mainLayout);
        layForGrid = findViewById(R.id.layoutForGrid);

        layForGrid.addView(gl);

        FillGrid(gridWidth,gridWidth, gl, addedButtons);

        try {
            socket = new DatagramSocket(null);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.loadUrl("http://node00.ddns.net:8080/flow/recv.html");
        webView.setInitialScale(500);

        mainLay.addView(webView);
    }

    private void SaveLayout(View view) {
        int[] buffer = new int[gridHeight*gridWidth];
        for (int i=0;i < addedButtons.size();i++) {
             buffer[i] = Integer.parseInt(addedButtons.get(i).getTag().toString());
        }
        int nid = StaticDb.database.getMaxIdForLayoutCombination() + 1;
        StaticDb.database.addGridLayoutCombination(nid, buffer, "Save #" + nid);

        Toast.makeText(getApplicationContext(), "Сетка успешно сохранена!", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("SetTextI18n")
    private void LoadLayout(View view){
        View customLayout = getLayoutInflater().inflate(R.layout.dialog_loadnetworksettings, null);
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setTitle("Загрузка разметки!");
        bld.setView(customLayout);

        Dialog dlg = bld.create();
        dlg.show();

        loadGL = customLayout.findViewById(R.id.lvNetworkSettings);
        adpLayout = new ArrayAdapter<GridLayoutCombination>(this, android.R.layout.simple_list_item_1, lstLayout);


        loadGL.setAdapter(adpLayout);

        loadGL.setOnItemClickListener((parent, _view, position, id) -> {
            GridLayoutCombination n = adpLayout.getItem(position);
                for (int i=0; i < addedButtons.size(); i++) {
                     addedButtons.get(i).setTag(n.tags.get(i));
                     addedButtons.get(i).setText(n.tags.get(i).toString());
                }
            dlg.cancel();
        });

        lstLayout.clear();
        StaticDb.database.getAllGridLayoutCombinations(lstLayout);
        adpLayout.notifyDataSetChanged();

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
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OnPanelClick(view);
                }
            });
            btn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ChangeTag(v);
                    return true;
                }
            });

            addedButtons.add(btn);
            gl.addView(btn);
        }
    }

    private void ChangeTag(View v) {
        Button senderButton = (Button)v;

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_changetag, null);
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setTitle("Смена тега");
//        bld.setMessage("");
        bld.setView(customLayout);

        Dialog dlg = bld.create();


        TextView currentTag = customLayout.findViewById(R.id.tvCurrentTag);
        Button acceptButton = customLayout.findViewById(R.id.bAccept);
        Button cancelButton = customLayout.findViewById(R.id.bCancel);
        EditText etTagNumber = customLayout.findViewById(R.id.etTagNumber);

        currentTag.setText(currentTag.getText() + " " + String.valueOf(senderButton.getTag()));

        dlg.show();
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                senderButton.setText(etTagNumber.getText());
                senderButton.setTag(etTagNumber.getText());
                dlg.cancel();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.cancel();
            }
        });
    }

    public void OnViewResult(View v)
    {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                byte[] message = new byte[1];

                try{
                    address = InetAddress.getByName(destinationAddress);
                    message[0] = 0;
                    packet = new DatagramPacket(message, message.length, address, destinationPort);
                    socket.send(packet);
                }
                catch (Exception e) { }
            }
        };

        Thread secondaryThread = new Thread(run);
        secondaryThread.start();
    }


    public void OnPanelClick(View v)
    {
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_selectcolor, null);
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setTitle("Выбор цвета!");
        bld.setView(customLayout);
//
        Dialog dlg = bld.create();
        dlg.show();


        Button acceptButton = customLayout.findViewById(R.id.bAcceptColor);
        Button cancelButton = customLayout.findViewById(R.id.bCancelColor);

        ImageView previewColor = customLayout.findViewById(R.id.colorPreview);

        EditText etRed = customLayout.findViewById(R.id.etRed);
        EditText etGreen = customLayout.findViewById(R.id.etGreen);
        EditText etBlue = customLayout.findViewById(R.id.etBlue);

        SeekBar red = customLayout.findViewById(R.id.sbRed);
        SeekBar green = customLayout.findViewById(R.id.sbGreen);
        SeekBar blue = customLayout.findViewById(R.id.sbBlue);

        red.setProgress(0);
        green.setProgress(0);
        blue.setProgress(0);

        etRed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int enteredValue;

                if (editable.length() == 0)
                {
                    enteredValue = 0;
                    etRed.setText("0");
                }
                else
                    enteredValue = Integer.valueOf(editable.toString());

                if (enteredValue > 255)
                {
                    etRed.setText("255");
                    enteredValue = 255;
                }else if (enteredValue < 0)
                {
                    etRed.setText(0);
                    enteredValue = 0;
                }

                red.setProgress(enteredValue);
            }
        });
        etGreen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                int enteredValue;

                if (editable.length() == 0)
                {
                    enteredValue = 0;
                    etGreen.setText("0");
                }
                else
                    enteredValue = Integer.valueOf(editable.toString());

                if (enteredValue > 255)
                {
                    etGreen.setText("255");
                    enteredValue = 255;
                }else if (enteredValue < 0)
                {
                    etGreen.setText(0);
                    enteredValue = 0;
                }
                green.setProgress(enteredValue);
            }
        });
        etBlue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int enteredValue;

                if (editable.length() == 0)
                {
                    enteredValue = 0;
                    etBlue.setText("0");
                }
                else
                    enteredValue = Integer.valueOf(editable.toString());


                if (enteredValue > 255)
                {
                    etBlue.setText("255");
                    enteredValue = 255;
                }else if (enteredValue < 0)
                {
                    etBlue.setText(0);
                    enteredValue = 0;
                }
                blue.setProgress(enteredValue);
            }
        });

        red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                previewColor.setBackgroundColor(Color.rgb(seekBar.getProgress(), green.getProgress(), blue.getProgress()));
                etRed.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                previewColor.setBackgroundColor(Color.rgb(seekBar.getProgress(), green.getProgress(), blue.getProgress()));
                etRed.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                previewColor.setBackgroundColor(Color.rgb(seekBar.getProgress(), green.getProgress(), blue.getProgress()));
                etRed.setText(String.valueOf(seekBar.getProgress()));
            }
        });
        green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                previewColor.setBackgroundColor(Color.rgb(red.getProgress(), seekBar.getProgress(), blue.getProgress()));
                etGreen.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                previewColor.setBackgroundColor(Color.rgb(red.getProgress(), seekBar.getProgress(), blue.getProgress()));
                etGreen.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                previewColor.setBackgroundColor(Color.rgb(red.getProgress(), seekBar.getProgress(), blue.getProgress()));
                etGreen.setText(String.valueOf(seekBar.getProgress()));
            }
        });
        blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                previewColor.setBackgroundColor(Color.rgb(red.getProgress(), green.getProgress(), seekBar.getProgress()));
                etBlue.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                previewColor.setBackgroundColor(Color.rgb(red.getProgress(), green.getProgress(), seekBar.getProgress()));
                etBlue.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                previewColor.setBackgroundColor(Color.rgb(red.getProgress(), green.getProgress(), seekBar.getProgress()));
                etBlue.setText(String.valueOf(seekBar.getProgress()));
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PacketForming(v, red.getProgress(), green.getProgress(), blue.getProgress());
                dlg.cancel();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlg.cancel();
            }
        });
    }

    private void PacketForming(View v, int red, int green, int blue) {
        Button b = (Button)v;
        b.setBackgroundColor(Color.rgb(red,green,blue));
        if (!IsAnimation)
        {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    Button b = (Button)v;

                    byte[] message = new byte[5];

                    try{
                        address = InetAddress.getByName(destinationAddress);

                        message[0] = 2;
                        message[1] = (byte)Integer.parseInt(b.getTag().toString());
                        message[2] = (byte)red; //R
                        message[3] = (byte)green; //G
                        message[4] = (byte)blue;
                        packet = new DatagramPacket(message, message.length, address, destinationPort);
                        socket.send(packet);
                    }
                    catch (Exception e) {return;}
                }
            };

            Thread secondaryThread = new Thread(run);
            secondaryThread.start();
        }
        else
        {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu1, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.Settings: {
                final View customLayout = getLayoutInflater().inflate(R.layout.dialog_settings, null);
                AlertDialog.Builder bld = new AlertDialog.Builder(this);
                bld.setTitle("Настройки подключения!");
                bld.setView(customLayout);

                Dialog dlg = bld.create();

                EditText address = customLayout.findViewById(R.id.etDestinationAddress);
                EditText port = customLayout.findViewById(R.id.etDestinationPort);
                EditText etGridWidth = customLayout.findViewById(R.id.etGridWidth);
                EditText etGridHeight = customLayout.findViewById(R.id.etGridHeight);

                Button accept = customLayout.findViewById(R.id.bAcceptSettings);
                Button cancel = customLayout.findViewById(R.id.bCancelSettings);
                Button save = customLayout.findViewById(R.id.bSaveNetworkSettingsDialog);
                Button load = customLayout.findViewById(R.id.bLoadNetworkSettingsDialog);

                address.setText(destinationAddress);
                port.setText(String.valueOf(destinationPort));
                etGridWidth.setText(String.valueOf(gridWidth));
                etGridHeight.setText(String.valueOf(gridHeight));

                dlg.show();

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        OnNetworkSettingsSave(view, "save", address.getText().toString(), port.getText().toString());
                    }
                });
                load.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        OnNetworkSettingsLoad(view, "load", address, port);
                    }
                });

                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        destinationAddress = String.valueOf(address.getText());
                        destinationPort = Integer.valueOf(String.valueOf(port.getText()));

                        if(String.valueOf(etGridWidth.getText()) != String.valueOf(gridWidth) || String.valueOf(etGridHeight.getText()) != String.valueOf(gridHeight))
                        {
                            GridChanged();
                        }
                        dlg.cancel();
                    }

                    private void GridChanged(){
                        gridWidth = Integer.valueOf(etGridWidth.getText().toString());
                        gridHeight = Integer.valueOf(etGridHeight.getText().toString());
                        gl.removeAllViews();
                        gl.setColumnCount(gridWidth);
                        gl.setRowCount(gridHeight);
                        FillGrid(gridWidth,gridHeight, gl, addedButtons);
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dlg.cancel();
                    }
                });
            }
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
            case R.id.Load: {
                Intent i = new Intent(this, StateActivity.class);
                startActivityForResult(i, 1);
            }
            break;
            case R.id.Live: {
                Intent i = new Intent(this, RealtimeImage.class);
                i.putExtra("width", gridWidth);
                i.putExtra("height", gridHeight);
                i.putExtra("address", destinationAddress);
                i.putExtra("port", destinationPort);

                int[] buffer = new int[16];
                for (int j = 0; j < 16; j++)
                {
                    buffer[j] = (int) addedButtons.get(j).getTag();
                }

                i.putExtra("buttons", (Serializable) buffer);
                startActivity(i);
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
            StateClass receivedState = (StateClass) data.getSerializableExtra("state");

            for (int i = 0; i < addedButtons.size(); i++) {
                int color = receivedState.colors.get(i);
                addedButtons.get(i).setBackgroundColor(color);
                PacketForming(addedButtons.get(i), Color.red(color), Color.green(color), Color.blue(color));
            }
        }
    }

    private void OnNetworkSettingsLoad(View view, String load, EditText address, EditText port) {
        View customLayout = getLayoutInflater().inflate(R.layout.dialog_loadnetworksettings, null);
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setTitle("Загрузка настроек подключения!");
        bld.setView(customLayout);

        Dialog dlg = bld.create();
        dlg.show();

        loadLV = customLayout.findViewById(R.id.lvNetworkSettings);
        adpNetwork = new ArrayAdapter<NetworkSettings>(this, android.R.layout.simple_list_item_1, lstNetwork);

        loadLV.setAdapter(adpNetwork);

        loadLV.setOnItemClickListener((parent, _view, position, id) -> {
            NetworkSettings n = adpNetwork.getItem(position);
            address.setText(n.Address);
            port.setText(String.valueOf(n.Port));
            dlg.cancel();
        });

        lstNetwork.clear();
        StaticDb.database.getAllNetworkSettings(lstNetwork);
        adpNetwork.notifyDataSetChanged();
    }

    private void OnNetworkSettingsSave(View view, String mode, String _address, String _port) {

        View customLayout = getLayoutInflater().inflate(R.layout.dialog_savenetworksettings, null);
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setTitle("Сохранение настроек подключения!");
        bld.setView(customLayout);

        Dialog dlg = bld.create();

        EditText title = customLayout.findViewById(R.id.etSaveName);
        EditText address = customLayout.findViewById(R.id.etAddressToSave);
        EditText port = customLayout.findViewById(R.id.etPortToSave);

        Button save = customLayout.findViewById(R.id.bSaveNetworkSettings);
        Button cancel = customLayout.findViewById(R.id.bCancelNetworkSettingsDialog);

        address.setText(_address);
        port.setText(_port);

        dlg.show();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nid = StaticDb.database.getMaxIdForNetworkSettings() + 1;
                StaticDb.database.addNetworkSettingsSave(nid, title.getText().toString(), address.getText().toString(), Integer.valueOf(port.getText().toString()));
                Toast.makeText(getApplicationContext(), "Запись добавлена!", Toast.LENGTH_LONG).show();
                dlg.cancel();
            }
        });
    }


}