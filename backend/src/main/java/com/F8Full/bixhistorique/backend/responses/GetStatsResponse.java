package com.F8Full.bixhistorique.backend.responses;

import com.F8Full.bixhistorique.backend.datamodel.DataStats;
import com.F8Full.bixhistorique.backend.datamodel.Network;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by F8Full on 2015-04-13.
 * This is the response object to a getstats request
 */
public class GetStatsResponse extends BaseResponse {

    private DataStats mData = new DataStats();

    //List<String> kindStrings = new ArrayList<>();

    public GetStatsResponse(){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


        Entity globalStat = datastore.prepare(new Query("__Stat_Total__")).asSingleEntity();
        //Long totalBytes = (Long) globalStat.getProperty("bytes");
        if (globalStat != null) {
            mData.nb_total_entities = (Long) globalStat.getProperty("count");
            Date globalTimestampFilter = (Date) globalStat.getProperty("timestamp");


            Query.Filter timeFilter =
                    new Query.FilterPredicate("timestamp",
                            Query.FilterOperator.EQUAL,
                            globalTimestampFilter);

            Query q = new Query("__Stat_Kind__")
                    .setFilter(timeFilter);

            PreparedQuery statKind = datastore.prepare(q);

            for (Entity statsForKind : statKind.asIterable()) {
                String kindName = (String) statsForKind.getProperty("kind_name");
                long kindCount = (Long) statsForKind.getProperty("count");

                if (kindName.equalsIgnoreCase("network")) {
                    mData.nb_network_entities = kindCount;
                } else if (kindName.equalsIgnoreCase("stationproperties")) {
                    mData.nb_stationproperties_entities = kindCount;
                }

                //Long totalBytes = (Long) globalStat.getProperty("bytes");
                //Long totalEntities = (Long) globalStat.getProperty("count");
                //String kindName = (String) globalStat.getProperty("kind_name");
                //resp.getWriter().println("[" + kindName + "] has " + totalEntities + " entities and takes up " + totalBytes + "bytes<br/>");
            }
        }

        //Retrieve last complete Network entity
        Query lastCompleteNetworkQuery = new Query(Network.class.getSimpleName())
                /*.setFilter(new Query.FilterPredicate("complete",
                        Query.FilterOperator.EQUAL,
                        true))*/ //No index required
                .addSort("Date_timestampUTC", Query.SortDirection.DESCENDING);

        PreparedQuery lastCompleteNetworkPreparedQuery = datastore.prepare(lastCompleteNetworkQuery);

        boolean gotPartial = false;
        boolean gotComplete = false;
        //let's retrieve twice as much records as the minimum should be (downtime) 24 record is two hours, let's get 30
        for (Entity networkEntity : lastCompleteNetworkPreparedQuery.asIterable(FetchOptions.Builder.withLimit(30))) {
            boolean complete = (boolean) networkEntity.getProperty("complete");
            if (complete)
            {
                if (!gotComplete) {
                    mData.last_complete_availability_check_date = (Date) networkEntity.getProperty("Date_timestampUTC");
                    ArrayList truc = (ArrayList) networkEntity.getProperty("nbBikesByStationId");
                    mData.last_complete_availability_check_nb_stations = truc.size() / 2;
                    gotComplete = true;
                }
            }
            else if(!gotPartial){
                mData.last_partial_availability_check_date = (Date)networkEntity.getProperty("Date_timestampUTC");
                ArrayList truc = (ArrayList)networkEntity.getProperty("nbBikesByStationId");
                mData.last_partial_availability_check_nb_stations = truc.size()/2;
                gotPartial = true;
            }

            if(gotPartial && gotComplete){
                break;
            }
        }

        //Retrieve last complete Network entity
        Query oldestNetworkQuery = new Query(Network.class.getSimpleName())
                /*.setFilter(new Query.FilterPredicate("complete",
                        Query.FilterOperator.EQUAL,
                        true))*/ //No index required
                .addSort("Date_timestampUTC", Query.SortDirection.ASCENDING);

        PreparedQuery oldestNetworkPreparedQuery = datastore.prepare(oldestNetworkQuery);

        //let's retrieve twice as much records as the minimum should be (downtime) 24 record is two hours, let's get 30
        for (Entity networkEntity : oldestNetworkPreparedQuery.asIterable(FetchOptions.Builder.withLimit(1))) {
            mData.oldest_availability_check_date = (Date)networkEntity.getProperty("Date_timestampUTC");
        }

        //Calculating additional stats data

        //Entity networkStat = datastore.prepare(new Query("__Stat_Kind__")).asSingleEntity();
    }

    public DataStats getData(){ return mData;}

}
