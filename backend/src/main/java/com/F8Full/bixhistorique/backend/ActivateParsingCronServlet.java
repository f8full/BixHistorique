package com.F8Full.bixhistorique.backend;

import com.F8Full.bixhistorique.backend.datamodel.ParsingStatus;
import com.F8Full.bixhistorique.backend.datautils.PMF;
import com.F8Full.bixhistorique.backend.utils.Utils;
import com.google.appengine.api.datastore.Key;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by F8Full on 2015-04-14.
 * Servlet for parse status switching. Can be invoqued with activate parameter et true to activate parsing (and create ParsingStatus
 * entity automatically if required)
 * false activate parameter simply updates the ParsingStatus entity
 */
public class ActivateParsingCronServlet extends HttpServlet{

    public void doGet(HttpServletRequest req, HttpServletResponse resp) /*throws IOException*/ {

        PersistenceManager pm = PMF.get().getPersistenceManager();

        Key parsingStatusKey = Utils.RetrieveUniqueKey.parsingStatus();

        if (parsingStatusKey == null){
            if(req.getParameter("activate").equalsIgnoreCase("true")){

                ParsingStatus statusToPersist = new ParsingStatus();
                statusToPersist.setParsing_active(true);

                try {
                    pm.makePersistent(statusToPersist);
                }finally {
                    pm.close();
                }
            }
        }
        else{

            try {
                ParsingStatus parsingStatus  = pm.getObjectById(ParsingStatus.class, parsingStatusKey);

                if(req.getParameter("activate").equalsIgnoreCase("true"))
                    parsingStatus.setParsing_active(true);
                else
                    parsingStatus.setParsing_active(false);


            } finally {
                pm.close();
            }
        }
    }




    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doGet(req, resp);
    }
}
