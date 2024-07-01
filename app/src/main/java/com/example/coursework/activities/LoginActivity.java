package com.example.coursework.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.coursework.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonSignIn;
    private Button buttonSignUp;
    ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Инициализация Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        editTextEmail = binding.editTextEmail;
        editTextPassword = binding.editTextPassword;
        buttonSignIn = binding.buttonSignIn;
        buttonSignUp = binding.buttonSignUp;

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                if (!email.isEmpty() && !password.isEmpty())
                    signIn(email, password);
            }
        });
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                if (!email.isEmpty() && !password.isEmpty())
                    signUp(email, password);
//                signUp(editTextEmail.getText().toString(), editTextPassword.getText().toString());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent=getIntent();
        if (!intent.hasExtra("fromAct")){
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        }
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            // Пользователь авторизован
            startActivity(new Intent(this, AvailableListsActivity.class));
            finish(); // Закрываем текущую активность, чтобы пользователь не мог вернуться по кнопке "назад"
        }
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Успешная аутентификация
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Переход к другой активности
                                Intent intent = new Intent(LoginActivity.this, AvailableListsActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // Аутентификация не удалась
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Успешная регистрация
                            Toast.makeText(LoginActivity.this, "Registration successful.",
                                    Toast.LENGTH_SHORT).show();
                        }
//                        else {
//                            // Регистрация не удалась
//                            Toast.makeText(LoginActivity.this, "Registration failed.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(binding.getRoot(), "Registration failed: "+e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}
