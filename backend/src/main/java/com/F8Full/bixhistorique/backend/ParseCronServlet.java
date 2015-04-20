package com.F8Full.bixhistorique.backend;

import com.F8Full.bixhistorique.backend.datamodel.AvailabilityPair;
import com.F8Full.bixhistorique.backend.datamodel.LastParseData;
import com.F8Full.bixhistorique.backend.datamodel.Network;
import com.F8Full.bixhistorique.backend.datamodel.ParsingStatus;
import com.F8Full.bixhistorique.backend.datamodel.StationProperties;
import com.F8Full.bixhistorique.backend.datautils.PMF;
import com.F8Full.bixhistorique.backend.utils.Utils;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by F8Full on 2015-03-22.
 * This file is part of BixHistorique -- Backend
 * This servlet is invoked to parse and process the XML source
 */
@SuppressWarnings("unchecked")  //ArrayList<Long> rawMap = (ArrayList)result.getProperty("latestUpdateTimeMap");
                                //resultNetwork.putAvailabilityforStationId((int)stationId, currAvailability);
public class ParseCronServlet extends HttpServlet{

    public final static String DATA_SOURCE_URL = "http://montreal.bixi.com/data/bikeStations.xml";
    //public final static String DATA_SOURCE_URL = "http://www.capitalbikeshare.com/data/stations/bikeStations.xml";
    public final static String DATA_SOURCE_LICENSE = "N/A";

    //AVAILABILITY_ALL_REFRESH_RATE_MINUTES MUST be synced with
    /*<cron>
        <url>/cron/parsecronjob?process=availability</url>
        <description>If active, parse bike availability from data source every five minutes</description>
        <schedule>every 5 minutes</schedule> <!-- MUST BE SYNCED WITH ParseCronServlet.AVAILABILITY_ALL_REFRESH_RATE_MINUTES-->
    </cron>*/
    public static int AVAILABILITY_ALL_REFRESH_RATE_MINUTES = 5; //how often, either complete or partial, a parse happens
    public static int AVAILABILITY_COMPLETE_REFRESH_RATE_MINUTES = 60; // how far spaced in time two complete record must be recorded

    private Key mParsingStatusKey = null;

    public void doGet(HttpServletRequest req, HttpServletResponse resp) /*throws IOException*/ {

        if(shouldParse()) {
            //The current status of the network
            Network curNetwork;

            SourceURL_XMLParser parser;

            try {
                parser = new SourceURL_XMLParser(DATA_SOURCE_URL);
                curNetwork = parser.parse();
            } catch (Exception e) {
                Logger.getLogger(ParseCronServlet.class.getName()).log(Level.SEVERE, "can't parse data source" + e.toString());
                return;
                //Catch here exception happening at parser creation level
                //Probably gonna happen when the data source is down. I think simply dropping any attempt should
                //be reasonable. I'm not sure I can't rewrite programmatically the cron job so that is would implement
                //random or exponential (or some kind of formula I don't really master) retry time. For now no such logic
                //is implemented.
            }

            if (req.getParameter("process").equalsIgnoreCase("availability"))
                processAvailability(curNetwork);
            else if (req.getParameter("process").equalsIgnoreCase("properties"))
                processProperties(curNetwork);

        }

        /*StationProperties response;

        PersistenceManager pm = PMF.get().getPersistenceManager();

        Key k = KeyFactory.createKey(StationProperties.class.getSimpleName(), "100_1427096445684");

        response = pm.getObjectById(StationProperties.class, k);

        pm.close();*/

        //return response;

    }

    private boolean shouldParse() {

        boolean toReturn = false;

        //Retrieve ParsingStatus entity from datastore
        mParsingStatusKey = Utils.RetrieveUniqueKey.parsingStatus();

        if (mParsingStatusKey != null) {
            //Retrieve actual object
            PersistenceManager pm = PMF.get().getPersistenceManager();
            try{
                ParsingStatus parsingStatus  = pm.getObjectById(ParsingStatus.class, mParsingStatusKey);
                toReturn = parsingStatus.isParsing_active();
            }finally {
                pm.close();
            }
        }

        return toReturn;
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doGet(req, resp);
    }

    private void processAvailability(Network curNetwork)
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        //First, retrieve data about the last recording
        Query lastTimeDataQuery = new Query(LastParseData.class.getSimpleName());
        PreparedQuery pq = datastore.prepare(lastTimeDataQuery);

        Entity result = pq.asSingleEntity();

