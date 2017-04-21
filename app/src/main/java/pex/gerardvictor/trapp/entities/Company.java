package pex.gerardvictor.trapp.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gerard on 21/04/17.
 */

public class Company {

    private String name;
    private List<Courier> couriers;
    private List<Delivery> deliveries;

    public Company() {
    }

    public Company(String name, List<Courier> couriers, List<Delivery> deliveries) {
        this.name = name;
        couriers = new ArrayList<>();
        deliveries = new ArrayList<>();
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

    @Override
    public String toString() {
        return "Company{" +
                "name='" + name + '\'' +
                ", couriers=" + couriers +
                ", deliveries=" + deliveries +
                '}';
    }
}
