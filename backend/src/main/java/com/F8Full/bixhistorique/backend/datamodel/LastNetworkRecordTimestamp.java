package com.F8Full.bixhistorique.backend.datamodel;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by F8Full on 2015-03-22.
 * This file is part of BixHistorique - Backend
 *
 */
@PersistenceCapable
public class LastNetworkRecordTimestamp {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
    private String encodedKey;

    private long timestamp;

    private LastNetworkRecordTimestamp(){}

    public LastNetworkRecordTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
