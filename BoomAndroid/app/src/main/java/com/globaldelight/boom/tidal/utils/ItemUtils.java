package com.globaldelight.boom.tidal.utils;

import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.ItemWrapper;

import java.util.List;

/**
 * Created by adarsh on 25/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 *
 * Utility methods for item
 */
public class ItemUtils {

    public static boolean isUserPlaylist(Item item) {
        return item.getItemType() == ItemType.PLAYLIST && item.getType().equals("USER");
    }

    public static String pathForTracks(Item item) {
        switch (item.getItemType()) {
            case ItemType.ALBUM:
                return "albums/"+item.getId()+"/tracks";

            case ItemType.ARTIST:
                return "artists/"+item.getId()+"/toptracks";

            case ItemType.PLAYLIST:
                return "playlists/"+item.getId()+"/items";
        }

        return "";
    }

    public static int getTrackCount(Item item) {
        int count = 999;
        Integer maxItems = item.getNumberOfTracks();
        if (maxItems != null) {
            count = maxItems.intValue();
        }
        return count;
    }


    public static List<Item> itemsFromWrapper(List<ItemWrapper> wrapper) {
        return null;
    }

    private ItemUtils() {

    }
}
