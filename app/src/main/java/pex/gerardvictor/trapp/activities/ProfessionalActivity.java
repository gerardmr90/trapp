package pex.gerardvictor.trapp.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.location.LocationRequest;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.entities.Delivery;
import pex.gerardvictor.trapp.session.Session;

public class ProfessionalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "ProfessionalActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private DatabaseReference deliveries;
    private DatabaseReference couriers;
    private ChildEventListener deliveriesChildEventListener;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private Context context;
    private Session session;

    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private GoogleMap map;

    private List<Delivery> deliveriesList = new ArrayList<>();

    private Button deliverButton;
    private Button showDeliveriesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        deliverButton = (Button) findViewById(R.id.deliver_button);
        showDeliveriesButton = (Button) findViewById(R.id.show_deliveries_button);

        deliveries = FirebaseDatabase.getInstance().getReference("deliveries");
        couriers = FirebaseDatabase.getInstance().getReference("couriers");

        session = new Session(this);

        context = getApplicationContext();

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
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
                    .findFragmentById(R.id.map_professional);
            mapFragment.getMapAsync(this);

            DeliveriesPopulator deliveriesPopulator = new DeliveriesPopulator();
            deliveriesPopulator.execute();

            deliverButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            showDeliveriesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (deliveriesList.size() == 0) {
                        Toast.makeText(context, "There's no deliveries", Toast.LENGTH_LONG).show();
                    } else {
                        addDeliveriesToMap(deliveriesList);
                    }
                }
            });
        }
    }

    private void addDeliveriesToMap(List<Delivery> list) {
        for (Delivery delivery : list) {
            if (!delivery.getState().equals("delivered")) {
                LatLng latLng = getLocationFromAddress(delivery.getAddress());
                map.addMarker(new MarkerOptions().position(latLng));
            }
        }
    }

    private void getDeliveriesFromDatabase() {
        deliveriesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                Delivery delivery = dataSnapshot.getValue(Delivery.class);
                deliveriesList.add(delivery);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                String deliveryKey = dataSnapshot.getKey();
                Delivery delivery = dataSnapshot.getValue(Delivery.class);
                deliveriesList.add(delivery);

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
                deliveriesList.add(movedDelivery);
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

        if (id == R.id.nav_new_delivery) {
            Intent delivery = new Intent(ProfessionalActivity.this, DeliveryCreatorActivity.class);
            startActivity(delivery);
        }
        if (id == R.id.nav_history) {
            Intent history = new Intent(ProfessionalActivity.this, HistoryActivity.class);
            startActivity(history);
        } else if (id == R.id.nav_settings) {
            Intent settings = new Intent(ProfessionalActivity.this, SettingsActivity.class);
            startActivity(settings);
        } else if (id == R.id.nav_logout) {
            session.setLoggedIn(false);
            firebaseAuth.signOut();
            finish();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_n) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation(location);
    }

    private void updateLocation(Location location) {
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();

        couriers.child(user.getUid()).child("latitude").setValue(latitude);
        couriers.child(user.getUid()).child("longitude").setValue(longitude);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (map != null) {
            // Access to the location has been granted to the app.
            map.setMyLocationEnabled(true);
        }
    }

    public LatLng getLocationFromAddress(String address) {
        Geocoder coder = new Geocoder(context);
        List<Address> addressList;
        LatLng latLng = null;

        try {
            addressList = coder.getFromLocationName(address, 5);
            if (address == null) {
                return null;
            }
            Address location = addressList.get(0);
            location.getLatitude();
            location.getLongitude();

            latLng = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return latLng;
    }

    private class DeliveriesPopulator extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            getDeliveriesFromDatabase();
            return null;
        }
    }

}

