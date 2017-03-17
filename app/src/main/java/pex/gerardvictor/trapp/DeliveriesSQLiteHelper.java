package pex.gerardvictor.trapp;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gerard on 17/03/17.
 */

public class DeliveriesSQLiteHelper extends SQLiteOpenHelper {

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlCreate = "CREATE TABLE Deliveries " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " name TEXT, " +
                " address TEXT, " +
                " state TEXT )";

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
