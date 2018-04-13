package com.globaldelight.boom.radio.utils;

import com.globaldelight.boom.radio.webconnector.responsepojo.RadioStationResponse;

import java.util.Comparator;

/**
 * Created by Manoj Kumar on 13-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class ContentComparator implements Comparator<RadioStationResponse.Content> {
    @Override
    public int compare(RadioStationResponse.Content content1, RadioStationResponse.Content content2) {
        return content1.getName().compareTo(content2.getName());
    }
}
