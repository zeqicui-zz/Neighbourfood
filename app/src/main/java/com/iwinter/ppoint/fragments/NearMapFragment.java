package com.iwinter.ppoint.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iwinter.ppoint.utils.StringHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.iwinter.ppoint.R;
import com.iwinter.ppoint.activities.ResultDetailActivity;
import com.iwinter.ppoint.models.ResultsListItem;
import com.iwinter.ppoint.repository.FieldRepository;

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
import java.util.List;
import java.util.Map;

/**
 * Created by sandi on 30.01.2016..
 */
public class NearMapFragment extends Fragment implements LocationListener,GoogleMap.OnInfoWindowClickListener {

    private ProgressBar footerProgressbar;
    private Map<Marker, ResultsListItem> markersContent = new HashMap<>();
    private Integer totalResults = null;
    private String searchParam = "";
    private LatLng locationFounded = null;
    private MapView gmap = null;

    protected LocationManager locationManager;
    boolean locationFound = false;

    public static NearMapFragment getInstance() {
        NearMapFragment fragment = new NearMapFragment();
        return fragment;
    }

    public static JSONObject listingJson = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_near_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gmap = (MapView) view.findViewById(R.id.near_map);
        gmap.onCreate(savedInstanceState);
        gmap.onResume();// needed to get the map to display immediately
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        footerProgressbar = (ProgressBar) getActivity().findViewById(R.id.footer_fixed);

        // Populate map
        gmap.getMap().getUiSettings().setZoomControlsEnabled(true);

