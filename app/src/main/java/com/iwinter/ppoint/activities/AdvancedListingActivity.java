package com.iwinter.ppoint.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iwinter.ppoint.R;
import com.iwinter.ppoint.adapters.ResultsAdapter;
import com.iwinter.ppoint.fragments.AdvancedSearchFragment;
import com.iwinter.ppoint.models.ResultsListItem;
import com.iwinter.ppoint.repository.FieldRepository;
import com.iwinter.ppoint.views.InfiniteScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sandi on 03.02.2016..
 */
public class AdvancedListingActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private ResultsAdapter mAdapter;
    private ProgressBar footerProgressbar;
    private TextView emptyMessage;

    private boolean firstLoadInit = false;
    private Integer offset = 0;
    private Integer perPage = 10;
    private Integer totalResults = null;
    private String searchParam = "";
    private ListView listView = null;

    private ListView getListView()
    {
        return listView;
    }

    private ActionBarActivity getActivity()
    {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_listing );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById( R.id.list );

        getListView().setOnScrollListener(new InfiniteScrollListener(5) {
            @Override
            public void loadMore(int page, int totalItemsCount) {

                if (offset < totalResults) {
                        /* Show loading indicator */
                    footerProgressbar.setVisibility(View.VISIBLE);

                        /* Initial next results */
                    new JSONTask().execute(getString(R.string.script_url) + "api/json/" +
                            getString(R.string.lang_code) + "/" + perPage.toString() + "/" + offset.toString());
                }

            }
        });

        getListView().setPadding(16, 16, 16, 16);
        getListView().setDivider(new ColorDrawable(Color.TRANSPARENT));
        getListView().setDividerHeight(16);
        getListView().setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        getListView().setClipToPadding(true);
        getListView().setOnItemClickListener(this);

        mAdapter = new ResultsAdapter(this, 0);

            /* Hide empty message */
        emptyMessage = (TextView) this.findViewById(R.id.empty);
        emptyMessage.setVisibility(View.GONE);

            /* Show loading indicator */
        footerProgressbar = (ProgressBar) this.findViewById(R.id.footer_fixed);
        footerProgressbar.setVisibility(View.VISIBLE);

            /* Initial load */
        new JSONTask().execute(getString(R.string.script_url) + "api/json/" +
                getString(R.string.lang_code) + "/" + perPage.toString() + "/" + offset.toString());

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), ResultDetailActivity.class);
        intent.putExtra(ResultDetailActivity.EXTRA_RESULT, mAdapter.getItem(position));

        startActivity(intent);
    }

    /* Fetching data */
    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {

                searchParam = AdvancedSearchFragment.getAdvancedCriteria().toString();
                searchParam = URLEncoder.encode(searchParam);
                String order = AdvancedSearchFragment.getAdvancedCriteriaOrder();
                if(!order.isEmpty())
                    order = URLEncoder.encode("is_featured DESC, " + order);

                URL url = new URL(params[0] + "?options_hide=false&search=" + searchParam+"&order="+order);

                if (FieldRepository.getInstance().getSeted()) {
                    url = new URL(params[0] + "?search=" + searchParam+"&order="+order);
                }

                Log.d("Log", params[0] + "?options_hide=false&search=" + searchParam+"&order="+order);

                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(20000);
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                return finalJson;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("Log", "Network error");
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();

                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Log.d("RES", result);
            } else {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                builder1.setMessage(getString(R.string.network_error_message));
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                footerProgressbar.setVisibility(View.GONE);
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();

                emptyMessage.setText(getString(R.string.network_error_message));
                emptyMessage.setVisibility(View.VISIBLE);
            }

            StringBuffer finalBufferedData = new StringBuffer();
            JSONObject rootObject = null;
            try {
                rootObject = new JSONObject(result);

                if (rootObject != null) {
                        /* Fetch field details */
                    JSONArray detailsArray = null;
                    Map<String, String> fields = new HashMap<String, String>();
                    ArrayList icons_exists = new ArrayList();

                    if (FieldRepository.getInstance().getSeted()) {
                        detailsArray = FieldRepository.getInstance().getDetailsArray();
                        fields = FieldRepository.getInstance().getFields();
                        icons_exists = FieldRepository.getInstance().getIcons_exists();
                    } else {
                        detailsArray = rootObject.getJSONArray("field_details");

                        Resources res = getResources();
                        for (int i = 0; i < detailsArray.length(); i++) {
                            JSONObject fieldDetails = detailsArray.getJSONObject(i);
                            String field_id = fieldDetails.getString("id");

                            fields.put(field_id + "_prefix", fieldDetails.getString("prefix"));
                            fields.put(field_id + "_suffix", fieldDetails.getString("suffix"));
                            fields.put(field_id + "_option", fieldDetails.getString("option"));

                            if (fieldDetails.getString("type").equalsIgnoreCase("CHECKBOX") &&
                                    res.getIdentifier("option_" + field_id, "drawable", getActivity().getPackageName()) != 0) {
                                icons_exists.add(field_id);
                            }
                        }

                        // Set field details into repository
                        FieldRepository.getInstance().setDetailsArray(detailsArray);
                        FieldRepository.getInstance().setIcons_exists(icons_exists);
                        FieldRepository.getInstance().setFields(fields);
                        FieldRepository.getInstance().setSeted(true);
                    }

                    /* Total results */

                    if (totalResults == null)
                        totalResults = Integer.parseInt(rootObject.getString("total_results"));

                    /* Fetch results */

                    JSONArray resultsArray = rootObject.getJSONArray("results");
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject itemObject = resultsArray.getJSONObject(i);
                        JSONObject listingDetails = itemObject.getJSONObject("listing");
                        JSONObject listingJSONDetails = listingDetails.getJSONObject("json_object");

                        // price generate
                        String price_formated = "";
                        if (listingDetails.getString("field_36_int").isEmpty() || listingDetails.getString("field_36_int") == "null") {
                            price_formated += "-";
                        } else {
                            price_formated += fields.get("36_prefix") + listingDetails.getString("field_36_int") + fields.get("36_suffix");
                        }
                        price_formated += " / ";
                        if (listingDetails.getString("field_37_int").isEmpty() || listingDetails.getString("field_37_int") == "null") {
                            price_formated += "-";
                        } else {
                            price_formated += fields.get("37_prefix") + listingDetails.getString("field_37_int") + fields.get("37_suffix");
                        }

                        // icons generate
                        ArrayList icons = new ArrayList();

                        for (int ii = 0; ii < icons_exists.size(); ii++) {
                            if (listingJSONDetails.getString("field_" + icons_exists.get(ii)).equalsIgnoreCase("true")) {
                                icons.add(icons_exists.get(ii));
                            }
                        }

                        ResultsListItem listItem = new ResultsListItem();
                        listItem.setName(listingJSONDetails.getString("field_10"));
                        listItem.setAddress(listingDetails.getString("address"));
                        listItem.setDescription(listingJSONDetails.getString("field_2") + ", " + listingJSONDetails.getString("field_4"));
                        listItem.setPrice(price_formated);
                        listItem.setThumbnail(getString(R.string.thumbnails_url) + listingDetails.getString("image_filename"));
                        listItem.setImage(getString(R.string.files_url) + listingDetails.getString("image_filename"));
                        listItem.setJsonString(listingDetails.toString());

                        listItem.setIcons(icons);
                        mAdapter.add(listItem);
                    }

                    mAdapter.notifyDataSetChanged();

                    offset += perPage;

                    if (!firstLoadInit)
                        getListView().setAdapter(mAdapter);

                    firstLoadInit = true;
                }

                    /* Hide progressbar */
                footerProgressbar.setVisibility(View.GONE);

                    /* Show empty message if no results */
                if (mAdapter.getCount() == 0) {
                    emptyMessage.setText(getString(R.string.empty_message));
                    emptyMessage.setVisibility(View.VISIBLE);
                }

            } catch (JSONException e) {
                Toast.makeText(getActivity(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
