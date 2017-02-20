package com.globaldelight.boom.ui.musiclist.adapter.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.handlers.MusicSearchHelper;

/**
 * Created by Rahul Agarwal on 14-12-16.
 */

public class SearchSuggestionAdapter extends SimpleCursorAdapter {
    private Context context;
    private LayoutInflater cursorInflater;
    public SearchSuggestionAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context=context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        RegularTextView textView=(RegularTextView) view.findViewById(R.id.searchItem);
        String title = cursor.getString(1);
        textView.setText(title);
    }
}
