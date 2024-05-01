package com.example.coursework;

public class User {
    private String name;
    private int avatarImage;
    private String id;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAvatarImage(int avatarImage) {
        this.avatarImage = avatarImage;
    }
}
