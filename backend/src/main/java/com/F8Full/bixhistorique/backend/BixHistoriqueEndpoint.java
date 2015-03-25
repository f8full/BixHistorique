/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.F8Full.bixhistorique.backend;

import com.F8Full.bixhistorique.backend.datamodel.MyBean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import java.io.IOException;

import javax.inject.Named;

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

    @SuppressWarnings("unchecked") //ArrayList<Long> rawMap = (ArrayList)result.getProperty("latestUpdateTimeMap");
    @ApiMethod(name = "testCode")
    public void testCode(@Named("fileindex")String fileIndex) throws IOException {


    }
}
