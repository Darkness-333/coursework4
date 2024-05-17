package com.example.coursework.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public class AddQueueDialog {

    // TODO: 02.05.2024 поработать с внешним видом, возможно перейти на фрагменты

    public interface DialogCallback {
        void onAddQueue(String name);

    }

    public static void showDialog(Context context, final DialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Введите id");

        // Set up the input
        final EditText input = new EditText(context);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    callback.onAddQueue(name);
                }
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
