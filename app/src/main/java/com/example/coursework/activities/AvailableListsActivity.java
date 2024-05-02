package com.example.coursework.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.coursework.AddQueueDialog;
import com.example.coursework.AvailableListsAdapter;
import com.example.coursework.CreateQueueDialog;
import com.example.coursework.databinding.ActivityAvailableListsBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AvailableListsActivity extends AppCompatActivity implements CreateQueueDialog.DialogCallback, AddQueueDialog.DialogCallback {

    ListView listView;
    DatabaseReference usersListsIdReference;
    List<String> listsName = new ArrayList<>();
    List<String> listsId = new ArrayList<>();
    ActivityAvailableListsBinding binding;
    FirebaseDatabase database;
    ArrayAdapter<String> adapter;

    String TAG="mylogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAvailableListsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        listView = binding.list;
//        adapter = new ArrayAdapter<>(AvailableListsActivity.this, android.R.layout.simple_list_item_1, listsName);
        adapter = new AvailableListsAdapter(AvailableListsActivity.this, listsName, listsId);
        listView.setAdapter(adapter);

        Button createButton=binding.createQueue;
        createButton.setEnabled(false);
        Button addButton=binding.addQueue;
        addButton.setEnabled(false);

        ProgressBar progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String userId = firebaseUser.getUid();
        // Получение доступных списков для текущего пользователя из базы данных Firebase
        database = FirebaseDatabase.getInstance();
        usersListsIdReference = database.getReference("users").child(userId).child("listsId");

//        Gson gson=new Gson();
        usersListsIdReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Получаем список всех id списка в snapshot, получаем конкретное id,
                // получаем ссылку на имя списка по id, заносим имя в список
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String listId = snapshot.getValue(String.class);
                    listsId.add(listId);
                    DatabaseReference listNameReference=database.getReference("lists").child(listId).child("name");
                    listNameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String listName = snapshot.getValue(String.class);
                            if (listName != null) {
                                listsName.add(listName);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Failed to read list name.", error.toException());
                        }
                    });
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                createButton.setEnabled(true);
                addButton.setEnabled(true);
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
                startActivity(intent);
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateQueueDialog.showDialog(AvailableListsActivity.this,AvailableListsActivity.this);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddQueueDialog.showDialog(AvailableListsActivity.this,AvailableListsActivity.this);
            }
        });
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
        usersListsIdReference.setValue(listsId);

        // записываем имя в отображаемый список
        listsName.add(name);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onAddQueue(String listId) {
//        String listId=listId;
        DatabaseReference nameRef=database.getReference("lists").child(listId).child("name");
        nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // добавляем id в список с id
                    listsId.add(listId);
                    // добавляем в ссылку со всеми id списков пользователя обновленный список
                    usersListsIdReference.setValue(listsId);
                    String listName=dataSnapshot.getValue(String.class);
                    listsName.add(listName);
                    adapter.notifyDataSetChanged();

                } else {
                    Snackbar.make(binding.getRoot(),"Неверный id",Snackbar.LENGTH_SHORT).show();
                    // DatabaseReference не существует
                    // Можете выполнить дополнительные действия здесь
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AvailableListsActivity.this, "Failed to read value.", Toast.LENGTH_SHORT).show();

                // Обработка ошибок, если таковые возникли при попытке чтения данных
            }
        });

    }

//    @Override
//    public void onNegativeButtonClicked() {    }
}
