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
    private int biggestGap;
    //Retains the max number of read that have been required to retrieve the availability of a
    //station. Baed on that, we will choose to generate a full Network instead of an incremental one
    //This will reduce the number of read required to rebuilt availability at any given time
    //at the cost of more writes when not many stations are used


    private LastParseData(){ this.biggestGap = 0;}

    public LastParseData(long timestamp)
    {
        this.timestamp = timestamp;
        this.latestUpdateTimeMap = new Hashtable<>();
        this.biggestGap = 0;
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

    public Set<Long> getLatestUpdateMapKeySet()
    {
        return latestUpdateTimeMap.keySet();
    }

    public long getLatestUpdateTimeForStationId(long stationId)
    {
        return latestUpdateTimeMap.get(stationId);
    }

    public void setBiggestGap(int gap){
        this.biggestGap = gap;
    }

    public int getBiggestGap(){
        return this.biggestGap;
    }


}
