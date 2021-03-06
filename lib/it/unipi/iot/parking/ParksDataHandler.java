package it.unipi.iot.parking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import it.unipi.iot.parking.data.ParkStatus;
import it.unipi.iot.parking.data.PaymentData;
import it.unipi.iot.parking.data.SpotStatus;
import it.unipi.iot.parking.data.UserData;
import it.unipi.iot.parking.om2m.ErrorCode;
import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.om2m.OM2MConstants;
import it.unipi.iot.parking.om2m.OM2MException;
import it.unipi.iot.parking.om2m.OM2MSession;
import it.unipi.iot.parking.om2m.data.ApplicationEntity;
import it.unipi.iot.parking.om2m.data.Container;
import it.unipi.iot.parking.om2m.data.ContentInstance;
import it.unipi.iot.parking.om2m.data.OM2MResource;
import it.unipi.iot.parking.om2m.data.Subscription;
import it.unipi.iot.parking.util.DateConverter;

// NOTICE: these methods should be synchronized properly!
public class ParksDataHandler {
    private static final Logger LOGGER = Logger.getLogger(ParksDataHandler.class.getName());
    
    static {
        LOGGER.setLevel(Level.OFF);
    }
    
    private static final String      CONTAINER_BASE_NAME = "park-";
    private static final OM2MSession SESSION_DATA;
    private static final OM2M        OM2M_NODE;
    
    static {
        SESSION_DATA = new OM2MSession(AppConfig.HOST_ADDRESS, AppConfig.PORT_NUMBER,
                AppConfig.CREDENTIALS);
        
        OM2M_NODE = new OM2M(SESSION_DATA);
    }
    
    public static ApplicationEntity initMN() throws OM2MException, TimeoutException {
        final String parentID;
        final String[] labels;
        final ApplicationEntity paymentsAE;
        
        parentID = getResourceIDFromPath(AppConfig.CSE_ID);
        
        labels = new LabelsFactory().setParentID(parentID)
                                    .setResourceName("payments")
                                    .setType("payments-cnt")
                                    .getLabels();
        
        paymentsAE = OM2M_NODE.createApplicationEntity(parentID, "payments-api", "payments",
                labels);
        
        return paymentsAE;
    }
    
    public static ApplicationEntity createPark(ParkConfig conf)
            throws OM2MException, TimeoutException {
        final String parentID;
        final String[] labels;
        final ApplicationEntity park;
        
        parentID = getResourceIDFromPath(AppConfig.CSE_ID);
        
        labels = new LabelsFactory().setParentID(parentID)
                                    .setResourceName(conf.name)
                                    .setType("park")
                                    .getLabels();
        
        park = OM2M_NODE.createApplicationEntity(parentID, conf.appID, conf.name, labels);
        
        // Now create park entry in payments application entity
        final String[] filters = new LabelsFactory().setResourceName("payments")
                                                    .getFilters();
        
        final String[] uril = OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
        
        if (uril.length > 1 || uril.length < 1)
            throw new OM2MException("Bad discovery request generated!", ErrorCode.OTHER);
        
        String paymentsID = getResourceIDFromPath(uril[0]);
        
        String[] plabels = new LabelsFactory().setParentID(paymentsID)
                                              .setResourceName(conf.name)
                                              .setParkID(park.getResourceID())
                                              .setType("payments-park")
                                              .getLabels();
        
        OM2M_NODE.createContainer(paymentsID, conf.name, plabels);
        
        for (int index = 0; index < conf.spots.length; ++index) {
            try {
                createSpot(park.getResourceID(), conf.spots[index], index);
            } catch (OM2MException e) {
                if (e.getCode() != ErrorCode.NAME_ALREADY_PRESENT)
                    throw e;
            }
        }
        
        return park;
    }
    
