package com.young2000.kyboard2;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ChineseDbHelper extends SQLiteOpenHelper {
    // private static final int DATABASE_VERSION = 1;
    private boolean path_flag;


    private static final String DATABASE_NAME = "chinese_word.db";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase myDataBase;
    private final Context context;

    public ChineseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("BiblePersonalDbHelper", "들어오나");
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("BPDH onCreate", "들어오나");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("onUpgrade", "들어오나");
    }

    public SQLiteDatabase openDataBase() throws SQLException{
        Log.d("openDataBase", "들어오나");

        // Path to the app's data directory where the database will be copied
        String dataPath = context.getApplicationInfo().dataDir + "/files/";

        // Path to the copied database file
        String myPath = dataPath + DATABASE_NAME; // Assuming DATABASE_NAME is the name of your database file

        // Check if the database already exists in the app's data directory
        if (!checkDataBase(myPath)) {
            try {
                // If the database doesn't exist, copy it from the assets folder
                copyDatabase(myPath);
            } catch (IOException e) {
                Log.e("DBCreator", "Error copying database: " + e.getMessage());
            }
        }

        //Open the database
        try {
            myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            Log.d("DBCreator", "open " + myPath);
            return myDataBase;
        }
        catch(SQLException ex)	{
            Log.d("DBCreator", "open exception " +ex.getMessage());
            return null;
        }
    }

    private boolean checkDataBase(String path) {
        File dbFile = new File(path);
        return dbFile.exists();
    }

    private void copyDatabase(String destinationPath) throws IOException {
        InputStream inputStream = context.getAssets().open(DATABASE_NAME);
        OutputStream outputStream = new FileOutputStream(destinationPath);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        Log.d("getHighlight", "copy done");
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public ArrayList<String> getWords (String searchStr) {
        Log.d("getHighlight", "들어오나");
        myDataBase = openDataBase();
        if (myDataBase==null) {
            return null;
        }

        String queryString = "SELECT keyword FROM pronunciation WHERE LOWER(pronunciation) = ?";
        Cursor cursor = myDataBase.rawQuery(queryString, new String[]{searchStr.toLowerCase()});

        cursor.moveToFirst();

        ArrayList<String> Words = new ArrayList<>();

        int AA = cursor.getCount();
        Log.d("test", "return cursor count is " + AA);
        if  (AA!=0){
            while (!cursor.isAfterLast()){
                Words.add(cursor.getString(0));
                cursor.moveToNext();
            }
            cursor.close();
            myDataBase.close();
            return Words;
        } else {
            cursor.close();
            myDataBase.close();
            return null;
        }

    }
}
