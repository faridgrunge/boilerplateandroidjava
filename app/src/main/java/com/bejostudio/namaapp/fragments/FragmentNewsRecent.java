package com.bejostudio.namaapp.fragments;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bejostudio.namaapp.Config;
import com.bejostudio.namaapp.R;
import com.bejostudio.namaapp.adapters.AdapterNewsRecent;
import com.bejostudio.namaapp.json.JsonConfig;
import com.bejostudio.namaapp.json.JsonUtils;
import com.bejostudio.namaapp.models.ItemNewsList;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentNewsRecent extends Fragment {

    RecyclerView recyclerView;
    List<ItemNewsList> arrayItemNewsList;
    AdapterNewsRecent adapterNewsRecent;
    ArrayList<String> array_news, array_news_cat_name, array_cid, array_cat_id, array_cat_name, array_title, array_image, array_desc, array_date;
    String[] str_news, str_news_cat_name, str_cid, str_cat_id, str_cat_name, str_title, str_image, str_desc, str_date;
    ItemNewsList itemNewsList;
    JsonUtils util;
    int textLength = 0;
    SwipeRefreshLayout swipeRefreshLayout = null;
    ProgressBar progressBar;
    InterstitialAd interstitialAd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_news_recent, container, false);
        setHasOptionsMenu(true);

        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue, R.color.red);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(3), true));
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        arrayItemNewsList = new ArrayList<ItemNewsList>();
        array_news = new ArrayList<String>();
        array_news_cat_name = new ArrayList<String>();
        array_cid = new ArrayList<String>();
        array_cat_id = new ArrayList<String>();
        array_cat_name = new ArrayList<String>();
        array_title = new ArrayList<String>();
        array_image = new ArrayList<String>();
        array_desc = new ArrayList<String>();
        array_date = new ArrayList<String>();

        str_news = new String[array_news.size()];
        str_news_cat_name = new String[array_news_cat_name.size()];
        str_cid = new String[array_cid.size()];
        str_cat_id = new String[array_cat_id.size()];
        str_cat_name = new String[array_cat_name.size()];
        str_title = new String[array_title.size()];
        str_image = new String[array_image.size()];
        str_desc = new String[array_desc.size()];
        str_date = new String[array_date.size()];

        util = new JsonUtils(this.getActivity());

        if (JsonUtils.isNetworkAvailable(getActivity())) {
            new MyTask().execute(Config.SERVER_URL + "/api.php?latest_news=70");
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.failed_connect_network), Toast.LENGTH_SHORT).show();
        }

        // Using to refresh webpage when user swipes the screen
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        clearData();
                        new RefreshTask().execute(Config.SERVER_URL + "/api.php?latest_news=70");
                    }
                }, 3000);
            }
        });

        return v;
    }

    public void clearData() {
        int size = this.arrayItemNewsList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.arrayItemNewsList.remove(0);
            }

            adapterNewsRecent.notifyItemRangeRemoved(0, size);
        }
    }

    private class MyTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressBar.setVisibility(View.GONE);

            if (null == result || result.length() == 0) {
                Toast.makeText(getActivity(), getResources().getString(R.string.failed_connect_network), Toast.LENGTH_SHORT).show();
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(JsonConfig.CATEGORY_ARRAY_NAME);
                    JSONObject objJson = null;


                    for(int i =0 ; i<jsonArray.length();i++){
                        if(jsonArray.getJSONObject(i).getString(JsonConfig.CATEGORY_NAME).contains(Config.CATEGORY)){
                            Log.d("TES", String.valueOf(i));

                            objJson = jsonArray.getJSONObject(i);

                            ItemNewsList objItem = new ItemNewsList();

                            objItem.setCId(objJson.getString(JsonConfig.CATEGORY_ITEM_CID));
                            objItem.setCategoryName(objJson.getString(JsonConfig.CATEGORY_ITEM_NAME));
                            objItem.setCatId(objJson.getString(JsonConfig.CATEGORY_ITEM_CAT_ID));
                            objItem.setNewsImage(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSIMAGE));
                            objItem.setNewsHeading(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSHEADING));
                            objItem.setNewsDescription(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSDESCRI));
                            objItem.setNewsDate(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSDATE));



                            arrayItemNewsList.add(objItem);
                        }

                    }
                   // for (int i = 0; i < jsonArray.length(); i++) {


