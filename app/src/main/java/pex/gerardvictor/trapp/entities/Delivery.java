package pex.gerardvictor.trapp.entities;

/**
 * Created by gerard on 18/03/17.
 */

public class Delivery {

    public Courier courier;
    public Receiver receiver;
    public Company company;
    public String date;
    public String state;

    public Delivery() {
    }

    public Delivery(Courier courier, Receiver receiver, Company company, String date, String state) {
        this.courier = courier;
        this.receiver = receiver;
        this.company = company;
        this.date = date;
        this.state = state;
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
