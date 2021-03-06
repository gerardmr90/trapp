package pex.gerardvictor.trapp.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.api.APIService;
import pex.gerardvictor.trapp.api.APIUtils;
import pex.gerardvictor.trapp.entities.Delivery;
import pex.gerardvictor.trapp.entities.SimplifiedDelivery;
import pex.gerardvictor.trapp.helpers.LocationHelper;
import pex.gerardvictor.trapp.session.Session;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfessionalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "ProfessionalActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final double THRESHOLD = 20.0;

    private DatabaseReference deliveries;
    private DatabaseReference couriers;
    private DatabaseReference courierDeliveries;
    private DatabaseReference receiverDeliveries;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private Context context;
    private Session session;

    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private GoogleMap map;

    private Map<String, Delivery> deliveriesMap = new HashMap<>();
    private Map<String, Location> deliveriesLocationsMap = new HashMap<>();
    private Map<String, Marker> deliveriesLocationsMarkersMap = new HashMap<>();
    private Marker selectedMarker;

    private Button deliverButton;
    private Button showDeliveriesButton;
    private Dialog dialog;

    private APIService apiService;

    private boolean close = false;

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
        deliverButton.setVisibility(View.INVISIBLE);
        showDeliveriesButton = (Button) findViewById(R.id.show_deliveries_button);

        session = new Session(this);

        context = getApplicationContext();

        apiService = APIUtils.getAPIService();

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

        if (checkGooglePlayServices() && user != null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();


            deliveries = FirebaseDatabase.getInstance().getReference("deliveries");
            courierDeliveries = FirebaseDatabase.getInstance().getReference("courier_deliveries").child(user.getUid());
            receiverDeliveries = FirebaseDatabase.getInstance().getReference("receiver_deliveries");
            couriers = FirebaseDatabase.getInstance().getReference("couriers");

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_professional);
            mapFragment.getMapAsync(this);

            final DeliveriesPopulator deliveriesPopulator = new DeliveriesPopulator();
            deliveriesPopulator.execute();

            dialog = createAlertDialog();

            showDeliveriesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (deliveriesMap.size() == 0) {
                        Toast.makeText(context, "There's no deliveries", Toast.LENGTH_LONG).show();
                    } else {
                        MarkerHandler markerHandler = new MarkerHandler();
                        markerHandler.execute(deliveriesMap);
                    }
                }
            });
        }
    }

    private Dialog createAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfessionalActivity.this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
        builder.setTitle("Set as delivered")
                .setMessage("Do you want to set the parcel as delivered?")
                .setPositiveButton(R.string.alert_dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!close) {
                            selectedMarker.remove();
                            String uid = updateFirebaseDatabase();
                            updateResources(uid);
                        } else {
                            Toast.makeText(context, "You have to be closer to delivery drop off", Toast.LENGTH_LONG).show();
                        }
                    }

                }).setNegativeButton(R.string.alert_dialog_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }

    private String updateFirebaseDatabase() {
        String uid = "";
        for (Map.Entry<String, Marker> entry : deliveriesLocationsMarkersMap.entrySet()) {
            if (entry.getValue().equals(selectedMarker)) {
                uid = entry.getKey();
                courierDeliveries.child(uid).child("state").setValue("Delivered");
                deliveries.child(uid).child("state").setValue("Delivered");
                courierDeliveries.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Delivery delivery = dataSnapshot.getValue(Delivery.class);
                        String deliveryUID = delivery.getUid();
                        String receiverUID = delivery.getReceiverUID();
                        receiverDeliveries.child(receiverUID).child(deliveryUID).child("state").setValue("Delivered");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                apiService.setStateToDelivered(uid, "Delivered").enqueue(new Callback<SimplifiedDelivery>() {
                    @Override
                    public void onResponse(Call<SimplifiedDelivery> call, Response<SimplifiedDelivery> response) {

                    }

                    @Override
                    public void onFailure(Call<SimplifiedDelivery> call, Throwable t) {

                    }
                });
            }
        }
        return uid;
    }

    private void updateResources(String key) {
        deliveriesLocationsMarkersMap.remove(key);
        deliveriesLocationsMap.remove(key);
        deliveriesMap.remove(key);
    }

    private Map<String, LatLng> addDeliveriesToHashMap(Map<String, Delivery> deliveriesMap) {
        Map<String, LatLng> map = new HashMap<>();
        for (Map.Entry<String, Delivery> entry : deliveriesMap.entrySet()) {
            if (!entry.getValue().getState().equals("Delivered")) {
                LatLng latLng = LocationHelper.getLocationFromAddress(entry.getValue().getAddress(), context);
                map.put(entry.getKey(), latLng);
            }
        }
        return map;
    }

    private void addMarkerToMap(Map<String, LatLng> hashMap) {
        for (Map.Entry<String, LatLng> entry : hashMap.entrySet()) {
            deliveriesLocationsMarkersMap.put(entry.getKey(), map.addMarker(new MarkerOptions()
                    .position(entry.getValue())));
        }
    }

    private void getDeliveriesFromDatabase() {
        Query query = courierDeliveries.orderByChild("state").startAt("Created").endAt("Created");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                Delivery delivery = dataSnapshot.getValue(Delivery.class);
                deliveriesMap.put(delivery.getUid(), delivery);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                Delivery delivery = dataSnapshot.getValue(Delivery.class);
                deliveriesMap.put(delivery.getUid(), delivery);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                Delivery movedDelivery = dataSnapshot.getValue(Delivery.class);
                deliveriesMap.put(movedDelivery.getUid(), movedDelivery);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getDeliveries:onCancelled", databaseError.toException());
            }
        });
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
                Toast.makeText(ProfessionalActivity.this, "There are no email applications installed.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_about) {

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
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
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
        close = checkClosenessToDeliveryDestination(location);
    }

    private void updateLocation(Location location) {
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();

        couriers.child(user.getUid()).child("latitude").setValue(latitude);
        couriers.child(user.getUid()).child("longitude").setValue(longitude);
    }

    private boolean checkClosenessToDeliveryDestination(Location location) {
        if (deliveriesLocationsMap.size() > 0) {
            for (Map.Entry<String, Location> entry : deliveriesLocationsMap.entrySet()) {
                if (location.distanceTo(entry.getValue()) < THRESHOLD) {
                    return true;
                }
            }
        }
        return false;
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

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                selectedMarker = marker;
                dialog.show();
                return false;
            }
        });
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

    private void getLocationFromDelivery() {
        for (Map.Entry<String, Delivery> entry : deliveriesMap.entrySet()) {
            LatLng lng = LocationHelper.getLocationFromAddress(entry.getValue().getAddress(), context);
            Location location = new Location("");
            location.setLatitude(lng.latitude);
            location.setLongitude(lng.longitude);
            deliveriesLocationsMap.put(entry.getKey(), location);
        }
    }

    private class DeliveriesPopulator extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            getDeliveriesFromDatabase();
            return null;
        }

    }

    private class MarkerHandler extends AsyncTask<Map<String, Delivery>, Void, Map<String, LatLng>> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ProfessionalActivity.this);
            this.dialog.setMessage("Getting deliveries");
            this.dialog.show();
        }


        @Override
        protected Map<String, LatLng> doInBackground(Map<String, Delivery>... params) {
            return addDeliveriesToHashMap(params[0]);
        }

        @Override
        protected void onPostExecute(Map<String, LatLng> map) {
            addMarkerToMap(map);
            getLocationFromDelivery();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

    }

}

