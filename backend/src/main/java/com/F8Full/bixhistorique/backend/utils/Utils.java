package com.F8Full.bixhistorique.backend.utils;

import com.F8Full.bixhistorique.backend.ParseCronServlet;
import com.F8Full.bixhistorique.backend.datamodel.ParsingStatus;
import com.F8Full.bixhistorique.backend.responses.BaseResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/**
 * Created by F8Full on 2015-03-15.
 * Class with static utilities
 */
public class Utils {
    /**
     * Created by F8Full on 2015-03-15.
     * Used to manipulate request result metadata and avoid repetitive code
     */
    public static class ResultMeta {

        public static void addLicense(BaseResponse _targetResponse)
        {
            _targetResponse.meta.put("original_data_license", ParseCronServlet.DATA_SOURCE_LICENSE);
            _targetResponse.meta.put("original_data_source", ParseCronServlet.DATA_SOURCE_URL);
            _targetResponse.meta.put("API_license_use", "https://creativecommons.org/licenses/by/4.0/");
            _targetResponse.meta.put("API_creator_handle", "@F8Full");
            _targetResponse.meta.put("API_source_code", "https://github.com/f8full/BixHistorique");
        }
    }

    public static class RetrieveUniqueKey {
        public static Key parsingStatus(){

            Key toReturn = null;

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

            //Only one object of ParsingStatus.class.getSimpleName() kind
            Query parsingStatusQuery = new Query(ParsingStatus.class.getSimpleName())
                    .setKeysOnly();
            PreparedQuery pq = datastore.prepare(parsingStatusQuery);

            Entity resultEntity = pq.asSingleEntity();

            if(resultEntity!= null)
                toReturn = resultEntity.getKey();

            return toReturn;

        }
    }
}
