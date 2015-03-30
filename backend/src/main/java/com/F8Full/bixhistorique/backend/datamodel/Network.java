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
    private Key Key_timestamp;

    //Used internally for queries
    @Persistent
    private Date Date_timestampUTC;

    @Persistent
    private boolean complete;   //True if this is a complete bike network state record

    @Persistent
    private Map<Integer, Integer> nbBikesByStationId;

    @Persistent
    private Map<Integer, Integer> nbEmptyDocksByStationId;

    @Persistent
    private Key previousNetworkKey;

    //Filled at parsing time
    @NotPersistent
    public Map<Integer, StationProperties> stationPropertieTransientMap = new Hashtable<>();  //Mapped by station ID

    //The two following maps are not in StationProperties because they are way less
    //stable in time than the other property of a station (name, position...)
    //Because one Network object will be persisted each time new data is available in the feed
    //it seems to be the right place to put those timestamps

    public Network(Key currentKey, Key previousKey){

        this(new Hashtable<Integer,Integer>(), new Hashtable<Integer,Integer>(), currentKey, previousKey, false);
    }

    private Network(Map<Integer, Integer> bikesMap, Map<Integer, Integer> docksMap, Key curKey, Key prevKey, boolean complete) {
        this.nbBikesByStationId = bikesMap;
        this.nbEmptyDocksByStationId = docksMap;
        setTimestamp(curKey);
        this.previousNetworkKey = prevKey;
        this.complete = complete;
    }

    public Key getKey(){
        return this.Key_timestamp;
    }


    //constructed from timestamp string, in ms since epoch
    public void setTimestamp(Key Key_timestamp){
        //this.Key_timestamp = KeyFactory.createKey(Network.class.getSimpleName(), timestamp);
        //JSON serializing forbids complex setter (KeyFactory is external)
        this.Key_timestamp = Key_timestamp;
        if (Key_timestamp != null)  //Happens at construction
            Date_timestampUTC = new Date(Long.parseLong(Key_timestamp.getName()));
    }

    public long getTimestamp(){
        return Long.parseLong(this.Key_timestamp.getName());
    }

    public void putStationProperties(int _key, StationProperties value)
    {
        this.stationPropertieTransientMap.put(_key, value);
    }

    public void putAvailabilityforStationId(int stationId, AvailabilityPair<Integer, Integer> availability)
    {
        this.nbBikesByStationId.put(stationId, availability.nbBikes);
        this.nbEmptyDocksByStationId.put(stationId, availability.nbEmptyDocks);
    }

    public Key getPreviousNetworkKey(){
        return this.previousNetworkKey;
    }

    public void setComplete(){
        this.complete = true;
    }

    public boolean isComplete(){
        return this.complete;
    }


    public boolean areAvailibilityMapNull(){
        return nbEmptyDocksByStationId == null;
        //Both map will always have the same key set
    }

    public boolean availabilityMapContains(int stationId){
        return nbBikesByStationId.containsKey(stationId);
        //Both map will always have the same key set
    }

    public AvailabilityPair<Integer,Integer> getAvailabilityForStation(int stationId){
        return new AvailabilityPair<>(nbBikesByStationId.get(stationId), nbEmptyDocksByStationId.get(stationId));
    }
}
