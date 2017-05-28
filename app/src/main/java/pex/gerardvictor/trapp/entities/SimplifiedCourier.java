package pex.gerardvictor.trapp.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by gerard on 27/05/17.
 */

public class SimplifiedCourier {

    @SerializedName("id")
    @Expose
    private String uid;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("email")
    @Expose
    private String email;

    public SimplifiedCourier() {
    }

    public SimplifiedCourier(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }

}
