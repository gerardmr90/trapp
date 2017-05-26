package pex.gerardvictor.trapp.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.entities.Delivery;
import pex.gerardvictor.trapp.ui.DeliveryAdapter;
import pex.gerardvictor.trapp.ui.DividerItemDecoration;

public class HistoryActivity extends AppCompatActivity {

    private final static String TAG = "HistoryActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private DatabaseReference database;
    private DatabaseReference receivers;
    private ChildEventListener childEventListener;

    private RecyclerView recyclerView;
    private List<Delivery> deliveryList = new ArrayList<>();
    private DeliveryAdapter deliveryAdapter;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        deliveryAdapter = new DeliveryAdapter(deliveryList);

        firebaseAuth = FirebaseAuth.getInstance();

        receivers = FirebaseDatabase.getInstance().getReference("receivers");
        database = FirebaseDatabase.getInstance().getReference("");

        context = getApplicationContext();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    receivers.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                database = FirebaseDatabase.getInstance().getReference("receiver_deliveries").child(user.getUid());
                                HistoryPopulator populator = new HistoryPopulator();
                                populator.execute();
                            } else {
                                database = FirebaseDatabase.getInstance().getReference("courier_deliveries").child(user.getUid());
                                HistoryPopulator populator = new HistoryPopulator();
                                populator.execute();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    finish();
                }
            }
        };
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
                deliveryList.add(delivery);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                Delivery movedDelivery = dataSnapshot.getValue(Delivery.class);
                deliveryList.add(movedDelivery);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postDeliveries:onCancelled", databaseError.toException());
            }
        };
        database.addChildEventListener(childEventListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        if (database != null) {
            database.removeEventListener(childEventListener);
        }
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
