package gr483.beklemishev.lampispower;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class StateActivity extends AppCompatActivity {

    ListView imagesList;
    ArrayList<StateClass> lstState = new ArrayList<StateClass>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);


        lstState.clear();
        StaticDb.database.getAllImageStates(lstState);

        imagesList = (ListView) findViewById(R.id.imagesList);
        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), lstState);
        imagesList.setAdapter(customAdapter);
    }
}