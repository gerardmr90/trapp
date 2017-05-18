package pex.gerardvictor.trapp.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.entities.Delivery;
import pex.gerardvictor.trapp.ui.DeliveryAdapter;
import pex.gerardvictor.trapp.ui.DividerItemDecoration;

public class HistoryActivity extends AppCompatActivity {

    private final static String TAG = "HistoryActivity";
    private RecyclerView recyclerView;
    private List<Delivery> deliveryList = new ArrayList<>();
    private DeliveryAdapter deliveryAdapter;
    private DatabaseReference database;
    private Context context;
    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        database = FirebaseDatabase.getInstance().getReference("deliveries");

        context = getApplicationContext();

        HistoryPopulator populator = new HistoryPopulator();
        populator.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        database.removeEventListener(childEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showHistory() {
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                Delivery delivery = dataSnapshot.getValue(Delivery.class);
                deliveryList.add(delivery);
                deliveryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                Delivery delivery = dataSnapshot.getValue(Delivery.class);
                String deliveryKey = dataSnapshot.getKey();
                deliveryList.add(delivery);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
                String deliveryKey = dataSnapshot.getKey();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                Delivery movedDelivery = dataSnapshot.getValue(Delivery.class);
                String deliveryKey = dataSnapshot.getKey();
                deliveryList.add(movedDelivery);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postDeliveries:onCancelled", databaseError.toException());
                Toast.makeText(context, "Failed to load deliveries.",
                        Toast.LENGTH_SHORT).show();

            }
        };
        database.addChildEventListener(childEventListener);
    }

    private class HistoryPopulator extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            showHistory();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            deliveryAdapter = new DeliveryAdapter(deliveryList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setHasFixedSize(true);
            recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(deliveryAdapter);
        }

    }

}
