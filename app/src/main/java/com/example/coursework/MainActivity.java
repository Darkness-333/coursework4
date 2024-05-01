package com.example.coursework;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.coursework.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ListView listView = binding.list;


        List<User> userList = new ArrayList<>();
// Добавьте данные пользователей в список
        userList.add(new User("Имя пользователя"));
        userList.add(new User("Имя пользователя 2"));
        userList.add(new User("Имя пользователя 3"));
        userList.add(new User("Имя пользователя 4"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 5"));
        userList.add(new User("Имя пользователя 7"));
        userList.add(new User("Имя пользователя 11"));

        ListAdapter adapter = new ListAdapter(this, userList);
        listView.setAdapter(adapter);

        binding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//                UUID.randomUUID().toString()
//                SharedPreferences sharedPrefs = getSharedPreferences("prefs", MODE_PRIVATE);
//                if (sharedPrefs.getString("id", "").isEmpty() || "".equals(sharedPrefs.getString("id",""))){
//                    SharedPreferences.Editor editor = sharedPrefs.edit();
//                    editor.putString("id", UUID.randomUUID().toString());
//                    editor.commit();
//                }

                SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
                String id = preferences.getString("id", "");


                if (id.isEmpty()) {
                    id = UUID.randomUUID().toString();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("id", id);
                    editor.apply();
                }
                User you=new User("Ты");
                you.setId(id);
//                Toast.makeText(getApplicationContext(),you.getId(),Toast.LENGTH_SHORT).show();
                boolean isUserInList = false;
                for (User user : userList) {
                    if (user.getId()!=null && user.getId().equals(id)) {
                        isUserInList = true;
                        break;
                    }
                }
                if (!isUserInList){
                    Toast.makeText(getApplicationContext(),"Добавлен",Toast.LENGTH_SHORT).show();

                    userList.add(you);
                    adapter.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Уже в очереди",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent=new Intent(this, AboutActivity.class);
        if (id==R.id.about_author){
            intent.putExtra("info","author");
        }
        else if(id==R.id.about_app){
            intent.putExtra("info","app");
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }
}