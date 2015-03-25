package com.F8Full.bixhistorique.backend.datamodel;

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

    //Should I construct a key from the id + the timestamp and provide special accessors ?
    @PrimaryKey
    Key key;    //"id|timestamp"
    //referencing this StationProperties entity

    @Persistent
    private
    int id; //Guaranted to be unique in datasource and stable in a season at least

    //Used internally for processing time based queries
    @Persistent
    Date Date_Timestamp;

    @Persistent
    private
    String name;

    @Persistent
    private
    String terminalName;

    @Persistent
    private
    GeoPt pos;

    @Persistent
    private
    boolean installed;

    @Persistent
    private
    Date installDate;

    @Persistent
    private
    Date removalDate;

    @Persistent
    private
    boolean locked;

    @Persistent
    private
    boolean temporary;

    @Persistent
    private
    boolean mPublic;    //public is a keyword


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

    public String getTerminalName() {
        return terminalName;
    }

    public void setTerminalName(String terminalName) {
        this.terminalName = terminalName;
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

    public Date getInstallDate() {
        return installDate;
    }

    public void setInstallDate(Date installDate) {
        this.installDate = installDate;
    }

    public Date getRemovalDate() {
        return removalDate;
    }

    public void setRemovalDate(Date removalDate) {
        this.removalDate = removalDate;
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

    public boolean isPublic() {
        return mPublic;
    }   //public is a keyword

    public void setPublic(boolean _public) {
        this.mPublic = _public;
    } //public is a keyword

    public void setDate_Timestamp(Date timestamp){
        this.Date_Timestamp = timestamp;
    }

    public Key getKey(){
        return this.key;
    }

    //"id|timestamp"
    public void setKey(Key fullKey)
    {
        //this.key = KeyFactory.createKey(StationProperties.class.getSimpleName(), "");
        //JSON serializing forbids complex setter (KeyFactory is external)
        this.key = fullKey;

        //id MUST be set before key, or big troubles are ahead
        String timestamp = fullKey.getName().substring(Integer.toString(this.id).length()+ "|".length());    //in ms from epoch
        this.Date_Timestamp = new Date(Long.parseLong(timestamp));

    }

    public long getTimestamp(){
        return Long.parseLong(this.key.getName().substring(Integer.toString(this.id).length() + "|".length()));    //in ms from epoch
    }
}
