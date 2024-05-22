package com.example.coursework;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.coursework.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserPositionService extends Service {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    Gson gson;
    String userId;
    DatabaseReference userListsIdRef;
    List<DatabaseReference> listIdRefs;
    String TAG="mylogs";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        userId = firebaseUser.getUid();

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        userListsIdRef = database.getReference("users").child(userId).child("listsId");

        listIdRefs =new ArrayList<>();

        userListsIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot elem : snapshot.getChildren()){
                    String listId=elem.getValue(String.class);
                    listIdRefs.add(database.getReference("lists").child(listId));
                }
                startTrackingUserPosition();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


//        dataRef = FirebaseDatabase.getInstance().getReference("lists").child("-NyH6c3oHsXy1v2eKLPD").child("data");
        gson=new Gson();

        startForegroundService();
    }

    private void startForegroundService() {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Сервис")
                .setContentText("Отслеживает позицию в очередях")
                .setSmallIcon(R.drawable.queue_icon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void startTrackingUserPosition() {
        int numb=0;


        for (DatabaseReference listIdRef: listIdRefs){
            numb++;
            listIdRef.child("data").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String data = dataSnapshot.getValue(String.class);
                        Type userListType = new TypeToken<List<User>>() {
                        }.getType();
                        List<User> updatedUserList = gson.fromJson(data, userListType);

                        String firstUserId = null;
                        if (updatedUserList != null) {
                            firstUserId = updatedUserList.get(0).getId();
                        }
                        if (firstUserId != null && firstUserId.equals(userId)) {
                            listIdRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String listName=snapshot.getValue(String.class);
                                    sendNotification("Очередь \""+listName+"\"", "Вы первый в очереди.");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Обработка ошибок при чтении из базы данных
                }
            });
        }

    }

    private int notificationId=0;
    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.queue_icon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setGroup("GROUP_KEY");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(notificationId++, notificationBuilder.build());

        Notification summaryNotification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("Новые уведомления")
                        .setSmallIcon(R.drawable.queue_icon)
                        .setGroup("GROUP_KEY")
                        .setGroupSummary(true)
                        .build();

        notificationManager.notify(0, summaryNotification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
