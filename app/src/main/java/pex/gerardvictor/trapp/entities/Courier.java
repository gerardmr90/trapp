package pex.gerardvictor.trapp.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gerard on 21/04/17.
 */

public class Courier {

    private String name;
    private String email;
    private List<Delivery> deliveries;

    public Courier() {
    }

    public Courier(String name, String email) {
        this.name = name;
        this.email = email;
        deliveries = new ArrayList<>();
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

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<Delivery> deliveries) {
        this.deliveries = deliveries;
    }

    @Override
    public String toString() {
        return "Courier{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", deliveries=" + deliveries +
                '}';
    }
}
