/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.F8Full.bixhistorique.backend;

import com.F8Full.bixhistorique.backend.datamodel.LastNetworkTimeData;
import com.F8Full.bixhistorique.backend.datamodel.MyBean;
import com.F8Full.bixhistorique.backend.datamodel.Network;
import com.F8Full.bixhistorique.backend.datamodel.StationProperties;
import com.F8Full.bixhistorique.backend.datautils.PMF;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.datastore.KeyFactory;

import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Named;
import javax.jdo.PersistenceManager;

/**
 * An endpoint class we are exposing
 */
@Api(name = "bixHistorique2015", version = "v1", description = "This API provides historical Montréal bike network status data for 2015 season",
        namespace = @ApiNamespace(ownerDomain = "backend.bixhistorique.F8Full.com",
                ownerName = "backend.bixhistorique.F8Full.com", packagePath = ""))
public class BixHistoriqueEndpoint {

    /**
     * A simple endpoint method that takes a name and says Hi back
     */
    @ApiMethod(name = "sayHi")
    public MyBean sayHi(@Named("name") String name) throws IOException {
        MyBean response = new MyBean();
        response.setData("Hi, " + name);

        return response;
    }

    @ApiMethod(name = "initialParse")
    public JSONObject initialParse(@Named("url")String url) throws IOException {
        //String represents future JSON response object;
        JSONObject response = new JSONObject();

        SourceURL_XMLParser parser = new SourceURL_XMLParser(url);

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


        return response;
    }
}
