package pex.gerardvictor.trapp.api;

/**
 * Created by gerard on 27/05/17.
 */

public class ApiUtils {

    public static final String BASE_URL = "http://trapp-app.herokuapp.com/";

    private ApiUtils() {
    }

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
