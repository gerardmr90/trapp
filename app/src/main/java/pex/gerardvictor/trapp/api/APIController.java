package pex.gerardvictor.trapp.api;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import okhttp3.ResponseBody;
import pex.gerardvictor.trapp.entities.Courier;
import pex.gerardvictor.trapp.entities.Delivery;
import pex.gerardvictor.trapp.entities.SimplifiedCourier;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gerard on 27/05/17.
 */

public class APIController {

    private static APIController instance;

    private APIController() {
    }

    public static synchronized APIController getInstance() {
        if (instance == null) {
            instance = new APIController();
        }
        return instance;
    }

    public Task<Void> saveCourier(Courier courier) {
        final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        SimplifiedCourier simplifiedCourier = new SimplifiedCourier(courier.getUid(), courier.getName(), courier.getEmail());

        ApiUtils.getService().saveCourier(simplifiedCourier)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.e("APISERVER", response.toString());
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("APISERVER", t.toString());
                    }
                });

        return taskCompletionSource.getTask();
    }


    public Task<Void> saveDelivery(Delivery delivery) {
        final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        ApiUtils.getService().saveDelivery(delivery)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.e("APISERVER", response.toString());
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("APISERVER", t.toString());
                    }
                });

        return taskCompletionSource.getTask();
    }

}
