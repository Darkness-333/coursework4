package com.example.coursework;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.coursework.activities.AvailableListsActivity;
import com.example.coursework.activities.UserListActivity;

public class NetworkChangeReceiver extends BroadcastReceiver {
    public static boolean isConnected;

    private ArrayAdapter adapter;

    public NetworkChangeReceiver(ArrayAdapter adapter) {
        this.adapter = adapter;

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();


        adapter.notifyDataSetChanged();

        Activity activity = (Activity) context;
        if (activity instanceof AvailableListsActivity){
            if (isConnected) {
                activity.findViewById(R.id.no_internet).setVisibility(View.GONE); // Скрываем иконку при наличии интернета
//                activity.findViewById(R.id.progressBar).setVisibility(View.GONE);
                activity.findViewById(R.id.createQueue).setEnabled(true);
                activity.findViewById(R.id.addQueue).setEnabled(true);
            } else {
                activity.findViewById(R.id.no_internet).setVisibility(View.VISIBLE); // Показываем иконку при отсутствии интернета
//                activity.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                activity.findViewById(R.id.createQueue).setEnabled(false);
                activity.findViewById(R.id.addQueue).setEnabled(false);
            }
        }
        else if(activity instanceof UserListActivity){
            if (isConnected) {
                activity.findViewById(R.id.no_internet).setVisibility(View.GONE); // Скрываем иконку при наличии интернета
                activity.findViewById(R.id.name).setVisibility(View.VISIBLE); // Скрываем иконку при наличии интернета
                activity.findViewById(R.id.add).setEnabled(true);
            } else {
                activity.findViewById(R.id.no_internet).setVisibility(View.VISIBLE); // Показываем иконку при отсутствии интернета
                activity.findViewById(R.id.name).setVisibility(View.GONE); // Показываем иконку при отсутствии интернета
                activity.findViewById(R.id.add).setEnabled(false);
            }
        }

    }
}
