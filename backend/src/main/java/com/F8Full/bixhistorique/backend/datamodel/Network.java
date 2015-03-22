package com.F8Full.bixhistorique.backend.datamodel;

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
    Key Key_timestamp;

    //Used internally for queries
    @Persistent
    Date Date_timestamp;


    @Persistent
    Map<Integer, StationProperties> stationPropertieMap;   //Mapped by station ID

    @Persistent
    Map<Integer, AvailabilityRecord> availabilityMap;  //Mapped by station ID

    public Network(){

        this(new Hashtable<Integer,StationProperties>(), new Hashtable<Integer,AvailabilityRecord>());
    }

    private Network(Map<Integer, StationProperties> propMap, Map<Integer, AvailabilityRecord> availMap) {
        this.stationPropertieMap = propMap;
        this.availabilityMap = availMap;
    }

    //in ms since epoch
    public void setTimestamp(String timestamp){
        this.Key_timestamp = KeyFactory.createKey(Network.class.getSimpleName(), timestamp);

        Date_timestamp = new Date(Long.parseLong(timestamp));
    }

    public void putStationProperties(int _key, StationProperties value)
    {
        stationPropertieMap.put(_key, value);
    }

    public void putAvailabilityRecord(int _key, AvailabilityRecord value)
    {
        availabilityMap.put(_key, value);
    }
}
