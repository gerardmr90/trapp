package pex.gerardvictor.trapp.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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


public class DeliveryCreatorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "DeliveryCreatorActivity";
    private DatabaseReference receivers;
    private DatabaseReference companies;
    private DatabaseReference database;
    private Context context;
    private List<String> receiversList = new ArrayList<>();
    private List<String> companiesList = new ArrayList<>();
    private Spinner receiversSpinner;
    private Spinner companiesSpinner;
    private Button createDeliveryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_creator);
        context = getApplicationContext();

        receiversSpinner = (Spinner) findViewById(R.id.receivers_spinner);
        companiesSpinner = (Spinner) findViewById(R.id.receivers_spinner);
        createDeliveryButton = (Button) findViewById(R.id.create_delivery_button);

        receivers = FirebaseDatabase.getInstance().getReference("receivers");
        companies = FirebaseDatabase.getInstance().getReference("companies");

        writeNewCompany("4", createCompany());

        ReceiversPopulator receiversPopulator = new ReceiversPopulator();
        CompaniesPopulator companiesPopulator = new CompaniesPopulator();
        receiversPopulator.execute();
        companiesPopulator.execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void populateReceivers() {
        receivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String email = snapshot.child("email").getValue(String.class);
                    receiversList.add(email);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, receiversList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                receiversSpinner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void populateCompanies() {
        companies.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("name").getValue(String.class);
                    companiesList.add(name);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, companiesList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                companiesSpinner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private Company createCompany() {
        String name = "PCcomponentes";
        return new Company(name);
    }

    private void writeNewCompany(String userID, Company company) {
        database = FirebaseDatabase.getInstance().getReference();
        database.child("companies").child(userID).setValue(company);
    }

    private class ReceiversPopulator extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            populateReceivers();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

        }

    }

    private class CompaniesPopulator extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            populateCompanies();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

        }

    }

}
