package pex.gerardvictor.trapp.helpers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by gerard on 25/05/17.
 */

public class LocationHelper {

    public static LatLng getLocationFromAddress(String address, Context context) {
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
}
