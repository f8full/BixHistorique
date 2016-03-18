/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.F8Full.bixhistorique.backend;

import com.F8Full.bixhistorique.backend.responses.GetStatsResponse;
import com.F8Full.bixhistorique.backend.utils.Utils;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

/**
 * An endpoint class we are exposing
 */
@Api(name = "data", version = "v1", description = "This API provides historical Montréal bike network status data",
        namespace = @ApiNamespace(ownerDomain = "backend.bixhistorique.F8Full.com",
                ownerName = "backend.bixhistorique.F8Full.com", packagePath = ""))
public class BixHistoriqueEndpoint {

    /**
     * A simple endpoint method that takes a name and says Hi back
     */
    /*@ApiMethod(name = "sayHi")
    public MyBean sayHi(@Named("name") String name) throws IOException {
        MyBean response = new MyBean();
        response.setData("Hi, " + name);

        return response;
    }*/

    @SuppressWarnings("unchecked") //ArrayList<Long> rawMap = (ArrayList)result.getProperty("latestUpdateTimeMap");
    @ApiMethod(name = "getStats")
    public GetStatsResponse getStats()  {

        //Return a stat response

        GetStatsResponse response = new GetStatsResponse();

        Utils.ResultMeta.addLicense(response);

        return response;
    }

    /**
     * TEST Meta
     */
    /*@ApiMethod(name = "testMeta")
    public TestMetaResponse testMeta() {

        TestMetaResponse response = new TestMetaResponse();

        Utils.ResultMeta.addLicense(response);

        //For a rapid demo of the JSONObject interface
        response.meta.put("testMetaString", "testMeta");
        response.meta.put("testMetaBool", true);
        response.meta.put("testMetaInt", 666);

        return response;
    }*/
}
