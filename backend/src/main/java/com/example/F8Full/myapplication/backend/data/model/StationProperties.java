package com.example.F8Full.myapplication.backend.data.model;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by F8Full on 2015-03-21.
 * * This file is part of BixHistorique -- Backend
 * This is a data class used to describe mostly stable properties of a bike station
 * - Key : constructed from the string representation of the timestamp
 * - Name
 * - Terminal name
 * - Latitude and longitude as a GeoPt
 * - Installed
 * - InstallDate
 * - RemovalDate
 * - Locked
 * - Temporary
 * - Public
 */
@PersistenceCapable
public class StationProperties {

    @PrimaryKey
    Key mKEY_Timestamp;

    //Used internally for processing queries
    @Persistent
    Date mDate_Timestamp;

    @Persistent
    String mName;

    @Persistent
    String mTerminalName;

    @Persistent
    GeoPt mPos;

    @Persistent
    boolean mInstalled;

    @Persistent
    Date mInstallDate;

    @Persistent
    Date mRemovalDate;

    @Persistent
    boolean mLocked;

    @Persistent
    boolean mTemporary;

    @Persistent
    boolean mPublic;
}
