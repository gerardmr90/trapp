package pex.gerardvictor.trapp.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by gerard on 27/05/17.
 */

public class ApiUtils {

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://trapp-app.herokuapp.com/")
            .client(getHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static APIService service = retrofit.create(APIService.class);

    private static OkHttpClient getHttpClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        return httpClient.build();
    }

    public static APIService getService() {
        return service;
    }
}
