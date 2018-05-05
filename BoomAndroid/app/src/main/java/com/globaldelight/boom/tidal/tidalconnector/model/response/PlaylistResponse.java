package com.globaldelight.boom.tidal.tidalconnector.model.response;

import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Kumar on 05-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class PlaylistResponse {

    @SerializedName("limit")
    @Expose
    private Integer limit;
    @SerializedName("offset")
    @Expose
    private Integer offset;
    @SerializedName("totalNumberOfItems")
    @Expose
    private Integer totalNumberOfItems;
    @SerializedName("items")
    @Expose
    private List<Items> items = null;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getTotalNumberOfItems() {
        return totalNumberOfItems;
    }

    public void setTotalNumberOfItems(Integer totalNumberOfItems) {
        this.totalNumberOfItems = totalNumberOfItems;
    }

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }

    public class Items {

        @SerializedName("item")
        @Expose
        private Item item;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("cut")
        @Expose
        private Object cut;

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

        public Object getCut() {
            return cut;
        }

        public void setCut(Object cut) {
            this.cut = cut;
        }

    }

}
