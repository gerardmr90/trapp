package pex.gerardvictor.trapp.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.db.DeliveriesSQLiteHelper;
import pex.gerardvictor.trapp.delivery.Delivery;
import pex.gerardvictor.trapp.delivery.DeliveryAdapter;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Delivery> deliveryList;
    private DeliveryAdapter deliveryAdapter;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        deliveryAdapter = new DeliveryAdapter(getDataFromDB());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(deliveryAdapter);
    }


    private List<Delivery> getDataFromDB() {
        DeliveriesSQLiteHelper deliveriesSQLiteHelper = new DeliveriesSQLiteHelper(this);
        database = deliveriesSQLiteHelper.getReadableDatabase();
        deliveryList = new ArrayList<>();

        String query = "SELECT * FROM Deliveries";
        Cursor cursor = database.rawQuery(query, null);
        while (cursor.moveToNext()) {
            int companyIndex = cursor.getColumnIndex("company");
            int receiverIndex = cursor.getColumnIndex("receiver");
            int addressIndex = cursor.getColumnIndex("address");
            int dateIndex = cursor.getColumnIndex("date");
            int stateIndex = cursor.getColumnIndex("state");

            String company = cursor.getString(companyIndex);
            String receiver = cursor.getString(receiverIndex);
            String address = cursor.getString(addressIndex);
            String date = cursor.getString(dateIndex);
            String state = cursor.getString(stateIndex);

            Delivery delivery = new Delivery(company, receiver, address, date, state);
            deliveryList.add(delivery);
            Toast.makeText(this, delivery.toString(), Toast.LENGTH_LONG).show();
        }
        return deliveryList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }
}
