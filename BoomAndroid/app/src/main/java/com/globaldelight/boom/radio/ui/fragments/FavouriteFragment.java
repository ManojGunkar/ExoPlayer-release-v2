package com.globaldelight.boom.radio.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.ui.adapter.RadioListAdapter;
import com.globaldelight.boom.radio.utils.SaveFavouriteRadio;
import com.globaldelight.boom.radio.webconnector.responsepojo.RadioStationResponse;

import java.util.List;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class FavouriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private RadioListAdapter mAdapter;
    private List<RadioStationResponse.Content> mContents;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);

        return recyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        mContents= SaveFavouriteRadio.getInstance(getContext()).getFavRadioStation();
        mAdapter = new RadioListAdapter(getActivity(),null,mContents);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}
