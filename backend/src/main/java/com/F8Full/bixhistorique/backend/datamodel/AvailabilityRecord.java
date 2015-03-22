package com.F8Full.bixhistorique.backend.datamodel;

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
    private Key key;

    @Persistent
    private
    int nbBikes;

    @Persistent
    private
    int nbEmptyDocks;

    //"NbBikes|NbEmptyDocks"
    public void setKey(String keyString){
        this.key = KeyFactory.createKey(AvailabilityRecord.class.getSimpleName(), keyString);
    }


    public int getNbBikes() {
        return nbBikes;
    }

    public void setNbBikes(int mNbBikes) {
        this.nbBikes = mNbBikes;
    }

    public int getNbEmptyDocks() {
        return nbEmptyDocks;
    }

    public void setNbEmptyDocks(int mNbDocks) {
        this.nbEmptyDocks = mNbDocks;
    }
}
