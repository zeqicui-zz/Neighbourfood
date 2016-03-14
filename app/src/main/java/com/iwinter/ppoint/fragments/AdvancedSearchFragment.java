package com.iwinter.ppoint.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.iwinter.ppoint.R;

import com.iwinter.ppoint.activities.AdvancedListingActivity;
import com.iwinter.ppoint.models.TextItemPair;
import com.iwinter.ppoint.repository.FieldRepository;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by sandi on 02.02.2016..
 */
public class AdvancedSearchFragment  extends Fragment implements View.OnClickListener {

    private View mainView;
    private static JSONObject advancedCriteria = null;

    private static String advancedCriteriaOrder = null;

    public static AdvancedSearchFragment getInstance() {
        AdvancedSearchFragment fragment = new AdvancedSearchFragment();

        return fragment;
    }

    public static JSONObject getAdvancedCriteria() {
        return advancedCriteria;
    }

    public static String getAdvancedCriteriaOrder() {
        return advancedCriteriaOrder;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_advanced_search, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Generate fields/inputs for search form
        mainView = view;
        String prefix = "";
        String suffix = "";

        // [purpose]
        RadioGroup field_4 = (RadioGroup) view.findViewById(R.id.field_4);

        String[] items = new String[] { getString(R.string.all)};
        JSONObject fieldDetails = FieldRepository.getInstance().getFieldJson("4");
        try {
            String values = getString(R.string.all)+","+fieldDetails.getString("values");
            items = values.split(",");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int childCount = field_4.getChildCount();
        for (int i=0; i < childCount; i++){
            RadioButton radio =  (RadioButton) field_4.getChildAt(i);
            if(i < items.length)
            {
                // Change text
                radio.setText(items[i]);
            }
            else
            {
                // Hide if not available on portal
                radio.setVisibility(View.GONE);
            }
        }
        // [/purpose]

        // [field #2] Type
        Spinner spinner_2 = (Spinner) view.findViewById(R.id.field_2);
        TextView label_2 = (TextView) view.findViewById(R.id.label_2);
        items = new String[] { getString(R.string.all)};

        fieldDetails = FieldRepository.getInstance().getFieldJson("2");
        try {
            String values = getString(R.string.all)+","+fieldDetails.getString("values");
            items = values.split(",");
            label_2.setText(fieldDetails.getString("option"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),R.layout.spinner_item, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner_2.setAdapter(adapter);
        // [/field #2]

        // [order]
        Spinner spinner_order = (Spinner) view.findViewById(R.id.order);
        TextItemPair[] items_order = new TextItemPair[] {
                new TextItemPair<>("id ASC", getString(R.string.by_date_asc)),
                new TextItemPair<>("id DESC", getString(R.string.by_date_desc)),
                new TextItemPair<>("price ASC", getString(R.string.by_price_asc)),
                new TextItemPair<>("price DESC", getString(R.string.by_price_desc)),
                };

        ArrayAdapter<TextItemPair> adapter_order = new ArrayAdapter<>(getContext(),R.layout.spinner_item, items_order);
        adapter_order.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner_order.setAdapter(adapter_order);
        spinner_order.setSelection(1);
        // [/order]

        // [field #36] price
        TextView label_36_from = (TextView) view.findViewById(R.id.label_36_from);
        TextView label_36_to = (TextView) view.findViewById(R.id.label_36_to);
        EditText field_36_from = (EditText) view.findViewById(R.id.field_36_from);
        EditText field_36_to = (EditText) view.findViewById(R.id.field_36_to);

        fieldDetails = FieldRepository.getInstance().getFieldJson("36");
        try {
            label_36_from.setText(getString(R.string.from)+" "+getString(R.string.price)+", "+
                                  fieldDetails.getString("prefix")+fieldDetails.getString("suffix"));

            label_36_to.setText(getString(R.string.to)+" "+getString(R.string.price)+", "+
                    fieldDetails.getString("prefix")+fieldDetails.getString("suffix"));

            field_36_from.setHint(fieldDetails.getString("prefix") + fieldDetails.getString("suffix"));
            field_36_to.setHint(fieldDetails.getString("prefix") + fieldDetails.getString("suffix"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // [/field #36] price

        // [field #19] Bathrooms
        TextView label_19_from = (TextView) view.findViewById(R.id.label_19_from);
        EditText field_19_from = (EditText) view.findViewById(R.id.field_19_from);
        fieldDetails = FieldRepository.getInstance().getFieldJson("19");
        try {
            prefix="";
            if(!fieldDetails.getString("prefix").isEmpty() && fieldDetails.getString("prefix") != "null")
            {
                prefix = fieldDetails.getString("prefix");
            }
            suffix="";
            if(!fieldDetails.getString("suffix").isEmpty() && fieldDetails.getString("suffix") != "null")
            {
                suffix = fieldDetails.getString("suffix");
            }

            label_19_from.setText(getString(R.string.min) + " " + fieldDetails.getString("option") + " " +
                    prefix + suffix);
            field_19_from.setHint(prefix + suffix);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // [/field #19]

        // [field #20] Bedrooms
        TextView label_20_from = (TextView) view.findViewById(R.id.label_20_from);
        EditText field_20_from = (EditText) view.findViewById(R.id.field_20_from);
        fieldDetails = FieldRepository.getInstance().getFieldJson("20");
        try {
            prefix="";
            if(!fieldDetails.getString("prefix").isEmpty() && fieldDetails.getString("prefix") != "null")
            {
                prefix = fieldDetails.getString("prefix");
            }
            suffix="";
            if(!fieldDetails.getString("suffix").isEmpty() && fieldDetails.getString("suffix") != "null")
            {
                suffix = fieldDetails.getString("suffix");
            }

            label_20_from.setText(getString(R.string.min) + " " + fieldDetails.getString("option") + " " +
                    prefix + suffix);
            field_20_from.setHint(prefix + suffix);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // [/field #20]

        // [field #57] Size precise
        TextView label_57_from = (TextView) view.findViewById(R.id.label_57_from);
        EditText field_57_from = (EditText) view.findViewById(R.id.field_57_from);
        fieldDetails = FieldRepository.getInstance().getFieldJson("57");
        try {
            prefix="";
            if(!fieldDetails.getString("prefix").isEmpty() && fieldDetails.getString("prefix") != "null")
            {
                prefix = fieldDetails.getString("prefix");
            }
            suffix="";
            if(!fieldDetails.getString("suffix").isEmpty() && fieldDetails.getString("suffix") != "null")
            {
                suffix = fieldDetails.getString("suffix");
            }

            label_57_from.setText(getString(R.string.min) + " " + fieldDetails.getString("option") + " " +
                    prefix + suffix);
            field_57_from.setHint(prefix + suffix);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // [/field #57]

        // [field #11] Balcony
        TextView label_11 = (TextView) view.findViewById(R.id.label_11);
        fieldDetails = FieldRepository.getInstance().getFieldJson("11");
        try {
            label_11.setText(fieldDetails.getString("option"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // [/field #11]

        // [field #22] Air conditioning
        TextView label_22 = (TextView) view.findViewById(R.id.label_22);
        fieldDetails = FieldRepository.getInstance().getFieldJson("22");
        try {
            label_22.setText(fieldDetails.getString("option"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // [/field #22]

        // [field #30] Lift
        TextView label_30 = (TextView) view.findViewById(R.id.label_30);
        fieldDetails = FieldRepository.getInstance().getFieldJson("30");
        try {
            label_30.setText(fieldDetails.getString("option"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // [/field #30]

        // [field #32] Parking
        TextView label_32 = (TextView) view.findViewById(R.id.label_32);
        fieldDetails = FieldRepository.getInstance().getFieldJson("32");
        try {
            label_32.setText(fieldDetails.getString("option"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // [/field #22]

        Button search_button = (Button) view.findViewById(R.id.search_button);
        search_button.setOnClickListener(this);

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        /*

        Example:
        {"order":"id DESC",
         "v_search_option_2":"",
         "v_search_option_3":"",
         "v_search_option_36_from":"",
         "v_search_option_36_to":"",
         "v_search_option_19":"",
         "v_search_option_20":"",
         "v_booking_date_from":"",
         "v_booking_date_to":""}

         */

        // Generate JSON query part
        JSONObject searchJson = new JSONObject();
        try {
            // [order]
            Spinner spinner_order = (Spinner) mainView.findViewById(R.id.order);
            String selectedOrder = ((TextItemPair<String>) spinner_order.getSelectedItem()).getItem();
            advancedCriteriaOrder = selectedOrder;
            // [/order]

            // [field #2] Type
            Spinner spinner_2 = (Spinner) mainView.findViewById(R.id.field_2);

            if(!spinner_2.getSelectedItem().toString().equals(getString(R.string.all)))
                searchJson.put("v_search_option_2", spinner_2.getSelectedItem());
            // [/field #2]

            // [field #4] Purpose
            JSONObject fieldDetails = FieldRepository.getInstance().getFieldJson("4");
            String values = getString(R.string.all)+","+fieldDetails.getString("values");
            String[] items = values.split(",");

            RadioGroup field_4 = (RadioGroup) mainView.findViewById(R.id.field_4);
            int radioButtonID = field_4.getCheckedRadioButtonId();
            View radioButton = field_4.findViewById(radioButtonID);
            int idx = field_4.indexOfChild(radioButton);

            if(!items[idx].equals(getString(R.string.all)))
                searchJson.put("v_search_option_4", items[idx]);
            // [/field #4]

            // [price]
            EditText field_36_from = (EditText) mainView.findViewById(R.id.field_36_from);
            EditText field_36_to = (EditText) mainView.findViewById(R.id.field_36_to);
            if(!field_36_from.getText().toString().equals(""))
                searchJson.put("v_search_option_36_from", field_36_from.getText());

            if(!field_36_to.getText().toString().equals(""))
                searchJson.put("v_search_option_36_to", field_36_to.getText());
            // [price]

            // [field #19] Bathrooms
            EditText field_19_from = (EditText) mainView.findViewById(R.id.field_19_from);
            if(!field_19_from.getText().toString().equals(""))
                searchJson.put("v_search_option_19_from", field_19_from.getText());
            // [/field #19]

            // [field #20] Min. Bedrooms
            EditText field_20_from = (EditText) mainView.findViewById(R.id.field_20_from);
            if(!field_20_from.getText().toString().equals(""))
                searchJson.put("v_search_option_20_from", field_20_from.getText());
            // [/field #20]

            // [field #57] From size
            EditText field_57_from = (EditText) mainView.findViewById(R.id.field_57_from);
            if(!field_57_from.getText().toString().equals(""))
                searchJson.put("v_search_option_57_from", field_57_from.getText());
            // [/field #57]
            
            // [field #11]
            CheckBox field_11 = (CheckBox) mainView.findViewById(R.id.field_11);
            TextView label_11 = (TextView) mainView.findViewById(R.id.label_11);
            if(field_11.isChecked())
                searchJson.put("v_search_option_11", "true"+label_11.getText());
            // [/field #11]

            // [field #22]
            CheckBox field_22 = (CheckBox) mainView.findViewById(R.id.field_22);
            TextView label_22 = (TextView) mainView.findViewById(R.id.label_22);
            if(field_22.isChecked())
                searchJson.put("v_search_option_22", "true"+label_22.getText());
            // [/field #22]

            // [field #30]
            CheckBox field_30 = (CheckBox) mainView.findViewById(R.id.field_30);
            TextView label_30 = (TextView) mainView.findViewById(R.id.label_30);
            if(field_30.isChecked())
                searchJson.put("v_search_option_30", "true"+label_30.getText());
            // [/field #30]

            // [field #32]
            CheckBox field_32 = (CheckBox) mainView.findViewById(R.id.field_32);
            TextView label_32 = (TextView) mainView.findViewById(R.id.label_32);
            if(field_32.isChecked())
                searchJson.put("v_search_option_32", "true"+label_32.getText());
            // [/field #32]
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        Log.d("JSON", searchJson.toString());

        this.advancedCriteria = searchJson;
        Intent intent = new Intent(getActivity(), AdvancedListingActivity.class);
        startActivity(intent);
    }
}
