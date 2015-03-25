package com.F8Full.bixhistorique.backend;

import com.F8Full.bixhistorique.backend.datamodel.AvailabilityRecord;
import com.F8Full.bixhistorique.backend.datamodel.LastNetworkTimeData;
import com.F8Full.bixhistorique.backend.datamodel.Network;
import com.F8Full.bixhistorique.backend.datamodel.StationProperties;
import com.F8Full.bixhistorique.backend.datautils.PMF;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

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
public class ParseCronServlet extends HttpServlet{

    private static final Logger log = Logger.getLogger(ParseCronServlet.class.getName());

    public void doGet(HttpServletRequest req, HttpServletResponse resp) /*throws IOException*/ {
        //The current status of the network
        Network curNetwork;
        String parseUrl = "http://www.capitalbikeshare.com/data/stations/bikeStations.xml";
        //String parseUrl = "WEB-INF/capitalBikeShare"+ fileIndex + ".xml";

        SourceURL_XMLParser parser;

        try
        {
            parser = new SourceURL_XMLParser(parseUrl);
            curNetwork = parser.parse();
        }
        catch (Exception e)
        {
            Logger.getLogger(ParseCronServlet.class.getName()).log(Level.SEVERE, "can't parse data source");
            return;
            //Catch here exception happening at parser creation level
            //Probably gonna happen when the data source is down. I think simply dropping any attempt should
            //be reasonable. I'm not sure I can't rewrite programmatically the cron job so that is would implement
            //random or exponential (or some kind of formula I don't really master) retry time. For now no such logic
            //is implemented.
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        //First, retrieve data about the last recording
        Query lastTimeDataQuery = new Query(LastNetworkTimeData.class.getSimpleName());
        PreparedQuery pq = datastore.prepare(lastTimeDataQuery);

        Entity result = pq.asSingleEntity();

        //Most common case (hopefully)
        if (result != null)
        {
            LastNetworkTimeData timeData = new LastNetworkTimeData((Long)result.getProperty("timestamp"));

            //Check if the source file timestamp is different from the last processed one
            if (timeData.getTimestamp() == curNetwork.getTimestamp())
            {
                Logger.getLogger(ParseCronServlet.class.getName()).log(Level.INFO, "unchanged Network -- Done");
                return;
            }

            timeData.setEncodedKey(KeyFactory.keyToString(result.getKey()));

            ArrayList<Long> rawMap = (ArrayList)result.getProperty("latestUpdateTimeMap");

            for (int i=0; i< rawMap.size()-1; i+=2)
            {
                timeData.putLatestUpdateTime(rawMap.get(i), rawMap.get(i + 1));
            }

            Key previousNetworkKey = KeyFactory.createKey( Network.class.getSimpleName(),
                    String.valueOf(timeData.getTimestamp()) );

            Key resultNetworkKey = curNetwork.getKey();

            //This network object will be persisted
            Network resultNetwork = new Network(resultNetworkKey, previousNetworkKey);

            PersistenceManager pm = PMF.get().getPersistenceManager();

            //Go through all stations and checks if latestUpdateTime changed
            for (long stationId : timeData.getLatestUpdateMapKeySet())
            {
                long previousLatest = timeData.getLatestUpdateTimeForStationId(stationId);

                long currentLatest = curNetwork.stationPropertieMap.get((int)stationId).getTimestamp();

                //Some new data available for this station
                if (previousLatest != currentLatest)
                {
                    //Record it for next processing
                    timeData.putLatestUpdateTime(stationId, currentLatest);


                    Network prevNetwork = pm.getObjectById(Network.class, previousNetworkKey);

                    //Availability key name is constructed as "NbBikes|NbEmptyDocks"
                    String prevAvailability = retrievePreviousAvailability(prevNetwork, stationId, pm).getName();
                    String currAvailability = curNetwork.availabilityMap.get((int)stationId).getNbBikes() + "|" + curNetwork.availabilityMap.get((int)stationId).getNbEmptyDocks();

                    if (!prevAvailability.equalsIgnoreCase(currAvailability))
                    {
                        AvailabilityRecord curAvail = new AvailabilityRecord(KeyFactory.createKey(AvailabilityRecord.class.getSimpleName(), currAvailability ));

                        resultNetwork.putAvailabilityRecord((int)stationId, curAvail);
                    }
                }
            }

            timeData.setTimestamp(curNetwork.getTimestamp());

            try{
                pm.makePersistentAll(resultNetwork.availabilityMap.values());
                pm.makePersistent(resultNetwork);
                pm.makePersistent(timeData);

            }finally {
                pm.close();
            }

            //List<Entity> list = pq.asList(FetchOptions.Builder.withDefaults());
        }
        else    //For some reason there is no record of a previous time
        {
            initialParse(parser);
        }

        /*StationProperties response;

        PersistenceManager pm = PMF.get().getPersistenceManager();

        Key k = KeyFactory.createKey(StationProperties.class.getSimpleName(), "100|1427096445684");

        response = pm.getObjectById(StationProperties.class, k);

        pm.close();*/

        //return response;

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doGet(req, resp);
    }

    private void initialParse(SourceURL_XMLParser parser) {

        //This is a read only copy of the network currently described by the data source
        final Network initialNetwork = parser.parse();

        //Initial parse : we have to setup a new LastNetworkTimeData entity
        //There will always be only one entity of that kind in the datastore
        //it will be updated each time a new Network object is persisted in the datastore
        //(every time except when data source down OR lastUpdate attribute of <stations> XML tag didn't change)
        LastNetworkTimeData networkTimeData = new LastNetworkTimeData(initialNetwork.getTimestamp());

        //Copy latestUpdateTime data from each StationProperties to the LastNetworkTimeData object
        //Update key to store the timestamp of the Network object (oldest one referring this particular StationProperties)
        for(int stationId : initialNetwork.stationPropertieMap.keySet())
        {
            networkTimeData.putLatestUpdateTime(stationId, initialNetwork.stationPropertieMap.get(stationId).getTimestamp());

            initialNetwork.stationPropertieMap.get(stationId).setKey(
                    KeyFactory.createKey(StationProperties.class.getSimpleName(),
                            stationId + "|" + initialNetwork.getTimestamp()));
        }

        PersistenceManager pm = PMF.get().getPersistenceManager();

        try{
            //First persist all StationProperties and AvailabilityRecord entities
            //Many to Many UNOWNED relationship
            //They are in non persistent maps
            pm.makePersistentAll(initialNetwork.availabilityMap.values());
            pm.makePersistentAll(initialNetwork.stationPropertieMap.values());

            /*for (AvailabilityRecord avail : outNetwork.availabilityMap.values())
            {
                //We could check if it is in datastore already by querying by key
                //otherwise we will overwrite the already persisted object
                pm.makePersistent(avail);
            }
            for (StationProperties prop : outNetwork.stationPropertieMap.values())
            {
                pm.makePersistent(prop)
            }*/
            pm.makePersistent(initialNetwork);
            pm.makePersistent(networkTimeData);
        }finally {
            pm.close();
        }
    }

    private Key retrievePreviousAvailability(Network curStep, long stationId, PersistenceManager pm)
    {
        if (!curStep.isKeyMapNull() && curStep.keyMapContains((int)stationId))
            return curStep.getAvailabilityRecordKeyForStation((int) stationId);
        else
        {
            if (curStep.getPreviousNetworkKey() == null)
            {
                Logger.getLogger(ParseCronServlet.class.getName()).log(Level.SEVERE, "can't retrieve previous avalability for stationId : " + stationId);
            }
            else
            {
                return retrievePreviousAvailability(pm.getObjectById(Network.class, curStep.getPreviousNetworkKey()), stationId, pm);
            }
        }
        return null;
    }
}
