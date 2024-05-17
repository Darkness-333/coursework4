package com.example.coursework.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.coursework.R;
import com.example.coursework.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> pickImageLauncher;

    private ImageView avatarImageView;
    private TextView nameTextView;
    private Button changeNameButton;
    private Button changeAvatarButton;
    private Button changeAccountButton;
    private EditText editTextName;
    private Button quitButton;
    ActivityProfileBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Привязка элементов макета к переменным
        editTextName = binding.editTextName;
        avatarImageView = binding.avatarImageView;
        nameTextView = binding.nameTextView;
        changeNameButton = binding.changeNameButton;
        changeAvatarButton = binding.changeAvatarButton;
        changeAccountButton = binding.changeAccountButton;
        quitButton = binding.quitButton;


        ActionBar actionBar;

        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#708AFF"));
        actionBar.setBackgroundDrawable(colorDrawable);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String userId = firebaseUser.getUid();

        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(userId + "/image");

        DatabaseReference userNameRef = database.getReference("users").child(userId).child("name");

        Bitmap loadBitmap = loadImageLocally("image_" + userId);
        if (loadBitmap != null) {
            avatarImageView.setImageBitmap(loadBitmap);
        }

        userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = (String) snapshot.getValue();
                    nameTextView.setText(name);
                } else {
                    nameTextView.setText("Пользователь");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        try {
//                            Picasso.get()
//                                    .load(uri)
//                                    .into(avatarImageView);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//
//                    }
//                });

        // Обработчик нажатия кнопки смены имени
        changeNameButton.setOnClickListener(v -> {
            String newName = editTextName.getText().toString();
            if (!newName.isEmpty()) {
                nameTextView.setText(newName);
                userNameRef.setValue(newName);
            }
        });

        // Обработчик нажатия кнопки смены аватарки
        changeAvatarButton.setOnClickListener(v -> {
            openImageChooser();
        });

        changeAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("fromAct", true);
            startActivity(intent);
        });

        quitButton.setOnClickListener(view -> finish());

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            avatarImageView.setImageBitmap(bitmap);
                            saveImageLocally(bitmap, "image_" + userId);
                            imageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                            Toast.makeText(getApplicationContext(), "Картинка сохранена", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                                        }
                                    });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(Intent.createChooser(intent, "Выберите изображение"));
    }

    private void saveImageLocally(Bitmap bitmap, String fileName) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap loadImageLocally(String fileName) {
        FileInputStream fis = null;
        try {
            fis = openFileInput(fileName);
            return BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent(this, AboutActivity.class);
        if (id == R.id.about_author) {
            intent.putExtra("info", "author");
        } else if (id == R.id.about_app) {
            intent.putExtra("info", "app");
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }
}
