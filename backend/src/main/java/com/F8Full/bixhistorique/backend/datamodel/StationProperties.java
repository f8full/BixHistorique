package com.F8Full.bixhistorique.backend.datamodel;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.NotPersistent;
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

    //Should I construct a key from the id + the timestamp and provide special accessors ?
    @PrimaryKey
    Key key;    //"id_timestamp"
    //referencing this StationProperties entity

    @Persistent
    private
    int id; //Guaranted to be unique in datasource and stable in a season at least

    //Used internally for processing time based queries
    @Persistent
    Date Date_TimestampUTC;

    @Persistent
    @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
    private
    String name;

    @Persistent
    private
    GeoPt pos;

    @NotPersistent
    private
    boolean installed;

    @NotPersistent
    private
    boolean locked;

    @Persistent
    private
    boolean temporary;

    public int getId() {
        return id;
    }

    public void setID(int mID) {
        this.id = mID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPt getPos() {
        return pos;
    }

    public void setPos(GeoPt pos) {
        this.pos = pos;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public void setDate_TimestampUTC(Date timestamp){
        this.Date_TimestampUTC = timestamp;
    }

    public Key getKey(){
        return this.key;
    }

    //"id_timestamp"
    public void setKey(Key fullKey)
    {
        //this.key = KeyFactory.createKey(StationProperties.class.getSimpleName(), "");
        //JSON serializing forbids complex setter (KeyFactory is external)
        this.key = fullKey;

        //id MUST be set before key, or big troubles are ahead
        String timestamp = fullKey.getName().substring(Integer.toString(this.id).length()+ "_".length());    //in ms from epoch
        this.Date_TimestampUTC = new Date(Long.parseLong(timestamp));

    }

    public long getTimestamp(){
        return Long.parseLong(this.key.getName().substring(Integer.toString(this.id).length() + "_".length()));    //in ms from epoch
    }
}
