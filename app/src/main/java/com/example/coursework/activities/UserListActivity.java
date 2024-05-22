package com.example.coursework.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.coursework.MembersListFragment;
import com.example.coursework.NetworkChangeReceiver;
import com.example.coursework.R;
import com.example.coursework.User;
import com.example.coursework.UserListAdapter;
//import com.example.coursework.databinding.ActivityDisplayListBinding;
//import com.example.coursework.databinding.ActivityMainBinding;
import com.example.coursework.databinding.ActivityUserListBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserListActivity extends AppCompatActivity {
    // TODO: 02.05.2024
    //  добавление собственноручно созданных участников, шторка со всеми участниками
    //  возможно одобрение на вход в очередь
    ActivityUserListBinding binding;
    String TAG = "mylogs";
    List<User> userList; // хранить информацию о пользователях очереди
    public UserListAdapter adapter;
    DatabaseReference dataRef; // ссылка на очередь в бд
    FirebaseDatabase database;
    private NetworkChangeReceiver networkChangeReceiver;

    boolean showMemberListFragment = true;
    boolean isAdmin = false;
    String userId;

    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
        adapter.setIsAdmin(admin);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ListView listView = binding.list;
        ProgressBar progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        Button addButton = binding.add;
        addButton.setEnabled(false);

        userList = new ArrayList<>();
        adapter = new UserListAdapter(this, userList);
        listView.setAdapter(adapter);
        networkChangeReceiver = new NetworkChangeReceiver(adapter);

        // получение ссылки из intent и передача ссылки в адаптер
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        database = FirebaseDatabase.getInstance();
        String listId = getIntent().getStringExtra("listId");
        String listName = getIntent().getStringExtra("listName");
        binding.name.setText(listName);

        DatabaseReference listIdRef = database.getReference("lists").child(listId);
        DatabaseReference membersRef = listIdRef.child("members");
        DatabaseReference listNameRef = listIdRef.child("name");
        dataRef = listIdRef.child("data");
        adapter.setDatabaseReference(listIdRef);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        userId = firebaseUser.getUid();

        Gson gson = new Gson();


        MembersListFragment fragment = new MembersListFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction(); // Добавляем фрагмент изначально, но скрываем его
        trans.add(R.id.fragment, fragment);
        trans.hide(fragment);
        trans.commit();

        binding.members.setOnClickListener(view -> {
            FragmentTransaction transaction = manager.beginTransaction();
            if (showMemberListFragment) {
                transaction.show(fragment);
                binding.content.setVisibility(View.GONE);
            } else {
                transaction.hide(fragment);
                binding.content.setVisibility(View.VISIBLE);
            }
            transaction.commit();
            showMemberListFragment = !showMemberListFragment;
        });

        membersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<User> members;
                    String membersJson = snapshot.getValue(String.class);
                    Type userListType = new TypeToken<ArrayList<User>>() {
                    }.getType();
                    members = gson.fromJson(membersJson, userListType);
                    boolean inQueue = false;
                    for (User member : members) {
                        if (member.getId().equals(userId)) {
                            inQueue = true;
                            break;
                        }
                    }
                    if(!inQueue && NetworkChangeReceiver.isConnected){
                        DatabaseReference userListsIdRef=database.getReference("users").child(userId).child("listsId");
                        userListsIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                List<String> listsId=new ArrayList<>();
                                for (DataSnapshot data : snapshot.getChildren()) {
                                    String curListId =data.getValue(String.class);
                                    listsId.add(curListId);
                                }
                                listsId.remove(listId);
                                userListsIdRef.setValue(listsId);
                                startActivity(new Intent(UserListActivity.this, AvailableListsActivity.class));
                                finish();
                                Toast.makeText(getApplicationContext(),"Вы были удалены из очереди",Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        listIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getApplicationContext(), "Очередь была удалена", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(UserListActivity.this, AvailableListsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // получение json с информацией о пользователях в очереди
                    String data = dataSnapshot.getValue(String.class);
                    Type userListType = new TypeToken<List<User>>() {
                    }.getType();
                    List<User> updatedUserList = gson.fromJson(data, userListType);

                    userList.clear(); // Очищаем текущий список
                    userList.addAll(updatedUserList); // Добавляем все элементы из нового списка
                    adapter.notifyDataSetChanged();

//                    boolean isFirstUser = !updatedUserList.isEmpty() && updatedUserList.get(0).getId().equals(userId);
//                    if (isFirstUser) {
////                        showSnackbar("Первый", 1000);
//                        showNotification("Очередь \""+listName+"\"", "Вы первый в очереди.");
//                    }
                }
                if (NetworkChangeReceiver.isConnected) {
                    addButton.setEnabled(true);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Failed to read value.", error.toException());
                progressBar.setVisibility(View.GONE);
            }
        });

        binding.back.setOnClickListener(view -> {
            finish();
        });

        addButton.setOnClickListener(view -> {
            listNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        addYourself();
                    } else {
                        Toast.makeText(getApplicationContext(), "Очередь была удалена", Toast.LENGTH_LONG).show();
                        listIdRef.removeValue();
                        startActivity(new Intent(UserListActivity.this, AvailableListsActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isAdmin) return false;
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDragAndDrop(data, shadowBuilder, position, 0);
                return true;
            }
        });

        listView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        // получаем позиции элементов списка и выполните перемещение
                        int positionFrom = (int) event.getLocalState();
                        int positionTo = listView.pointToPosition((int) event.getX(), (int) event.getY());
                        if (positionTo != ListView.INVALID_POSITION) {
                            // меняем местами элементы в списке данных
                            User item = adapter.getItem(positionFrom);
                            adapter.remove(item);
                            adapter.insert(item, positionTo);
                            adapter.notifyDataSetChanged();

                            // преобразуем список в json и отправляем изменения в бд
                            String userListJson = gson.toJson(userList);
                            dataRef.setValue(userListJson);
                        }
                        break;
                }
                return true;
            }
        });

    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "user_queue_channel";
        String channelName = "User Queue Notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.queue_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    public void addYourself() {
        //получение id пользователя
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        userId = firebaseUser.getUid();

        DatabaseReference userNameRef = database.getReference("users").child(userId).child("name");
        User newUser = new User();

        userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                // имя совпадает с указанным в профиле
