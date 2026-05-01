package com.govsim.govsim.model;

/** Represents a system user */

public class User {
    private int id;
    private String username;
    private String password;

    /** Creates a new User */
    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    // Getters
    public int getId(){
        return  id;
    }

    public  String getUsername(){
        return  username;
    }

    public  String getPassword(){
        return  password;
    }

    public void setId(int id){
        this.id = id;
    }

    // Export
    @Override
    public  String  toString() {
        return "User [" + id + "] " + username;
    }

}
