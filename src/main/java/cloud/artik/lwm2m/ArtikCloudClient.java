package cloud.artik.lwm2m;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Random;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.leshan.LwM2mId;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.californium.LeshanTCPClient;
import org.eclipse.leshan.client.californium.LeshanTCPClientBuilder;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.util.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.artik.lwm2m.enums.SupportedBinding;

/**
 * This LWM2M Objects provides the data related to a LWM2M Server and also provides the keying material of a LWM2M Client 
 * appropriate to access a specified LWM2M Server. One Object Instance SHOULD address a LWM2M Bootstrap Server.
 * This is the main entry point for setting up and registering the Lwm2m client
 * with the Artik Cloud Device Management services.
 * 
 *
 * @link http://technical.openmobilealliance.org/tech/profiles/LWM2M_Security-v1_0.xml
 * @link http://technical.openmobilealliance.org/tech/profiles/LWM2M_Server-v1_0.xml
 * @author Maneesh Sahu
 */
public class ArtikCloudClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtikCloudClient.class);

    private final int serverUDPPort = 5686;
    private final int serverTCPPort = 5689;
    
    protected LeshanClient client = null;
    protected LeshanTCPClient clientTCP = null;
    protected String deviceId = null;
    protected String deviceToken = null;
    protected int shortServerID = -1;
    protected long lifetime = LwM2mId.SRV_LIFETIME;
    protected boolean notifyWhenDisable = false;
    protected String serverName = "coap.artik.cloud";

    protected Device device = null;
    protected Location location = null;
    protected FirmwareUpdate updater = null;

    /**
     * Initialize the LWM2M Client with the DeviceId and the DeviceToken. 
     * This sets the shortServerID to a random Integer, the lifetime to LwM2mId.SRV_LIFETIME
     * and notifyWhenDisable to true.
     * 
     * Start the registration process with start().
     *
     * @param deviceId
     * @param deviceToken
     * @param device
     */
    public ArtikCloudClient(String deviceId, String deviceToken, Device device) {
        this(deviceId, deviceToken, device, new Random().nextInt(Integer.MAX_VALUE),  LwM2mId.SRV_LIFETIME, true);
    }
    
    /**
     * Initialize the LWM2M Client with the DeviceId, DeviceToken, shortServerId, lifetime and notifyWhenDisable.
     *  
     * Start the registration process with start().
     *
     * @param deviceId
     * @param deviceToken
     * @param device
     * @param shortServerID - Used as link to associate server Object Instance (1-65535)
     * @param lifetime - Specify the lifetime of the registration in seconds.
     * @param notifyWhenDisable - If true, the LWM2M Client stores “Notify” operations to the LWM2M Server while the LWM2M Server account is disabled or the LWM2M Client is offline. 
     *                            After the LWM2M Server account is enabled or the LWM2M Client is online, the LWM2M Client reports the stored “Notify” operations to the Server.
     *                            If false, the LWM2M Client discards all the “Notify” operationsor temporally disables the Observe function while the LWM2M Server is disabled or the LWM2M Client is offline.
     *                            The default value is true. The maximum number of storing Notification per the Server is up to the implementation.
     */
    public ArtikCloudClient(String deviceId, String deviceToken, Device device, int shortServerID, long lifetime, boolean notifyWhenDisable) {
        this.deviceId = deviceId;
        this.deviceToken = deviceToken;
        this.device = device;
        this.shortServerID = shortServerID;
        this.lifetime = lifetime;
        this.notifyWhenDisable = notifyWhenDisable;
    }

    /**
     * Starts the Registration Process
     */
    public void start() {
        if (client == null) {
            if (device == null) {
                throw new NullPointerException("Device is null");
            }

            String coapURL = "coaps://" + serverName + ":" +
                    ((device.getSupportedBinding() != SupportedBinding.TCP) ? serverUDPPort : serverTCPPort);

            // Initialize object list
            ObjectsInitializer initializer = new ObjectsInitializer();

            // Create common object instances
            initializer.setInstancesForObject(LwM2mId.DEVICE, this.device);
            
            initializer.setInstancesForObject(
                    LwM2mId.SERVER,
                    new Server(
                            this.shortServerID,
                            this.lifetime,
                            device.getSupportedBinding().toBindingMode(),
                            this.notifyWhenDisable));

            initializer.setInstancesForObject(
                    LwM2mId.SECURITY,
                    Security.psk(
                            coapURL,
                            this.shortServerID,
                            deviceId.getBytes(),
                            Hex.decodeHex(deviceToken.toCharArray())));
            
            /*
             * LOCATION is not supported right now. if (location != null) {
             * initializer.setInstancesForObject(LwM2mId.LOCATION,
             * this.location); }
             */
            
            List<LwM2mObjectEnabler> objectEnablers = null;
            
            if (this.updater != null) {
                initializer.setInstancesForObject(LwM2mId.FIRMWARE, this.updater);
                
                objectEnablers = initializer.create(
                        LwM2mId.SECURITY,
                        LwM2mId.SERVER,
                        LwM2mId.DEVICE,
                        LwM2mId.FIRMWARE);                
            } else {
                objectEnablers = initializer.create(
                        LwM2mId.SECURITY,
                        LwM2mId.SERVER,
                        LwM2mId.DEVICE);
            }

            if (device.getSupportedBinding() != SupportedBinding.TCP) {
                // Create udp client
                LeshanClientBuilder builder = new LeshanClientBuilder(deviceId);
                builder.setObjects(objectEnablers);
                client = builder.build();

                // Start the udp client
                client.start();
            } else {
                // Create the tcp client
                LeshanTCPClientBuilder builder = new LeshanTCPClientBuilder(deviceId, serverName, serverTCPPort);
                builder.setObjects(objectEnablers);
                builder.secure().setSSLContext(getTLSContext()).configure().configure();
                clientTCP = builder.build();

                // Start the tcp client
                clientTCP.start();
            }
        }
    }

    /*
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }*/
    
    public FirmwareUpdate getFirmwareUpdate() {
        return updater;
    }
    
    public void setFirmwareUpdate(FirmwareUpdate updater) {
        this.updater = updater;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public void close() {
        if (client != null)
            client.destroy(true);

        if (clientTCP != null)
            clientTCP.destroy(true);
    }

    public void stop(boolean desregister) {
        if (client != null)
            client.stop(desregister);

        if (clientTCP != null)
            clientTCP.stop();
    }

    public String getRegistrationId() {
        if (client != null) {
            return client.getRegistrationId();
        } else if (clientTCP != null) {
            return clientTCP.getRegistrationId();
        }
        return null;
    }

    private SSLContext getTLSContext() {
        SSLContext sslContext = null;

        // Install the all-trusting trust manager
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (Exception e) {
            //
        }

        return sslContext;
    }
}
