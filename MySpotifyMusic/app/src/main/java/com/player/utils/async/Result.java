package com.player.utils.async;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
//

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;


public final class Result implements Iterable<Action<?>> {
    private HashMap<String, Action<?>> mMap = new HashMap();

    protected Result() {
    }

    @Nullable
    public Action<?> get(String id) {
        return (Action)this.mMap.get(id);
    }

    protected void put(@NonNull Action action) {
        if(action.id() != null) {
            this.mMap.put(action.id(), action);
        }
    }

    @Nullable
    public String[] ids() {
        return this.mMap.size() == 0?null:(String[])this.mMap.keySet().toArray(new String[this.mMap.keySet().size()]);
    }

    public Iterator<Action<?>> iterator() {
        return this.mMap.values().iterator();
    }
}
