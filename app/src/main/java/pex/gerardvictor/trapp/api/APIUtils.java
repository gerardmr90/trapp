package pex.gerardvictor.trapp.api;

/**
 * Created by gerard on 27/05/17.
 */

public class APIUtils {

    public static final String BASE_URL = "https://trapp-app.herokuapp.com/";

    private APIUtils() {
    }

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
