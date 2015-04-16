package com.F8Full.bixhistorique.backend.datamodel;

import java.util.Date;

/**
 * Created by F8Full on 2015-04-13.
 * Class data for stats. TODO: Refactor with ParsingStatus to improve getstats reads cost (no datastore query)
 */
public class DataStats {


    public int availability_refresh_rate_minutes;
    public String stationproperties_refresh_rate = "24h 03:00 America/Montreal";
    public Date oldest_availability_date;
    public Date stats_generated_on_datetime = new Date();
    public long nb_total_entities;
    public long nb_network_entities;
    public long nb_stationproperties_entities;

    public int last_complete_availability_nb_stations;
    public Date last_complete_availability_date;

    public Date last_partial_availability_date;
    public int last_partial_availability_nb_stations;

    public boolean parsing_active;
    public long nb_complete_availability_parse;
    public long nb_partial_availability_parse;
    public long total_nb_availability_parse;
    public long nb_days_of_data;
}
