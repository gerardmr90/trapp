package pex.gerardvictor.trapp.delivery;

/**
 * Created by gerard on 18/03/17.
 */

public class Delivery {

    public String company;
    public String receiver;
    public String date;
    public String address;
    public String state;

    public Delivery(String company, String receiver, String date, String address, String state) {
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

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
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

    public void setState(String state) {
        this.state = state;
    }
}