//                newUser.setName(name);
                newUser.setId(userId);

                dataRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Gson gson = new Gson();
                        List<User> userList = new ArrayList<>();
                        if (currentData.getValue() != null) {
                            String json = currentData.getValue(String.class);
                            userList = gson.fromJson(json, new TypeToken<List<User>>(){}.getType());
                        }

                        boolean isUserInList = false;
                        for (User user : userList) {
                            if (user.getId() != null && user.getId().equals(userId)) {
                                isUserInList = true;
                                break;
                            }
                        }
                        if (!isUserInList) {
                            userList.add(newUser);
                            String userListJson = gson.toJson(userList);
                            currentData.setValue(userListJson);
                        }
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot currentData) {
                        if (committed) {
                            adapter.notifyDataSetChanged();
                            showSnackbar("Добавлен", 500);
                        } else {
                            showSnackbar("Уже в очереди или произошла ошибка", 100);
                        }
                    }
                });

//                // поиск пользователя в списке
//                boolean isUserInList = false;
//                for (User user : userList) {
//                    if (user.getId() != null && user.getId().equals(userId)) {
//                        isUserInList = true;
//                        break;
//                    }
//                }
//                if (!isUserInList) {
//                    Gson gson = new Gson();
////                    Snackbar.make(binding.getRoot(), "Добавлен", Snackbar.LENGTH_SHORT).show();
//                    userList.add(newUser);
//                    adapter.notifyDataSetChanged();
//                    String userListJson = gson.toJson(userList);
//                    dataRef.setValue(userListJson);
//                    showSnackbar("Добавлен", 500);
//                } else {
//                    showSnackbar("Уже в очереди", 100);
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void showSnackbar(String text, int duration) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), text, Snackbar.LENGTH_SHORT);
        snackbar.setDuration(duration);
        snackbar.show();
    }

}