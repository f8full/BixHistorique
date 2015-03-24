package com.F8Full.bixhistorique.backend.datamodel;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by F8Full on 2015-03-21.
 * This file is part of BixHistorique -- Backend
 * This is a data file used to save a pair of availability values
 * Key is built from the concatenation of NbBikes and  NbEmptyDocks (ex : "3|5")
 * Those record are independent and not duplicated in the database
 */
@PersistenceCapable
public class AvailabilityRecord {

    //key name will be in the form of "NbBikes|NbEmptyDocks"
    @PrimaryKey
    private Key key;

    @Persistent
    private
    int nbBikes;

    @Persistent
    private
    int nbEmptyDocks;

    public AvailabilityRecord(){}

    //Key as "nbBikes|nbEmptyDocks"
    public AvailabilityRecord(Key key)
    {
        String[] numbers = key.getName().split("\\|");

        this.nbBikes = Integer.parseInt(numbers[0]);
        this.nbEmptyDocks = Integer.parseInt(numbers[1]);
        this.key = key;
    }

    //fully constructed as "NbBikes|NbEmptyDocks"
    public void setKey(Key fullKey){
        //this.key = KeyFactory.createKey(AvailabilityRecord.class.getSimpleName(), keyString);
        //JSON serializing forbids complex setter (KeyFactory is external)
        this.key = fullKey;
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

    public Key getKey(){
        return this.key;
    }
}
