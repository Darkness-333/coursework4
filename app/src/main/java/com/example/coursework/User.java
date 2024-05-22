package com.example.coursework;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String id;
    private boolean isAdmin=false;

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean getAdmin(){
        return isAdmin;
    }

    public User(){}
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

}
