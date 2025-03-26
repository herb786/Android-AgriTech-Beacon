package com.hacaller.farmbeacon.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Herbert Caller on 21/08/2018.
 */
public class Attachment {

    @SerializedName("namespacedType")
    @Expose
    private String namespacedType;
    @SerializedName("data")
    @Expose
    private String data;

    public String getNamespacedType() {
        return namespacedType;
    }

    public void setNamespacedType(String namespacedType) {
        this.namespacedType = namespacedType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}