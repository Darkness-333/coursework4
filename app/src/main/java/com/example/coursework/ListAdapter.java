package com.example.coursework;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
//public class ListAdapter extends RecyclerView.Adapter<User>{
public class ListAdapter extends ArrayAdapter<User> {

//    private ImageView avatar;
//    private TextView name;
//    private ImageView control;

    public ListAdapter(@NonNull Context context, List<User> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

//        avatar = convertView.findViewById(R.id.avatar);
//        name = convertView.findViewById(R.id.name);
//        control = convertView.findViewById(R.id.control);

        TextView number=convertView.findViewById(R.id.number);
        ImageView avatar = convertView.findViewById(R.id.avatar);
        TextView name = convertView.findViewById(R.id.name);
        ImageView control = convertView.findViewById(R.id.control);

        User user = getItem(position);



        int numberWidth = String.valueOf(getCount()).length(); // Ширина самого большого номера
        String formattedNumber = String.format("%0" + numberWidth + "d", position + 1);
        number.setText(formattedNumber);
//        number.setText(position+1);


        name.setText(user.getName());
        control.setOnClickListener(view -> {
            showPopupMenu(view, position);

        });

//        if (user != null) {
////            String formattedItemNumber = String.format("%02d", itemNumber);
//            int numberWidth = String.valueOf(getCount()).length(); // Ширина самого большого номера
//            String formattedNumber = String.format("%0" + numberWidth + "d", position + 1);
//            number.setText(formattedNumber);
////            number.setText(Integer.toString(position+1));
////            avatar.setImageResource(user.getImageResource());
//            name.setText(user.getName());
//            control.setOnClickListener(view -> {
//                    showPopupMenu(view, position);
//
//            });
//        }

        return convertView;
    }

    public void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        User currentUser=getItem(position);

        popupMenu.setOnMenuItemClickListener(item -> {
            int id=item.getItemId();
            if (id == R.id.action_edit) {
                currentUser.setName("edit");
                notifyDataSetChanged();
                return true;
            } else if (id == R.id.action_delete) {
                remove(currentUser);
                notifyDataSetChanged();
                return true;
            }
            else if(id == R.id.action_end){
                remove(currentUser);
                add(currentUser);
                notifyDataSetChanged();
                return true;
            }

            else if(id==R.id.action_skip){
                remove(currentUser);

                // Вычисляем позицию, на которую нужно переместить элемент
                int newPosition = position + 1;

                // Вставка элемента на новую позицию
                insert(currentUser, newPosition);

                notifyDataSetChanged();
                return true;
            }
            return false;


        });

        popupMenu.show();
    }
}
