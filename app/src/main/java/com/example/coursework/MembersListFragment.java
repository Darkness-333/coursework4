package com.example.coursework;

import android.content.ClipData;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.coursework.activities.AvailableListsActivity;
import com.example.coursework.activities.UserListActivity;
import com.example.coursework.databinding.ActivityUserListBinding;
import com.example.coursework.databinding.FragmentMembersListBinding;
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


public class MembersListFragment extends Fragment  {

    FragmentMembersListBinding binding;

    //    List<User> userList; // хранить информацию о пользователях очереди
    UserListAdapter adapter;
//    DatabaseReference dataRef; // ссылка на очередь в бд
//    FirebaseDatabase database;
//    private NetworkChangeReceiver networkChangeReceiver;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        networkChangeReceiver = new NetworkChangeReceiver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMembersListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = binding.list;

        ArrayList<User> userList = new ArrayList<>();
        adapter = new UserListAdapter(getContext(), userList);
        adapter.setWorkWithUsers(false);
        listView.setAdapter(adapter);

        // получение ссылки из intent и передача ссылки в адаптер
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String listId = getActivity().getIntent().getStringExtra("listId");

        DatabaseReference listIdRef = database.getReference("lists").child(listId);
        DatabaseReference membersRef = listIdRef.child("members");
        adapter.setDatabaseReference(listIdRef);

        Gson gson = new Gson();

        membersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> members = new ArrayList<>();
                for (DataSnapshot elem : snapshot.getChildren()) {
                    // Получение значений из снимка данных и добавление их в список строк
                    String memberId = elem.getValue(String.class);
                    User newUser = new User();
                    newUser.setId(memberId);
                    members.add(newUser);
                }
                userList.clear(); // Очищаем текущий список
                userList.addAll(members); // Добавляем все элементы из нового списка
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }


}