    public static Container createSpot(final String parentID, final String data, final int index)
            throws OM2MException, TimeoutException {
        
        String[] labels;
        
        final String containerName;
        final Container container;
        final LabelsFactory factory;
        
        containerName = assignSpotName(index);
        
        factory = new LabelsFactory().setParentID(parentID)
                                     .setResourceName(containerName)
                                     .setSpotName(containerName)
                                     .setParkID(parentID);
        
        if (index == 0)
            factory.setType("manifest");
        else
            factory.setType("spot");
        
        labels = factory.getLabels();
        
        container = OM2M_NODE.createContainer(parentID, containerName, labels);
        
        factory.reset()
               .setParentID(container.getResourceID())
               .setParkID(parentID)
               .setSpotName(containerName);
        
        if (index == 0)
            factory.setType("manifest-data");
        else {
            factory.setType("instance")
                   .setFirst(true);
        }
        
        labels = factory.getLabels();
        
        OM2M_NODE.createContentInstance(container.getResourceID(), data, labels);
        
        return container;
        
    }
    
    public static void deleteAllSubscriptions(String startingNodePath)
            throws OM2MException, TimeoutException {
        final String[] filters;
        
        filters = new LabelsFactory().setType("subscription")
                                     .getFilters();
        
        String[] subscriptions = OM2M_NODE.discovery(startingNodePath, filters);
        
        for (String s : subscriptions) {
            OM2M_NODE.delete(s);
        }
    }
    
    public static String[] getAllParksList() throws OM2MException, TimeoutException {
        final String[] filters;
        final String[] uril;
        final List<String> parkList;
        final String[] parkIDs;
        
        filters = new LabelsFactory().setType("park")
                                     .getFilters();
        
        uril = OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
        parkList = new ArrayList<>();
        
        for (String uri : uril) {
            parkList.add(getResourceIDFromPath(uri));
        }
        
        parkIDs = parkList.toArray(new String[parkList.size()]);
        
        return parkIDs;
    }
    
    public static ParkStatus getEmptyParkStatus(String parkID)
            throws OM2MException, TimeoutException {
        final String[] filters;
        final String[] uril;
        final ParkStatus parkStatus;
        
        filters = new LabelsFactory().setType("manifest")
                                     .setParkID(parkID)
                                     .getFilters();
        
        uril = OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
        
        if (uril.length > 1 || uril.length < 1)
            throw new OM2MException("Bad discovery request generated!", ErrorCode.OTHER);
        
        ContentInstance status = (ContentInstance) OM2M_NODE.get(uril[0] + "/la");
        
        parkStatus = new ParkStatus(parkID, status.getContentValue());
        
        return parkStatus;
    }
    
    public static ParkStatus getParkStatus(String parkID) throws OM2MException, TimeoutException {
        final String[] filters;
        final String[] uril;
        final ParkStatus parkStatus;
        
        parkStatus = getEmptyParkStatus(parkID);
        parkStatus.setAvailable(true); // TODO: implement it
        
        // Get list of spots and add each one to a JSONArray
        filters = new LabelsFactory().setType("spot")
                                     .setParkID(parkID)
                                     .getFilters();
        
        uril = OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
        
        for (String uri : uril) {
            String spotID = getResourceIDFromPath(uri);
            ContentInstance value = (ContentInstance) OM2M_NODE.get(uri + "/la");
            SpotStatus spotStatus = new SpotStatus(parkID, spotID, value.getContentValue());
            parkStatus.addOrReplaceSpot(spotStatus);
        }
        
        return parkStatus;
    }
    
    private static boolean checkType(OM2MResource resource, String type) {
        final String expectedType = LabelsFactory.getTypeFilter(type);
        final String[] labels = resource.getLabels();
        
        return (Arrays.asList(labels)
                      .contains(expectedType));
    }
    
    public static boolean isParkStatusUpdate(OM2MResource resource) {
        return checkType(resource, "manifest-data");
    }
    
    public static boolean isSpotStatusUpdate(OM2MResource resource) {
        return checkType(resource, "instance") && !LabelsFactory.isFirst(resource.getLabels());
    }
    
