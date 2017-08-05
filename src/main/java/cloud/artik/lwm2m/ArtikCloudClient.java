package cloud.artik.lwm2m;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.eclipse.leshan.LwM2mId;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
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
 * @author Maneesh Sahu
 * @link http://technical.openmobilealliance.org/tech/profiles/LWM2M_Security-v1_0.xml
 * @link http://technical.openmobilealliance.org/tech/profiles/LWM2M_Server-v1_0.xml
 */
public class ArtikCloudClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtikCloudClient.class);

    public final static int DEFAULT_LIFETIME = 300;

    private final int serverUDPPort = 5686;
    private final int serverTCPPort = 5688;
    private final int serverTLSPort = 5689;

    protected LeshanClient client = null;
    protected String deviceId = null;
    protected String deviceToken = null;
    protected int shortServerID = -1;
    protected long lifetime = LwM2mId.SRV_LIFETIME;
    protected boolean notifyWhenDisable = false;
    protected String serverName = "coaps-api.artik.cloud";
    protected SSLContext sslContext = null;
    protected KeyConfig keyConfig = null;

    protected Device device = null;
    protected Location location = null;
    protected FirmwareUpdate updater = null;

    /**
     * Initialize the LWM2M Client with the DeviceId and the DeviceToken.
     * This sets the shortServerID to a random Integer, the lifetime to DEFAULT_LIFETIME (300 seconds)
     * and notifyWhenDisable to true.
     * <p>
     * Start the registration process with start().
     *
     * @param deviceId
     * @param deviceToken
     * @param device
     */
    public ArtikCloudClient(String deviceId, String deviceToken, Device device) {
        this(deviceId, deviceToken, device, null, new Random().nextInt(Integer.MAX_VALUE), DEFAULT_LIFETIME,
                true);
    }
    
    /**
     * Initialize the LWM2M Client with the DeviceId and the DeviceToken.
     * This sets the shortServerID to a random Integer, the lifetime to DEFAULT_LIFETIME (300 seconds)
     * and notifyWhenDisable to true.
     * <p>
     * Start the registration process with start().
     *
     * @param deviceId
     * @param deviceToken
     * @param device
     * @param keyConfig Set the key config to use device certificates
     */
    public ArtikCloudClient(String deviceId, String deviceToken, Device device, KeyConfig keyConfig) {
        this(deviceId, deviceToken, device, keyConfig, new Random().nextInt(Integer.MAX_VALUE),
                DEFAULT_LIFETIME, true);
    }

    /**
     * Initialize the LWM2M Client with the DeviceId, DeviceToken, shortServerId, lifetime and
     * notifyWhenDisable.
     * <p>
     * Start the registration process with start().
     *
     * @param deviceId
     * @param deviceToken
     * @param device
     * @param keyConfig         - Used to enable device certificates
     * @param shortServerID     - Used as link to associate server Object Instance (1-65535)
     * @param lifetime          - Specify the lifetime of the registration in seconds.
     * @param notifyWhenDisable - If true, the LWM2M Client stores “Notify” operations to the LWM2M Server
     *                            while the LWM2M Server account is disabled or the LWM2M Client is offline.
     */
    public ArtikCloudClient(String deviceId, String deviceToken, Device device, KeyConfig keyConfig,
            int shortServerID, long lifetime, boolean notifyWhenDisable) {
        this.deviceId = deviceId;
        this.deviceToken = deviceToken;
        this.device = device;
        this.keyConfig = keyConfig;
        this.shortServerID = shortServerID;
        this.lifetime = lifetime;
        this.notifyWhenDisable = notifyWhenDisable;
        this.sslContext = getTLSContext();
    }

    /**
     * Starts the Registration Process
     */
    public void start() {
        if (client == null) {
            if (device == null) {
                throw new NullPointerException("Device is null");
            }

            String coapURL;
            if (device.getSupportedBinding() == SupportedBinding.TCP) {
                if (sslContext == null) {
                    coapURL = "coap+tcp://" + serverName + ":" + serverTCPPort;
                } else {
                    coapURL = "coaps+tcp://" + serverName + ":" + serverTLSPort;
                }
            } else {
                coapURL = "coaps://" + serverName + ":" + serverUDPPort;
            }

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

            if (device.getSupportedBinding() == SupportedBinding.TCP) {
                if (sslContext == null) {
                    initializer.setInstancesForObject(
                            LwM2mId.SECURITY,
                            Security.tcp(
                                    coapURL,
                                    this.shortServerID
                            ));
                } else {
                    initializer.setInstancesForObject(
                            LwM2mId.SECURITY,
                            Security.tls(
                                    coapURL,
                                    this.shortServerID
                            ));
                }
            } else {
                // PSK
                if (keyConfig == null) {
                    initializer.setInstancesForObject(
                            LwM2mId.SECURITY,
                            Security.psk(
                                    coapURL,
                                    this.shortServerID,
                                    deviceId.getBytes(),
                                    Hex.decodeHex(deviceToken.toCharArray())));
                // Certificate
                } else {
                    try {
                        X509Certificate clientCert = keyConfig.getClientCertificate();
                        PrivateKey privateKey = keyConfig.getPrivateKey();
                        X509Certificate serverCert = keyConfig.getServerCertificate();
                        Security security = Security.certificate(coapURL, this.shortServerID,
                                clientCert.getEncoded(), privateKey.getEncoded(), serverCert.getEncoded());
                        initializer.setInstancesForObject(
                                LwM2mId.SECURITY,
                                security);
                    } catch (CertificateEncodingException e) {
                        throw new IllegalArgumentException("Certificate encoding error", e);
                    }
                }
            }

            List<LwM2mObjectEnabler> objectEnablers;

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

            LeshanClientBuilder builder = new LeshanClientBuilder(deviceId);
            builder.setObjects(objectEnablers);

            Map<String, String> additionalAttributes = new HashMap<String, String>();
            additionalAttributes.put("token", this.deviceToken);
            builder.setAdditionalAttributes(additionalAttributes);

            if (device.getSupportedBinding() == SupportedBinding.TCP && sslContext != null) {
                builder.setSSLContext(sslContext);
            }

            client = builder.build();

            // Start the client
            client.start();
        } else {
            client.start();
        }
    }

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
        if (client != null) {
            try {
                client.destroy(true);
            } catch (NullPointerException npe) {
                if (device.getSupportedBinding() != SupportedBinding.TCP) {
                    throw npe;
                }
            }
        }
    }

    public void stop(boolean deregister) {
        if (client != null)
            client.stop(deregister);
    }

    public String getRegistrationId() {
        if (client != null) {
            return client.getRegistrationId();
        }
        return null;
    }

    private SSLContext getTLSContext() {
        SSLContext sslContext = null;
        
        if (keyConfig == null) {
            // Install the all-trusting trust manager
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                
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
                LOGGER.error("Exception initializing SSL context", e);
            }
        } else {
            // Configure the client certificate and trusted server root certificate
            try {
                KeyStore keystore = KeyStore.getInstance("JKS");
                keystore.load(null);
                X509Certificate clientCert = keyConfig.getClientCertificate();
                keystore.setCertificateEntry("client", clientCert);
                keystore.setKeyEntry("key", keyConfig.getPrivateKey(), "changeit".toCharArray(), new Certificate[]{clientCert});
    
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keystore, "changeit".toCharArray());
                
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(null); // You don't need the KeyStore instance to come from a file.
                X509Certificate rootCert = keyConfig.getServerCertificate();
                ks.setCertificateEntry("server-root", rootCert);
                tmf.init(ks);
              
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new java.security.SecureRandom());
            } catch (Exception e) {
                LOGGER.error("Exception initializing SSL context", e);
            }
        }

        return sslContext;
    }

    public void setNoSec() {
        sslContext = null;
    }
}
