package gr483.beklemishev.lampispower;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DataBaseClass extends SQLiteOpenHelper {
    public DataBaseClass(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE GridLayoutsCombination (id INT, tagPosition INT, tag INT, name TXT);";
        db.execSQL(sql);

        sql = "CREATE TABLE NetworkSettings (id INT, title TXT, address TXT, port INT);";
        db.execSQL(sql);

        sql = "CREATE TABLE SavedImages (id INT, colorPosition INT, color INT, name TXT);";
        db.execSQL(sql);
    }

    public int getMaxIdForNetworkSettings()
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT Max(id) FROM NetworkSettings;";
        Cursor cur = db.rawQuery(sql, null);
        if(cur.moveToFirst()) return cur.getInt(0);
        cur.close();
        return 0;
    }

    public int getMaxIdForLayoutCombination()
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT Max(id) FROM GridLayoutsCombination;";
        Cursor cur = db.rawQuery(sql, null);
        if(cur.moveToFirst()) {
            int buffer = cur.getInt(0);
            cur.close();
            return buffer;
        }
        cur.close();
        return 0;
    }

    public void addNetworkSettingsSave (int id, String title, String address, int port)
    {
        String sid = String.valueOf(id);
        SQLiteDatabase db = getWritableDatabase();
        String sql = "INSERT INTO NetworkSettings VALUES (" + sid + ", '" + title + "' ,'" + address + "', "+ port +");";
        db.execSQL(sql);
    }

    public void addGridImageStates (int id, int[] colors, String name)
    {

        String sid = String.valueOf(id);
        int[] savedColors = colors;
        SQLiteDatabase db = getWritableDatabase();
        for (int i = 0; i < savedColors.length; i++)
        {
            String sql = "INSERT INTO SavedImages VALUES (" + sid + ","+ i +"," + savedColors[i] +" ,'" + name + "');";
            db.execSQL(sql);
        }
    }

    public void addGridLayoutCombination (int id, int[] tags, String name)
    {

        String sid = String.valueOf(id);
        int[] savedTags = tags;
        SQLiteDatabase db = getWritableDatabase();
        for (int i = 0; i < savedTags.length; i++)
        {
            String sql = "INSERT INTO GridLayoutsCombination VALUES (" + sid + ","+ i +"," + savedTags[i] +" ,'" + name + "');";
            db.execSQL(sql);
        }
    }

//    public void updateNote(int id, String text)
//    {
//        String sid = String.valueOf(id);
//        SQLiteDatabase db = getWritableDatabase();
//        String sql = "UPDATE notes SET text = '"+ text + "' Where id = " + sid + ";";
//        db.execSQL(sql);
//    }

    public void deleteGridLayoutCombination(int sid)
    {
        String sql = "DELETE FROM GridLayoutsCombination WHERE id = '"+ sid +"';";
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL(sql);
    }

//    public String getNote (int id)
//    {
//        String sid = String.valueOf(id);
//        SQLiteDatabase db = getReadableDatabase();
//        String sql = "SELECT text FROM notes WHERE id = " + sid + ";";
//        Cursor cur = db.rawQuery(sql, null);
//        if (cur.moveToFirst() == true) return cur.getString(0);
//        return "";
//    }
@SuppressLint("Range")
public void getAllGridLayoutCombinations(ArrayList<GridLayoutCombination> lst)
{
    SQLiteDatabase db = getReadableDatabase();
    String sql = "SELECT id, tagPosition, tag, name FROM GridLayoutsCombination ORDER By id ASC, tagPosition ASC;";
    Cursor cur = db.rawQuery(sql,null);
    GridLayoutCombination n = new GridLayoutCombination();
    cur.moveToPosition(0);
        int currentID;
        int previousID = cur.getInt(0);
        boolean firstIteration = true;
        do
        {
            currentID = cur.getInt(0);
            if (firstIteration) {
                n = new GridLayoutCombination();
                n.id = cur.getInt(cur.getColumnIndex("id"));
                n.tags.add(n.tags.size(),cur.getInt(cur.getColumnIndex("tag")));
                n.Name = cur.getString(3);
                firstIteration = false;
            }
            else if (currentID == previousID )
            {
                n.tags.add(n.tags.size(),cur.getInt(cur.getColumnIndex("tag")));
            }
            else
            {
                lst.add(n);
                previousID = currentID;
                firstIteration = true;
                cur.moveToPrevious();
                continue;
            }
        } while (cur.moveToNext());

        cur.moveToPrevious();
        if (cur.getInt(0) == getMaxIdForLayoutCombination())
        {
            lst.add(n); //колхоз
        }
    cur.close();
}

    @SuppressLint("Range")
    public void getAllImageStates(ArrayList<StateClass> lst)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id, tagPosition, tag, name FROM SavedImages ORDER By id ASC, tagPosition ASC;";
        Cursor cur = db.rawQuery(sql,null);
        StateClass n = new StateClass();
        cur.moveToPosition(0);
        int currentID;
        int previousID = cur.getInt(0);
        boolean firstIteration = true;
        do
        {
            currentID = cur.getInt(0);
            if (firstIteration) {
                n = new StateClass();
                n.id = cur.getInt(cur.getColumnIndex("id"));

                n.Name = cur.getString(3);
                firstIteration = false;
            }
            else if (currentID == previousID )
            {

            }
            else
            {
                lst.add(n);
                previousID = currentID;
                firstIteration = true;
                cur.moveToPrevious();
                continue;
            }
        } while (cur.moveToNext());

        cur.moveToPrevious();
        if (cur.getInt(0) == getMaxIdForLayoutCombination())
        {
            lst.add(n); //колхоз
        }
        cur.close();
    }

    public void getAllNetworkSettings(ArrayList<NetworkSettings> lst)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id, Title, Address, Port FROM NetworkSettings;";
        Cursor cur = db.rawQuery(sql,null);
        if(cur.moveToFirst()){
            do {
                NetworkSettings n = new NetworkSettings();
                n.id = cur.getInt(0);
                n.Title = cur.getString(1);
                n.Address = cur.getString(2);
                n.Port = cur.getInt(3);
                lst.add(n);
            } while (cur.moveToNext());
        }
        cur.close();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}