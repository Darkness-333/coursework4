package com.example.coursework;



import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import com.example.coursework.activities.AvailableListsActivity;
import com.example.coursework.activities.UserListActivity;
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
import java.util.Iterator;
import java.util.List;

public class AvailableListsAdapter extends ArrayAdapter<String> {
    List<String> listsId;
    String userId;
    Gson gson;
    boolean isAdmin = false;


    public AvailableListsAdapter(@NonNull Context context, List<String> listsName, List<String> listsId) {
        super(context, 0, listsName);
        this.listsId = listsId;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        gson = new Gson();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        TextView number = convertView.findViewById(R.id.number);
        TextView name = convertView.findViewById(R.id.name);
        ImageView control = convertView.findViewById(R.id.control);

        boolean isConnected = NetworkChangeReceiver.isConnected;
        if (isConnected) {
            control.setVisibility(View.VISIBLE);
        } else {
            control.setVisibility(View.INVISIBLE);
        }

        name.setText(getItem(position));

        int numberWidth = String.valueOf(getCount()).length(); // Ширина самого большого номера
        String formattedNumber = String.format("%0" + numberWidth + "d", position + 1);
        number.setText(formattedNumber);

        control.setOnClickListener(view -> {
            showPopupMenu(view, position);
        });

        return convertView;
    }


    public void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.available_lists_popup_menu, popupMenu.getMenu());

        String listId = listsId.get(position);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference listsReference = database.getReference("lists");
        DatabaseReference membersRef = listsReference.child(listId).child("members");

        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<User> members;
                    String membersJson = snapshot.getValue(String.class);
                    Type userListType = new TypeToken<ArrayList<User>>() {
                    }.getType();
                    members = gson.fromJson(membersJson, userListType);
                    for (User member : members) {
                        if (member.getId().equals(userId)) {
                            isAdmin = member.getAdmin();
                            if (isAdmin) {
                                Menu menu = popupMenu.getMenu();
                                menu.findItem(R.id.action_delete).setVisible(true);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        if (!isAdmin) {
            Menu menu = popupMenu.getMenu();
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        String currentQueue = getItem(position);

        DatabaseReference userListsIdRef = database.getReference("users").child(userId).child("listsId");

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            boolean needNotify = false;
//            String listId=listsId.get(position);
            if (id == R.id.action_get) {
//                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                // Создаем объект ClipData с нужным текстом
                ClipData clip = ClipData.newPlainText("label", listId);
                // Копируем ClipData в буфер обмена
                clipboard.setPrimaryClip(clip);
            } else if (id == R.id.action_quit) {
                membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            List<User> members;
                            String membersJson = snapshot.getValue(String.class);
                            Type userListType = new TypeToken<ArrayList<User>>() {
                            }.getType();
                            members = gson.fromJson(membersJson, userListType);
                            if (members.size() == 1) {
                                DatabaseReference listRef = database.getReference("lists").child(listId);
                                listRef.removeValue();
                            } else {
                                Iterator<User> iterator = members.iterator();
                                while (iterator.hasNext()) {
                                    User member = iterator.next();
                                    if (member.getId().equals(userId)) {
                                        iterator.remove();
                                    }
                                }
                                String newMembersJson = gson.toJson(members);
                                membersRef.setValue(newMembersJson);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
                remove(currentQueue);
                listsId.remove(position);
                userListsIdRef.setValue(listsId);
                needNotify = true;
            } else if (id == R.id.action_delete) {
                remove(currentQueue);
                //удаление из всех списков
                DatabaseReference listRef = database.getReference("lists").child(listId);
                listRef.removeValue();
                //удаление у пользователя
                listsId.remove(position);
                userListsIdRef.setValue(listsId);
                needNotify = true;
            }
            if (needNotify) {
                notifyDataSetChanged();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }


}
