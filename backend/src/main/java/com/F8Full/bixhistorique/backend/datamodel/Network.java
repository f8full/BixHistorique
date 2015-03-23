package com.F8Full.bixhistorique.backend.datamodel;

import com.google.appengine.api.datastore.Key;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import javax.jdo.annotations.NotPersistent;
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

    //Constructed from the timestamp
    @PrimaryKey
    Key Key_timestamp;

    //Used internally for queries
    @Persistent
    Date Date_timestamp;


    //Implementing relationship with StationProperties
    @Persistent
    Map<Integer, Key> stationPropertieKeyMap;   //Mapped by station ID

    //Implementing relationship with AvailabilityRecord
    @Persistent
    Map<Integer, Key> availabilityKeyMap;  //Mapped by station ID

    //Filled at parsing time
    //Relationships are unowned, hence entities must be persisted independently
    //we will choose only the pertinent ones
    @NotPersistent
    public Map<Integer, StationProperties> stationPropertieMap = new Hashtable<>();  //Mapped by station ID
    @NotPersistent
    public Map<Integer, AvailabilityRecord> availabilityMap = new Hashtable<>();  //Mapped by station ID

    //The two following maps are not in StationProperties because they are way less
    //stable in time than the other property of a station (name, position...)
    //Because one Network object will be persisted each time new data is available in the feed
    //it seems to be the right place to put those timestamps
    @Persistent
    Map<Integer, Long> latestUpdateTimeMap;  //Mapped by station ID
    @Persistent
    Map<Integer, Long> lastCommWithServerMap;  //Mapped by station ID

    public Network(){

        this(new Hashtable<Integer,Key>(), new Hashtable<Integer,Key>(),
                new Hashtable<Integer,Long>(), new Hashtable<Integer,Long>());
    }

    private Network(Map<Integer, Key> propMap, Map<Integer, Key> availMap,
                    Map<Integer, Long> updateTimeMap, Map<Integer, Long> commWithServerTimeMap) {
        this.stationPropertieKeyMap = propMap;
        this.availabilityKeyMap = availMap;
        this.latestUpdateTimeMap = updateTimeMap;
        this.lastCommWithServerMap = commWithServerTimeMap;
    }

    //constructed from timestamp string, in ms since epoch
    public void setTimestamp(Key Key_timestamp){
        //this.Key_timestamp = KeyFactory.createKey(Network.class.getSimpleName(), timestamp);
        //JSON serializing forbids complex setter (KeyFactory is external)
        this.Key_timestamp = Key_timestamp;
        Date_timestamp = new Date(Long.parseLong(Key_timestamp.getName()));
    }

    public long getTimestamp(){
        return Long.parseLong(this.Key_timestamp.getName());
    }

    public void putStationProperties(int _key, StationProperties value)
    {
        this.stationPropertieMap.put(_key, value);

        this.stationPropertieKeyMap.put(_key, value.getKey());
        //value.addNetworkParentKey(this.Key_timestamp);
    }

    public void putAvailabilityRecord(int _key, AvailabilityRecord value)
    {
        this.availabilityMap.put(_key, value);

        this.availabilityKeyMap.put(_key, value.getKey());
    }

    public void putLatestUpdateTime(int stationId, long timestamp)
    {
        this.latestUpdateTimeMap.put(stationId,timestamp);
    }

    public void putLastCommWithServer(int stationId, long timestamp)
    {
        this.lastCommWithServerMap.put(stationId, timestamp);
    }
}
