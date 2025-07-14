package com.example.shiftscheduler;

public class User {
    private String email;
    private String name;
    private boolean admin;
    private String dateOfBirth;
    private long salary;


    public User() {
    }

    public User(String email,String name, boolean admin, String dateOfBirth, long salary) {
        this.email = email;
        this.name = name;
        this.admin = admin;
        this.dateOfBirth = dateOfBirth;
        this.salary = salary;

    }

    // Getters
    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public long getSalary() {
        return salary;
    }

    public String getName(){
        return name;
    }

    // Setters (if you need them, e.g., for manual updates)


    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setSalary(long salary) {
        this.salary = salary;
    }
    public void  setName(String name){
        this.name = name;
    }


}
