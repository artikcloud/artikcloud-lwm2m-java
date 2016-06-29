package cloud.artik.lwm2m;

import java.util.Random;

import org.eclipse.leshan.LwM2mId;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.util.Hex;

/**
 * This is the main entry point for setting up and registering the Lwm2m client
 * with Artik Cloud Device Management services.
 * 
 * @author Maneesh Sahu
 */
public class ArtikCloudClient {
    protected LeshanClient client = null;
    protected String deviceId = null;
    protected String deviceToken = null;

    protected Device device = null;
    protected Location location = null;

    /**
     * Initialize the LWM2M Client with the DeviceId and the DeviceToken. Start
     * the registration process with start().
     * 
     * @param deviceId
     * @param deviceToken
     */
    public ArtikCloudClient(String deviceId, String deviceToken, Device device) {
        this.deviceId = deviceId;
        this.deviceToken = deviceToken;
        this.device = device;
    }

    /**
     * Starts the Registration Process
     */
    public void start() {
        if (client == null) {
            Random rand = new Random();
            int serverID = rand.nextInt(Integer.MAX_VALUE);

            // Initialize object list
            ObjectsInitializer initializer = new ObjectsInitializer();

            initializer.setInstancesForObject(
                    LwM2mId.SECURITY, 
                    Security.psk(
                            "coaps://coap.artik.cloud:5686", 
                            serverID,
                            deviceId.getBytes(),
                            Hex.decodeHex(deviceToken.toCharArray())));
            initializer.setInstancesForObject(
                    LwM2mId.SERVER, 
                    new Server(
                            serverID, 
                            LwM2mId.SRV_LIFETIME, 
                            device.getSupportedBinding().toBindingMode(), 
                            false));

            if (device == null) {
                throw new NullPointerException("Device is null");
            }
            initializer.setInstancesForObject(LwM2mId.DEVICE, this.device);

            /*
             * LOCATION is not supported right now. if (location != null) {
             * initializer.setInstancesForObject(LwM2mId.LOCATION,
             * this.location); }
             */

            // Create client
            LeshanClientBuilder builder = new LeshanClientBuilder(deviceId);
            builder.setLocalAddress(null, 0);
            builder.setLocalSecureAddress(null, 0);
            builder.setObjects(
                    initializer.create(
                            LwM2mId.SECURITY,
                            LwM2mId.SERVER, 
                            LwM2mId.DEVICE));
            client = builder.build();
        }

        // Start the client
        client.start();
    }

    /*
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }*/

    public void close() {
        if (client != null)
            client.destroy(true);
    }

    public void stop(boolean desregister) {
        if (client != null)
            client.stop(desregister);
    }

    public String getRegistrationId() {
        if (client != null)
            return client.getRegistrationId();
        return null;
    }
}
