package pex.gerardvictor.trapp.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.entities.Courier;
import pex.gerardvictor.trapp.entities.Delivery;
import pex.gerardvictor.trapp.session.Session;

public class PersonalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "PersonalActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private DatabaseReference deliveries;
    private ChildEventListener deliveriesChildEventListener;
    private DatabaseReference couriers;
    private ChildEventListener couriersChildEventListener;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private Context context;
    private Session session;

    private GoogleApiClient googleApiClient;
    private GoogleMap map;

    private Map<String, Delivery> deliveriesMap = new HashMap<>();
    private Map<String, Courier> couriersMap = new HashMap<>();

    private Button findParcelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        findParcelButton = (Button) findViewById(R.id.find_parcel_button);

        context = getApplicationContext();

        session = new Session(this);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Log.d(TAG, "Firebase Token:" + FirebaseInstanceId.getInstance().getToken());
                    View navigationDrawerView = navigationView.getHeaderView(0);
                    TextView userEmail = (TextView) navigationDrawerView.findViewById(R.id.user_email_textView);
                    TextView userName = (TextView) navigationDrawerView.findViewById(R.id.user_name_textView);
                    ImageView imageView = (ImageView) navigationDrawerView.findViewById(R.id.imageView);
                    imageView.setVisibility(View.VISIBLE);
                    userEmail.setText(user.getEmail());
                    userName.setText(user.getDisplayName());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    finish();
                }
            }
        };

        if (checkGooglePlayServices()) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_personal);
            mapFragment.getMapAsync(this);

            couriers = FirebaseDatabase.getInstance().getReference("couriers");
            deliveries = FirebaseDatabase.getInstance().getReference("deliveries");

            DeliveriesPopulator deliveriesPopulator = new DeliveriesPopulator();
            deliveriesPopulator.execute();

            findParcelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (couriersMap.size() == 0) {
                        Toast.makeText(context, "All your parcels have been delivered", Toast.LENGTH_LONG).show();
                    } else {
                        addParcelLocationToMap(couriersMap);
                    }
                }
            });
        }
    }

    private void addParcelLocationToMap(Map<String, Courier> hashMap) {
        for (Map.Entry<String, Courier> entry : hashMap.entrySet()) {
            LatLng latLng = new LatLng(entry.getValue().getLatitude(), entry.getValue().getLongitude());
            map.addMarker(new MarkerOptions()
                    .position(latLng));
        }
    }

    private void getCouriersFromDatabase() {
        couriersMap.clear();
        couriersChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                Courier courier = dataSnapshot.getValue(Courier.class);
                if (deliveriesMap.containsKey(courier.getUid())) {
                    couriersMap.put(courier.getUid(), courier);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                Courier courier = dataSnapshot.getValue(Courier.class);
                if (deliveriesMap.containsKey(courier.getUid())) {
                    couriersMap.put(courier.getUid(), courier);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                Courier movedCourier = dataSnapshot.getValue(Courier.class);
                if (deliveriesMap.containsKey(movedCourier.getUid())) {
                    couriersMap.put(movedCourier.getUid(), movedCourier);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getCoriers:onCancelled", databaseError.toException());
                Toast.makeText(context, "Failed to get Couriers.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        couriers.addChildEventListener(couriersChildEventListener);
    }

    private void getDeliveriesFromDatabase() {
        deliveriesMap.clear();
        deliveriesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                Delivery delivery = dataSnapshot.getValue(Delivery.class);
                if (!delivery.getState().equals("Delivered")) {
                    deliveriesMap.put(delivery.getCourierUID(), delivery);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                Delivery delivery = dataSnapshot.getValue(Delivery.class);
                if (!delivery.getState().equals("Delivered")) {
                    deliveriesMap.put(delivery.getCourierUID(), delivery);
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                Delivery movedDelivery = dataSnapshot.getValue(Delivery.class);
                if (!movedDelivery.getState().equals("Delivered")) {
                    deliveriesMap.put(movedDelivery.getCourierUID(), movedDelivery);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getDeliveries:onCancelled", databaseError.toException());
                Toast.makeText(context, "Failed to get deliveries.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        deliveries.addChildEventListener(deliveriesChildEventListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            Intent history = new Intent(PersonalActivity.this, HistoryActivity.class);
            startActivity(history);
        } else if (id == R.id.nav_settings) {
            Intent settings = new Intent(PersonalActivity.this, SettingsActivity.class);
            startActivity(settings);
        } else if (id == R.id.nav_logout) {
            session.setLoggedIn(false);
            firebaseAuth.signOut();
            finish();
        } else if (id == R.id.nav_share) {
            String url = "https://gitlab.com/gerardmr90/PEX_Trapp";
            Intent browser = new Intent(Intent.ACTION_VIEW);
            browser.setData(Uri.parse(url));
            startActivity(browser);
        } else if (id == R.id.nav_send) {
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setType("text/plain");
            email.putExtra(Intent.EXTRA_EMAIL, "feedback@trapp.com");
            try {
                startActivity(email);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(PersonalActivity.this, "There are no email applications installed.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMyLocationButtonClickListener(this);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            map.setMyLocationEnabled(true);
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) throws SecurityException {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    map.setMyLocationEnabled(true);
                } else {
                    Toast.makeText(this, "Permission was denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
            return false;
        } else {
            Toast.makeText(this, getText(R.string.common_google_play_services_install_text), Toast.LENGTH_LONG).show();
            return false;
        }
    }


    public class DeliveriesPopulator extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            getDeliveriesFromDatabase();
            getCouriersFromDatabase();
            return null;
        }

    }

}

