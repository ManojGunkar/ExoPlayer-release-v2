package com.globaldelight.boom.app.adapters.search;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.globaldelight.boom.R;

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
        TextView textView=(TextView) view.findViewById(R.id.searchItem);
        String title = cursor.getString(1);
        textView.setText(title);
    }
}
