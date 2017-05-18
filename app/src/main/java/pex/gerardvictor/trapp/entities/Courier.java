package pex.gerardvictor.trapp.entities;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gerard on 21/04/17.
 */

public class Courier {

    private String uid;
    private String name;
    private String email;
    private Double latitude;
    private Double longitude;

    public Courier() {
    }

    public Courier(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public Courier(String uid, String name, String email, Double latitude, Double longitude) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("email", email);
        result.put("latitude", latitude);
        result.put("longitude", longitude);

        return result;
    }

    @Override
    public String toString() {
        return "Courier{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

}
