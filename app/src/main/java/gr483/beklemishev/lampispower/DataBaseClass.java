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
        String sql = "CREATE TABLE GridLayouts (id INT, tags INT, text TXT);";
        db.execSQL(sql);
        sql = "CREATE TABLE NetworkSettins (id INT, title TXT, address TXT, port, INT);";
        db.execSQL(sql);
    }

//    public int getMaxId()
//    {
//        SQLiteDatabase db = getReadableDatabase();
//        String sql = "SELECT Max(id) FROM notes;";
//        Cursor cur = db.rawQuery(sql, null);
//        if(cur.moveToFirst() == true) return cur.getInt(0);
//        return 0;
//    }

    public void addGridLayoutSave (int id, int[] tags, String name)
    {
        String sid = String.valueOf(id);
        int[] savedTags = tags;
        SQLiteDatabase db = getWritableDatabase();
        String sql = "INSERT INTO GridLayouts VALUES (" + sid + ", " + tags +" ,'" + name + "');";
        db.execSQL(sql);
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

    public void getAllNotes(ArrayList<GridLayoutSave> lst)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id, savedLayout, text FROM GridLayouts;";
        Cursor cur = db.rawQuery(sql,null);
        if(cur.moveToFirst() == true){
            do {
                GridLayoutSave n = new GridLayoutSave();
                n.id = cur.getInt(0);
//                n.savedlayout = cur.getString(1);
                n.Name = cur.getString(2);
                lst.add(n);
            } while (cur.moveToNext() == true);
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}