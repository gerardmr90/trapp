package pex.gerardvictor.trapp.session;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by gerard on 21/04/17.
 */

public class Session {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public Session(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("Trapp", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setLoggedIn(boolean loggedIn) {
        editor.putBoolean("loggInMode",loggedIn);
        editor.commit();
    }

    public boolean loggedIn() {
        return sharedPreferences.getBoolean("loggInMode", false);
    }
}
