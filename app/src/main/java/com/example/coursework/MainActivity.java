package com.example.coursework;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.coursework.databinding.ActivityMainBinding;
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
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String TAG = "mylogs";
    List<User> userList;
    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ListView listView = binding.list;

        ProgressBar progressBar=binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        Button addButton=binding.add;
        addButton.setEnabled(false);

        userList = new ArrayList<>();

        adapter = new ListAdapter(this, userList);
        listView.setAdapter(adapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("list1");

        Gson gson = new Gson();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                Type userListType = new TypeToken<List<User>>() {
                }.getType();
                List<User> updatedUserList = gson.fromJson(data, userListType);
                for (User elem : updatedUserList) {
                    Log.d(TAG, "onDataChange: " + elem.getName());
                }
//                userList=updatedUserList;
                userList.clear(); // Очищаем текущий список
                userList.addAll(updatedUserList); // Добавляем все элементы из нового списка
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                addButton.setEnabled(true);
                Log.d(TAG, "Value is: " + updatedUserList.get(0).getName());
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
                String userListJson = gson.toJson(userList);
                myRef.setValue(userListJson);
            }
        });
    }

    public void addYourself() {
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
//            Toast.makeText(getApplicationContext(),"Добавлен",Toast.LENGTH_SHORT).show();
            Snackbar.make(binding.getRoot(), "Добавлен", Snackbar.LENGTH_SHORT).show();
            showSnackbar("Добавлен", 500);
            userList.add(newUser);
            adapter.notifyDataSetChanged();
        } else {
            showSnackbar("Уже в очереди", 100);
//            Snackbar snackbar = Snackbar.make(binding.getRoot(), "Уже в очереди", Snackbar.LENGTH_SHORT);
//            snackbar.setDuration(50);
//            snackbar.show();
        }
    }
    
    public void showSnackbar(String text, int duration){
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