package com.globaldelight.boom.webapiconnector;

import com.globaldelight.boom.Constants;
import com.globaldelight.boom.player.AudioEffect;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by adarsh on 16/02/18.
 */

public class Headset {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("device_type")
    @Expose
    private String deviceType;

    public String getName() {
        return name;
    }

    public @Constants.Headphone int getType() {
        switch (deviceType) {
            case "in_canal":
                return Constants.Headphone.IN_CANAL;

            case "in_ear":
                return Constants.Headphone.IN_EAR;

            case "on_ear":
                return Constants.Headphone.ON_EAR;

            default:
                return Constants.Headphone.OVER_EAR;
        }
    }
}
