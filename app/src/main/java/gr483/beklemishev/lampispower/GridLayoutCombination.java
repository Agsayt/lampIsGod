package gr483.beklemishev.lampispower;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class GridLayoutCombination {
    public int id;
    public List<Integer> tags = new ArrayList<>();
    public String Name;

    @Override
    public String toString() {
        String buffer = "";
        for (int i = 0; i < tags.size();i++)
        {
            buffer += tags.get(i) + " ";
        }

        return Name + " " + buffer;
    }
}
