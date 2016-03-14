package com.iwinter.ppoint.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.iwinter.ppoint.utils.EventBus;
import com.squareup.otto.Subscribe;
import com.iwinter.ppoint.R;
import com.iwinter.ppoint.events.DrawerSectionItemClickedEvent;
import com.iwinter.ppoint.fragments.AdvancedSearchFragment;
import com.iwinter.ppoint.fragments.NearMapFragment;
import com.iwinter.ppoint.fragments.QuickSearchFragment;
import com.iwinter.ppoint.repository.FieldRepository;

public class MainActivity extends ActionBarActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private String mCurrentFragmentTitle;
    private static Boolean firstInit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout .activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_opened, R.string.drawer_closed) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.drawer_opened);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.drawer_closed);
            }
        };

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);



        if(firstInit || getSupportFragmentManager().getFragments() == null)
            displayInitialFragment();
        firstInit=false;
    }

    private void displayInitialFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, QuickSearchFragment.getInstance()).commit();
        mCurrentFragmentTitle = getString(R.string.section_quick_search);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (mActionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getInstance().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getInstance().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onDrawerSectionItemClickEvent(DrawerSectionItemClickedEvent event) {
        mDrawerLayout.closeDrawers();

        if (event == null || TextUtils.isEmpty(event.section) || event.section.equalsIgnoreCase(mCurrentFragmentTitle)) {
            //return;
        }

        //Toast.makeText(this, "MainActivity: Section Clicked: " + event.section, Toast.LENGTH_SHORT).show();

        if (event.section.equalsIgnoreCase(getString(R.string.section_map))) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, NearMapFragment.getInstance()).commit();
        } else if(event.section.equalsIgnoreCase(getString(R.string.section_quick_search))) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, QuickSearchFragment.getInstance()).commit();
        } else if(event.section.equalsIgnoreCase(getString(R.string.advanced_search))) {
            if(FieldRepository.getInstance().getSeted())
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, AdvancedSearchFragment.getInstance()).commit();
            }
            else
            {
                Toast.makeText(this, getString(R.string.fields_not_loaded), Toast.LENGTH_LONG).show();
            }
        } else if(event.section.equalsIgnoreCase(getString(R.string.add_property))) {

            // go to external website
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(getString(R.string.script_url) +
                                    "frontend/login/" +
                                    getString(R.string.lang_code)));
            startActivity(intent);

        } else if(event.section.equalsIgnoreCase(getString(R.string.open_website))) {

            // go to external website
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(getString(R.string.script_url)));
            startActivity(intent);

        } else {
            return;
        }

        mCurrentFragmentTitle = event.section;
    }

}