package com.globaldelight.boom.webapiconnector;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Kumar on 12-02-2018.
 */

public class ResponseBody {
    @SerializedName("_id")
    @Expose
    private Id id;
    @SerializedName("appid")
    @Expose
    private String appid;
    @SerializedName("app_active_state")
    @Expose
    private Boolean appActiveState;
    @SerializedName("supported_headphones")
    @Expose
    private List<Headset> supportedHeadphones = null;

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public Boolean getAppActiveState() {
        return appActiveState;
    }

    public void setAppActiveState(Boolean appActiveState) {
        this.appActiveState = appActiveState;
    }

    public List<Headset> getSupportedHeadphones() {
        return supportedHeadphones;
    }

    public void setSupportedHeadphones(List<Headset> supportedHeadphones) {
        this.supportedHeadphones = supportedHeadphones;
    }


    public class Id {

        @SerializedName("$id")
        @Expose
        private String $id;

        public String get$id() {
            return $id;
        }

        public void set$id(String $id) {
            this.$id = $id;
        }
    }
}
