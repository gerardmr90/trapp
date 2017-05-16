package pex.gerardvictor.trapp.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.entities.Company;
import pex.gerardvictor.trapp.entities.Receiver;


public class DeliveryCreatorActivity extends AppCompatActivity {

    private static final String TAG = "DeliveryCreatorActivity";
    private DatabaseReference receivers;
    private DatabaseReference companies;
    private Context context;
    private List<Receiver> receiversList = new ArrayList<>();
    private List<Company> companiesList = new ArrayList<>();
    private List<String> receiversNamesList = new ArrayList<>();
    private List<String> companiesNamesList = new ArrayList<>();
    private Spinner receiversSpinner;
    private Spinner companiesSpinner;

    private Button createDeliveryButton;
    private ChildEventListener receiversChildEventListener;
    private ChildEventListener companiesChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_creator);
        context = getApplicationContext();

        receiversSpinner = (Spinner) findViewById(R.id.receivers_spinner);
        companiesSpinner = (Spinner) findViewById(R.id.companies_spinner);
        createDeliveryButton = (Button) findViewById(R.id.create_delivery_button);

        receivers = FirebaseDatabase.getInstance().getReference("receivers");
        companies = FirebaseDatabase.getInstance().getReference("companies");

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
                createDelivery();
            }
        });
    }

    private void getReceiversEmailFromDatabase() {
        receivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String email = snapshot.child("email").getValue(String.class);
                    receiversNamesList.add(email);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, receiversNamesList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                receiversSpinner.setAdapter(arrayAdapter);
                receiversSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(context, "Selected" + parent.getItemAtPosition(position), Toast.LENGTH_LONG).show();
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
                    String name = snapshot.child("name ").getValue(String.class);
                    companiesNamesList.add(name);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, companiesNamesList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                companiesSpinner.setAdapter(arrayAdapter);
                companiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(context, "Selected " + parent.getItemAtPosition(position), Toast.LENGTH_LONG).show();
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
        addDeliveryToReceiver();
        addDeliveryToCourier();
        addDeliveryToCompany();
    }

    private void addDeliveryToReceiver() {

    }

    private void addDeliveryToCourier() {

    }

    private void addDeliveryToCompany() {

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
