package com.manoj.youtube.utils;

import com.manoj.youtube.modal.YoutubeModal;

import java.util.ArrayList;

/**
 * Created by Manoj on 11/1/2016.
 */
public class DataHolder {

    private final static DataHolder ourInstance = new DataHolder();
    private ArrayList<YoutubeModal.Item> list;

    private DataHolder() {
    }

    public static DataHolder getInstance() {
        return ourInstance;
    }

    public ArrayList<YoutubeModal.Item> getList() {
        return list;
    }

    public void setList(ArrayList<YoutubeModal.Item> list) {
        this.list = list;
    }
}
