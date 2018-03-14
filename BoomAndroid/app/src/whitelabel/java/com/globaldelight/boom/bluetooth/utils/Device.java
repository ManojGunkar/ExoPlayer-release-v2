package com.globaldelight.boom.bluetooth.utils;

/**
 * Created by Manoj Kumar on 16-01-2018.
 */

public class Device {

    private String name;
    private String mMacAddress;
    private int state;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return mMacAddress;
    }

    public void setMacAddress(String macAddress) {
        this.mMacAddress = macAddress;
    }
}