    public static ParkStatus getParkDataFromURI(String parkPath)
            throws OM2MException, TimeoutException {
        final String parkID;
        
        parkID = getResourceIDFromPath(parkPath);
        
        return getParkStatus(parkID);
    }
    
    public static String[] getAllRemoteNodes() throws OM2MException, TimeoutException {
        final String[] filters;
        
        filters = new String[] { "rty=" + OM2MConstants.RESOURCE_TYPE_REMOTE_CSE };
        
        return OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
    }
    
    public static boolean freeSpot(String parkID, int index)
            throws OM2MException, TimeoutException {
        // setSpot shall return the previous user and the previous price, unfortunately
        SpotStatus oldStatus = setSpot(parkID, index, true, null);
        
        if (oldStatus == null)
            return false;
        
        // Create the payment
        String username = oldStatus.getUser();
        Date startTime = oldStatus.getStartTime();
        Date endTime = new Date();
        
        long diffInMillies = endTime.getTime() - startTime.getTime();
        long minutesElapsed = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
        
        // Price is hourly, but you pay for real minutes
        double cost = oldStatus.getPrice() * ((double) minutesElapsed) / 60;
        
        cost = Math.floor(cost * 100) / 100;
        
        PaymentData payment = new PaymentData(cost, startTime, endTime);
        
        // Get payments container for the given parkID
        String[] filters = new LabelsFactory().setParkID(parkID)
                                              .setType("payments-park")
                                              .getFilters();
        
        String[] uril = OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
        
        if (uril.length < 1 || uril.length > 1)
            throw new OM2MException("Bad discovery request generated!", ErrorCode.OTHER);
        
        String parentID = getResourceIDFromPath(uril[0]);
        
        String[] labels = new LabelsFactory().setParentID(parentID)
                                             .setType("payment-data")
                                             .setUsername(username)
                                             .setDate(startTime)
                                             .getLabels();
        
        OM2M_NODE.createContentInstance(parentID, payment.toString(), labels);
        
        return true;
    }
    
    private static final Object OCCUPY_SPOT_MONITOR = new Object();
    
    public static boolean payForSpot(String parkID, int index, String user, String credit)
            throws OM2MException, TimeoutException {
        // Check that payment information are correct
        String[] filters;
        String[] uril;
        ContentInstance userContent;
        UserData userData;
        
        filters = new LabelsFactory().setUsername(user)
                                     .setType("user")
                                     .getFilters();
        
        uril = OM2M_NODE.discovery(AppConfig.IN_ID, filters);
        
        if (uril.length < 1 || uril.length > 1)
            return false; // User not found!
            
        userContent = (ContentInstance) OM2M_NODE.get(uril[0] + "/la");
        userData = new UserData(userContent.getContentValue());
        
        // Wrong payment info
        if (!credit.equals(userData.getCredit()))
            return false;
        
        // OK payment info are correct, let us try to occupy a spot
        if (occupySpot(parkID, index, user) == null)
            return false; // Was not able to occupy the spot
            
        return true;
    }
    
    public static SpotStatus occupySpot(String parkID, int index, String user)
            throws OM2MException, TimeoutException {
        return setSpot(parkID, index, false, user);
    }
    
