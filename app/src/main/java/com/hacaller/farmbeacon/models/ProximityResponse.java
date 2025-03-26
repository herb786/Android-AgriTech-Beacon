package com.hacaller.farmbeacon.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Herbert Caller on 20/08/2018.
 */
public class ProximityResponse implements Serializable {

    @SerializedName("beacons")
    @Expose
    private List<Beacon> beacons = null;

    public List<Beacon> getBeacons() {
        return beacons;
    }

    public void setBeacons(List<Beacon> beacons) {
        this.beacons = beacons;
    }

}
