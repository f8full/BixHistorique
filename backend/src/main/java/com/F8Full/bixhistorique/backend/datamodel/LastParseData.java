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
    private Map<Long, Long> gapByStationId;
    //Retains the max number of read that have been required to retrieve the availability of a
    //station. Based on that, we will choose to record the availability of a station regardless
    //of an actual change when over a certain threshold. Doesn't contains a stationid key if gap == 0
    //This will reduce the number of read required to rebuilt availability at any given time
    //at the cost of more writes when not many stations are used


    private LastParseData(){}

    public LastParseData(long timestamp)
    {
        this.timestamp = timestamp;
        this.latestUpdateTimeMap = new Hashtable<>();
        this.gapByStationId = new Hashtable<>();
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

    public void increaseGapForStationId(long stationId)
    {
        if (this.gapByStationId.containsKey(stationId))
            this.gapByStationId.put(stationId, this.gapByStationId.get(stationId) + 1);
        else
            this.gapByStationId.put(stationId, 1L);  //map doesn't keep values == 0
    }

    public long getGapForStationId(long stationId)
    {
        if (!this.gapByStationId.containsKey(stationId))
            return 0;
        else
            return this.gapByStationId.get(stationId);
    }

    public void resetGapForStationId(long stationId)
    {
        this.gapByStationId.remove(stationId);
    }

    public void putGapForStationId(long stationId, long gap)
    {
        this.gapByStationId.put(stationId, gap);
    }
}
