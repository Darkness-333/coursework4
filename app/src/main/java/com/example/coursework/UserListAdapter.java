package com.example.coursework;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;

import java.util.List;

public class UserListAdapter extends ArrayAdapter<User> {

//    private ImageView avatar;
//    private TextView name;
//    private ImageView control;

    // TODO: 03.05.2024 добавить права пользователя и админа 

    List<User> users;
    DatabaseReference databaseRef;

    public UserListAdapter(@NonNull Context context, List<User> users) {
        super(context, 0, users);
        this.users=users;
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

        int numberWidth = String.valueOf(getCount()).length(); // Ширина самого большого номера
        String formattedNumber = String.format("%0" + numberWidth + "d", position + 1);
        number.setText(formattedNumber);

        name.setText(user.getName());
        control.setOnClickListener(view -> {
            showPopupMenu(view, position);
        });

        return convertView;
    }


    public void setDatabaseReference(DatabaseReference ref){
        databaseRef=ref;
    }

    public void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.user_popup_menu, popupMenu.getMenu());
        User currentUser = getItem(position);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            boolean needNotify = false;
            if (id == R.id.action_edit) {
                currentUser.setName("edit");
                needNotify = true;
            }
            else if (id == R.id.action_delete) {
                remove(currentUser);
                needNotify = true;
            }
            else if (id == R.id.action_end) {
                remove(currentUser);
                add(currentUser);
                needNotify = true;
            }
            else if (id == R.id.action_skip) {
                if (position+1<=getCount()-1){
                    remove(currentUser);
                    int newPosition = position + 1;
                    insert(currentUser, newPosition);
                    needNotify = true;
                }
            }

            if (needNotify) {
                notifyDataSetChanged();
                Gson gson = new Gson();
                String userListJson = gson.toJson(users);
                databaseRef.setValue(userListJson);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }
}
