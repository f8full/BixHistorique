package com.F8Full.bixhistorique.backend.utils;

import com.F8Full.bixhistorique.backend.responses.BaseResponse;

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
            _targetResponse.meta.put("original_data_license", "NONE");
            _targetResponse.meta.put("original_data_source", "https://montreal.bixi.com/data/bikeStations.xml");
            _targetResponse.meta.put("API_license", "https://creativecommons.org/licenses/by/4.0/");
            _targetResponse.meta.put("API_creator_handle", "@F8Full");
        }
    }
}
