package pex.gerardvictor.trapp.api;

import okhttp3.ResponseBody;
import pex.gerardvictor.trapp.entities.Delivery;
import pex.gerardvictor.trapp.entities.SimplifiedCourier;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by gerard on 27/05/17.
 */

public interface APIService {

    @POST("deliveries")
    Call<ResponseBody> saveDelivery(@Body Delivery delivery);

    @POST("/couriers")
    @FormUrlEncoded
    Call<SimplifiedCourier> saveCourier(@Field("uid") String uid,
                                        @Field("name") String name,
                                        @Field("email") String email,
                                        @Field("latitude") String latitude,
                                        @Field("longitude") String longitude);
}
