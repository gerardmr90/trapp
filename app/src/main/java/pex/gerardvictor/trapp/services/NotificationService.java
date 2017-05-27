package pex.gerardvictor.trapp.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

import pex.gerardvictor.trapp.R;
import pex.gerardvictor.trapp.activities.ChooserActivity;
import pex.gerardvictor.trapp.activities.HistoryActivity;
import pex.gerardvictor.trapp.entities.Courier;
import pex.gerardvictor.trapp.entities.Delivery;
import pex.gerardvictor.trapp.helpers.LocationHelper;

import static android.app.Notification.PRIORITY_MAX;

public class NotificationService extends Service {

    private static final String TAG = "NotificationService";
    private static final int THRESHOLD = 500;

    private static NotificationService instance = null;

    private DatabaseReference couriers;
    private DatabaseReference deliveries;

    private FirebaseUser user;

    private String address = "";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        user = FirebaseAuth.getInstance().getCurrentUser();

        address = getAddress();

        startCouriersLocationCheck();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    private String getAddress() {
        DatabaseReference receiver = FirebaseDatabase.getInstance().getReference("receivers").child(user.getUid()).child("address");
        receiver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                address = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return address;
    }

    private void startCouriersLocationCheck() {
        deliveries = FirebaseDatabase.getInstance().getReference("receiver_deliveries").child(user.getUid());
        deliveries.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> couriersUID = new HashSet<String>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    couriersUID.add(data.getValue(Delivery.class).getCourierUID());
                }
                for (String uid : couriersUID) {
                    checkCourierLocation(uid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkCourierLocation(String uid) {
        LatLng latLng = LocationHelper.getLocationFromAddress(address, getApplicationContext());
        final Location dropOff = new Location("");
        dropOff.setLatitude(latLng.latitude);
        dropOff.setLongitude(latLng.longitude);
        couriers = FirebaseDatabase.getInstance().getReference("couriers").child(uid);
        couriers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Courier courier = dataSnapshot.getValue(Courier.class);
                Location courierLocation = new Location("");
                courierLocation.setLatitude(courier.getLatitude());
                courierLocation.setLongitude(courier.getLongitude());
                if (courierLocation.distanceTo(dropOff) < THRESHOLD) {
                    sendNotification();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification() {
        Intent intent = new Intent(this, ChooserActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ChooserActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        long[] v = {500, 1000};
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(getString(R.string.trapp_notification_header))
                .setContentText("Courier is close to your address, your parcel will be delivered soon").setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(v)
                .setPriority(PRIORITY_MAX)
                .setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());

    }
}
