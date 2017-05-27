package pex.gerardvictor.trapp.entities;

/**
 * Created by gerard on 27/05/17.
 */

public class SimplifiedCourier {

    private String uid;
    private String name;
    private String email;
    private String latitude;
    private String longitude;

    public SimplifiedCourier(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.latitude = "N/A";
        this.longitude = "N/A";
    }

}
