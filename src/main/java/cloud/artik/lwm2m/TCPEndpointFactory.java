package cloud.artik.lwm2m;

import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.observe.ObservationStore;
import org.eclipse.californium.elements.tcp.TcpClientConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.leshan.core.californium.EndpointFactory;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;

class TCPEndpointFactory implements EndpointFactory {

    private final int CONNTECT_TIMEOUT_MILLIS = 100000;
    private final int IDLE_TIMEOUT_SECONDS = 3600;

    private final SSLContext sslContext;

    public TCPEndpointFactory(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    public CoapEndpoint createUnsecuredEndpoint(InetSocketAddress address, NetworkConfig coapConfig, ObservationStore store) {
        TcpClientConnector connector = new TcpClientConnector(1, CONNTECT_TIMEOUT_MILLIS, IDLE_TIMEOUT_SECONDS);
        return new CoapEndpoint(connector, coapConfig, store, null);
    }

    @Override
    public CoapEndpoint createSecuredEndpoint(DtlsConnectorConfig dtlsConfig, NetworkConfig coapConfig, ObservationStore store) {
        CustomTlsClientConnector connector = new CustomTlsClientConnector(sslContext,1, CONNTECT_TIMEOUT_MILLIS, IDLE_TIMEOUT_SECONDS);
        return new CoapEndpoint(connector, coapConfig, store, null);
    }
}
