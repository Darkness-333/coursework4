package com.example.coursework;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.database.FirebaseDatabase;
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
    DatabaseReference usersRef;

    boolean workWithUsers=true;

    public void setWorkWithUsers(boolean state) {
        workWithUsers = state;
    }
//    int position;

    public UserListAdapter(@NonNull Context context, List<User> users) {
        super(context, 0, users);
        this.users = users;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
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
        TextView nameView = convertView.findViewById(R.id.name);
        ImageView control = convertView.findViewById(R.id.control);

        boolean isConnected = NetworkChangeReceiver.isConnected;
        if (isConnected) {
            control.setVisibility(View.VISIBLE);
        } else {
            control.setVisibility(View.INVISIBLE);
        }

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
                    }
                });

        int numberWidth = String.valueOf(getCount()).length(); // Ширина самого большого номера
        String formattedNumber = String.format("%0" + numberWidth + "d", position + 1);
        number.setText(formattedNumber);

        String name = user.getName();
        if (name == null) {
            usersRef.child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userName = snapshot.getValue(String.class);
                    nameView.setText(userName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else
            nameView.setText(user.getName());
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
        User currentUser = getItem(position);
        if (workWithUsers) {
            popupMenu.getMenuInflater().inflate(R.menu.user_popup_menu, popupMenu.getMenu());
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
                    notifyUserChanges();
                    return true;
                }
                return false;
            });
        }
        else{
            popupMenu.getMenuInflater().inflate(R.menu.member_popup_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                return true;
            });
        }
        popupMenu.show();
    }

    @Override
    public void onChangeName(String name, int position) {
        User currentUser = getItem(position);
        currentUser.setName(name);
        notifyUserChanges();

    }

    private void notifyUserChanges() {
        listIdRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
