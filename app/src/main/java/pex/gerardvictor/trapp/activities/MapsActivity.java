package pex.gerardvictor.trapp.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.db.DeliveriesSQLiteHelper;
import pex.gerardvictor.trapp.delivery.Delivery;
import pex.gerardvictor.trapp.ui.DeliveryAdapter;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    Button nextDeliveryButton;
    private GoogleMap mMap;
    private boolean mPermissionDenied = false;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private RecyclerView recyclerView;
    private List<Delivery> deliveryList;
    private DeliveryAdapter deliveryAdapter;
    private SQLiteDatabase database;
    private Integer numShipping = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        nextDeliveryButton = (Button) findViewById(R.id.nextDeliveryBtn);
        nextDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    nextShipping();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, getText(R.string.common_google_play_services_install_text), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            try {
                nextShipping();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "No se ha podido obtener el siguiente envio", Toast.LENGTH_LONG).show();
            }

            return;
        }*/
        enableMyLocation();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            mPermissionDenied = false;
        }
    }

    /*private void goToLastKnownLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        try {
            Location myLocation = locationManager.getLastKnownLocation(provider);
            double latitude = myLocation.getLatitude();
            double longitude = myLocation.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(getString(R.string.last_known_positon_text)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        } catch (SecurityException e) {

        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.history:
                Intent history = new Intent(MapsActivity.this, HistoryActivity.class);
                startActivity(history);
                break;
            case R.id.settings:
                Intent settings = new Intent(MapsActivity.this, SettingsActivity.class);
                startActivity(settings);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
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

    private List<Delivery> getDataFromDB() {
        DeliveriesSQLiteHelper deliveriesSQLiteHelper = new DeliveriesSQLiteHelper(this);
        database = deliveriesSQLiteHelper.getReadableDatabase();
        deliveryList = new ArrayList<>();

        String query = "SELECT * FROM Deliveries";
        Cursor cursor = database.rawQuery(query, null);
        while (cursor.moveToNext()) {
            int companyIndex = cursor.getColumnIndex("company");
            int receiverIndex = cursor.getColumnIndex("receiver");
            int addressIndex = cursor.getColumnIndex("address");
            int dateIndex = cursor.getColumnIndex("date");
            int stateIndex = cursor.getColumnIndex("state");

            String company = cursor.getString(companyIndex);
            String receiver = cursor.getString(receiverIndex);
            String address = cursor.getString(addressIndex);
            String date = cursor.getString(dateIndex);
            String state = cursor.getString(stateIndex);

            Delivery delivery = new Delivery(company, receiver, address, date, state);
            deliveryList.add(delivery);
        }
        return deliveryList;
    }


    private Delivery GetDelivery() {
        try {
            List<Delivery> shippings = getDataFromDB();
            return shippings.get(numShipping++);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private void nextShipping() throws IOException {
        Delivery nextDelivery = GetDelivery();
        if (nextDelivery != null) {
            LatLng position = getLocationFromAddress(nextDelivery.address);
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(nextDelivery.receiver));

            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(position)
                    .zoom(17)
                    .build();

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {
            Toast.makeText(this, "No hay mas envios", Toast.LENGTH_LONG).show();
        }
    }

    public LatLng getLocationFromAddress(String strAddress) throws IOException {

        Geocoder coder = new Geocoder(this);
        List<Address> addresses = coder.getFromLocationName(strAddress, 2);
        Address addressresult = addresses.get(0);
        LatLng result = new LatLng(addressresult.getLatitude(), addressresult.getLongitude());

        return result;
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

}
