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
 * - ID (from XML source)
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

    @Persistent
    private
    int mID;

    //Used internally for processing queries
    @Persistent
    Date mDate_Timestamp;

    @Persistent
    private
    String mName;

    @Persistent
    private
    String mTerminalName;

    @Persistent
    private
    GeoPt mPos;

    @Persistent
    private
    boolean mInstalled;

    @Persistent
    private
    Date mInstallDate;

    @Persistent
    private
    Date mRemovalDate;

    @Persistent
    private
    boolean mLocked;

    @Persistent
    private
    boolean mTemporary;

    @Persistent
    private
    boolean mPublic;


    public int getID() {
        return mID;
    }

    public void setID(int mID) {
        this.mID = mID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getTerminalName() {
        return mTerminalName;
    }

    public void setTerminalName(String mTerminalName) {
        this.mTerminalName = mTerminalName;
    }

    public GeoPt getPos() {
        return mPos;
    }

    public void setPos(GeoPt mPos) {
        this.mPos = mPos;
    }

    public boolean isInstalled() {
        return mInstalled;
    }

    public void setInstalled(boolean mInstalled) {
        this.mInstalled = mInstalled;
    }

    public Date getInstallDate() {
        return mInstallDate;
    }

    public void setInstallDate(Date mInstallDate) {
        this.mInstallDate = mInstallDate;
    }

    public Date getRemovalDate() {
        return mRemovalDate;
    }

    public void setRemovalDate(Date mRemovalDate) {
        this.mRemovalDate = mRemovalDate;
    }

    public boolean isLocked() {
        return mLocked;
    }

    public void setLocked(boolean mLocked) {
        this.mLocked = mLocked;
    }

    public boolean isTemporary() {
        return mTemporary;
    }

    public void setTemporary(boolean mTemporary) {
        this.mTemporary = mTemporary;
    }

    public boolean isPublic() {
        return mPublic;
    }

    public void setPublic(boolean mPublic) {
        this.mPublic = mPublic;
    }
}
