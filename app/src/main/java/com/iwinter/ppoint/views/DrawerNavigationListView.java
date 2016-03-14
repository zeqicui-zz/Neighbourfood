package com.iwinter.ppoint.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.iwinter.ppoint.R;
import com.iwinter.ppoint.adapters.DrawerNavigationListAdapter;
import com.iwinter.ppoint.events.DrawerSectionItemClickedEvent;
import com.iwinter.ppoint.utils.EventBus;

/**
 * Created by sandi on 04.01.2016..
 */
public class DrawerNavigationListView extends ListView implements AdapterView.OnItemClickListener{
    public DrawerNavigationListView(Context context) {
        this(context, null);
    }

    public DrawerNavigationListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawerNavigationListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Populate menu
        DrawerNavigationListAdapter adapter = new DrawerNavigationListAdapter(getContext(), 0);
        adapter.add(getContext().getString(R.string.section_quick_search));
        adapter.add(getContext().getString(R.string.section_map));
        adapter.add(getContext().getString(R.string.advanced_search));

        if(getContext().getString(R.string.website_enabled).equalsIgnoreCase("true"))
        {
            adapter.add(getContext().getString(R.string.add_property));
            adapter.add(getContext().getString(R.string.open_website));
        }

        setAdapter(adapter);

        setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Toast.makeText(getContext(), "SectionClicked: " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();

        EventBus.getInstance().post(new DrawerSectionItemClickedEvent( (String) parent.getItemAtPosition(position)));
    }
}
