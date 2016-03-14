package com.iwinter.ppoint.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iwinter.ppoint.utils.StringHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.iwinter.ppoint.R;
import com.iwinter.ppoint.models.ResultsListItem;
import com.iwinter.ppoint.repository.FieldRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sandi on 08.01.2016..
 */
public class ResultDetailActivity extends ActionBarActivity {

    public static final String EXTRA_RESULT = "extra_result";

    private static JSONObject jsonObj = null;

    private static JSONArray imageRepository = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_detail);

        imageRepository = null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ResultsListItem item = getIntent().getExtras().getParcelable(EXTRA_RESULT);

        TextView description = (TextView) findViewById(R.id.description);
        TextView name = (TextView) findViewById(R.id.name);
        TextView address = (TextView) findViewById(R.id.address);
        TextView purpose = (TextView) findViewById(R.id.purpose);
        TextView price = (TextView) findViewById(R.id.price);
        TextView type = (TextView) findViewById(R.id.type);
        LinearLayout overview_list = (LinearLayout) findViewById(R.id.overview_list);

        name.setText(item.getName());
        address.setText(item.getAddress());
        price.setText(item.getPrice());

        try {
            jsonObj = new JSONObject(item.getJsonString());
            JSONObject listingJSONDetails = jsonObj.getJSONObject("json_object");

            purpose.setText(listingJSONDetails.getString("field_4"));
            type.setText(listingJSONDetails.getString("field_2"));
            description.setText(listingJSONDetails.getString("field_8"));

            // Populate overview
            ArrayList<JSONObject> fields = FieldRepository.getInstance().getByParent("1");
            for (int i = 0; i < fields.size(); i++) {
                JSONObject fieldDetails = fields.get(i);

                String title = fieldDetails.getString("option");
                String value = listingJSONDetails.getString("field_" + fieldDetails.getString("id"));

                String prefix = fieldDetails.getString("prefix");
                String suffix = fieldDetails.getString("suffix");

                if (prefix.equals("null")) prefix = "";
                if (suffix.equals("null")) suffix = "";

                if (!value.isEmpty()) {
                    LinearLayout lv = new LinearLayout(overview_list.getContext());
                    TextView tVtitle = new TextView(lv.getContext());
                    TextView tVvalue = new TextView(lv.getContext());
                    tVtitle.setText(title + ":");
                    tVvalue.setText(prefix + value + suffix);

                    lv.setPadding(0, 5, 0, 5);
                    tVtitle.setTypeface(null, Typeface.BOLD);
                    tVtitle.setPadding(20, 10, 20, 10);
                    tVvalue.setPadding(20, 10, 20, 10);

                    if (fieldDetails.getString("type").equals("DROPDOWN")) {
                        tVvalue.setBackgroundColor(Color.parseColor("#5CB85C"));
                        tVvalue.setTextColor(Color.parseColor("#FFFFFF"));
                    }

                    lv.addView(tVtitle);
                    lv.addView(tVvalue);
                    overview_list.addView(lv);
                }

            }

            // Populate indoor amenities
            LinearLayout indoor_amenities_list = (LinearLayout) findViewById(R.id.indoor_amenities_list);
            fields = FieldRepository.getInstance().getByParent("21");
            for (int i = 0; i < fields.size(); i++) {
                JSONObject fieldDetails = fields.get(i);

                String title = fieldDetails.getString("option");
                String value = listingJSONDetails.getString("field_" + fieldDetails.getString("id"));

                String prefix = fieldDetails.getString("prefix");
                String suffix = fieldDetails.getString("suffix");
                if (prefix.equals("null")) prefix = "";
                if (suffix.equals("null")) suffix = "";

                if (value.equals("true")) {
                    Resources res = indoor_amenities_list.getContext().getResources();

                    LinearLayout lv = new LinearLayout(indoor_amenities_list.getContext());
                    TextView tVtitle = new TextView(lv.getContext());
                    tVtitle.setText(title);

                    ImageView iv = new ImageView(lv.getContext());
                    iv.setBackgroundResource(R.drawable.checkbox);

                    lv.addView(iv);

                    tVtitle.setTypeface(null, Typeface.BOLD);
                    tVtitle.setPadding(20, 0, 20, 0);
                    lv.addView(tVtitle);

                    int res_icon = res.getIdentifier("option_" + fieldDetails.getString("id"),
                            "drawable",
                            indoor_amenities_list.getContext().getPackageName());

                    if (res_icon != 0) {
                        ImageView iv2 = new ImageView(indoor_amenities_list.getContext());
                        iv2.setBackgroundResource(res_icon);
                        lv.addView(iv2);
                    }

                    lv.setPadding(20, 10, 20, 10);
                    indoor_amenities_list.addView(lv);
                }
            }

            // Populate outdoor amenities
            LinearLayout outdoor_amenities_list = (LinearLayout) findViewById(R.id.outdoor_amenities_list);
            fields = FieldRepository.getInstance().getByParent("52");
            for (int i = 0; i < fields.size(); i++) {
                JSONObject fieldDetails = fields.get(i);

                String title = fieldDetails.getString("option");
                String value = listingJSONDetails.getString("field_" + fieldDetails.getString("id"));

                String prefix = fieldDetails.getString("prefix");
                String suffix = fieldDetails.getString("suffix");
                if (prefix.equals("null")) prefix = "";
                if (suffix.equals("null")) suffix = "";

                if (value.equals("true")) {
                    Resources res = outdoor_amenities_list.getContext().getResources();

                    LinearLayout lv = new LinearLayout(indoor_amenities_list.getContext());
                    TextView tVtitle = new TextView(lv.getContext());
                    tVtitle.setText(title);

                    ImageView iv = new ImageView(lv.getContext());
                    iv.setBackgroundResource(R.drawable.checkbox);

                    lv.addView(iv);

                    tVtitle.setTypeface(null, Typeface.BOLD);
                    tVtitle.setPadding(20, 0, 20, 0);
                    lv.addView(tVtitle);

                    int res_icon = res.getIdentifier("option_" + fieldDetails.getString("id"),
                            "drawable",
                            outdoor_amenities_list.getContext().getPackageName());

                    if (res_icon != 0) {
                        ImageView iv2 = new ImageView(outdoor_amenities_list.getContext());
                        iv2.setBackgroundResource(res_icon);
                        lv.addView(iv2);
                    }

                    lv.setPadding(20, 10, 20, 10);
                    outdoor_amenities_list.addView(lv);
                }
            }

            // Populate distances
            LinearLayout distances_list = (LinearLayout) findViewById(R.id.distances_list);
            fields = FieldRepository.getInstance().getByParent("43");
            for (int i = 0; i < fields.size(); i++) {
                JSONObject fieldDetails = fields.get(i);

                String title = fieldDetails.getString("option");
                String value = listingJSONDetails.getString("field_" + fieldDetails.getString("id"));

                String prefix = fieldDetails.getString("prefix");
                String suffix = fieldDetails.getString("suffix");
                if (prefix.equals("null")) prefix = "";
                if (suffix.equals("null")) suffix = "";

                if (!value.isEmpty()) {
                    Resources res = distances_list.getContext().getResources();

                    LinearLayout lv = new LinearLayout(indoor_amenities_list.getContext());
                    TextView tVtitle = new TextView(lv.getContext());

                    int res_icon = res.getIdentifier("option_" + fieldDetails.getString("id"),
                            "drawable",
                            distances_list.getContext().getPackageName());

                    if (res_icon != 0) {
                        ImageView iv2 = new ImageView(distances_list.getContext());
                        iv2.setBackgroundResource(res_icon);
                        lv.addView(iv2);
                    }

                    tVtitle.setTypeface(null, Typeface.BOLD);
                    tVtitle.setPadding(20, 0, 20, 0);
                    lv.addView(tVtitle);

                    TextView tVvalue = new TextView(lv.getContext());
                    tVtitle.setText(title + ":");
                    tVvalue.setText(prefix + value + suffix);
                    tVtitle.setPadding(20, 0, 20, 0);
                    lv.addView(tVvalue);

                    lv.setPadding(20, 10, 20, 10);
                    distances_list.addView(lv);
                }
            }

            // Populate map
            MapView gmap = (MapView) findViewById(R.id.location_map);
            gmap.onCreate(savedInstanceState);
            gmap.onResume();// needed to get the map to display immediately
            //gmap.getMap().getUiSettings().setZoomControlsEnabled(true);

            try {
                MapsInitializer.initialize(gmap.getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            GoogleMap googleMap = gmap.getMap();
            // latitude and longitude
            double latitude = jsonObj.getDouble("lat");
            double longitude = jsonObj.getDouble("lng");

            // create marker
            MarkerOptions marker = new MarkerOptions().position(
                    new LatLng(latitude, longitude)).title(item.getName());

            // adding marker
            googleMap.addMarker(marker);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude)).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));


            // Populate agent details
            TextView agent_name = (TextView) findViewById(R.id.agent_name);
            TextView agent_phone = (TextView) findViewById(R.id.agent_phone);
            TextView agent_mail = (TextView) findViewById(R.id.agent_mail);

            if (!jsonObj.has("name_surname") || jsonObj.getString("name_surname").equals("null") || jsonObj.getString("name_surname").isEmpty()) {
                TextView agent_details_title = (TextView) findViewById(R.id.agent_details_title);
                LinearLayout agent_details_layout = (LinearLayout) findViewById(R.id.agent_details_layout);
                agent_details_title.setVisibility(View.GONE);
                agent_details_layout.setVisibility(View.GONE);
            } else {
                agent_name.setText(jsonObj.getString("name_surname"));
                agent_phone.setText(jsonObj.getString("phone"));
                agent_mail.setText(jsonObj.getString("mail"));

                String agent_image_url = getString(R.string.files_url) + jsonObj.getString("image_user_filename");
                agent_image_url = StringHelper.escapeURLPathParam(agent_image_url);

                final ImageView agent_image = (ImageView) findViewById(R.id.agent_image);
                Picasso.with(this).load(agent_image_url).into(agent_image, new Callback() {
                    @Override
                    public void onSuccess() {
                        agent_image.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                        //agent_image.setImageResource(R.drawable.no_image);
                        agent_image.setVisibility(View.GONE);
                    }
                });

                Log.e("IMG_AGENT", agent_image_url);
            }

            Button button = (Button) findViewById(R.id.button_link);

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    try {
                        intent.setData(Uri.parse(getString(R.string.script_url) +
                                getString(R.string.property_uri) + "/" +
                                jsonObj.getString("id") + "/" +
                                getString(R.string.lang_code)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
            });

            Button payLink = (Button) findViewById(R.id.pay_link);

            payLink.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(ResultDetailActivity.this, PaymentFormActivity.class);
                    intent.putExtra("Amount",  R.id.price);
                    startActivity(intent);
                }
            });

            if (!getString(R.string.website_enabled).equalsIgnoreCase("true")) {
                button.setVisibility(View.GONE);
            }

            imageRepository = jsonObj.getJSONArray("image_repository");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ImageFragmentPagerAdapter imageFragmentPagerAdapter = new ImageFragmentPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        if (imageRepository == null) {
            viewPager.setVisibility(View.GONE);
        } else {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            ViewGroup.LayoutParams params = viewPager.getLayoutParams();
            params.height = height / 3;
            viewPager.setLayoutParams(params);

            viewPager.setAdapter(imageFragmentPagerAdapter);

            viewPager.setVisibility(View.VISIBLE);
        }

    }

    public static class ImageFragmentPagerAdapter extends FragmentPagerAdapter {
        public ImageFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            if(imageRepository==null)return 0;

            return imageRepository.length();
        }

        @Override
        public Fragment getItem(int position) {
            SwipeFragment fragment = new SwipeFragment();
            return SwipeFragment.newInstance(position);
        }
    }

    public static class SwipeFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View swipeView = inflater.inflate(R.layout.swipe_fragment, container, false);
            Bundle bundle = getArguments();
            int position = bundle.getInt("position");
            String imageFileName = null;
            try {
                imageFileName = getString(R.string.files_url )+imageRepository.get(position).toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            imageFileName = StringHelper.escapeURLPathParam(imageFileName);
            Log.e("IMG_SWIPE", imageFileName);
            final ImageView imageView = (ImageView) swipeView.findViewById(R.id.image_view);
            Picasso.with( getContext() ).load( imageFileName ).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    imageView.setVisibility(View.VISIBLE);
                }
                @Override
                public void onError() {
                    imageView.setImageResource(R.drawable.no_image);
                    imageView.setVisibility(View.VISIBLE);
                }
            });
            return swipeView;
        }

        static SwipeFragment newInstance(int position) {
            SwipeFragment swipeFragment = new SwipeFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            swipeFragment.setArguments(bundle);
            return swipeFragment;
        }
    }
}