    /**
     * 
     * @param parkID
     * @param index
     * @param free
     * @param user
     * @return
     * @throws TimeoutException
     */
    private static SpotStatus setSpot(String parkID, int index, boolean free, String user)
            throws OM2MException, TimeoutException {
        final String containerName;
        final ContentInstance value;
        
        containerName = assignSpotName(index);
        
        // NOTICE: I used parentID to avoid matching copies of the spot in the IN node
        String[] filters = new LabelsFactory().setParentID(parkID)
                                              .setResourceName(containerName)
                                              .getFilters();
        
        String[] uri = OM2M_NODE.discovery(AppConfig.CSE_ID, filters); // parkID
        
        if (uri.length > 1)
            throw new OM2MException(
                    "Too many spots associated with the same parkID and resourceName!",
                    ErrorCode.OTHER);
        
        String spotID = getResourceIDFromPath(uri[0]);
        
        synchronized (OCCUPY_SPOT_MONITOR) {
            value = (ContentInstance) OM2M_NODE.get(uri[0] + "/la");
            
            SpotStatus spot = new SpotStatus(parkID, spotID, value.getContentValue());
            
            if (spot.isFree() == free)
                return null;
            
            if (free) {
                spot.free();
            } else {
                // Gotta get the current price at the time of the occupation
                filters = new LabelsFactory().setParentID(parkID)
                                             .setType("manifest")
                                             .getFilters();
                
                uri = OM2M_NODE.discovery(parkID, filters);
                
                if (uri.length < 1 || uri.length > 1)
                    throw new OM2MException("Bad discovery request generated", ErrorCode.OTHER);
                
                OM2MResource res = OM2M_NODE.get(uri[0] + "/la");
                
                final double price = getParkStatus(res).getPrice();
                
                spot.occupy(user, price);
            }
            
            final String[] labels = new LabelsFactory().setParentID(spotID)
                                                       .setParkID(parkID)
                                                       .setSpotName(containerName)
                                                       .setType("instance")
                                                       .getLabels();
            
            OM2M_NODE.createContentInstance(spotID, spot.toString(), labels);
            
            return new SpotStatus(parkID, spotID, value.getContentValue());
        }
    }
    
    public static String[] getDirectChildrenList(final String resourcePath)
            throws OM2MException, TimeoutException {
        
        final String parentID = getResourceIDFromPath(resourcePath);
        
        String[] uriQueries = new LabelsFactory().setParentID(parentID)
                                                 .getFilters();
        
        return OM2M_NODE.discovery(resourcePath, uriQueries);
    }
    
    public static Subscription subscribe(final String remoteID, final String subscriberURI)
            throws OM2MException, TimeoutException {
        final String[] labels;
        final String resourceID = getResourceIDFromPath(remoteID);
        
        labels = new LabelsFactory().setParentID(resourceID)
                                    .setType("subscription")
                                    .getLabels();
        
        return OM2M_NODE.createSubscription(remoteID, subscriberURI, labels);
    }
    
    private static String findLocalParentID(final String remoteParentID, final String copyBasePath)
            throws OM2MException, TimeoutException {
        final String[] uril;
        
        // Find the local copy with remoteID = remoteParentID
        String[] filters = new LabelsFactory().setRemoteID(remoteParentID)
                                              .getFilters();
        
        uril = OM2M_NODE.discovery(copyBasePath, filters);
        
        if (uril.length > 1)
            throw new OM2MException("Bad discovery request generated!", ErrorCode.OTHER);
        
        if (uril.length < 1) {
            // Oops, it was a RemoteCSE then, let us just use the copyBasePath then
            return copyBasePath;
        }
        
        return getResourceIDFromPath(uril[0]);
        
        /*
         * filters = new LabelsFactory().setParentID(AppConfig.CSE_ID) .getFilters();
         * 
         * uril = OM2M_NODE.discovery(copyBasePath, new String[] { "pi=" +
         * AppConfig.CSE_ID });
         * 
         * if (uril.length < 1) throw new
         * OM2MException("Bad discovery request generated!", ErrorCode.OTHER);
         * 
         * for (String uri : uril) { OM2MResource res = OM2M_NODE.get(uri);
         * 
         * if (res instanceof RemoteNode) { if (((RemoteNode) res).getCSEID()
         * .equals(remoteID)) { uril = new String[] { uri }; break; } } }
         * 
         * if (uril.length > 1) throw new
         * OM2MException("Bad discovery request generated!", ErrorCode.OTHER); }
         */
    }
    
