package com.F8Full.bixhistorique.backend;

import com.F8Full.bixhistorique.backend.datamodel.AvailabilityRecord;
import com.F8Full.bixhistorique.backend.datamodel.Network;
import com.F8Full.bixhistorique.backend.datamodel.StationProperties;
import com.google.appengine.api.datastore.GeoPt;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by F8Full on 2015-03-21.
 * This file is part of BixHistorique -- Backend
 * It parses a Bixi XML feed v2 and returns a Network object
 * <stations lastUpdate="1426975650204" version="2.0">
 <script/>
 <station>
 <id>1</id>
 <name>20th & Bell St</name>
 <terminalName>31000</terminalName>
 <lastCommWithServer>1426975381729</lastCommWithServer>
 <lat>38.8561</lat>
 <long>-77.0512</long>
 <installed>true</installed>
 <locked>false</locked>
 <installDate>0</installDate>
 <removalDate/>
 <temporary>false</temporary>
 <public>true</public>
 <nbBikes>0</nbBikes>
 <nbEmptyDocks>11</nbEmptyDocks>
 <latestUpdateTime>1426975381729</latestUpdateTime>
 </station>
 ...
 </stations>
 */
public class SourceURL_XMLParser extends DefaultHandler{

    //To work with Sax way of handling things
    StringBuffer mBufferedString;

    InputSource mIs;

    //The result of the parsing
    Network mNetworkToReturn;

    //Used to accumulate data before putting it into the Network map
    StationProperties mTempStationProperties;
    AvailabilityRecord mTempAvailabilityRecord;
    float mTempLat;

    public SourceURL_XMLParser(String _url) throws IOException {
        mNetworkToReturn = new Network();

        URL url = new URL(_url);

        mIs = new InputSource(new InputStreamReader(url.openStream(), "UTF-8"));
        mIs.setEncoding("UTF-8");
    }

    public Network parse()
    {
        //URL url = new URL("http://www.example.com/atom.xml");
        //new InputStreamReader(url.openStream())

        //FileInputStream stream = new FileInputStream("WEB-INF/xml/wordsDictionary.xml");
        //InputSource is = new InputSource(new InputStreamReader(stream, "UTF-8"));
        //is.setEncoding("UTF-8");
        //reader.parse(is);

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            //factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();

            parser.parse(mIs, this);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return mNetworkToReturn;
    }

    @Override
    public void startElement(String _s, String _s1, String _elementName, Attributes _attributes) throws SAXException
    {
        mBufferedString = new StringBuffer();

        if (_elementName.equalsIgnoreCase("stations"))
        {
            //TODO: validate XML version here and throw exception
            //extract timestamp of data source
            mNetworkToReturn.setTimestamp(_attributes.getValue("lastUpdate"));
        }
        else if (_elementName.equalsIgnoreCase("station"))
        {
            mTempStationProperties = new StationProperties();
            mTempAvailabilityRecord = new AvailabilityRecord();
        }
    }

    @Override
    public void endElement(String _s, String _s1, String _element) throws SAXException
    {
        if (_element.equalsIgnoreCase("id"))
        {
            mTempStationProperties.setID(Integer.parseInt(mBufferedString.toString()));
        }
        else if (_element.equalsIgnoreCase("name"))
        {
            mTempStationProperties.setName(mBufferedString.toString());
        }
        else if (_element.equalsIgnoreCase("terminalName"))
        {
            mTempStationProperties.setTerminalName(mBufferedString.toString());
        }
        else if (_element.equalsIgnoreCase("lat"))
        {
            mTempLat = Float.parseFloat(mBufferedString.toString());
        }
        else if (_element.equalsIgnoreCase("long"))
        {
            mTempStationProperties.setPos(new GeoPt(mTempLat, Float.parseFloat(mBufferedString.toString())));
        }
        else if (_element.equalsIgnoreCase("installed"))
        {
            mTempStationProperties.setInstalled(Boolean.parseBoolean(mBufferedString.toString()));
        }
        else if (_element.equalsIgnoreCase("locked"))
        {
            mTempStationProperties.setLocked(Boolean.parseBoolean(mBufferedString.toString()));
        }
        else if (_element.equalsIgnoreCase("installDate"))
        {
            if (!mBufferedString.toString().isEmpty())
            {
                mTempStationProperties.setInstallDate(new Date(Long.parseLong(mBufferedString.toString())));
            }
        }
        else if (_element.equalsIgnoreCase("removalDate"))
        {
            if (!mBufferedString.toString().isEmpty())
            {
                mTempStationProperties.setRemovalDate(new Date(Long.parseLong(mBufferedString.toString())));
            }
        }
        else if (_element.equalsIgnoreCase("temporary"))
        {
            mTempStationProperties.setTemporary(Boolean.parseBoolean(mBufferedString.toString()));
        }
        else if (_element.equalsIgnoreCase("public"))
        {
            mTempStationProperties.setPublic(Boolean.parseBoolean(mBufferedString.toString()));
        }
        else if (_element.equalsIgnoreCase("nbBikes"))
        {
            mTempAvailabilityRecord.setNbBikes(Integer.parseInt(mBufferedString.toString()));
        }
        else if (_element.equalsIgnoreCase("nbEmptyDocks"))
        {
            mTempAvailabilityRecord.setNbEmptyDocks(Integer.parseInt(mBufferedString.toString()));

            mTempAvailabilityRecord.setKey(Integer.toString(mTempAvailabilityRecord.getNbBikes())
                    + "|" + mBufferedString.toString());
        }
        else if (_element.equalsIgnoreCase("station"))
        {
            mNetworkToReturn.putStationProperties(mTempStationProperties.getID(), mTempStationProperties);
            mNetworkToReturn.putAvailabilityRecord(mTempStationProperties.getID(), mTempAvailabilityRecord);
        }
    }

    @Override
    public void characters(char[] _ac, int i, int j) throws SAXException {

        mBufferedString.append(new String(_ac, i, j));

    }
}
