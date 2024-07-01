package com.example.coursework.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.coursework.NetworkChangeReceiver;
import com.example.coursework.User;
import com.example.coursework.dialogs.AddQueueDialog;
import com.example.coursework.AvailableListsAdapter;
import com.example.coursework.dialogs.CreateQueueDialog;
import com.example.coursework.databinding.ActivityAvailableListsBinding;
import com.google.android.material.snackbar.Snackbar;
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

public class AvailableListsActivity extends AppCompatActivity implements CreateQueueDialog.DialogCallback, AddQueueDialog.DialogCallback {
    ListView listView;
    List<String> listsName = new ArrayList<>();
    List<String> listsId = new ArrayList<>();
    ActivityAvailableListsBinding binding;
    DatabaseReference userListsIdRef;
    FirebaseDatabase database;
    ArrayAdapter<String> adapter;
    String userId;
    Gson gson;
    String TAG="mylogs";
    private NetworkChangeReceiver networkChangeReceiver;


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAvailableListsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        listView = binding.list;
        adapter = new AvailableListsAdapter(AvailableListsActivity.this, listsName, listsId);
        listView.setAdapter(adapter);

        networkChangeReceiver= new NetworkChangeReceiver(adapter);

        Button createButton=binding.createQueue;
        createButton.setEnabled(false);
        Button addButton=binding.addQueue;
        addButton.setEnabled(false);

        ProgressBar progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        userId = firebaseUser.getUid();

        // Получение доступных списков для текущего пользователя из базы данных Firebase
        // синхронизация с интернетом
        database = FirebaseDatabase.getInstance();
        userListsIdRef = database.getReference("users").child(userId).child("listsId");
        userListsIdRef.keepSynced(true);

        gson=new Gson();

        binding.profile.setOnClickListener(view -> {
            Intent intent=new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        userListsIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            boolean needUpdateListsId=false;
            int totalChildrenCount;
            int completedChildrenCount = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    if (NetworkChangeReceiver.isConnected){
                        createButton.setEnabled(true);
                        addButton.setEnabled(true);
                    }
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                totalChildrenCount = (int) dataSnapshot.getChildrenCount();
                // Получаем список всех id списка в snapshot, получаем конкретное id,
                // получаем ссылку на имя списка по id, заносим имя в список
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: "+needUpdateListsId+" "+totalChildrenCount+" "+completedChildrenCount);
                    String listId = snapshot.getValue(String.class);
//                    listsId.add(listId);
                    DatabaseReference listNameReference=database.getReference("lists").child(listId).child("name");
                    listNameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                Log.d(TAG, "onDataChange: "+completedChildrenCount);
                                listsId.add(listId);
                                String listName = snapshot.getValue(String.class);
                                listsName.add(listName);
                                adapter.notifyDataSetChanged();
                            }
                            else{
                                needUpdateListsId=true;
                            }
                            completedChildrenCount++;
                            if (completedChildrenCount == totalChildrenCount) {
                                // TODO: 19.05.2024 проверить удаляется ли очередь 
                                if (needUpdateListsId) {
                                    userListsIdRef.setValue(listsId);
                                }
                                if (NetworkChangeReceiver.isConnected){
                                    createButton.setEnabled(true);
                                    addButton.setEnabled(true);
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Failed to read list name.", error.toException());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AvailableListsActivity.this, "Failed to read value.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Переход к активности с содержимым выбранного списка
                Intent intent = new Intent(AvailableListsActivity.this, UserListActivity.class);
                intent.putExtra("listId", listsId.get(position));
                intent.putExtra("listName", listsName.get(position));
                startActivity(intent);
            }
        });
        createButton.setOnClickListener(view ->
                CreateQueueDialog.showDialog(AvailableListsActivity.this,AvailableListsActivity.this));

        addButton.setOnClickListener(view ->
                AddQueueDialog.showDialog(AvailableListsActivity.this,AvailableListsActivity.this));
    }

    @Override
    public void onCreateQueue(String name) {
        //получаем ссылку на все списки
        DatabaseReference listsReference=database.getReference("lists");
        // создаем id для нового списка
        String listId=listsReference.push().getKey();
        // записываем имя списка с таким id в ссылке на все списки
        listsReference.child(listId).child("name").setValue(name);
        // добавляем id в список с id
        listsId.add(listId);
        // добавляем в ссылку со всеми id списков пользователя обновленный список
        userListsIdRef.setValue(listsId);

        List<User> members=new ArrayList<>();
        User creator=new User();
        creator.setId(userId);
        creator.setAdmin(true);
        members.add(creator);
        gson=new Gson();
        String membersJson=gson.toJson(members);
        listsReference.child(listId).child("members").setValue(membersJson);

        // записываем имя в отображаемый список
        listsName.add(name);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAddQueue(String listId) {
        DatabaseReference nameRef=database.getReference("lists").child(listId).child("name");
        nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String nameQueue=dataSnapshot.getValue(String.class);
                    DatabaseReference listsReference=database.getReference("lists");
                    DatabaseReference membersRef=listsReference.child(listId).child("members");
                    membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (dataSnapshot.exists()){
                                List<User> members;
                                String membersJson=snapshot.getValue(String.class);
                                Type userListType = new TypeToken<ArrayList<User>>(){}.getType();
                                members = gson.fromJson(membersJson, userListType);
                                boolean inQueue=false;
                                for (User member:members){
                                    if (member.getId().equals(userId)){
                                        inQueue=true;
                                    }
                                }
                                if (!inQueue){
                                    // добавляем id в список с id
                                    listsId.add(listId);
                                    // добавляем в ссылку со всеми id списков пользователя обновленный список
                                    userListsIdRef.setValue(listsId);
                                    //получаем название списка и отображаем его
                                    String listName=dataSnapshot.getValue(String.class);
                                    listsName.add(listName);
                                    adapter.notifyDataSetChanged();

                                    User newMember=new User();
                                    newMember.setId(userId);
                                    members.add(newMember);
                                    String newMembersJson=gson.toJson(members);
                                    membersRef.setValue(newMembersJson);
                                }
                                else {
                                    Toast.makeText(getApplicationContext(),"Очередь \""+nameQueue+"\" уже добавлена",Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
                else {
                    Snackbar.make(binding.getRoot(),"Неверный id",Snackbar.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AvailableListsActivity.this, "Failed to read value.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
