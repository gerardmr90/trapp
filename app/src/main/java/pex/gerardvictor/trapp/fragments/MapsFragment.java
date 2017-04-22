package pex.gerardvictor.trapp.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.entities.Delivery;
import pex.gerardvictor.trapp.ui.DeliveryAdapter;

import static android.content.Context.LOCATION_SERVICE;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.maps_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*Button nextDeliveryButton = (Button) getActivity().findViewById(R.id.next_delivery_button);
        nextDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    nextShipping();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
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
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void goToLastKnownLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

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
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        if (location == null) {
            Toast.makeText(getActivity(), getText(R.string.location_unknown_text), Toast.LENGTH_LONG).show();
        } else {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    /*private List<Delivery> getDataFromDB() {
        DeliveriesSQLiteHelper deliveriesSQLiteHelper = new DeliveriesSQLiteHelper(getActivity());
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
    }*/


    /*private Delivery GetDelivery() {
        try {
            List<Delivery> shippings = getDataFromDB();
            return shippings.get(numShipping++);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }*/

    /*private Delivery getDeliveriesFromDB() {

    }*/

    /*private void nextShipping() throws IOException {
        Delivery nextDelivery = getDeliveriesFromDB();
        if (nextDelivery != null) {
            LatLng position = getLocationFromAddress(nextDelivery.address);
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(nextDelivery.getReceiver().getName()));

            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(position)
                    .zoom(17)
                    .build();

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {
            Toast.makeText(getActivity(), "No hay mas envios", Toast.LENGTH_LONG).show();
        }
    }*/

    public LatLng getLocationFromAddress(String strAddress) throws IOException {

        Geocoder coder = new Geocoder(getActivity());
        List<Address> addresses = coder.getFromLocationName(strAddress, 2);
        Address addressresult = addresses.get(0);
        LatLng result = new LatLng(addressresult.getLatitude(), addressresult.getLongitude());

        return result;
    }

    private void checkGooglePlayServices() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(getActivity());
        if (isAvailable == ConnectionResult.SUCCESS) {

        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(getActivity(), isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(getActivity(), getText(R.string.common_google_play_services_install_text), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkGooglePlayServices();
    }
}
