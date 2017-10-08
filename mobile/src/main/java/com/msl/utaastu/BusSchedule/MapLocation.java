package com.msl.utaastu.BusSchedule;

import android.support.annotation.Keep;

import com.google.android.gms.maps.model.LatLng;

@Keep
public class MapLocation {
    private String name;
    private LatLng center;

    public MapLocation(String name, double lat, double lng) {
        this.name = name;
        this.center = new LatLng(lat, lng);
    }

    public LatLng getCenter() {
        return center;
    }

    public String getName() {
        return name;
    }
}