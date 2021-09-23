package gr483.beklemishev.lampispower;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DataBaseClass extends SQLiteOpenHelper {
    public DataBaseClass(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        String sql = "CREATE TABLE GridLayouts (id INT, text TXT);";
        String sql = "CREATE TABLE NetworkSettings (id INT, title TXT, address TXT, port INT);";
        db.execSQL(sql);
    }

    public int getMaxIdForNetworkSettings()
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT Max(id) FROM NetworkSettings;";
        Cursor cur = db.rawQuery(sql, null);
        if(cur.moveToFirst() == true) return cur.getInt(0);
        return 0;
    }

    public void addNetworkSettingsSave (int id, String title, String address, int port)
    {
        String sid = String.valueOf(id);
        SQLiteDatabase db = getWritableDatabase();
        String sql = "INSERT INTO NetworkSettings VALUES (" + sid + ", '" + title + "' ,'" + address + "', "+ port +");";
        db.execSQL(sql);
    }

    public void addGridLayoutCombination (int id, int[] tags, String name)
    {
        String sid = String.valueOf(id);
        int[] savedTags = tags;
        for (int i = 0; i < savedTags.length; i++)
        {
            SQLiteDatabase db = getWritableDatabase();
            String sql = "INSERT INTO GridLayouts VALUES (" + sid + ", " + savedTags[i] +" ,'" + name + "');";
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

    public void deleteGridLayoutSave(int sid)
    {
        String sql = "DELETE FROM notes WHERE id = '"+ sid +"';";
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
public void getAllGridLayoutCombinations(ArrayList<GridLayoutCombination> lst)
{
    SQLiteDatabase db = getReadableDatabase();
    String sql = "SELECT id, Title, Address, Port FROM NetworkSettings;";
    Cursor cur = db.rawQuery(sql,null);
    if(cur.moveToFirst() == true){
        do {
            GridLayoutCombination n = new GridLayoutCombination();
            n.id = cur.getInt(0);
            lst.add(n);
        } while (cur.moveToNext() == true);
    }
}

    public void getAllNetworkSettings(ArrayList<NetworkSettings> lst)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id, Title, Address, Port FROM NetworkSettings;";
        Cursor cur = db.rawQuery(sql,null);
        if(cur.moveToFirst() == true){
            do {
                NetworkSettings n = new NetworkSettings();
                n.id = cur.getInt(0);
                n.Title = cur.getString(1);
                n.Address = cur.getString(2);
                n.Port = cur.getInt(3);
                lst.add(n);
            } while (cur.moveToNext() == true);
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}