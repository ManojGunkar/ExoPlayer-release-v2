package com.manoj.youtube;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.exoplayer2.demo.PlayerActivity;
import com.google.android.exoplayer2.demo.R;
import com.google.android.exoplayer2.demo.SampleChooserActivity;
import com.manoj.youtube.adapter.MyListAdapter;
import com.manoj.youtube.modal.YoutubeModal;
import com.manoj.youtube.utils.Config;
import com.manoj.youtube.utils.DataHolder;
import com.manoj.youtube.web.RestClient;

import java.util.ArrayList;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;

/**
 * Created by Manoj on 11/01/2016.
 */
public class MainVideoListActivity extends AppCompatActivity {

    private static final String DEFAULT_SEARCH_QUERY = "Trending Music";
    private static final String SEARCH_HISTORY_KEY = "search_history_key";

    private ListView mListVIew;
    private MyListAdapter mAdapter;
    private ProgressDialog mDialog;
    private ArrayList<YoutubeModal.Item> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_video_list);

        mListVIew = (ListView) findViewById(R.id.list_home);
        mListVIew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String videoId = mList.get(position).getId().getVideoId();
                loadYoutubeURL("https://www.youtube.com/watch?v="+videoId);
            }
        });
//        if (isNetworkConnected()){
//              getYoutubeFeeds();
//        }
//        else {
//            Toast.makeText(MainVideoListActivity.this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
//            if (DataHolder.getInstance().getList() != null) {
//                mAdapter = new MyListAdapter(MainVideoListActivity.this, DataHolder.getInstance().getList());
//                mListVIew.setAdapter(mAdapter);
//            }
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( mList == null || mList.size() == 0 ) {
            String query = getPreferences(MODE_PRIVATE).getString(SEARCH_HISTORY_KEY, DEFAULT_SEARCH_QUERY);
            searchVideo(query);
        }
    }

    private void getYoutubeFeeds() {
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("loading...");
        mDialog.setCancelable(false);
        mDialog.show();
        RestClient.GitApiInterface service = RestClient.getClient();
        Call<YoutubeModal> call = service.getYoutubeVideosList(Config.API_KEY,
                Config.CHANNEL_ID,
                "snippet",
                Config.ORDER, "20");
        call.enqueue(new Callback<YoutubeModal>() {
            @Override
            public void onResponse(Response<YoutubeModal> response) {
                if (response.isSuccess()) {
                    if (mDialog != null) {
                        mDialog.cancel();
                        mDialog = null;
                    }
                    Toast.makeText(MainVideoListActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                    ArrayList<YoutubeModal.Item> list = (ArrayList<YoutubeModal.Item>) response.body().getItems();
                    mList = list;
                    DataHolder.getInstance().setList(list);
                    mAdapter = new MyListAdapter(MainVideoListActivity.this, list);
                    mListVIew.setAdapter(mAdapter);
                } else {
                    if (mDialog != null) {
                        mDialog.cancel();
                        mDialog = null;
                    }
                    Toast.makeText(MainVideoListActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (mDialog != null) {
                    mDialog.cancel();
                    mDialog = null;
                }
                Toast.makeText(MainVideoListActivity.this, "Failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchVideo(String searchQuery) {
        if (searchQuery==null)
            searchQuery=DEFAULT_SEARCH_QUERY;
        getPreferences(MODE_PRIVATE).edit().putString(SEARCH_HISTORY_KEY, searchQuery).apply();
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("searching...");
        mDialog.setCancelable(false);
        mDialog.show();
        RestClient.GitApiInterface service = RestClient.getClient();
        Call<YoutubeModal> call = service.searchYoutubeVideo(Config.API_KEY,"snippet", "snippet",searchQuery, "20");
        call.enqueue(new Callback<YoutubeModal>() {
            @Override
            public void onResponse(Response<YoutubeModal> response) {
                if (response.isSuccess()) {
                    if (mDialog != null) {
                        mDialog.cancel();
                        mDialog = null;
                    }
                    Toast.makeText(MainVideoListActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                    ArrayList<YoutubeModal.Item> list = (ArrayList<YoutubeModal.Item>) response.body().getItems();
                    mList = list;
                    DataHolder.getInstance().setList(list);
                    mAdapter = new MyListAdapter(MainVideoListActivity.this, list);
                    mListVIew.setAdapter(mAdapter);
                } else {
                    if (mDialog != null) {
                        mDialog.cancel();
                        mDialog = null;
                    }
                    Toast.makeText(MainVideoListActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (mDialog != null) {
                    mDialog.cancel();
                    mDialog = null;
                }
                Toast.makeText(MainVideoListActivity.this, "Failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkConnected() {
        return ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                    searchVideo(query);
                searchView.setQuery("", false);
                searchItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {

                return true;
            }
        });

//        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                return true;
//            }
//        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadYoutubeURL(String url) {
        onLoadingStarted();
        new YouTubeExtractor(this) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
                onFinishLoading();
                String downloadUrl = null;
                if (sparseArray != null) {
                    int preferredFormats[] = {18, 22, 36, 43};
                    for ( int i = 0; i < preferredFormats.length; i++ ) {
                        downloadUrl = sparseArray.get(preferredFormats[i]).getUrl();
                        if ( downloadUrl != null ) {
                            break;
                        }
                    }
                }

                if ( downloadUrl != null ) {
                    Intent intent = new Intent(MainVideoListActivity.this, PlayerActivity.class);
                    String[] uris = new String[]{downloadUrl};
                    intent.putExtra(PlayerActivity.URI_LIST_EXTRA, uris);
                    intent.setAction(PlayerActivity.ACTION_VIEW_LIST);
                    startActivity(intent);
                }
                else {
                    onError();
                }

            }
        }.extract(url, true, true);
        // Bundle args = new Bundle();
        // args.putString("url", textField.getText().toString());
        // getLoaderManager().initLoader(0, args, new YoutubeLoaderCallbacks(SampleChooserActivity.this)).forceLoad();

    }

    private ProgressDialog progressDialog;
    private void onLoadingStarted() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void onFinishLoading() {
        if ( progressDialog != null ) {
            progressDialog.dismiss();
        }
    }

    private void onError() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Failed to fetch the video url.")
                .show();

    }

}
