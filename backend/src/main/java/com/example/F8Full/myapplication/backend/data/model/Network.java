package com.example.F8Full.myapplication.backend.data.model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by F8Full on 2015-03-21.
 * This file is part of BixHistorique -- Backend
 * This is a data class describing the complete state of the Bixi network for a given timestamp
 * - a Key : constructed with the string representation of the timestamp
 * - a Date object representing the timestamp and used internally when processing queries
 * - Two Maps, for properties and availability for a given stationID
 */
@PersistenceCapable
public class Network {

    //Constructed from the timestamp given in the XML file
    @PrimaryKey
    Key mKEY_timestamp;

    //Used internally for queries
    @Persistent
    Date mDate_timestamp;

    @Persistent
    Map<Integer, StationProperties> mStationPropertieMap;   //Mapped by station ID

    @Persistent
    Map<Integer, AvailabilityRecord> mAvailabilityMap;  //Mapped by station ID

    public Network(){
        mStationPropertieMap = new Hashtable<>();
        mAvailabilityMap = new Hashtable<>();
    }

    //in ms since epoch
    public void setTimestamp(String _timestamp){
        this.mKEY_timestamp = KeyFactory.createKey(Network.class.getSimpleName(), _timestamp);

        mDate_timestamp = new Date(Long.parseLong(_timestamp));
    }

    public void putStationProperties(int _key, StationProperties _value)
    {
        mStationPropertieMap.put(_key, _value);
    }

    public void putAvailabilityRecord(int _key, AvailabilityRecord _value)
    {
        mAvailabilityMap.put(_key, _value);
    }
}
