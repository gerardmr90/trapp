package pex.gerardvictor.trapp.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.entities.Company;
import pex.gerardvictor.trapp.entities.Courier;
import pex.gerardvictor.trapp.entities.Delivery;
import pex.gerardvictor.trapp.entities.Receiver;


public class DeliveryCreatorActivity extends AppCompatActivity {

    private static final String TAG = "DeliveryCreatorActivity";
    private static final String state = "Created";

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private DatabaseReference receivers;
    private DatabaseReference companies;
    private DatabaseReference couriers;
    private DatabaseReference database;
    private ChildEventListener receiversChildEventListener;
    private ChildEventListener companiesChildEventListener;

    private Context context;

    private List<Receiver> receiversList = new ArrayList<>();
    private List<Company> companiesList = new ArrayList<>();
    private List<String> receiversEmailsList = new ArrayList<>();
    private List<String> companiesNamesList = new ArrayList<>();

    private Spinner receiversSpinner;
    private Spinner companiesSpinner;
    private Button createDeliveryButton;
    private EditText dateEditText;

    private String receiverEmail;
    private String companyName;
    private Receiver receiver;
    private Company company;
    private Courier courier;
    private String date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_creator);
        context = getApplicationContext();

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    finish();
                }
            }
        };

        receiversSpinner = (Spinner) findViewById(R.id.receivers_spinner);
        companiesSpinner = (Spinner) findViewById(R.id.companies_spinner);
        createDeliveryButton = (Button) findViewById(R.id.create_delivery_button);
        dateEditText = (EditText) findViewById(R.id.date_editText);

        database = FirebaseDatabase.getInstance().getReference();
        receivers = FirebaseDatabase.getInstance().getReference("receivers");
        companies = FirebaseDatabase.getInstance().getReference("companies");
        couriers = FirebaseDatabase.getInstance().getReference("couriers");

        ReceiversPopulator receiversPopulator = new ReceiversPopulator();
        ReceiversEmailPopulator receiversEmailPopulator = new ReceiversEmailPopulator();
        CompaniesPopulator companiesPopulator = new CompaniesPopulator();
        CompaniesNamePopulator companiesNamePopulator = new CompaniesNamePopulator();
        receiversPopulator.execute();
        receiversEmailPopulator.execute();
        companiesPopulator.execute();
        companiesNamePopulator.execute();

        createDeliveryButton = (Button) findViewById(R.id.create_delivery_button);
        createDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    createDelivery();
                    Toast.makeText(context, getString(R.string.delivery_created), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    private void getReceiversEmailFromDatabase() {
        receivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String email = snapshot.child("email").getValue(String.class);
                    receiversEmailsList.add(email);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, receiversEmailsList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                receiversSpinner.setAdapter(arrayAdapter);
                receiversSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        receiverEmail = parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCompaniesNameFromDatabase() {
        companies.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("name").getValue(String.class);
                    companiesNamesList.add(name);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, companiesNamesList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                companiesSpinner.setAdapter(arrayAdapter);
                companiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        companyName = parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getReceiversFromFirebase() {
        receiversChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                Receiver receiver = dataSnapshot.getValue(Receiver.class);
                receiversList.add(receiver);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                Receiver receiver = dataSnapshot.getValue(Receiver.class);
                String receiverKey = dataSnapshot.getKey();
                receiversList.add(receiver);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
                String receiverKey = dataSnapshot.getKey();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                Receiver movedReceiver = dataSnapshot.getValue(Receiver.class);
                String receiverKey = dataSnapshot.getKey();
                receiversList.add(movedReceiver);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getReceivers:onCancelled", databaseError.toException());
                Toast.makeText(context, "Failed to get receivers.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        receivers.addChildEventListener(receiversChildEventListener);
    }

    private void getCompaniesFromFirebase() {
        companiesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                Company company = dataSnapshot.getValue(Company.class);
                companiesList.add(company);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                Company company = dataSnapshot.getValue(Company.class);
                String companyKey = dataSnapshot.getKey();
                companiesList.add(company);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
                String companyKey = dataSnapshot.getKey();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                Company movedCompany = dataSnapshot.getValue(Company.class);
                String companyKey = dataSnapshot.getKey();
                companiesList.add(movedCompany);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getCompanies:onCancelled", databaseError.toException());
                Toast.makeText(context, "Failed to get companies.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        companies.addChildEventListener(companiesChildEventListener);
    }

    private void createDelivery() {
        flushDatabase(writeDelivery());
    }

    private Delivery writeDelivery() {
        String key = database.child("deliveries").push().getKey();
        receiver = searchForReceiver();
        company = searchForCompany();
        date = dateEditText.getText().toString();
        courier = new Courier(user.getUid(), user.getDisplayName(), user.getEmail());
        Delivery delivery = new Delivery(key, courier, receiver, company, date, state);
        Map<String, Object> postValues = delivery.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/deliveries/" + key, postValues);
        childUpdates.put("/couriers/" + courier.getUid() + "/" + key, postValues);
        childUpdates.put("/receivers/" + receiver.getUid() + "/" + key, postValues);

        database.updateChildren(childUpdates);

        return delivery;
    }

    private Receiver searchForReceiver() {
        Iterator iterator = receiversList.iterator();
        while (iterator.hasNext()) {
            Receiver recv = (Receiver) iterator.next();
            if (recv.getEmail().equals(receiverEmail)) {
                return recv;
            }
        }
        return null;
    }

    private Company searchForCompany() {
        Iterator iterator = companiesList.iterator();
        while (iterator.hasNext()) {
            Company comp = (Company) iterator.next();
            if (comp.getName().equals(companyName)) {
                return comp;
            }
        }
        return null;
    }

    private void flushDatabase(Delivery delivery) {
        couriers.child(user.getUid()).child(delivery.getUid()).child("courier").removeValue();
        receivers.child(receiver.getUid()).child(delivery.getUid()).child("receiver").removeValue();
    }

    private boolean validateForm() {
        boolean valid = true;

        String date = dateEditText.getText().toString();
        if (TextUtils.isEmpty(date)) {
            dateEditText.setError(getString(R.string.empty_date_error));
            valid = false;
        } else {
            dateEditText.setError(null);
        }

        return valid;
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    private class ReceiversPopulator extends AsyncTask {

        @Override
        protected List<String> doInBackground(Object[] params) {
            getReceiversFromFirebase();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

        }

    }

    private class CompaniesPopulator extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            getCompaniesFromFirebase();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
        }

    }

    private class ReceiversEmailPopulator extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            getReceiversEmailFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

        }

    }

    private class CompaniesNamePopulator extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            getCompaniesNameFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

        }

    }

}
