package gr483.beklemishev.lampispower;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class StateActivity extends AppCompatActivity {

    ListView imagesList;
    ArrayList<StateClass> lstState = new ArrayList<StateClass>();
    String mode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);

        Intent i = getIntent();

        mode = i.getStringExtra("mode");

        lstState.clear();
        StaticDb.database.getAllImageStates(lstState);

        imagesList = (ListView) findViewById(R.id.imagesList);
        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), lstState);

        imagesList.setAdapter(customAdapter);

        imagesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mode != "gallery"){
                    Intent intent = new Intent();

                    StateClass state = (StateClass) customAdapter.getItem(i);
                    intent.putExtra("state", state);

                    setResult(RESULT_OK, intent);
                }

                finish();
            }
        });

        imagesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                customAdapter.deleteItem(i);
                customAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }
}