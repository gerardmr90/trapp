package pex.gerardvictor.trapp.entities;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by gerard on 28/05/17.
 */

public class DeliveryMarker implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;

    public DeliveryMarker(double lat, double lng) {
        position = new LatLng(lat, lng);
    }

    public DeliveryMarker(LatLng position, String title, String snippet) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }
}
