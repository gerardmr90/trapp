package pex.gerardvictor.trapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.session.Session;

public class DeliveryCreatorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "DeliveryCreatorActivity";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_creator);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
