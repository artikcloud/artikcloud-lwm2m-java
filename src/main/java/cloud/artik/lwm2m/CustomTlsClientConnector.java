package cloud.artik.lwm2m;

import cloud.artik.lwm2m.exception.NoCoapCipherSuiteSupportedException;
import io.netty.channel.Channel;
import io.netty.handler.ssl.SslHandler;
import org.eclipse.californium.elements.tcp.TlsClientConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class CustomTlsClientConnector extends TlsClientConnector {

    private final static Logger LOGGER = LoggerFactory.getLogger(CustomTlsClientConnector.class);

    private final SSLContext sslContext;

    public CustomTlsClientConnector(SSLContext sslContext, int numberOfThreads, int connectTimeoutMillis, int idleTimeout) {
        super(sslContext, numberOfThreads, connectTimeoutMillis, idleTimeout);
        this.sslContext = sslContext;
    }

    @Override
    protected void onNewChannelCreated(SocketAddress remote, Channel ch) {
        SSLEngine sslEngine = createSllEngine(remote);
        sslEngine.setUseClientMode(true);

        String[] cipherSuites = TLSUtils.getSupportedCoapCipherSuites(sslEngine.getSupportedCipherSuites());
        if (cipherSuites.length == 0) {
            throw new NoCoapCipherSuiteSupportedException();
        } else {
            sslEngine.setEnabledCipherSuites(cipherSuites);

            ch.pipeline().addFirst(new SslHandler(sslEngine));
        }
    }

    /**
     * Create SSL engine for remote socket address.
     *
     * @param remoteAddress for SSL engine
     * @return created SSL engine
     */
    private SSLEngine createSllEngine(SocketAddress remoteAddress) {
        if (remoteAddress instanceof InetSocketAddress) {
            InetSocketAddress remote = (InetSocketAddress) remoteAddress;
            LOGGER.info("Connection to inet {0}", remote);
            return sslContext.createSSLEngine(remote.getHostString(), remote.getPort());
        } else {
            LOGGER.info("Connection to {0}", remoteAddress);
            return sslContext.createSSLEngine();
        }
    }
}
