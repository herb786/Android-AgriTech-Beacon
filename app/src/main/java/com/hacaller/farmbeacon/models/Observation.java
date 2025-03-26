package com.hacaller.farmbeacon.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hacaller.farmbeacon.models.AdvertisedId;

/**
 * Created by Herbert Caller on 21/08/2018.
 */
public class Observation {

    @SerializedName("advertisedId")
    @Expose
    private AdvertisedId advertisedId;

    public AdvertisedId getAdvertisedId() {
        return advertisedId;
    }

    public void setAdvertisedId(AdvertisedId advertisedId) {
        this.advertisedId = advertisedId;
    }

}
