package com.example.coursework.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

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
    // TODO: 02.05.2024 добавить стелку назад для перехода в меню выбора очереди,
    //  добавление собственноручно созданных участников, шторка со всеми участниками
    //  возможно одбрение на вход в очередь
    ActivityUserListBinding binding;
    String TAG = "mylogs";
    List<User> userList; // хранить информацию о пользователях очереди
    UserListAdapter adapter;
    DatabaseReference dataRef; // ссылка на очередь в бд

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
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String listId=getIntent().getStringExtra("listId");
        dataRef = database.getReference("lists").child(listId).child("data");
        adapter.setDatabaseReference(dataRef);

        Gson gson = new Gson();

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
                else {
                    showSnackbar("Список пуст", Snackbar.LENGTH_SHORT);
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

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addYourself();
//                userList.add(new User("last"));
//                for (int i=1;i<=15;i++){
//                    userList.add(new User("User"+i));
//                }

                // преобразуем список в json и отправляем изменения в бд
                String userListJson = gson.toJson(userList);
                dataRef.setValue(userListJson);
            }
        });
    }



    public void addYourself() {
        //получение id пользователя
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String id = firebaseUser.getUid();
        User newUser = new User("Ты");
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
            Snackbar.make(binding.getRoot(), "Добавлен", Snackbar.LENGTH_SHORT).show();
            showSnackbar("Добавлен", 500);
            userList.add(newUser);
            adapter.notifyDataSetChanged();
        } else {
            showSnackbar("Уже в очереди", 100);
        }
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