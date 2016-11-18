package com.globaldelight.boom.utils;

/**
 * Created by nidhin on 2/11/16.
 */

public class HeadSetType {

    int imageResource, position;
    int imageActiveResource;
    String Title;
    int textColor;
    int textColoreActive;
    boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }


    public int getImageActiveResource() {
        return imageActiveResource;
    }

    public void setImageActiveResource(int imageActiveResource) {
        this.imageActiveResource = imageActiveResource;
    }

    public int getTextColoreActive() {
        return textColoreActive;
    }

    public void setTextColoreActive(int textColoreActive) {
        this.textColoreActive = textColoreActive;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
