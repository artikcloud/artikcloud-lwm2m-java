package cloud.artik.lwm2m;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCertificateClient extends BaseClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCertificateClient.class);
    
    /**
     * Return the classpath location of the client certificate in PEM format.
     */
    protected abstract String clientCertLocation();
    
    /**
     * Return the classpath location of the client private key in PEM format.
     */
    protected abstract String privateKeyLocation();
    
    /**
     * Return the classpath location of the server root certificate in PEM format.
     */
    protected abstract String serverCertLocation();
    
    
    protected X509Certificate readClientCert() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(clientCertLocation());
        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(is);
        } catch (CertificateException e) {
            LOGGER.error("failed to load client certificate", e);
            return null;
        }
    }
    
    protected PrivateKey readPrivateKey() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(privateKeyLocation());
        PEMParser pemParser = new PEMParser(new InputStreamReader(is));;
        try {
            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            return converter.getPrivateKey(privateKeyInfo);
        } finally {
            pemParser.close();
        }
    }
    
    protected X509Certificate readServerCert() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(serverCertLocation());
        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(is);
        } catch (CertificateException e) {
            LOGGER.error("failed to load server ca", e);
            return null;
        }
    }
    
}