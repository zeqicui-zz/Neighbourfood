package com.iwinter.ppoint.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iwinter.ppoint.utils.StringHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.iwinter.ppoint.R;
import com.iwinter.ppoint.models.ResultsListItem;

import java.util.ArrayList;

/**
 * Created by sandi on 24.01.2016..
 */
public class ResultsAdapter extends ArrayAdapter<ResultsListItem> {

    public ResultsAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_results_list_item, parent, false);
            holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.address = (TextView) convertView.findViewById(R.id.address);
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.price = (TextView) convertView.findViewById(R.id.price);
            holder.description = (TextView) convertView.findViewById(R.id.description);
            holder.icons = (LinearLayout) convertView.findViewById(R.id.icons);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.thumbnail.setVisibility(View.GONE);
        holder.progress.setVisibility(View.VISIBLE);
        holder.name.setText(getItem(position).getName());
        holder.address.setText(getItem(position).getAddress());
        holder.price.setText(getItem(position).getPrice());
        holder.description.setText(getItem(position).getDescription());

        ArrayList icons_list = getItem(position).getIcons();
        Resources res = getContext().getResources();

        holder.icons.removeAllViews();
        for(int i=0;i<icons_list.size() && i<10;i++)
        {
            ImageView iv = new ImageView(getContext());
            iv.setBackgroundResource(res.getIdentifier("option_" + icons_list.get(i), "drawable", getContext().getPackageName()));
            holder.icons.addView(iv);

            // Define margin/space between icons
            int imgCarMarginRightPx = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, res.getDisplayMetrics());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) iv.getLayoutParams();
            lp.setMargins(0, 0, imgCarMarginRightPx, 0);
            iv.setLayoutParams(lp);
        }

        final ViewHolder tmp = holder;

        String imageUrl = StringHelper.escapeURLPathParam(getItem(position).getThumbnail());
        //Log.d("IMG_LIST", imageUrl);
        //Log.d("IMG_LIST", getItem(position).getName());
        Picasso.with(getContext()).load(imageUrl).into(holder.thumbnail, new Callback() {
            @Override
            public void onSuccess() {
                tmp.progress.setVisibility(View.GONE);
                tmp.thumbnail.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {
                tmp.progress.setVisibility(View.GONE);
                tmp.thumbnail.setImageResource(R.drawable.no_image);
                tmp.thumbnail.setVisibility(View.VISIBLE);
            }
        });

        return convertView;
    }

    class ViewHolder{
        ImageView thumbnail;
        ProgressBar progress;
        TextView name;
        TextView description;
        TextView address;
        LinearLayout icons;
        TextView price;
    }
}
