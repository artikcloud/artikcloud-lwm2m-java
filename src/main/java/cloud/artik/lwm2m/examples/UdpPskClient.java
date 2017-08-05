package cloud.artik.lwm2m.examples;

import org.eclipse.leshan.core.response.ExecuteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.artik.lwm2m.ArtikCloudClient;
import cloud.artik.lwm2m.BaseClient;
import cloud.artik.lwm2m.Device;
import cloud.artik.lwm2m.enums.SupportedBinding;

public class UdpPskClient extends BaseClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpPskClient.class);
    
    public void run() {
        final String deviceId = "a7edc44d39fb4435a96852e182410459";
        final String deviceToken = "a5b116a701aa449ba9f1ced58053785c";
        
        final Device device = new Device("ArtikCloud", "1", "1", SupportedBinding.UDP) {
            @Override
            public ExecuteResponse executeReboot() {
                LOGGER.info("executeReboot");
                return ExecuteResponse.success();
            }
            
            @Override
            public ExecuteResponse executeFactoryReset() {
                LOGGER.info("executeFactoryReset");
                return ExecuteResponse.success();
            }
            
            @Override
            protected ExecuteResponse executeResetErrorCode() {
                LOGGER.info("executeResetErrorCode");
                return super.executeResetErrorCode();
            }
        };
        
        final ArtikCloudClient client = new ArtikCloudClient(deviceId, deviceToken, device);
        
        // Register
        client.start();
        
        // De-register on shutdown and stop client.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                client.close(); // send de-registration request before destroy
            }
        });

    }

    public static void main(String[] args) throws InterruptedException {
        UdpPskClient client = new UdpPskClient();
        client.run();

        synchronized (UdpPskClient.class) {
            UdpPskClient.class.wait();
        }
    }
}