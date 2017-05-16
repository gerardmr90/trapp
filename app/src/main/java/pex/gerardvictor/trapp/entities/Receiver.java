package pex.gerardvictor.trapp.entities;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gerard on 21/04/17.
 */

public class Receiver {

    private String uid;
    private String name;
    private String email;
    private String address;
    private List<Delivery> deliveries;

    public Receiver() {
    }

    public Receiver(String uid, String name, String email, String address) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.address = address;
        this.deliveries = new ArrayList<>();
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<Delivery> deliveries) {
        this.deliveries = deliveries;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("email", email);
        result.put("address", address);
        result.put("deliveries", deliveries);

        return result;
    }

    @Override
    public String toString() {
        return "Receiver{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", deliveries=" + deliveries +
                '}';
    }
}
