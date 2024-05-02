package com.example.coursework;

//import static androidx.core.content.ContextCompat.getSystemService;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.google.gson.Gson;

import java.util.List;

public class AvailableListsAdapter extends ArrayAdapter<String> {
    List<String> listsId;

    public AvailableListsAdapter(@NonNull Context context, List<String> listsName, List<String> listsId) {
        super(context,0,listsName);
        this.listsId=listsId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        
        TextView number = convertView.findViewById(R.id.number);
        TextView name = convertView.findViewById(R.id.name);
        ImageView control = convertView.findViewById(R.id.control);

        number.setText(Integer.toString(position+1));
        name.setText(getItem(position));

//        User user = getItem(position);
//
//        int numberWidth = String.valueOf(getCount()).length(); // Ширина самого большого номера
//        String formattedNumber = String.format("%0" + numberWidth + "d", position + 1);
//        number.setText(formattedNumber);
//
//        name.setText(user.getName());
        control.setOnClickListener(view -> {
            showPopupMenu(view, position);
        });

        return convertView;
    }



    public void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.available_lists_popup_menu, popupMenu.getMenu());
        String currentQueue = getItem(position);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            boolean needNotify = false;
            if (id == R.id.action_get) {
//                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                // Создаем объект ClipData с нужным текстом
                ClipData clip = ClipData.newPlainText("label", listsId.get(position));
                // Копируем ClipData в буфер обмена
                clipboard.setPrimaryClip(clip);
                // Выводим уведомление о копировании
//                Toast.makeText(getContext(), "Текст скопирован в буфер обмена", Toast.LENGTH_SHORT).show();
            }
            else if (id == R.id.action_quit) {
                // TODO: 02.05.2024 удалить список из доступных пользователю
                remove(currentQueue);
                needNotify = true;
            }
            else if (id == R.id.action_delete) {
                // TODO: 02.05.2024 удалить список, и его id у всех участников 
                remove(currentQueue);
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
