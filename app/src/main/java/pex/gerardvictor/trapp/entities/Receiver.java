package pex.gerardvictor.trapp.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gerard on 21/04/17.
 */

public class Receiver {

    private String name;
    private String email;
    private String address;
    private List<Delivery> deliveries;

    public Receiver() {
    }

    public Receiver(String name, String email, String address) {
        this.name = name;
        this.email = email;
        this.address = address;
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
