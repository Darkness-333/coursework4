package com.example.coursework;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import com.example.coursework.activities.AvailableListsActivity;
import com.example.coursework.dialogs.ChangeNameDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserListAdapter extends ArrayAdapter<User> implements ChangeNameDialog.DialogCallback {

    //    private ImageView avatar;
//    private TextView name;
//    private ImageView control;
    String TAG = "dellog";

    // TODO: 03.05.2024 добавить права пользователя и админа 

    List<User> users;
    DatabaseReference listIdRef;
//    int position;

    public UserListAdapter(@NonNull Context context, List<User> users) {
        super(context, 0, users);
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_item, parent, false);
        }

//        avatar = convertView.findViewById(R.id.avatar);
//        name = convertView.findViewById(R.id.name);
//        control = convertView.findViewById(R.id.control);

        TextView number = convertView.findViewById(R.id.number);
        ImageView avatar = convertView.findViewById(R.id.avatar);
        TextView name = convertView.findViewById(R.id.name);
        ImageView control = convertView.findViewById(R.id.control);


        User user = getItem(position);

        String userId = user.getId();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(userId + "/image");
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    Picasso.get()
                            .load(uri)
                            .into(avatar);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                avatar.setImageResource(R.drawable.user);
//                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

        int numberWidth = String.valueOf(getCount()).length(); // Ширина самого большого номера
        String formattedNumber = String.format("%0" + numberWidth + "d", position + 1);
        number.setText(formattedNumber);

        name.setText(user.getName());
        control.setOnClickListener(view -> {

            showPopupMenu(view, position);
        });

        return convertView;
    }


    public void setDatabaseReference(DatabaseReference ref) {
        listIdRef = ref;
    }

    public void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.user_popup_menu, popupMenu.getMenu());
        User currentUser = getItem(position);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            boolean needNotify = false;
            if (id == R.id.action_edit) {
                ChangeNameDialog.showDialog(getContext(), UserListAdapter.this, position);

            } else if (id == R.id.action_delete) {
                remove(currentUser);
                needNotify = true;
            } else if (id == R.id.action_end) {
                remove(currentUser);
                add(currentUser);
                needNotify = true;
            } else if (id == R.id.action_start) {
                remove(currentUser);
                insert(currentUser, 0);
//                add(currentUser);
                needNotify = true;
            } else if (id == R.id.action_exchange) {
                // TODO: 05.05.2024
//                remove(currentUser);
//                add(currentUser);
//                needNotify = true;
            } else if (id == R.id.action_skip) {
                if (position + 1 <= getCount() - 1) {
                    remove(currentUser);
                    int newPosition = position + 1;
                    insert(currentUser, newPosition);
                    needNotify = true;
                }
            }

            if (needNotify) {
//                notifyDataSetChanged();
//                Gson gson = new Gson();
//                String userListJson = gson.toJson(users);
//                databaseRef.setValue(userListJson);
                Log.d(TAG, "showPopupMenu: " + listIdRef);
                notifyChanges();
//                listIdRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        Log.d(TAG, "showPopupMenu: "+dataSnapshot);
//
//                        if (dataSnapshot.exists()) {
//                            notifyDataSetChanged();
//                            Gson gson = new Gson();
//                            String userListJson = gson.toJson(users);
//                            listIdRef.setValue(userListJson);
//                        } else {
//                            Toast.makeText(getContext(),"Кажется очередь была удалена", Toast.LENGTH_LONG).show();
//                            listIdRef.removeValue();
//                            getContext().startActivity(new Intent(getContext(), AvailableListsActivity.class));
//                        }
//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                    }
//                });

                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public void onChangeName(String name, int position) {
        User currentUser = getItem(position);
        currentUser.setName(name);
        notifyChanges();

        //                currentUser.setName("edit");
    }

    private void notifyChanges() {
        listIdRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "showPopupMenu: " + dataSnapshot);

                if (dataSnapshot.exists()) {
                    notifyDataSetChanged();
                    Gson gson = new Gson();
                    String userListJson = gson.toJson(users);
                    listIdRef.child("data").setValue(userListJson);
                } else {
                    Toast.makeText(getContext(), "Кажется очередь была удалена", Toast.LENGTH_LONG).show();
                    listIdRef.removeValue();
                    getContext().startActivity(new Intent(getContext(), AvailableListsActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
