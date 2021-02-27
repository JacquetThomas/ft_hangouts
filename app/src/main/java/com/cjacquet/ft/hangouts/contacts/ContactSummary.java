package com.cjacquet.ft.hangouts.contacts;

public class ContactSummary {
    private String index;
    private int id;
    private String name;
    private String lastname;
    private String phoneNumber;
    private boolean fav;

    public ContactSummary(String index, int id, String name, String lastname, String phoneNumber, boolean fav) {
        this.index = index;
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.phoneNumber = phoneNumber;
        this.fav = fav;
    }

    public String getIndex() {
        return index;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isFav() {
        return fav;
    }
}
