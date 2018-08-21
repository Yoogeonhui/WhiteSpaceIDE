package org.dimigo.whitespaceide;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBManager {
    public static final String DATABASE_NAME = "white.db";
    public static final String TABLE_NAME = "WHITE";
    public static final int DATABASE_VERSION = 1;
    SQLiteDatabase database;

    private static DBManager manager;
    private Context context;

    private DBManager(Context context){
        this.context = context;
    }

    public boolean execSQL(String sql){
        try{
            database.execSQL(sql);
            return true;
        }catch(Exception e){
            Log.e("TAG", e.toString());
        }
        return false;
    }

    public long insertSQL(ContentValues contentValues){
        try{
            long res = database.insert(TABLE_NAME,"", contentValues);
            return res;
        }catch(Exception e){
            Log.e("TAG", e.toString());
        }
        return -1;
    }

    public void open(){
        MemoDBHelper helper = new MemoDBHelper(context);
        database = helper.getWritableDatabase();
    }

    public void close(){
        if(database!=null){
            database.close();
        }
    }

    public Cursor rawQuery(String sql){
        return database.rawQuery(sql, null);
    }

    public static DBManager getInstance(Context context){
        if(manager==null){
            manager = new DBManager(context);
        }
        return manager;
    }

    private class MemoDBHelper extends SQLiteOpenHelper {

        public MemoDBHelper(Context context){
            super(context, DATABASE_NAME,null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            Log.d("TAG", "onCreate() 호출");
            
            String sql = "CREATE TABLE "+TABLE_NAME+"("
                    + " _id integer PRIMARY KEY autoincrement, "
                    + "title text, "
                    + "code text)";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //테이블 삭제하고 재생성하는 로직을 넣어야 함.
            Log.d("TAG", "onUpgrade() 호출");
            String sql = "DROP TABLE IF EXISTS "+TABLE_NAME;
            db.execSQL(sql);
            onCreate(db);
        }
    }

}
