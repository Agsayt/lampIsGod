package gr483.beklemishev.lampispower;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StateClass implements Serializable {
    public int id;
    public List<Integer> colors = new ArrayList<>();
    public String Name;

    @Override
    public String toString() {
        return Name.toString();
    }
}