    private static ApplicationEntity createAECopy(final ApplicationEntity original,
            final String localParentID) throws OM2MException, TimeoutException {
        final String remoteAppID;
        final String remoteName;
        final String remoteID;
        final String parkID;
        final String type;
        final String[] labels;
        
        remoteAppID = original.getApplicationID();
        remoteName = original.getResourceName();
        remoteID = original.getResourceID();
        parkID = original.getResourceID();
        type = LabelsFactory.getType(original.getLabels());
        
        labels = new LabelsFactory().setParentID(localParentID)
                                    .setParkID(parkID)
                                    .setRemoteID(remoteID)
                                    .setResourceName(remoteName)
                                    .setType(type)
                                    .getLabels();
        
        return OM2M_NODE.createApplicationEntity(localParentID, remoteAppID, remoteName, labels);
    }
    
    private static Container createCNTCopy(final Container original, final String localParentID)
            throws OM2MException, TimeoutException {
        final String remoteName;
        final String remoteID;
        final String parkID;
        final String spotName;
        final String type;
        final String[] remoteLabels;
        final String[] labels;
        
        remoteName = original.getResourceName();
        remoteID = original.getResourceID();
        spotName = remoteName;
        remoteLabels = original.getLabels();
        parkID = localParentID;
        type = LabelsFactory.getType(remoteLabels);
        
        labels = new LabelsFactory().setParentID(localParentID)
                                    .setParkID(parkID)
                                    .setRemoteID(remoteID)
                                    .setResourceName(remoteName)
                                    .setSpotName(spotName)
                                    .setType(type)
                                    .getLabels();
        
        return OM2M_NODE.createContainer(localParentID, remoteName, labels);
    }
    
    private static ContentInstance createCICopy(final ContentInstance original,
            final String localParentID) throws OM2MException, TimeoutException {
        final String remoteName;
        final String remoteID;
        final String parkID;
        final String spotName;
        final String value;
        final String type;
        final String[] remoteLabels;
        final String[] labels;
        
        remoteName = original.getResourceName();
        remoteID = original.getResourceID();
        value = original.getContentValue()
                        .toString();
        
        remoteLabels = original.getLabels();
        type = LabelsFactory.getType(remoteLabels);
        
        parkID = LabelsFactory.getParkID(get(localParentID).getLabels());
        
        LabelsFactory factory = new LabelsFactory().setParentID(localParentID)
                                                   .setParkID(parkID)
                                                   .setRemoteID(remoteID)
                                                   .setResourceName(remoteName);
        
        if (type.equals("payment-data")) {
            final String username = LabelsFactory.getUsername(remoteLabels);
            // TODO: get date (and set it when creating the original payment)
            final Date date = LabelsFactory.getDate(remoteLabels);
            labels = factory.setUsername(username)
                            .setType(type)
                            .setDate(date)
                            .getLabels();
        } else {
            boolean firstInstance = LabelsFactory.isFirst(remoteLabels);
            spotName = LabelsFactory.getSpotName(remoteLabels);
            labels = factory.setSpotName(spotName)
                            .setFirst(firstInstance)
                            .setType(type)
                            .getLabels();
        }
        
        return OM2M_NODE.createContentInstance(localParentID, remoteName, value, labels);
    }
    
    // I don't need to check if it already exists, because an error will be thrown
    // for the duplicate resource name, however, outside it much be known that the
    // exception will be thrown
    public static OM2MResource createCopy(final OM2MResource original, final String copyBasePath)
            throws OM2MException, TimeoutException {
        final String localParentID;
        OM2MResource copy;
        
        localParentID = findLocalParentID(original.getParentID(), copyBasePath);
        
        switch (original.getResourceType()) {
        case OM2MConstants.RESOURCE_TYPE_APPLICATION_ENTITY:
            copy = createAECopy((ApplicationEntity) original, localParentID);
            break;
        case OM2MConstants.RESOURCE_TYPE_CONTAINER:
            copy = createCNTCopy((Container) original, localParentID);
            break;
        case OM2MConstants.RESOURCE_TYPE_CONTENT_INSTANCE:
            copy = createCICopy((ContentInstance) original, localParentID);
            break;
        default:
            throw new IllegalArgumentException("Unsupported resource to be copied!");
        }
        
        return copy;
    }
    
