package com.example.coursework;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public class ChangeNameDialog {

    // TODO: 02.05.2024 поработать с внешним видом, возможно перейти на фрагменты

    public interface DialogCallback {
        void onChangeName(String name, int position);

    }

    public static void showDialog(Context context, final DialogCallback callback, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Введите имя");

        // Set up the input
        final EditText input = new EditText(context);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Редактировать имя", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    callback.onChangeName(name, position);
                }
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
