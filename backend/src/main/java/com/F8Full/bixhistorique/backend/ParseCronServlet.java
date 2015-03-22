package com.F8Full.bixhistorique.backend;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by F8Full on 2015-03-22.
 * This file is part of BixHistorique -- Backend
 * This servlet is invoked to parse and process the XML source
 */
public class ParseCronServlet extends HttpServlet{

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    {

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doGet(req, resp);
    }
}