    public static OM2MResource get(String resourcePath) throws OM2MException, TimeoutException {
        return OM2M_NODE.get(resourcePath);
    }
    
    private static String getResourceIDFromPath(String resourcePath)
            throws OM2MException, TimeoutException {
        return OM2M_NODE.get(resourcePath)
                        .getResourceID();
    }
    
    private static String assignSpotName(final int index) {
        return CONTAINER_BASE_NAME
                + ((index > 99) ? "" + index : (index > 9) ? "0" + index : "00" + index);
    }
    
    private static class LabelsFactory {
        private static final String SEP = ":";
        
        private String  parentID;
        private String  resourceName;
        private String  type;
        private String  parkID;
        private String  spotName;
        private String  remoteID;
        private String  username;
        private Date    date;
        private boolean first;
        
        public static String getParentFilter(String parentID) {
            return "pi" + SEP + parentID;
        }
        
        public static String getNameFilter(String resourceName) {
            return "rn" + SEP + resourceName;
        }
        
        public static String getTypeFilter(String type) {
            return "ty" + SEP + type;
        }
        
        public static String getParkFilter(String parkID) {
            return "pkid" + SEP + parkID;
        }
        
        public static String getSpotFilter(String spotName) {
            return "sn" + SEP + spotName;
        }
        
        public static String getRemoteFilter(String remoteID) {
            return "rmtid" + SEP + remoteID;
        }
        
        public static String getUsernameFilter(String username) {
            return "user" + SEP + username;
        }
        
        public static String getFirstFilter(boolean first) {
            return "first" + SEP + first;
        }
        
        public static String getDateFilter(Date date) {
            return "date" + SEP + DateConverter.fromDate(date)
                                               .substring(0, 9)
                    + "000000";
        }
        
        private static String getValue(String[] labels, String prefix) {
            for (String s : labels) {
                if (s.startsWith(prefix)) {
                    return s.substring(prefix.length());
                }
            }
            
            throw new RuntimeException(
                    "The requested label couldn't be found! Prefix was `" + prefix + "`");
        }
        
        public static String getParkID(String[] labels) {
            final String prefix = "pkid" + SEP;
            return getValue(labels, prefix);
        }
        
        public static String getSpotName(String[] labels) {
            final String prefix = "sn" + SEP;
            return getValue(labels, prefix);
        }
        
        public static String getType(String[] labels) {
            final String prefix = "ty" + SEP;
            return getValue(labels, prefix);
        }
        
        public static String getUsername(String[] labels) {
            final String prefix = "user" + SEP;
            return getValue(labels, prefix);
        }
        
        public static boolean isFirst(String[] labels) {
            final String prefix = "first" + SEP;
            try {
                return Boolean.parseBoolean(getValue(labels, prefix));
            } catch (RuntimeException e) {
                return false;
            }
        }
        
        public static String getRemote(String[] labels) {
            final String prefix = "rmtid" + SEP;
            return getValue(labels, prefix);
        }
        
        public static Date getDate(String[] labels) {
            final String prefix = "date" + SEP;
            String dateString = getValue(labels, prefix);
            
            return DateConverter.fromString(dateString);
        }
        
        public LabelsFactory() {
            reset();
        }
        
        public LabelsFactory reset() {
            parentID = null;
            resourceName = null;
            type = null;
            parkID = null;
            spotName = null;
            remoteID = null;
            date = null;
            first = false;
            
            return this;
        }
        
        public LabelsFactory setParentID(String parentID) {
            this.parentID = parentID;
            return this;
        }
        
        public LabelsFactory setResourceName(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }
        
        public LabelsFactory setType(String type) {
            this.type = type;
            return this;
        }
        
        public LabelsFactory setParkID(String parkID) {
            this.parkID = parkID;
            return this;
        }
        
        public LabelsFactory setSpotName(String spotName) {
            this.spotName = spotName;
            return this;
        }
        
