package gr483.beklemishev.lampispower;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    Context context;
    ArrayList<StateClass> state;
    LayoutInflater inflter;

    public CustomAdapter(Context applicationContext, ArrayList<StateClass> _state) {
        this.context = context;
        this.state = _state;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return state.size();
    }

    @Override
    public Object getItem(int i) {
        return state.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void deleteItem(int i){
        StaticDb.database.deleteImage(state.get(i).id);
        state.remove(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.state_listview, null);
        TextView imageName = (TextView) view.findViewById(R.id.nameListItem);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        LinearLayout preview = (LinearLayout) view.findViewById(R.id.previewListItem);

        GridLayout gl = new GridLayout(view.getContext());


        gl.setColumnCount(4);
        gl.setRowCount(4);


        List<Button> addedBtns = new ArrayList<>();

        FillGrid(view, gl.getColumnCount(), gl.getRowCount(), gl, addedBtns);

        for (int j = 0; j < addedBtns.size(); j++){
            Button btn = addedBtns.get(j);
            int color = state.get(i).colors.get(j);
            btn.setBackgroundColor(color);
        }

        imageName.setText(state.get(i).toString());

        preview.addView(gl);

        return view;
    }

    private void FillGrid(View view, int width, int height, GridLayout gl, List<Button> addedButtons) {
        addedButtons.clear();
        int size = width * height;
        for (int i = 0; i < size; i++)
        {
            Button btn = new Button(view.getContext());

            btn.setText(i + "");
            btn.setEnabled(false);
            btn.setTextColor(Color.BLACK);
            btn.setWidth(5);
            btn.setHeight(5);

            addedButtons.add(btn);
            gl.addView(btn);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();


            params.width = 60;
            params.height = 50;
            btn.setLayoutParams(params);
        }
    }
}