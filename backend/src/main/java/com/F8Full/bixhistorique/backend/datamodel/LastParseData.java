package com.F8Full.bixhistorique.backend.datamodel;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by F8Full on 2015-03-22.
 * This file is part of BixHistorique - Backend
 * Used to persist the timestamp (hence key) of the last persisted Network entity
 * Also maintains a complete map of each station lastUpdateTime
 * It is overwritten each time a new Network entity is persisted
 *
 */
@PersistenceCapable
public class LastParseData {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
    private String encodedKey;

    @Persistent
    private long timestamp;

    @Persistent
    Map<Long, Long> latestUpdateTimeMap;  //Mapped by station ID

    @Persistent
    private int countSinceLastComplete; //How many parse since last complete record

    private LastParseData(){}

    public LastParseData(long timestamp)
    {
        this.timestamp = timestamp;
        this.latestUpdateTimeMap = new Hashtable<>();
        this.countSinceLastComplete = 0;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public void setEncodedKey(String encodedKey){
        this.encodedKey = encodedKey;
    }


    public void putLatestUpdateTime(long stationId, long timestamp)
    {
        this.latestUpdateTimeMap.put(stationId,timestamp);
    }

    public void removeStationIDKey(long toRemove){
        this.latestUpdateTimeMap.remove(toRemove);
    }

    public Set<Long> getLatestUpdateMapKeySet()
    {
        return latestUpdateTimeMap.keySet();
    }

    public long getLatestUpdateTimeForStationId(long stationId)
    {
        return latestUpdateTimeMap.get(stationId);
    }

    public void setCountSinceLastComplete(int count){
        this.countSinceLastComplete = count;
    }

}
