package com.F8Full.bixhistorique.backend.datamodel;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by F8Full on 2015-04-14.
 * Persistable data class for parsing status data. It's updated by parse cron job. It's also updated
 * by ActivateParsing cron job. It's automatically created.
 */
@PersistenceCapable
public class ParsingStatus {

    //Those STATIC properties are not stored in datastore.
    //It's because they derive of something we don't have control over : the cron GAE scheduler
    //Hence it MUST be synced with
    /*<cron>
        <url>/cron/parsecronjob?process=availability</url>
        <description>If active, parse bike availability from data source every five minutes</description>
        <schedule>every 5 minutes</schedule> <!-- MUST BE SYNCED WITH ParsingStatus.availabilityAllRefreshRateMinutes-->
    </cron>*/

    @NotPersistent
    public static int availabilityAllRefreshRateMinutes = 5; //how often, either complete or partial, a parse happens
    @NotPersistent
    public static int availabilityCompleteRefreshRateMinutes = 60; // how far spaced in time two complete record must be recorded

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
    private String encodedKey;

    //Nothing is indexed as there will be only one entity of this kind and it will always be
    //retrieved by key -- see Utils.RetrieveUniqueKey.parsingStatus()
    @Persistent
    @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
    private boolean parsing_active;

    @Persistent
    @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
    private int nb_complete_network_parsing;

    @Persistent
    @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
    private int nb_partial_network_parsing;

    @Persistent
    @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
    private int nb_stationproperties_parsing;

    /*public String getEncodedKey() {
        return encodedKey;
    }

    public void setEncodedKey(String encodedKey) {
        this.encodedKey = encodedKey;
    }*/

    public boolean isParsing_active() {
        return parsing_active;
    }

    public void setParsing_active(boolean parsing_active) {
        this.parsing_active = parsing_active;
    }

    public int getNb_complete_network_parsing() {
        return nb_complete_network_parsing;
    }
/*
    public void setNb_complete_network_parsing(int nb_complete_network_parsing) {
        this.nb_complete_network_parsing = nb_complete_network_parsing;
    }
*/
    public int getNb_partial_network_parsing() {
        return nb_partial_network_parsing;
    }

    public void increment_nb_partial_network(){
        ++this.nb_partial_network_parsing;
    }

    public void increment_nb_complete_network(){
        ++this.nb_complete_network_parsing;
    }

    public void increment_nb_stationproperties(){
        ++this.nb_stationproperties_parsing;
    }


    public int getNb_stationproperties_parsing() {
        return nb_stationproperties_parsing;
    }
/*
    public void setNb_stationpropertie_parsing(int nb_stationpropertie_parsing) {
        this.nb_stationpropertie_parsing = nb_stationpropertie_parsing;
    }*/
}
