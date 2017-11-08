package com.acculoc;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Prateek on 11/8/2017.
 */

public class AmbulanceSkeleton {
    String uid;
    String name;
    public double lat,lon;
    String phone;
    String email;
    String registrationNumber;
    public AmbulanceSkeleton(){}
    public AmbulanceSkeleton(String uid,String name,String phone){
        this.uid=uid;
        this.name=name;
        this.phone=phone;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


}