//                        objJson = jsonArray.getJSONObject(1);
//
//                        ItemNewsList objItem = new ItemNewsList();
//
//                        objItem.setCId(objJson.getString(JsonConfig.CATEGORY_ITEM_CID));
//                        objItem.setCategoryName(objJson.getString(JsonConfig.CATEGORY_ITEM_NAME));
//                        objItem.setCatId(objJson.getString(JsonConfig.CATEGORY_ITEM_CAT_ID));
//                        objItem.setNewsImage(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSIMAGE));
//                        objItem.setNewsHeading(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSHEADING));
//                        objItem.setNewsDescription(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSDESCRI));
//                        objItem.setNewsDate(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSDATE));
//
//
//
//                        arrayItemNewsList.add(objItem);

                   // }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < arrayItemNewsList.size(); j++) {

                    itemNewsList = arrayItemNewsList.get(j);

                    array_cat_id.add(itemNewsList.getCatId());
                    str_cat_id = array_cat_id.toArray(str_cat_id);

                    array_cat_name.add(itemNewsList.getCategoryName());
                    str_cat_name = array_cat_name.toArray(str_cat_name);

                    array_cid.add(String.valueOf(itemNewsList.getCId()));
                    str_cid = array_cid.toArray(str_cid);

                    array_image.add(String.valueOf(itemNewsList.getNewsImage()));
                    str_image = array_image.toArray(str_image);

                    array_title.add(String.valueOf(itemNewsList.getNewsHeading()));
                    str_title = array_title.toArray(str_title);

                    array_desc.add(String.valueOf(itemNewsList.getNewsDescription()));
                    str_desc = array_desc.toArray(str_desc);

                    array_date.add(String.valueOf(itemNewsList.getNewsDate()));
                    str_date = array_date.toArray(str_date);

                }

                setAdapterToRecyclerView();
            }

        }
    }

    private class RefreshTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressBar.setVisibility(View.GONE);

            if (null == result || result.length() == 0) {
                Toast.makeText(getActivity(), getResources().getString(R.string.failed_connect_network), Toast.LENGTH_SHORT).show();
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(JsonConfig.CATEGORY_ARRAY_NAME);
                    JSONObject objJson = null;

                    for(int i =0 ; i<jsonArray.length();i++){
                        if(jsonArray.getJSONObject(i).getString(JsonConfig.CATEGORY_NAME).contains(Config.CATEGORY)){
                            Log.d("TES", String.valueOf(i));

                            objJson = jsonArray.getJSONObject(i);

                            ItemNewsList objItem = new ItemNewsList();

                            objItem.setCId(objJson.getString(JsonConfig.CATEGORY_ITEM_CID));
                            objItem.setCategoryName(objJson.getString(JsonConfig.CATEGORY_ITEM_NAME));
                            objItem.setCatId(objJson.getString(JsonConfig.CATEGORY_ITEM_CAT_ID));
                            objItem.setNewsImage(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSIMAGE));
                            objItem.setNewsHeading(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSHEADING));
                            objItem.setNewsDescription(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSDESCRI));
                            objItem.setNewsDate(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSDATE));



                            arrayItemNewsList.add(objItem);
                        }

                    }
                    //for (int i = 0; i < jsonArray.length(); i++) {
//                        objJson = jsonArray.getJSONObject(1);
//
//                        ItemNewsList objItem = new ItemNewsList();
//
//                        objItem.setCId(objJson.getString(JsonConfig.CATEGORY_ITEM_CID));
//                        objItem.setCategoryName(objJson.getString(JsonConfig.CATEGORY_ITEM_NAME));
//                        objItem.setCatId(objJson.getString(JsonConfig.CATEGORY_ITEM_CAT_ID));
//                        objItem.setNewsImage(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSIMAGE));
//                        objItem.setNewsHeading(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSHEADING));
//                        objItem.setNewsDescription(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSDESCRI));
//                        objItem.setNewsDate(objJson.getString(JsonConfig.CATEGORY_ITEM_NEWSDATE));
//
//
//
//                        arrayItemNewsList.add(objItem);

                    //}

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < arrayItemNewsList.size(); j++) {

                    itemNewsList = arrayItemNewsList.get(j);

                    array_cat_id.add(itemNewsList.getCatId());
                    str_cat_id = array_cat_id.toArray(str_cat_id);

                    array_cat_name.add(itemNewsList.getCategoryName());
                    str_cat_name = array_cat_name.toArray(str_cat_name);

                    array_cid.add(String.valueOf(itemNewsList.getCId()));
                    str_cid = array_cid.toArray(str_cid);

                    array_image.add(String.valueOf(itemNewsList.getNewsImage()));
                    str_image = array_image.toArray(str_image);

                    array_title.add(String.valueOf(itemNewsList.getNewsHeading()));
                    str_title = array_title.toArray(str_title);

                    array_desc.add(String.valueOf(itemNewsList.getNewsDescription()));
                    str_desc = array_desc.toArray(str_desc);

                    array_date.add(String.valueOf(itemNewsList.getNewsDate()));
                    str_date = array_date.toArray(str_date);

                }

                setAdapterToRecyclerView();
            }
        }
    }

    public void setAdapterToRecyclerView() {
        adapterNewsRecent = new AdapterNewsRecent(getActivity(), arrayItemNewsList);
        recyclerView.setAdapter(adapterNewsRecent);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search, menu);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));

        final MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchView.setQueryHint(getResources().getString(R.string.search_query_text));

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            searchMenuItem.collapseActionView();
                            searchView.setQuery("", false);
                        }
                    }
                });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {

                textLength = newText.length();
                arrayItemNewsList.clear();

                for (int i = 0; i < str_title.length; i++) {
                    if (textLength <= str_title[i].length()) {
                        if (str_title[i].toLowerCase().contains(newText.toLowerCase())) {

                            ItemNewsList objItem = new ItemNewsList();

                            objItem.setCategoryName((str_cat_name[i]));
                            objItem.setCatId(str_cat_id[i]);
                            objItem.setCId(str_cid[i]);
                            objItem.setNewsDate(str_date[i]);
                            objItem.setNewsDescription(str_desc[i]);
                            objItem.setNewsHeading(str_title[i]);
                            objItem.setNewsImage(str_image[i]);
                            arrayItemNewsList.add(objItem);
                        }
                    }
                }

                setAdapterToRecyclerView();
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {

                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:

                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    private void loadInterstitialAd() {
        Log.d("TAG", "showAd");
        interstitialAd = new InterstitialAd(this.getContext());
        interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }
}
