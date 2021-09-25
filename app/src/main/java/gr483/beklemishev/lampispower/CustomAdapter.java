package gr483.beklemishev.lampispower;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

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
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.state_listview, null);
        TextView imageName = (TextView) view.findViewById(R.id.textView);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        imageName.setText(state.get(i).toString());
        icon.setImageResource(state.get(i).image);
        return view;
    }
}