        try {
            MapsInitializer.initialize(gmap.getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setting a custom info window adapter for the google map
        gmap.getMap().setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(final Marker marker) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(final Marker marker) {

                ResultsListItem item = markersContent.get(marker);

                if(item == null)
                    return null;

                // Getting view from the layout file info_window_layout
                View v = getActivity().getLayoutInflater().inflate(R.layout.info_window_layout, null);

                // Getting the position from the marker
                TextView title = (TextView) v.findViewById(R.id.title);
                TextView address = (TextView) v.findViewById(R.id.address);
                TextView price = (TextView) v.findViewById(R.id.price);
                final ImageView thumbnail = (ImageView) v.findViewById(R.id.thumbnail);

                title.setText(item.getName());
                address.setText(item.getAddress());
                price.setText(item.getPrice());

                thumbnail.setVisibility(View.GONE);
                String imageUrl = StringHelper.escapeURLPathParam(item.getThumbnail());
                Picasso.with(getContext()).load(imageUrl).into(thumbnail, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        thumbnail.setVisibility(View.VISIBLE);

                        if (marker != null && marker.isInfoWindowShown()) {
                            marker.hideInfoWindow();
                            marker.showInfoWindow();
                        }
                    }

                    @Override
                    public void onError() {
                    }
                });

                // Returning the view containing InfoWindow contents
                return v;
            }
        });

        gmap.getMap().setOnInfoWindowClickListener(this);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.loadLocationDisabled();
        }
        else
        {
            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                /* Show loading indicator */
                footerProgressbar = (ProgressBar) getActivity().findViewById(R.id.footer_fixed);
                footerProgressbar.setVisibility(View.VISIBLE);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
            else
            {
                this.loadLocationDisabled();
            }
        }

            /* Search feature */
        ImageButton btn = (ImageButton) getActivity().findViewById(R.id.searchButton);
        View.OnClickListener button_click = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Fetch search text
                EditText searchText = (EditText) getActivity().findViewById(R.id.searchText);
                searchParam = searchText.getText().toString();

                if(searchParam.isEmpty())return;

                Geocoder geocoder = new Geocoder(getContext());
                List<Address> addresses;
                try {
                    addresses = geocoder.getFromLocationName(searchParam, 1);

                    if(addresses.size() > 0) {
                        // Load results
                        locationFounded = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                        gmap.getMap().clear();

                        // create marker
                        MarkerOptions marker = new MarkerOptions().position(
                                locationFounded).title(getString(R.string.my_location));
                        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location));

                        // adding marker
                        gmap.getMap().addMarker(marker);
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(locationFounded).zoom(10).build();
                        gmap.getMap().animateCamera(CameraUpdateFactory
                                .newCameraPosition(cameraPosition));

                        footerProgressbar.setVisibility(View.VISIBLE);
                        new JSONTask().execute(getString(R.string.script_url) + "api/json/" +
                                getString(R.string.lang_code) + "/30/0");
                    }
                    else
                    {
                        Toast.makeText(getActivity(), getString(R.string.address_not_found), Toast.LENGTH_LONG).show();
                        footerProgressbar.setVisibility(View.GONE);
                    }
                } catch (IOException e) {
                    footerProgressbar.setVisibility(View.GONE);
                    e.printStackTrace();
                }

                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchText.getWindowToken(),
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
        };
        btn.setOnClickListener(button_click);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    public void loadLocationDisabled()
    {
        Toast.makeText(getActivity(), getString(R.string.location_unavailable), Toast.LENGTH_LONG).show();

        /* Show loading indicator */
        footerProgressbar.setVisibility(View.VISIBLE);

        // load 30 last added properties

        /* Initial load */
        new JSONTask().execute(getString(R.string.script_url) + "api/json/" +
                getString(R.string.lang_code) + "/30/0");
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        ResultsListItem item = markersContent.get(marker);

        Intent intent = new Intent(getActivity(), ResultDetailActivity.class);
        intent.putExtra(ResultDetailActivity.EXTRA_RESULT, item);

        startActivity(intent);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Run it only once
        if(locationFound || !isAdded())return;

        locationFounded = new LatLng(location.getLatitude(), location.getLongitude());

        Log.d("LAT", String.valueOf(location.getLatitude()));
        Log.d("LNG", String.valueOf(location.getLongitude()));

        // latitude and longitude
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // create marker
        MarkerOptions marker = new MarkerOptions().position(
                new LatLng(latitude, longitude)).title(getString(R.string.my_location));
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location));

        // adding marker
        gmap.getMap().addMarker(marker);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).zoom(10).build();
        gmap.getMap().animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        new JSONTask().execute(getString(R.string.script_url) + "api/json/" +
                getString(R.string.lang_code) + "/30/0");

        locationFound=true;
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    /* Fetching data */
        public class JSONTask extends AsyncTask<String, String, String> {

            @Override
            protected String doInBackground(String... params) {

                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    // calculate rectangle
                    String query_rectangle = "";
                    if(locationFounded != null)
                    {
                        double R = 6371;  // earth radius in km
                        double radius = 50; // km
                        double x1 = locationFounded.longitude - Math.toDegrees(radius/R/Math.cos(Math.toRadians(locationFounded.latitude)));
                        double x2 = locationFounded.longitude + Math.toDegrees(radius/R/Math.cos(Math.toRadians(locationFounded.latitude)));
                        double y1 = locationFounded.latitude + Math.toDegrees(radius/R);
                        double y2 = locationFounded.latitude - Math.toDegrees(radius/R);

                        query_rectangle = "&v_rectangle_sw="+URLEncoder.encode(String.valueOf(y2)+", "+String.valueOf(x1))+"&v_rectangle_ne="+ URLEncoder.encode(String.valueOf(y1) + ", " + String.valueOf(x2));
                    }

                    URL url = new URL(params[0]+"?options_hide=false"+query_rectangle);
                    if(FieldRepository.getInstance().getSeted())
                    {
                        url = new URL(params[0]+"?options_hide=true"+query_rectangle);
                    }

                    Log.d("Log", params[0] + "?options_hide=true" + query_rectangle);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(20000);
                    connection.connect();

                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while((line = reader.readLine()) != null )
                    {
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
                    if(connection != null)
                        connection.disconnect();

                    try {
                        if(reader != null)
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

                if(result != null)
                {
                    Log.d("RES", result);
                }
                else
                {
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
                }

                StringBuffer finalBufferedData = new StringBuffer();
                JSONObject rootObject = null;
                try {
                    rootObject = new JSONObject(result);

                    if(rootObject != null)
                    {
                        /* Fetch field details */
                        JSONArray detailsArray = null;
                        Map<String, String> fields = new HashMap<String, String>();
                        ArrayList icons_exists = new ArrayList();

                        if(FieldRepository.getInstance().getSeted())
                        {
                            detailsArray = FieldRepository.getInstance().getDetailsArray();
                            fields = FieldRepository.getInstance().getFields();
                            icons_exists = FieldRepository.getInstance().getIcons_exists();
                        }
                        else
                        {
                            detailsArray = rootObject.getJSONArray("field_details");

                            Resources res = getResources();
                            for(int i=0; i<detailsArray.length(); i++) {
                                JSONObject fieldDetails = detailsArray.getJSONObject(i);
                                String field_id = fieldDetails.getString("id");

                                fields.put(field_id+"_prefix", fieldDetails.getString("prefix"));
                                fields.put(field_id+"_suffix", fieldDetails.getString("suffix"));
                                fields.put(field_id + "_option", fieldDetails.getString("option"));

                                if(fieldDetails.getString("type").equalsIgnoreCase("CHECKBOX") &&
                                        res.getIdentifier("option_"+field_id, "drawable", getActivity().getPackageName()) != 0 )
                                {
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

                        if(totalResults == null)
                            totalResults = Integer.parseInt(rootObject.getString("total_results"));

                        /* Fetch results */

                        double totalLat = 0;
                        double totalLng = 0;

                        JSONArray resultsArray = rootObject.getJSONArray("results");
                        for(int i=0; i<resultsArray.length(); i++)
                        {
                            JSONObject itemObject = resultsArray.getJSONObject(i);
                            JSONObject listingDetails = itemObject.getJSONObject("listing");
                            JSONObject listingJSONDetails = listingDetails.getJSONObject("json_object");

                            // price generate
                            String price_formated = "";
                            if(listingDetails.getString("field_36_int").isEmpty() || listingDetails.getString("field_36_int") == "null")
                            {
                                price_formated+= "-";
                            }
                            else
                            {
                                price_formated+= fields.get("36_prefix")+listingDetails.getString("field_36_int")+fields.get("36_suffix");
                            }
                            price_formated+= " / ";
                            if(listingDetails.getString("field_37_int").isEmpty() || listingDetails.getString("field_37_int") == "null")
                            {
                                price_formated+= "-";
                            }
                            else
                            {
                                price_formated+= fields.get("37_prefix")+listingDetails.getString("field_37_int")+fields.get("37_suffix");;
                            }

                            // icons generate
                            ArrayList icons = new ArrayList();

                            for(int ii=0;ii<icons_exists.size();ii++)
                            {
                                if(listingJSONDetails.getString("field_"+icons_exists.get(ii)).equalsIgnoreCase("true"))
                                {
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
                            listingJson = listingJSONDetails;

                            // latitude and longitude
                            double latitude = Double.parseDouble(listingDetails.getString("lat"));
                            double longitude = Double.parseDouble(listingDetails.getString("lng"));

                            // create marker
                            MarkerOptions marker = new MarkerOptions().position(
                                    new LatLng(latitude, longitude)).title(listingDetails.getString("address"));

                            // Changing marker icon
                            String marker_name = listingJSONDetails.getString("field_6");
                            Resources res = getResources();
                            int marker_res_id = res.getIdentifier(marker_name, "drawable", getActivity().getPackageName());

                            if(marker_res_id != 0 )
                            {
                                marker.icon(BitmapDescriptorFactory.fromResource(marker_res_id));
                            }
                            else
                            {
                                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.empty));
                            }

                            // adding marker
                            Marker r_marker = gmap.getMap().addMarker(marker);
                            markersContent.put(r_marker, listItem);

                            totalLat+=latitude;
                            totalLng+=longitude;
                        }

                        double totalMarkers = resultsArray.length();
                        if(locationFounded != null)
                        {
                            totalMarkers++;
                            totalLat+= locationFounded.latitude;
                            totalLng+= locationFounded.longitude;
                        }

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(totalLat/totalMarkers, totalLng/totalMarkers)).zoom(8).build();
                        gmap.getMap().animateCamera(CameraUpdateFactory
                                .newCameraPosition(cameraPosition));
                    }

                    /* Hide progressbar */
                    footerProgressbar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }



