package cloud.artik.lwm2m;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Class to hold certificates and private keys.
 * 
 * LwM2M specification only allows a single client and server certificate. Hence, for the
 * server certificate, the root certificate must be used.
 * 
 * @author aschen77
 */
public class KeyConfig {
    
    private X509Certificate clientCertificate;
    
    private PrivateKey privateKey;
    
    private X509Certificate serverCertificate;
    
    /**
     * Constructor.
     * 
     * @param clientCertificate The client certificate registered with ARTIK Cloud for the device.
     * @param privateKey The client private key.
     * @param serverCertificate The root certificate of the server.
     */
    public KeyConfig(X509Certificate clientCertificate, PrivateKey privateKey,
            X509Certificate serverCertificate) {
        this.clientCertificate = clientCertificate;
        this.privateKey = privateKey;
        this.serverCertificate = serverCertificate;
    }
    
    public X509Certificate getClientCertificate() {
        return clientCertificate;
    }
    
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    public X509Certificate getServerCertificate() {
        return serverCertificate;
    }
    
}