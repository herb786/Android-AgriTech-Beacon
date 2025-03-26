package com.hacaller.farmbeacon.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Herbert Caller on 20/08/2018.
 */
public class ProximityRequest implements Serializable {

    @SerializedName("observations")
    @Expose
    private List<Observation> observations = null;
    @SerializedName("namespacedTypes")
    @Expose
    private List<String> namespacedTypes = null;

    public List<Observation> getObservations() {
        return observations;
    }

    public void setObservations(List<Observation> observations) {
        this.observations = observations;
    }

    public List<String> getNamespacedTypes() {
        return namespacedTypes;
    }

    public void setNamespacedTypes(List<String> namespacedTypes) {
        this.namespacedTypes = namespacedTypes;
    }
}
