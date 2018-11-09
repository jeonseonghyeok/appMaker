package com.example.developer.appmaker;

import android.graphics.drawable.Drawable;

public class ListViewItem {
    private Drawable iconDrawable;
    private String restaurantName;
    private String restaurantGrade;
    ListViewItem(Drawable icon, String restaurantName, String restaurantGrade){
        this.iconDrawable=icon;
        this.restaurantName=restaurantName;
        this.restaurantGrade=restaurantGrade;
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
}

