package cloud.artik.lwm2m;

import cloud.artik.lwm2m.enums.SupportedBinding;
import org.eclipse.leshan.LwM2mId;
import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.client.LwM2mTCPClient;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.californium.LeshanTCPClientBuilder;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.client.util.LinkFormatHelper;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.request.DeregisterRequest;
import org.eclipse.leshan.core.request.RegisterRequest;
import org.eclipse.leshan.core.response.RegisterResponse;
import org.eclipse.leshan.util.Hex;

import java.net.InetSocketAddress;
import java.util.Random;

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
            if (device == null) {
                throw new NullPointerException("Device is null");
            }

            Random rand = new Random();
            int serverID = rand.nextInt(Integer.MAX_VALUE);

            // Initialize object list
            ObjectsInitializer initializer = new ObjectsInitializer();

            initializer.setInstancesForObject(LwM2mId.DEVICE, this.device);

            if (device.getSupportedBinding() != SupportedBinding.TCP) {
                initializer.setInstancesForObject(
                        LwM2mId.SECURITY,
                        Security.psk(
                                "coaps://coap-dev.artik.cloud:5686",
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

                // Start the client
                client.start();
            } else {
                final LwM2mTCPClient client;
                final InetSocketAddress serverAddress = new InetSocketAddress("coap-dev.artik.cloud", 5688);
                final LeshanTCPClientBuilder builder = new LeshanTCPClientBuilder(deviceId);

                client = builder.setObjectsInitializer(initializer).setServerAddress(serverAddress).build();

                client.start();

                RegisterResponse response = client.send(new RegisterRequest(deviceId, (long) 3000, null, BindingMode.C, null,
                        LinkFormatHelper.getClientDescription(client.getObjectEnablers(), null), null));
                if (response == null) {
                    System.out.println("Registration request timeout");
                    return;
                }

                System.out.println("Device Registration (Success? " + response.getCode() + ")");
                if (response.getCode() != ResponseCode.CREATED) {
                    System.err.println("\tDevice Unable to connect.  Registration Error: " + response.getCode());
                }

                final String registrationID = response.getRegistrationID();
                System.out.println("\tDevice: Registered Client Location '" + registrationID + "'");

                // De-register on shutdown and stop client.
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        if (registrationID != null) {
                            System.out.println("\tDevice: De-registering Client '" + registrationID + "'");
                            client.send(new DeregisterRequest(registrationID), 1000);
                            client.stop();
                        }
                    }
                });
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
