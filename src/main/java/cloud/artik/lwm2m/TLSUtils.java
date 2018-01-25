package cloud.artik.lwm2m;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.*;
import java.util.*;

public class TLSUtils {

    static {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new BouncyCastleJsseProvider());
    }

    /* The list of cipher suites required by CoAP protocol for TLS */
    private static final String[] COAP_CIPHER_SUITES = {
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8"
    };

    /**
     * Gets the list of CoAP cipher suites that are supported..
     *
     * @param supportedCipherSuites the array of supported cipher suites
     * @return the array of supported CoAP cipher suites
     */
    public static String[] getSupportedCoapCipherSuites(String[] supportedCipherSuites) {
        Set<String> supportedCipherSuitesSet = new HashSet<>(Arrays.asList(supportedCipherSuites));
        List<String> supportedCoapCipherSuites = new ArrayList<>();

        for (String cipherSuite : COAP_CIPHER_SUITES) {
            if (supportedCipherSuitesSet.contains(cipherSuite)) {
                supportedCoapCipherSuites.add(cipherSuite);
            }
        }

        return supportedCoapCipherSuites.toArray(new String[] {});
    }

    /**
     * Creates and init a SSLContext supporting CoAP cipher suites.
     *
     * @param keyManagers
     * @param trustManagers
     * @return the new SSLContext
     */
    public static SSLContext createSSLContext(KeyManager[] keyManagers, TrustManager[] trustManagers) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS", BouncyCastleJsseProvider.PROVIDER_NAME);

            sslContext.init(
                    keyManagers,
                    trustManagers,
                    SecureRandom.getInstance("DEFAULT", BouncyCastleProvider.PROVIDER_NAME));

            return sslContext;
        } catch (KeyManagementException | NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new RuntimeException("Unable to initialize SSL context", ex);
        }
    }

    /**
     * Creates and init a KeyManagerFactory using the given KeyStore and password.
     *
     * @param keyStore
     * @param password
     * @return the new KeyManagerFactory
     */
    public static KeyManagerFactory createKeyManagerFactory(KeyStore keyStore, char[] password) {
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("PKIX", BouncyCastleJsseProvider.PROVIDER_NAME);
            keyManagerFactory.init(keyStore, password);

            return keyManagerFactory;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException | UnrecoverableKeyException ex) {
            throw new RuntimeException("Unable to create Key Manager Factory", ex);
        }
    }
}
