package com.example.coursework.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {
    // TODO: 02.05.2024
    //  добавление собственноручно созданных участников, шторка со всеми участниками
    //  возможно одобрение на вход в очередь
    ActivityUserListBinding binding;
    String TAG = "mylogs";
    List<User> userList; // хранить информацию о пользователях очереди
    UserListAdapter adapter;
    DatabaseReference dataRef; // ссылка на очередь в бд
    FirebaseDatabase database;

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

        // получение ссылки из intent и передача ссылки в адаптер
        database = FirebaseDatabase.getInstance();
        String listId=getIntent().getStringExtra("listId");


        DatabaseReference listIdRef=database.getReference("lists").child(listId);
        DatabaseReference listName=listIdRef.child("name");
        dataRef = listIdRef.child("data");
        adapter.setDatabaseReference(listIdRef);

        Gson gson = new Gson();

        listIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    Toast.makeText(getApplicationContext(),"Кажется очередь была удалена", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(UserListActivity.this,AvailableListsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    // получение json с информацией о пользователях в очереди
                    String data = dataSnapshot.getValue(String.class);
                    Type userListType = new TypeToken<List<User>>() {
                    }.getType();
                    List<User> updatedUserList = gson.fromJson(data, userListType);
                    userList.clear(); // Очищаем текущий список
                    userList.addAll(updatedUserList); // Добавляем все элементы из нового списка
                    adapter.notifyDataSetChanged();
                }

                progressBar.setVisibility(View.GONE);
                addButton.setEnabled(true);
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
            listName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "showPopupMenu: "+dataSnapshot);

                    if (dataSnapshot.exists()) {
                        addYourself();

                    } else {
                        Toast.makeText(getApplicationContext(),"Очередь была удалена", Toast.LENGTH_LONG).show();
                        listIdRef.removeValue();
                        startActivity(new Intent(UserListActivity.this,AvailableListsActivity.class));
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
                        // Получите позиции элементов списка и выполните перемещение
                        int positionFrom = (int) event.getLocalState();
                        int positionTo = listView.pointToPosition((int) event.getX(), (int) event.getY());
                        if (positionTo != ListView.INVALID_POSITION) {
                            // Поменяйте местами элементы в списке данных
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



    public void addYourself() {
        //получение id пользователя
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String id = firebaseUser.getUid();

        DatabaseReference userNameRef = database.getReference("users").child(id).child("name");
        User newUser = new User();

        userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name=snapshot.getValue(String.class);
                newUser.setName(name);
                newUser.setId(id);

                // поиск пользователя в списке
                boolean isUserInList = false;
                for (User user : userList) {
                    if (user.getId() != null && user.getId().equals(id)) {
                        isUserInList = true;
                        break;
                    }
                }
                if (!isUserInList) {
                    Gson gson = new Gson();
//                    Snackbar.make(binding.getRoot(), "Добавлен", Snackbar.LENGTH_SHORT).show();
                    userList.add(newUser);
                    adapter.notifyDataSetChanged();
                    String userListJson = gson.toJson(userList);
                    dataRef.setValue(userListJson);
                    showSnackbar("Добавлен", 500);
                } else {
                    showSnackbar("Уже в очереди", 100);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        User newUser = new User("Ты");
//        newUser.setId(id);
//
//        // поиск пользователя в списке
//        boolean isUserInList = false;
//        for (User user : userList) {
//            if (user.getId() != null && user.getId().equals(id)) {
//                isUserInList = true;
//                break;
//            }
//        }
//        if (!isUserInList) {
//            Snackbar.make(binding.getRoot(), "Добавлен", Snackbar.LENGTH_SHORT).show();
//            showSnackbar("Добавлен", 500);
//            userList.add(newUser);
//            adapter.notifyDataSetChanged();
//        } else {
//            showSnackbar("Уже в очереди", 100);
//        }
    }

    public void showSnackbar(String text, int duration) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), text, Snackbar.LENGTH_SHORT);
        snackbar.setDuration(duration);
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent(this, AboutActivity.class);
        if (id == R.id.about_author) {
            intent.putExtra("info", "author");
        } else if (id == R.id.about_app) {
            intent.putExtra("info", "app");
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }
}