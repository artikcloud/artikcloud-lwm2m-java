package cloud.artik.lwm2m;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.artik.lwm2m.enums.SupportedBinding;

public class ArtikCloudClientTest {
    private static final Logger LOG = LoggerFactory.getLogger(ArtikCloudClientTest.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testStart() throws Exception{
        
        Device device = new Device("ArtikCloud", "1", "1", SupportedBinding.UDP);
        // Available Power Sources - Battery(0) and USB(5)
        HashMap<Integer, Long> availablePowerSources = new HashMap<Integer, Long>();
        availablePowerSources.put(new Integer(0), 0l);
        availablePowerSources.put(new Integer(1), 5l);
        device.setAvailablePowerSources(availablePowerSources, false);

        ArtikCloudClient client = new ArtikCloudClient("24936ceccdb24a54a58a341ee7c5d1a3", "2f1a098e131b4d4c9aaaaf38bb06df87", device);
        // TBD: Remove when PROD push
        client.setServerName("coap-dev.artik.cloud");
        
        // Register
        client.start();
        
        // Sleep for 10 seconds for the registration to complete
        Thread.sleep(10000);
        
        // De-Register
        client.stop(true);
        // Finish
        client.close();
    }

}
