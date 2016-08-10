package cloud.artik.lwm2m;

import cloud.artik.lwm2m.enums.SupportedBinding;
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Random;

/**
 * This is the main entry point for setting up and registering the Lwm2m client
 * with Artik Cloud Device Management services.
 *
 * @author Maneesh Sahu
 */
public class ArtikCloudClient {
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

    /**
     * Initialize the LWM2M Client with the DeviceId and the DeviceToken. 
     * This sets the shortServerID to a random Integer, the lifetime to LwM2mId.SRV_LIFETIME
     * and notifyWhenDisable to false.
     * 
     * Start the registration process with start().
     *
     * @param deviceId
     * @param deviceToken
     * @param device
     */
    public ArtikCloudClient(String deviceId, String deviceToken, Device device) {
        this(deviceId, deviceToken, device, new Random().nextInt(Integer.MAX_VALUE),  LwM2mId.SRV_LIFETIME, false);
    }
    
    /**
     * Initialize the LWM2M Client with the DeviceId, DeviceToken, shortServerId, lifetime and notifyWhenDisable.
     *  
     * Start the registration process with start().
     *
     * @param deviceId
     * @param deviceToken
     * @param device
     * @param shortServerID - Short Server ID
     * @param lifetime - Registration Lifetime
     * @param notifyWhenDisable - Notify When Disable 
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

            List<LwM2mObjectEnabler> objectEnablers = initializer.create(
                    LwM2mId.SECURITY,
                    LwM2mId.SERVER,
                    LwM2mId.DEVICE);


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
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

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
