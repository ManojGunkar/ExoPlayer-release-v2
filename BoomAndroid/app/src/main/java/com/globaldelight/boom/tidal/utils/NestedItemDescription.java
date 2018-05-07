package com.globaldelight.boom.tidal.utils;

import com.globaldelight.boom.tidal.tidalconnector.model.Item;

import java.util.List;

/**
 * Created by adarsh on 03/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class NestedItemDescription<T> {
    public static final int LIST_VIEW = 0;
    public static final int GRID_VIEW = 1;

    public int titleResId;
    public int type;
    public List<T> itemList;
    public String apiPath;

    public NestedItemDescription(int titleResId, int type, List<T> items,String apiPath) {
        this.titleResId = titleResId;
        this.type = type;
        this.apiPath=apiPath;
        this.itemList = items;
    }
}