        //Most common case (hopefully)
        if (result != null)
        {
            LastParseData parseData = new LastParseData((Long)result.getProperty("timestamp"));

            //Check if the source file timestamp is different from the last processed one
            if (parseData.getTimestamp() == curNetwork.getTimestamp())
            {
                Logger.getLogger(ParseCronServlet.class.getName()).log(Level.INFO, "unchanged Network -- Done");
                return;
            }

            parseData.setEncodedKey(KeyFactory.keyToString(result.getKey())); //only one LastParseData entity

            ArrayList<Long> rawlatestUpdateMap = (ArrayList)result.getProperty("latestUpdateTimeMap");

            for (int i=0; i< rawlatestUpdateMap.size()-1; i+=2)
            {
                parseData.putLatestUpdateTime(rawlatestUpdateMap.get(i), rawlatestUpdateMap.get(i + 1));
            }

            //using variables allows it to be adjusted later on
            int MAX_STATION_GAP = AVAILABILITY_COMPLETE_REFRESH_RATE_MINUTES / AVAILABILITY_ALL_REFRESH_RATE_MINUTES;

            long countSinceLastComplete = (Long) result.getProperty("countSinceLastComplete");

            boolean needCompleteRecord =  countSinceLastComplete > MAX_STATION_GAP;

            Key previousNetworkKey = KeyFactory.createKey( Network.class.getSimpleName(),
                    String.valueOf(parseData.getTimestamp()) );

            Key resultNetworkKey = curNetwork.getKey();

            //This network object will be persisted
            Network resultNetwork = new Network(resultNetworkKey, previousNetworkKey);

            PersistenceManager pm = PMF.get().getPersistenceManager();

            //Go through all stations and checks if latestUpdateTime changed
            for (long stationId : parseData.getLatestUpdateMapKeySet())
            {
                long previousLatest = parseData.getLatestUpdateTimeForStationId(stationId);

                long currentLatest = curNetwork.stationPropertieTransientMap.get((int)stationId).getTimestamp();

                //time for a complete record OR some new data available for this station
                if (needCompleteRecord || previousLatest != currentLatest)
                {
                    //Update it for next processing
                    parseData.putLatestUpdateTime(stationId, currentLatest);

                    AvailabilityPair currAvailability = curNetwork.getAvailabilityForStation((int)stationId);

                    boolean stationLocked = curNetwork.stationPropertieTransientMap.get((int)stationId).isLocked();
                    boolean stationInstalled = curNetwork.stationPropertieTransientMap.get((int)stationId).isInstalled();

                    if (!stationInstalled)
                    {
                        currAvailability = new AvailabilityPair(-1,-1);
                    }
                    else if (stationLocked)
                    {
                        //A locked station don't gives out bikes but bikes can be docked to it
                        currAvailability = new AvailabilityPair(-1, currAvailability.nbEmptyDocks);
                    }

                    resultNetwork.putAvailabilityforStationId((int)stationId, currAvailability);
                }
            }

            parseData.setTimestamp(curNetwork.getTimestamp());

            if(needCompleteRecord) {
                parseData.setCountSinceLastComplete(0);
                resultNetwork.setComplete();
                addCompleteToParsingStatus();
            }
            else {
                parseData.setCountSinceLastComplete((int) countSinceLastComplete + 1);
                addPartialToParsingStatus();
            }

            try{
                pm.makePersistent(resultNetwork);
                pm.makePersistent(parseData);

                if(mParsingStatusKey != null){
                    ParsingStatus parsingStatus  = pm.getObjectById(ParsingStatus.class, mParsingStatusKey);
                    pm.makePersistent(parsingStatus);
                }


            }finally {
                pm.close();
            }

            //List<Entity> list = pq.asList(FetchOptions.Builder.withDefaults());
        }
        else    //For some reason there is no record of a previous time
        {
            initialParse(curNetwork);
        }

    }

    private void addPartialToParsingStatus() {
        if(mParsingStatusKey != null){
            PersistenceManager pm = PMF.get().getPersistenceManager();
            try{
                ParsingStatus parsingStatus  = pm.getObjectById(ParsingStatus.class, mParsingStatusKey);
                parsingStatus.increment_nb_partial_network();
            }finally {
                pm.close();
            }
        }
    }

    private void addCompleteToParsingStatus() {
        if(mParsingStatusKey != null){
            PersistenceManager pm = PMF.get().getPersistenceManager();
            try{
                ParsingStatus parsingStatus  = pm.getObjectById(ParsingStatus.class, mParsingStatusKey);
                parsingStatus.increment_nb_complete_network();
            }finally {
                pm.close();
            }
        }
    }

    private void initialParse(Network initialNetwork) {

        processProperties(initialNetwork);

        initialNetwork.setComplete();

        //Initial parse : we have to setup a new LastNetworkTimeData entity
        //There will always be only one entity of that kind in the datastore
        //it will be updated each time a new Network object is persisted in the datastore
        //(every time except when data source down OR lastUpdate attribute of <stations> XML tag didn't change)
        LastParseData parseData = new LastParseData(initialNetwork.getTimestamp());

        //Copy latestUpdateTime data from each StationProperties to the LastNetworkTimeData object
        for(int stationId : initialNetwork.stationPropertieTransientMap.keySet())
        {
            parseData.putLatestUpdateTime(stationId, initialNetwork.stationPropertieTransientMap.get(stationId).getTimestamp());
        }

        PersistenceManager pm = PMF.get().getPersistenceManager();

        addCompleteToParsingStatus();

        try{
            pm.makePersistent(initialNetwork);
            pm.makePersistent(parseData);

            if(mParsingStatusKey != null){
                ParsingStatus parsingStatus  = pm.getObjectById(ParsingStatus.class, mParsingStatusKey);
                pm.makePersistent(parsingStatus);
            }

        }finally {
            pm.close();
        }
    }

    private void processProperties(Network curNetwork)
    {
        DateTime today = new DateTime(DateTimeZone.UTC).withTimeAtStartOfDay();

        for(int stationId : curNetwork.stationPropertieTransientMap.keySet())
        {
            curNetwork.stationPropertieTransientMap.get(stationId).setKey(
                    KeyFactory.createKey(StationProperties.class.getSimpleName(),
                            stationId + "_" + today.getMillis() ));
        }

        PersistenceManager pm = PMF.get().getPersistenceManager();
        addStationToParsingStatus();

        try{
            pm.makePersistentAll(curNetwork.stationPropertieTransientMap.values());
        }finally {
            pm.close();
        }

    }

    private void addStationToParsingStatus() {

        if(mParsingStatusKey != null){
            PersistenceManager pm = PMF.get().getPersistenceManager();
            try{
                ParsingStatus parsingStatus  = pm.getObjectById(ParsingStatus.class, mParsingStatusKey);
                parsingStatus.increment_nb_stationproperties();
            }finally {
                pm.close();
            }
        }
    }
}
