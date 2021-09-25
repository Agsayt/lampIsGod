package gr483.beklemishev.lampispower;

import java.util.ArrayList;
import java.util.List;

public class StateClass {
    public int id;
    public List<Integer> colors = new ArrayList<>();
    public String Name;
    public int image;

    @Override
    public String toString() {
        return Name.toString();
    }
}
