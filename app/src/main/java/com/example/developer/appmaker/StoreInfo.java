package com.example.developer.appmaker;

import com.google.android.gms.maps.model.LatLng;

public class StoreInfo {
    LatLng gpsPosition;
    String name;
    float grade;
    StoreInfo(LatLng gpsPosition, String name, float grade){
        this.gpsPosition=gpsPosition;
        this.name=name;
        this.grade=grade;
    }
}
