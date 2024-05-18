package com.example.coursework.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.coursework.R;

import com.example.coursework.databinding.ActivityAboutProgramBinding;

public class AboutProgramActivity extends AppCompatActivity {

    ActivityAboutProgramBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutProgramBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.back.setOnClickListener(view -> finish());
    }
}