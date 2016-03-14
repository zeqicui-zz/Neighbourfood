package com.iwinter.ppoint.repository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sandi on 29.01.2016..
 */
public class FieldRepository {

    JSONArray detailsArray =null;
    Map<String, String> fields = new HashMap<String, String>();
    ArrayList icons_exists = new ArrayList();
    Boolean seted = false;

    private static FieldRepository instance = null;
    protected FieldRepository() {
        // Exists only to defeat instantiation.
    }

    public static FieldRepository getInstance() {
        if(instance == null) {
            instance = new FieldRepository();
        }
        return instance;
    }

    public JSONArray getDetailsArray() {
        return detailsArray;
    }

    public void setDetailsArray(JSONArray detailsArray) {
        this.detailsArray = detailsArray;
    }

    public ArrayList getIcons_exists() {
        return icons_exists;
    }

    public void setIcons_exists(ArrayList icons_exists) {
        this.icons_exists = icons_exists;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public Boolean getSeted() {
        return seted;
    }

    public void setSeted(Boolean seted) {
        this.seted = seted;
    }

    public JSONObject getFieldJson(String id)
    {
        try {
            for(int i=0; i<detailsArray.length(); i++) {
                JSONObject fieldDetails = null;
                fieldDetails = detailsArray.getJSONObject(i);
                String curr_id = fieldDetails.getString("id");

                if(curr_id.equals(id))
                {
                    return fieldDetails;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList getByParent(String id)
    {
        ArrayList<JSONObject> fields = new ArrayList<JSONObject>();

        try {
            for(int i=0; i<detailsArray.length(); i++) {
                JSONObject fieldDetails = null;
                fieldDetails = detailsArray.getJSONObject(i);
                String parent_id = fieldDetails.getString("parent_id");

                if(parent_id.equals(id))
                {
                    fields.add(fieldDetails);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return fields;
    }

}
