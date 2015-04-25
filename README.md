# BixHistorique

You can see a deployed version at the following address [https://bixhistorique-2015-demo.appspot.com/](https://bixhistorique-2015-demo.appspot.com/)

### Licenses
from getStats read API call metadata

  - original data, not available despite my best effort to find it
  - API use : https://creativecommons.org/licenses/by/4.0/
  - API source code : http://opensource.org/licenses/MIT


### Configuration
   - ####General
The code should be deployable either locally or remotely on any appengine configured system. To create, develop, maintain and use the code, I used a setup in [Android Studio].
It contains an 'app' module that is empty for now, and the juicy backend part is in the '[backend]' module.

You can directly import the roject in [Android Studio] and click run on the backend module to have it in a local environment, or deploy through the Build/Deploy to AppEngine menu option to have it online.
The code is mainly in [ParseCronServlet]. It mainly contains two methods that are called from the same CRON URL only with different parameters

     private void processAvailability(Network curNetwork)
     private void processProperties(Network curNetwork)
     
   - ####CRON tasks
If you deploy the version in the master branch, by default it will be in a non parsing state. To activate it, you have to call a CRON URL as follow

    http://[localhost or deployed domain .appspot.com]
    
    
    /cron/activateparsingcronjob?activate=true

Then, if you call either

    [..]/cron/parsecronjob?process=availability
or
    
    [..]/cron/parsecronjob?process=properties
    
The datasource will be parsed, processed if suitable and depending on the calling parameter, a new [Network] entity will be created in the datastore
or a collection of [StationProperties]

On the deployed environment, calling those URLs is handled through CRON jobs defined in [cron.xml]
(for example the deployed demo)

    <?xml version="1.0" encoding="UTF-8"?>
    <cronentries>
     <!-- By decommenting those entries and setting up schedule to your liking, you'll activate
     autoCron parsing in a DEPLOYED environment. In local (or even deployed), you can also directly request
     the cron URLs if you're logged as administrator.-->
     <cron>
        <url>/cron/activateparsingcronjob?activate=true</url>
        <description>After this job is called the parsing will be active</description>
        <schedule>14 of april 23:00</schedule>
        <timezone>America/Montreal</timezone>
    </cron>
    <cron>
        <url>/cron/activateparsingcronjob?activate=false</url>
        <description>After this job is called the parsing will NOT be active</description>
        <schedule>28 of april 23:00</schedule>
        <timezone>America/Montreal</timezone>
    </cron>
    <cron>
        <url>/cron/parsecronjob?process=availability</url>
        <description>If active, parse bike availability from data source every five minutes</description>
        <schedule>every 1 minutes</schedule> <!-- MUST BE SYNCED WITH ParseCronServlet.availabilityAllRefreshRateMinutes-->
    </cron>
    <cron>
        <url>/cron/parsecronjob?process=properties</url>
        <description>If active, parse station properties from data source every day at 3AM ETC</description>
        <schedule>every day 03:00</schedule>
        <timezone>America/Montreal</timezone>
    </cron>
    </cronentries>
    
In a local environment, you either have to manually call the URLs through your browser or setup your own cron requesting them for you.

Important : if you deploy, you have to do this 

    <!-- MUST BE SYNCED WITH ParseCronServlet.availabilityAllRefreshRateMinutes-->


### Data
The parsed original datasource is an XML file available at the following address : [https://montreal.bixi.com/data/bikeStations.xml](https://montreal.bixi.com/data/bikeStations.xml)

An excerpt follows

    <stations lastUpdate="1429587646730" version="2.0">
    <script/>
    <station>
    <id>1</id>
    <name>Notre Dame / Place Jacques Cartier</name>
    <terminalName>6001</terminalName>
    <lastCommWithServer>1429587044191</lastCommWithServer>
    <lat>45.508498</lat>
    <long>-73.553786</long>
    <installed>true</installed>
    <locked>false</locked>
    <installDate>1395935940000</installDate>
    <removalDate>1353352920000</removalDate>
    <temporary>false</temporary>
    <public>true</public>
    <nbBikes>2</nbBikes>
    <nbEmptyDocks>25</nbEmptyDocks>
    <latestUpdateTime>1429580092961</latestUpdateTime>
    </station>
    <station>
    ...
    </station>
    ...
    </stations>

The goal here is to retrieve the mostly stable data (station names, ids and location) less often than the volatile data (nb of bikes / docks) par station.

To that end, two dataclasses are defined
[StationProperties] and [Network]. They take care respectivaley of stable and volatile data.
Both are [JDO] anotated to be manipulated by the datastore.

Let's dig first into the infrequent data, typically this is recorded once every 24 hours. Presented here edited for clarity

    /**
    * Created by F8Full on 2015-03-21.
    * * This file is part of BixHistorique -- Backend
    * This is a data class used to describe mostly stable properties of a bike station
    * - Key : constructed from the string representation of the timestamp
    * - ID (from XML source)
    * - Name
    * - Terminal name
    * - Latitude and longitude as a GeoPt
    * - Installed
    * - InstallDate
    * - RemovalDate
    * - Locked
    * - Temporary
    * - Public
    */
    @PersistenceCapable //JDO annotation
    public class StationProperties {
    
    @PrimaryKey
    Key key;    //"id_timestamp"
    
    @Persistent //JDO annotation
    private
    int id; //Guaranted to be unique in datasource and stable in a season at least

    //Used internally for processing time based queries (SOON)
    @Persistent
    Date Date_TimestampUTC;

    @Persistent
    @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
    private
    String name;

    @Persistent
    private
    GeoPt pos;
    
     @Persistent
    private
    boolean temporary;

    @NotPersistent  //Is encoded in bikes/docks availability during parsing process
                    //Hence it's only transient when grabbing data from the source
    private
    boolean installed;

    @NotPersistent//Is encoded in bikes/docks availability during parsing process
                    //Hence it's only transient when grabbing data from the source
    private
    boolean locked;

Network retains mostly two maps by stationID of Bikes and Docks numbers
    code
    /**
    * Created by F8Full on 2015-03-21.
    * This file is part of BixHistorique -- Backend
    * This is a data class describing the complete state of the Bixi network for a given timestamp
    * - a Key : constructed with the string representation of the timestamp
    * - a Date object representing the timestamp and used internally when processing queries
    * - Two Maps, for properties and availability for a given stationID
    */
    @PersistenceCapable
    public class Network {

    //Constructed from the timestamp
    @PrimaryKey
    private Key Key_timestamp;

    //Used internally for queries
    @Persistent
    private Date Date_timestampUTC;

    @Persistent
    private boolean complete;   //True if this is a complete bike network state record

    @Persistent //REMOVED FROM INDEX to optimize index size, no later requests on those directly in datastore
    @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
    private Map<Integer, Integer> nbBikesByStationId;

    @Persistent //REMOVED FROM INDEX to optimize index size, no later requests on those directly in datastore
    @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
    private Map<Integer, Integer> nbEmptyDocksByStationId;

    @Persistent //REMOVED FROM INDEX to optimize index size, no later requests on those directly in datastore
    //@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
    private Key previousNetworkKey;

    //Filled at parsing time
    @NotPersistent  //Persisted separately in the 24h refresh rate job
    public Map<Integer, StationProperties> stationPropertieTransientMap = new Hashtable<>();  //Mapped by station ID
    

### Tech
### Installation

### Functioning


[StationProperties]:https://github.com/f8full/BixHistorique/blob/BixHistorique-2015-demok/backend/src/main/java/com/F8Full/bixhistorique/backend/datamodel/StationProperties.java
[Network]:https://github.com/f8full/BixHistorique/blob/BixHistorique-2015-demok/backend/src/main/java/com/F8Full/bixhistorique/backend/datamodel/Network.java
[JDO]:https://cloud.google.com/appengine/docs/java/datastore/jdo/overview-dn2
[Android Studio]:http://developer.android.com/tools/studio/index.html
[backend]:https://github.com/f8full/BixHistorique/tree/BixHistorique-2015-demok/backend/src/main/java/com/F8Full/bixhistorique/backend
[ParseCronServlet]:https://github.com/f8full/BixHistorique/blob/BixHistorique-2015-demok/backend/src/main/java/com/F8Full/bixhistorique/backend/ParseCronServlet.java
[cron.xml]:https://github.com/f8full/BixHistorique/blob/BixHistorique-2015-demok/backend/src/main/webapp/WEB-INF/cron.xml