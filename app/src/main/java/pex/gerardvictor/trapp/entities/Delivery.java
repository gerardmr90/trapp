package pex.gerardvictor.trapp.entities;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gerard on 18/03/17.
 */

public class Delivery {

    private String uid;
    private Courier courier;
    private Receiver receiver;
    private Company company;
    private String date;
    private String state;

    public Delivery() {
    }

    public Delivery(String uid, Courier courier, Receiver receiver, Company company, String date, String state) {
        this.uid = uid;
        this.courier = courier;
        this.receiver = receiver;
        this.company = company;
        this.date = date;
        this.state = state;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Courier getCourier() {
        return courier;
    }

    public void setCourier(Courier courier) {
        this.courier = courier;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("courier", courier);
        result.put("receiver", receiver);
        result.put("company", company);
        result.put("date", date);
        result.put("state", state);

        return result;
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "courier=" + courier +
                ", receiver=" + receiver +
                ", company=" + company +
                ", date='" + date + '\'' +
                ", state='" + state + '\'' +
                '}';
    }

}
