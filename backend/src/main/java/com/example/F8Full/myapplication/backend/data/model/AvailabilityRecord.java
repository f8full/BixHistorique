package com.example.F8Full.myapplication.backend.data.model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by F8Full on 2015-03-21.
 * This file is part of BixHistorique -- Backend
 * This is a data file used to save a pair of availability values
 * Key is built from the concatenation of NbBikes and  NbEmptyDocks (ex : "3|5")
 * Those record are independent and not duplicated in the database
 */
public class AvailabilityRecord {

    @PrimaryKey
    private Key mKey;

    @Persistent
    private
    int mNbBikes;

    @Persistent
    private
    int mNbEmptyDocks;

    //"NbBikes|NbEmptyDocks"
    public void setKey(String _keyString){
        mKey = KeyFactory.createKey(AvailabilityRecord.class.getSimpleName(), _keyString);
    }


    public int getNbBikes() {
        return mNbBikes;
    }

    public void setNbBikes(int mNbBikes) {
        this.mNbBikes = mNbBikes;
    }

    public int getNbEmptyDocks() {
        return mNbEmptyDocks;
    }

    public void setNbEmptyDocks(int mNbDocks) {
        this.mNbEmptyDocks = mNbDocks;
    }
}
