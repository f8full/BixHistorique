package com.example.F8Full.myapplication.backend.data.model;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by F8Full on 2015-03-21.
 * This file is part of BixHistorique -- Backend
 * This is a data file used to save a pair of availability values
 * Those record are independent and not duplicated in the database
 */
public class AvailabilityRecord {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    int mNbBikes;

    @Persistent
    int mNbDocks;
}
