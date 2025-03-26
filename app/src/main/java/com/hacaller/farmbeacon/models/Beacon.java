package com.hacaller.farmbeacon.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Herbert Caller on 21/08/2018.
 */
public class Beacon {

    @SerializedName("advertisedId")
    @Expose
    private AdvertisedId advertisedId;
    @SerializedName("beaconName")
    @Expose
    private String beaconName;
    @SerializedName("attachments")
    @Expose
    private List<Attachment> attachments = null;

    public AdvertisedId getAdvertisedId() {
        return advertisedId;
    }

    public void setAdvertisedId(AdvertisedId advertisedId) {
        this.advertisedId = advertisedId;
    }

    public String getBeaconName() {
        return beaconName;
    }

    public void setBeaconName(String beaconName) {
        this.beaconName = beaconName;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

}