        public LabelsFactory setRemoteID(String remoteID) {
            this.remoteID = remoteID;
            return this;
        }
        
        public LabelsFactory setUsername(String username) {
            this.username = username;
            return this;
        }
        
        public LabelsFactory setFirst(boolean first) {
            this.first = first;
            return this;
        }
        
        public LabelsFactory setDate(Date startTime) {
            this.date = startTime;
            return this;
        }
        
        public String[] getLabels() {
            ArrayList<String> labels = new ArrayList<>();
            
            if (parentID != null)
                labels.add(getParentFilter(parentID));
            
            if (resourceName != null)
                labels.add(getNameFilter(resourceName));
            
            if (type != null) {
                labels.add(getTypeFilter(type));
            }
            
            if (parkID != null) {
                labels.add(getParkFilter(parkID));
            }
            
            if (spotName != null) {
                labels.add(getSpotFilter(spotName));
            }
            
            if (remoteID != null) {
                labels.add(getRemoteFilter(remoteID));
            }
            
            if (username != null) {
                labels.add(getUsernameFilter(username));
            }
            
            if (date != null) {
                labels.add(getDateFilter(date));
            }
            
            if (first) {
                labels.add(getFirstFilter(first));
            }
            
            return labels.toArray(new String[labels.size()]);
        }
        
        public String[] getFilters() {
            final String[] labels = getLabels();
            
            for (int i = 0; i < labels.length; ++i) {
                labels[i] = "lbl=" + labels[i];
            }
            
            return labels;
        }
        
    }
    
    public static ParkStatus getParkStatus(OM2MResource resource) {
        final ContentInstance actualResource;
        final JSONObject data;
        final ParkStatus park;
        final String parkID;
        
        if (!isParkStatusUpdate(resource))
            throw new IllegalArgumentException("The given value is not a park status update!");
        
        actualResource = (ContentInstance) resource;
        data = actualResource.getContentValue();
        parkID = LabelsFactory.getParkID(actualResource.getLabels());
        park = new ParkStatus(parkID, data);
        
        return park;
    }
    
    public static SpotStatus getSpotStatus(OM2MResource resource) {
        final ContentInstance actualResource;
        final JSONObject data;
        final SpotStatus park;
        final String parkID, spotID;
        
        if (!isSpotStatusUpdate(resource))
            throw new IllegalArgumentException("The given value is not a spot status update!");
        
        actualResource = (ContentInstance) resource;
        data = actualResource.getContentValue();
        parkID = LabelsFactory.getParkID(actualResource.getLabels());
        spotID = actualResource.getParentID();
        park = new SpotStatus(parkID, spotID, data);
        
        return park;
    }
    
    public static ApplicationEntity initUsers() throws OM2MException, TimeoutException {
        final String parentID;
        final String[] labels;
        final ApplicationEntity users;
        
        parentID = getResourceIDFromPath(AppConfig.CSE_ID);
        
        labels = new LabelsFactory().setParentID(parentID)
                                    .setResourceName("users")
                                    .setType("users")
                                    .getLabels();
        
        users = OM2M_NODE.createApplicationEntity(parentID, "users-api", "users", labels);
        
        return users;
    }
    
    public static UserData register(String username, String password, String email, String credit)
            throws OM2MException, TimeoutException {
        
        String[] labels;
        
        final Container container;
        final String userID;
        final String parentID;
        
        final String[] filters = new LabelsFactory().setParentID("/" + AppConfig.CSE_ID)
                                                    .setType("users")
                                                    .getFilters();
        
        final String[] uril = OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
        
        if (uril.length < 1 || uril.length > 1) {
            throw new OM2MException("Bad discovery request generated!", ErrorCode.OTHER);
        }
        
        labels = new LabelsFactory().setUsername(username)
                                    .setType("user")
                                    .getLabels();
        
        parentID = getResourceIDFromPath(uril[0]);
        
        try {
            container = OM2M_NODE.createContainer(parentID, username, labels);
        } catch (OM2MException e) {
            if (e.getCode() == ErrorCode.NAME_ALREADY_PRESENT) {
                return null; // Tried to register two times the same username
            } else {
                throw e;
            }
        }
        
        userID = container.getResourceID();
        
        labels = new LabelsFactory().setParentID(userID)
                                    .setType("user-data")
                                    .getLabels();
        
        UserData data = new UserData(userID, username, email, credit);
        
        JSONObject value = data.toJSONObject()
                               .put("password", password);
        
        OM2M_NODE.createContentInstance(container.getResourceID(), value.toString(), labels);
        
        return data;
        
    }
    
