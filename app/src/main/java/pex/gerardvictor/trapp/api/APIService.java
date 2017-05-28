package pex.gerardvictor.trapp.api;

import pex.gerardvictor.trapp.entities.Delivery;
import pex.gerardvictor.trapp.entities.SimplifiedCourier;
import pex.gerardvictor.trapp.entities.SimplifiedDelivery;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by gerard on 27/05/17.
 */

public interface APIService {

    @POST("/deliveries")
    @FormUrlEncoded
    Call<SimplifiedDelivery> saveDelivery(@Field("uid") String uid,
                                          @Field("courier_uid") String courier_uid,
                                          @Field("date") String date,
                                          @Field("state") String state);

    @POST("/couriers")
    @FormUrlEncoded
    Call<SimplifiedCourier> saveCourier(@Field("uid") String uid,
                                        @Field("name") String name,
                                        @Field("email") String email);
}
