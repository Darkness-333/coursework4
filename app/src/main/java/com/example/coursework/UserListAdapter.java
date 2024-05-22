package com.example.coursework;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import com.example.coursework.activities.AvailableListsActivity;
import com.example.coursework.dialogs.ChangeNameDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserListAdapter extends ArrayAdapter<User> implements ChangeNameDialog.DialogCallback {

    //    private ImageView avatar;
//    private TextView name;
//    private ImageView control;
    String TAG = "mylogs";

    // TODO: 03.05.2024 добавить права пользователя и админа 

    List<User> users;
    DatabaseReference listIdRef;
    DatabaseReference usersRef;

    boolean workWithUsers = true;
    boolean isAdmin = false;
    String userId;
    FirebaseDatabase database;
    Gson gson;

    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
    }


    public void setWorkWithUsers(boolean state) {
        workWithUsers = state;
    }
//    int position;

    public UserListAdapter(@NonNull Context context, List<User> users) {
        super(context, 0, users);
        this.users = users;
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        gson = new Gson();
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

        User currentUser = getItem(position);

        String currentUserId = currentUser.getId();

        if (currentUserId.equals(userId)) {
            number.setTextColor(Color.parseColor("#00008B"));
            nameView.setTextColor(Color.parseColor("#00008B"));
            number.setTextSize(18);
            nameView.setTextSize(18);
            // Изменить стиль текста имени (например, сделать жирным)
//            nameView.setTypeface(nameView.getTypeface(), Typeface.BOLD);
        } else {
            number.setTextColor(Color.parseColor("#000000"));
            nameView.setTextColor(Color.parseColor("#000000"));
            number.setTextSize(16);
            nameView.setTextSize(16);
        }

        if (!isAdmin && !workWithUsers) {
            control.setVisibility(View.INVISIBLE);
        }

        if (!isAdmin && !currentUserId.equals(userId)) {
            control.setVisibility(View.INVISIBLE);
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(currentUserId + "/image");
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        try {
                            Picasso.get()
                                    .load(uri)
                                    .resize(100, 100)
                                    .centerCrop()
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

        String name = currentUser.getName();
        if (name == null) {
            usersRef.child(currentUserId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
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
            nameView.setText(currentUser.getName());
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
            if (!isAdmin) {
                Menu menu = popupMenu.getMenu();
//                menu.findItem(R.id.action_delete).setVisible(false);
                menu.findItem(R.id.action_start).setVisible(false);
            }
//            popupMenu.getMenu().findItem(R.id.action_delete).setVisible(false);
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                boolean needNotify = false;
//                if (id == R.id.action_edit) {
//                    ChangeNameDialog.showDialog(getContext(), UserListAdapter.this, position);
//                } else
                if (id == R.id.action_delete) {
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
        } else {
            popupMenu.getMenuInflater().inflate(R.menu.member_popup_menu, popupMenu.getMenu());
            MenuItem itemDel = popupMenu.getMenu().findItem(R.id.action_delete);
            SpannableString spannableString = new SpannableString(itemDel.getTitle());
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, spannableString.length(), 0); // Задаем цвет
            itemDel.setTitle(spannableString);
//            пока что не нужно
//            if (!isAdmin){
//                Menu menu=popupMenu.getMenu();
//                menu.findItem(R.id.action_delete).setVisible(false);
//            }
            popupMenu.setOnMenuItemClickListener(item -> {


                int id = item.getItemId();
                User selectedUser = users.get(position);
                String selectedUserId = selectedUser.getId();
                String selectedUserName = selectedUser.getName();
                if (id == R.id.action_add) {
                    User newUser = new User();
                    listIdRef.child("data").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String data = snapshot.getValue(String.class);
                            Type userListType = new TypeToken<List<User>>() {
                            }.getType();
                            List<User> updatedUserList = gson.fromJson(data, userListType);

                            newUser.setName(selectedUserName);
                            newUser.setId(selectedUserId);

                            // поиск пользователя в списке
                            boolean isUserInList = false;
                            for (User user : updatedUserList) {
                                if (user.getId() != null && user.getId().equals(selectedUserId)) {
                                    isUserInList = true;
                                    break;
                                }
                            }
                            if (!isUserInList) {
                                Gson gson = new Gson();
                                updatedUserList.add(newUser);
                                notifyDataSetChanged();
                                String userListJson = gson.toJson(updatedUserList);
                                listIdRef.child("data").setValue(userListJson);
                                Toast.makeText(getContext(), "Добавлен", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Уже в очереди", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                } else if (id == R.id.action_admin) {
                    DatabaseReference membersRef = listIdRef.child("members");
                    membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                List<User> members;
                                String membersJson = snapshot.getValue(String.class);
                                Type userListType = new TypeToken<ArrayList<User>>() {
                                }.getType();
                                members = gson.fromJson(membersJson, userListType);
                                for(User member:members){
                                    if (member.getId().equals(selectedUserId)){
                                        member.setAdmin(true);
                                    }
                                }
                                String newMembersJson = gson.toJson(members);
                                membersRef.setValue(newMembersJson);

                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

                } else if (id == R.id.action_delete) {


                    DatabaseReference membersRef = listIdRef.child("members");
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
//                                    DatabaseReference listRef = database.getReference("lists").child(listId);
                                    listIdRef.removeValue();
                                } else {
                                    Iterator<User> iterator = members.iterator();
                                    while (iterator.hasNext()) {
                                        User member = iterator.next();
                                        if (member.getId().equals(selectedUserId)) {
                                            iterator.remove();
                                        }
                                    }
                                    String newMembersJson = gson.toJson(members);
                                    membersRef.setValue(newMembersJson);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
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
