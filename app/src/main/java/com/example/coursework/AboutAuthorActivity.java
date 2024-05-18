package com.example.coursework;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.coursework.databinding.ActivityAboutAuthorBinding;
import com.example.coursework.databinding.ActivityAboutProgramBinding;

public class AboutAuthorActivity extends AppCompatActivity {

    ActivityAboutAuthorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutAuthorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.back.setOnClickListener(view -> finish());
    }
}