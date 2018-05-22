package com.globaldelight.boom.tidal.tidalconnector.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by adarsh on 22/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class ItemCollection<T> {
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
    private List<T> items = null;

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

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
