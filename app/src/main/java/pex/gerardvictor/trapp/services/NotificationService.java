package pex.gerardvictor.trapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;

public class NotificationService extends Service {

    private DatabaseReference couriers;
    private DatabaseReference deliveries;
    private ChildEventListener couriersChildEventListener;
    private ChildEventListener deliveriesChildEventListener;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
