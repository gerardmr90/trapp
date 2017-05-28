package pex.gerardvictor.trapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.api.APIService;
import pex.gerardvictor.trapp.api.APIUtils;
import pex.gerardvictor.trapp.entities.Courier;
import pex.gerardvictor.trapp.entities.Receiver;
import pex.gerardvictor.trapp.entities.SimplifiedCourier;
import pex.gerardvictor.trapp.session.Session;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference database;
    private FirebaseUser user;
    private Session session;

    private Button signUpButton;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText addressEditText;
    private RadioButton personalRadioButton;
    private RadioButton professionalRadioButton;
    private RadioGroup radioGroup;

    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameEditText = (EditText) findViewById(R.id.register_name_editText);
        emailEditText = (EditText) findViewById(R.id.register_email_editText);
        passwordEditText = (EditText) findViewById(R.id.register_password_editText);
        addressEditText = (EditText) findViewById(R.id.register_address_editText);
        signUpButton = (Button) findViewById(R.id.register_sign_up_button);
        personalRadioButton = (RadioButton) findViewById(R.id.personal_radioButton);
        professionalRadioButton = (RadioButton) findViewById(R.id.professional_radioButton);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        session = new Session(this);

        apiService = APIUtils.getAPIService();

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(emailEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {
                            user = task.getResult().getUser();
                            addUserName();
                            registerUser();
                            session.setLoggedIn(true);
                            Intent login = new Intent(RegisterActivity.this, ChooserActivity.class);
                            login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(login);
                            finish();
                        } else {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(RegisterActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addUserName() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nameEditText.getText().toString())
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String name = nameEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError(getString(R.string.empty_name_error));
            valid = false;
        } else {
            nameEditText.setError(null);
        }

        String email = emailEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.empty_email_error));
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.empty_password_error));
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        if (personalRadioButton.isChecked()) {
            String address = addressEditText.getText().toString();
            if (TextUtils.isEmpty(address)) {
                addressEditText.setError(getString(R.string.empty_address_error));
                valid = false;
            } else {
                addressEditText.setError(null);
            }
        }

        return valid;
    }

    private void registerUser() {
        if (professionalRadioButton.isChecked()) {
            writeNewCourier(user.getUid(), createCourier());
        } else {
            writeNewReceiver(user.getUid(), createReceiver());
        }
    }

    private void writeNewCourier(String userID, Courier courier) {
        database = FirebaseDatabase.getInstance().getReference();
        database.child("couriers").child(userID).setValue(courier);
//        APIController.getInstance().saveCourier(courier);
    }

    private void writeNewReceiver(String userID, Receiver receiver) {
        database = FirebaseDatabase.getInstance().getReference();
        database.child("receivers").child(userID).setValue(receiver);
    }

    private Receiver createReceiver() {
        String uid = user.getUid();
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String address = addressEditText.getText().toString();
        return new Receiver(uid, name, email, address);
    }

    private Courier createCourier() {
        String uid = user.getUid();
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();

        apiService.saveCourier(uid, name, email).enqueue(new Callback<SimplifiedCourier>() {
            @Override
            public void onResponse(Call<SimplifiedCourier> call, Response<SimplifiedCourier> response) {
                Log.e(TAG, response.toString());
            }

            @Override
            public void onFailure(Call<SimplifiedCourier> call, Throwable t) {
            }
        });

        return new Courier(uid, name, email);
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
        finish();
    }

}
