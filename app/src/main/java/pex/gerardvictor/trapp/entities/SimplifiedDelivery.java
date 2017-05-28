package pex.gerardvictor.trapp.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by gerard on 28/05/17.
 */

public class SimplifiedDelivery {

    @SerializedName("id")
    @Expose
    private String delivery;

    @SerializedName("courier_uid")
    @Expose
    private String courier_uid;

    @SerializedName("date")
    @Expose
    private String date;

    @SerializedName("state")
    @Expose
    private String state;

    public SimplifiedDelivery() {
    }

    public SimplifiedDelivery(String delivery, String courier_uid, String date, String state) {
        this.delivery = delivery;
        this.courier_uid = courier_uid;
        this.date = date;
        this.state = state;
    }
}
