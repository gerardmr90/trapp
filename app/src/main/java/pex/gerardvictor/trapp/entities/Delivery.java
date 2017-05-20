package pex.gerardvictor.trapp.entities;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gerard on 18/03/17.
 */

public class Delivery {

    private String uid;
    private String courierUID;
    private String receiverUID;
    private String companyUID;
    private String companyName;
    private String address;
    private String date;
    private String state;

    public Delivery() {
    }

    public Delivery(String uid, String courierUID, String receiverUID, String companyUID, String companyName, String address, String date, String state) {
        this.uid = uid;
        this.courierUID = courierUID;
        this.receiverUID = receiverUID;
        this.companyUID = companyUID;
        this.companyName = companyName;
        this.address = address;
        this.date = date;
        this.state = state;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCourierUID() {
        return courierUID;
    }

    public void setCourierUID(String courierUID) {
        this.courierUID = courierUID;
    }

    public String getReceiverUID() {
        return receiverUID;
    }

    public void setReceiverUID(String receiverUID) {
        this.receiverUID = receiverUID;
    }

    public String getCompanyUID() {
        return companyUID;
    }

    public void setCompanyUID(String companyUID) {
        this.companyUID = companyUID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
        result.put("courierUID", courierUID);
        result.put("receiverUID", receiverUID);
        result.put("companyUID", companyUID);
        result.put("companyName", companyName);
        result.put("address", address);
        result.put("date", date);
        result.put("state", state);

        return result;
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "uid='" + uid + '\'' +
                ", courierUID='" + courierUID + '\'' +
                ", receiverUID='" + receiverUID + '\'' +
                ", companyUID='" + companyUID + '\'' +
                ", companyName='" + companyName + '\'' +
                ", address='" + address + '\'' +
                ", date='" + date + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
