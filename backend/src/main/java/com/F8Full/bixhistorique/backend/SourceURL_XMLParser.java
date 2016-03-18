package com.F8Full.bixhistorique.backend;

import com.F8Full.bixhistorique.backend.datamodel.AvailabilityPair;
import com.F8Full.bixhistorique.backend.datamodel.Network;
import com.F8Full.bixhistorique.backend.datamodel.StationProperties;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.KeyFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    int mTempNbBikes;
    float mTempLat;

    public SourceURL_XMLParser(String _url) throws IOException {
        mNetworkToReturn = new Network(null, null);

        URL url = new URL(_url);

        mIs = new InputSource(new InputStreamReader(url.openStream(), "UTF-8"));
        mIs.setEncoding("UTF-8");
    }

    //This returns a complete Network object representing the XML data source
    //Note : the latestUpdateTime of each station is stored in the corresponding StationProperties
    //This is to pass along the data to the processing code
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

        } catch (ParserConfigurationException | SAXException | IOException | IllegalArgumentException e) {
            Logger.getLogger(SourceURL_XMLParser.class.getName()).log(Level.SEVERE, "Low level error in parser. Are your XML tags case sensitive ?" + e.toString());
        }

        return mNetworkToReturn;
    }

    public Network parseFile(String filePath)
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

            parser.parse(new File(filePath),this);

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
            //TODO: validate XML feed version here and throw exception with sensible message
            //extract timestamp of data source and builds key from it
            mNetworkToReturn.setTimestamp( KeyFactory.createKey( Network.class.getSimpleName(),
                                                            _attributes.getValue("LastUpdate") ) );
        }
        else if (_elementName.equalsIgnoreCase("station"))
        {
            mTempStationProperties = new StationProperties();
            mTempStationProperties.setDate_TimestampUTC(new Date(mNetworkToReturn.getTimestamp()));
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
        //dropped
        /*else if (_element.equalsIgnoreCase("terminalName"))
        {
            mTempStationProperties.setTerminalName(mBufferedString.toString());
        }
        else if (_element.equalsIgnoreCase("lastCommWithServer"))
        {
            //SAX guarantees setID will have been called before executing this
            //mNetworkToReturn.putLastCommWithServer(mTempStationProperties.getId(), Long.parseLong(mBufferedString.toString()));
        }*/
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
        /*else if (_element.equalsIgnoreCase("installDate"))
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
        }*/

        else if (_element.equalsIgnoreCase("nbBikes"))
        {
            mTempNbBikes = Integer.parseInt(mBufferedString.toString());
        }
        else if (_element.equalsIgnoreCase("nbEmptyDocks"))
        {
            mNetworkToReturn.putAvailabilityforStationId(mTempStationProperties.getId(),new AvailabilityPair<>(mTempNbBikes, Integer.parseInt(mBufferedString.toString())));
        }
        else if (_element.equalsIgnoreCase("lastUpdateTime"))
        {
            //Sax guarantees parsing occurs in order, because this function MUST be called after setID()

            //This is to pass along the data to the processing code
            //When a StationProperties entity is persisted, the stored timestamp corresponds to
            //the timestamp of the oldest Network entity referring it

            //empty lastUpdateTime in feed must be trimmed out
            if (!mBufferedString.toString().isEmpty())
            {
                mTempStationProperties.setKey(KeyFactory.createKey( StationProperties.class.getSimpleName(),
                        mTempStationProperties.getId() + "_" + mBufferedString.toString() ) );
            }
            else
            {
                mTempStationProperties.setKey(KeyFactory.createKey( StationProperties.class.getSimpleName(),
                        mTempStationProperties.getId() + "_0" ) );
            }

        }
        else if (_element.equalsIgnoreCase("station"))
        {
            mNetworkToReturn.putStationProperties(mTempStationProperties.getId(), mTempStationProperties);
        }
    }

    @Override
    public void characters(char[] _ac, int i, int j) throws SAXException {

        mBufferedString.append(new String(_ac, i, j));

    }
}
