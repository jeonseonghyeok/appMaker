package com.example.developer.appmaker;

import android.graphics.drawable.Drawable;

public class ListViewItem {
    private Drawable iconDrawable;
    private String restaurantName;
    private String restaurantGrade;
    private String restaurantAddress;
    private String restaurantTelephone;
    ListViewItem(Drawable icon, String restaurantName, String restaurantGrade,String restaurantAddress,String restaurantTelephone){
        this.iconDrawable=icon;
        this.restaurantName=restaurantName;
        this.restaurantGrade=restaurantGrade;
        this.restaurantAddress=restaurantAddress;
        this.restaurantTelephone=restaurantTelephone;
    }
    public void setIcon(Drawable icon) {
        iconDrawable = icon;
    }

    public void setTitle(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public void setDesc(String restaurantGrade) {
        this.restaurantGrade = restaurantGrade;
    }

    public Drawable getIcon() {
        return this.iconDrawable;
    }

    public String getTitle() {
        return this.restaurantName;
    }

    public String getDesc() {
        return this.restaurantGrade;
    }

    public String getAddress() {
        return restaurantAddress;
    }

    public String getTelephone() {
        return restaurantTelephone;
    }
}

