package com.iwinter.ppoint.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by sandi on 07.01.2016..
 */
public class ResultsListItem implements Parcelable{

    private String name;
    private String address;
    private String description;
    private String thumbnail;
    private String image;
    private String price;
    private ArrayList icons;
    private String jsonString;

    public ResultsListItem(){

    }

    public ResultsListItem(String name, String address, String description, String thumbnail, String image, String price, ArrayList icons, String jsonString){
        this.name = name;
        this.address = address;
        this.description = description;
        this.thumbnail = thumbnail;
        this.image = image;
        this.price = price;
        this.icons = icons;
        this.jsonString = jsonString;
    }

    public ResultsListItem(Parcel source)
    {
        name = source.readString();
        address = source.readString();
        description = source.readString();
        thumbnail = source.readString();
        image = source.readString();
        price = source.readString();
        jsonString = source.readString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public ArrayList getIcons() {
        return icons;
    }

    public void setIcons(ArrayList icons) {
        this.icons = icons;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        applyDefaultValues();

        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(description);
        dest.writeString(thumbnail);
        dest.writeString(image);
        dest.writeString(price);
        dest.writeString(jsonString);
    }

    private void applyDefaultValues() {
        if(name == null)
            name="";

        if(address == null)
            address="";

        if(description == null)
            description="";

        if(thumbnail == null)
            thumbnail="";

        if(image == null)
            image="";

        if(price == null)
            price="";

        if(jsonString == null)
            jsonString="";
    }

    public static Creator<ResultsListItem> CREATOR = new Creator<ResultsListItem>(){

        @Override
        public ResultsListItem createFromParcel(Parcel source) {
            return new ResultsListItem(source);
        }

        @Override
        public ResultsListItem[] newArray(int size) {
            return new ResultsListItem[size];
        }
    };



}