    public static UserData login(String username, String password)
            throws OM2MException, TimeoutException {
        final String[] filters;
        final String[] uril;
        final ContentInstance content;
        final JSONObject value;
        
        filters = new LabelsFactory().setType("user")
                                     .setUsername(username)
                                     .getFilters();
        
        uril = OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
        
        if (uril.length < 1)
            return null;
        
        if (uril.length > 1)
            throw new OM2MException("Bad discovery request generated!", ErrorCode.OTHER);
        
        content = (ContentInstance) OM2M_NODE.get(uril[0] + "/la");
        
        value = content.getContentValue();
        
        if (value.getString("password")
                 .equals(password)) {
            return new UserData(value);
        }
        
        return null; // Login failed
    }
    
    public static PaymentData[] getPayments(String username, Date date)
            throws OM2MException, TimeoutException {
        // Discovery to get all urils with username and date and type payment-data
        String[] filters = new LabelsFactory().setUsername(username)
                                              .setDate(date)
                                              .setType("payment-data")
                                              .getFilters();
        
        String[] uril = OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
        
        PaymentData[] payments = new PaymentData[uril.length];
        
        for (int i = 0; i < uril.length; ++i) {
            // Get payment data
            ContentInstance content = (ContentInstance) OM2M_NODE.get(uril[i]);
            
            payments[i] = new PaymentData(content.getContentValue());
        }
        
        return payments;
    }
    
    public static void updatePrice(String parkID, int sign) throws OM2MException, TimeoutException {
        // TODO: test this
        
        // Get the remote park object
        final String[] filters;
        final String[] uril;
        
        OM2MResource localPark = OM2M_NODE.get(parkID);
        
        final String remoteParkID = LabelsFactory.getRemote(localPark.getLabels());
        
        // Now that I have the remote park id, I want to modify its manifest, first I
        // get its manifest data
        
        filters = new LabelsFactory().setType("manifest")
                                     .setParkID(remoteParkID)
                                     .getFilters();
        
        uril = OM2M_NODE.discovery(remoteParkID, filters);
        
        if (uril.length > 1 || uril.length < 1)
            throw new OM2MException("Bad discovery request generated!", ErrorCode.OTHER);
        
        // Now I have the manifest URI, let's get its data
        ContentInstance status = (ContentInstance) OM2M_NODE.get(uril[0] + "/la");
        JSONObject value = status.getContentValue();
        
        double price = value.getDouble("price");
        // int spotsN = value.getInt("spotsN");
        
        price += sign * 0.05; // Optionally: use the number of spots
        
        price = Math.round(price * 100) / 100.;
        
        value.put("price", price);
        
        // Now I gotta write back the value to a new ContentInstance
        
        OM2M_NODE.createContentInstance(uril[0], value.toString(), status.getLabels());
        
        // Done
    }
    
    public static String getParkID(String name) throws OM2MException, TimeoutException {
        final String[] filters = new LabelsFactory().setType("park")
                                                    .setResourceName(name)
                                                    .getFilters();
        final String[] uril = OM2M_NODE.discovery(AppConfig.CSE_ID, filters);
        
        if (uril.length != 1)
            throw new OM2MException("Bad discovery request generated!", ErrorCode.OTHER);
        
        return getResourceIDFromPath(uril[0]);
        
    }
    
}

// TODO: a way to get park id from name
