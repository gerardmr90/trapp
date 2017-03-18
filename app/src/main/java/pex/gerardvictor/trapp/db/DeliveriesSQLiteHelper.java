package pex.gerardvictor.trapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.R.attr.version;

/**
 * Created by gerard on 17/03/17.
 */

public class DeliveriesSQLiteHelper extends SQLiteOpenHelper {

    private static final String CREATE_TABLE = "CREATE TABLE Deliveries " +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " company TEXT" +
            " receiver TEXT, " +
            " address TEXT, " +
            " date TEXT" +
            " state TEXT )";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS Deliveries";

    public DeliveriesSQLiteHelper(Context context) {
        super(context, "DeliveriesDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE);
        } catch (SQLException e) {
            Log.e("SQL Create", "Could not create SQL table");
        }

        ContentValues amazon = new ContentValues();
        ContentValues ebay = new ContentValues();
        ContentValues currys = new ContentValues();

        amazon.put("company", "Amazon");
        amazon.put("receiver", "Gerard");
        amazon.put("address", "12, Pollard St");
        amazon.put("date", "18/1/2017");
        amazon.put("state", "Delivered");

        ebay.put("company", "Ebay");
        ebay.put("receiver", "Gerard");
        ebay.put("address", "12, Pollard St");
        ebay.put("date", "22/2/2017");
        ebay.put("state", "Delivered");

        currys.put("company", "Currys");
        currys.put("receiver", "Gerard");
        currys.put("address", "12, Pollard St");
        currys.put("date", "3/13/2017");
        currys.put("state", "Delivered");

        db.insert("Deliveries", null, amazon);
        db.insert("Deliveries", null, ebay);
        db.insert("Deliveries", null, currys);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(DROP_TABLE);
            onCreate(db);
        } catch (SQLException e) {
            Log.e("SQL Update", "Could not update SQL table");
        }

    }
}
