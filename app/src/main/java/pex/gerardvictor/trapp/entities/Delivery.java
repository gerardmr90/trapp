package pex.gerardvictor.trapp.entities;

/**
 * Created by gerard on 18/03/17.
 */

public class Delivery {

    public String company;
    public Receiver receiver;
    public String date;
    public String address;
    public String state;

    public Delivery() {
    }

    public Delivery(String company, Receiver receiver, String date, String address, String state) {
        this.company = company;
        this.receiver = receiver;
        this.date = date;
        this.address = address;
        this.state = state;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "company='" + company + '\'' +
                ", receiver='" + receiver + '\'' +
                ", date='" + date + '\'' +
                ", address='" + address + '\'' +
                ", state='" + state + '\'' +
                '}';
    }


}
