package com.globaldelight.boom.tidal.tidalconnector.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by adarsh on 22/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class ItemWrapper {
    @SerializedName("item")
    @Expose
    private Item item;

    @SerializedName("type")
    @Expose
    private String type;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
