package pex.gerardvictor.trapp.entities;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gerard on 21/04/17.
 */

public class Company {

    private String uid;
    private String name;
    private List<Courier> couriers;
    private List<Delivery> deliveries;

    public Company() {
    }

    public Company(String name) {
        this.name = name;
    }

    public Company(String uid, String name) {
        this.uid = uid;
        this.name = name;
        this.couriers = new ArrayList<>();
        this.deliveries = new ArrayList<>();
    }

    public Company(String uid, String name, List<Courier> couriers, List<Delivery> deliveries) {
        this.uid = uid;
        this.name = name;
        this.couriers = couriers;
        this.deliveries = deliveries;
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

    public List<Courier> getCouriers() {
        return couriers;
    }

    public void setCouriers(List<Courier> couriers) {
        this.couriers = couriers;
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
        result.put("couriers", couriers);
        result.put("deliveries", deliveries);

        return result;
    }

    @Override
    public String toString() {
        return "Company{" +
                "name='" + name + '\'' +
                ", couriers=" + couriers +
                ", deliveries=" + deliveries +
                '}';
    }
}
