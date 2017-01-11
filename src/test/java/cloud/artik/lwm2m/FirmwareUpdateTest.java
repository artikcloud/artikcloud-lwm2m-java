package cloud.artik.lwm2m;

import cloud.artik.lwm2m.enums.FirmwareUpdateEnum;
import cloud.artik.lwm2m.enums.FirmwareUpdateResult;
import cloud.artik.lwm2m.enums.SupportedBinding;
import cloud.artik.lwm2m.exception.ConnectionLostException;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by p.ayyavu on 1/9/17.
 */
public class FirmwareUpdateTest {

    private Device device = null;
    private FirmwareUpdate firmwareUpdate = null;
    private String deviceId = "1340f16ae1614c3194d04282b10da588";
    private String deviceToken = "8e74dacc0d5c44a7a50cef844322cf7f";
    private String initialFirmwareVersion = "0.1";
    private String imageDownloadUrl = "http://downloadUrl/updateId";

    @Before
    public void initialize() {
        device = new Device("FirmwareUpdateTestDeviceMftr", "ModelNumber", "SerialNumber", SupportedBinding.UDP) {
            @Override
            public ExecuteResponse executeReboot() {
                return ExecuteResponse.success();
            }

            @Override
            public ExecuteResponse executeFactoryReset() {
                return ExecuteResponse.success();
            }
        };

        device.setFirmwareVersion(initialFirmwareVersion, true);
    }

    public long getCurrentState(FirmwareUpdate firmwareUpdate) {
        ReadResponse readResponse = firmwareUpdate.read(FirmwareUpdateEnum.STATE.getResourceId());
        assertNotNull(readResponse);
        LwM2mSingleResource resource = ((LwM2mSingleResource) readResponse.getContent());
        return (Long) resource.getValue();
    }

    @Test
    public void SuccessTest() {
        try {
            ArtikCloudClient client = new ArtikCloudClient(deviceId, deviceToken, device);

            firmwareUpdate = new FirmwareUpdate() {
                @Override
                public FirmwareUpdateResult downloadPackage(String packageUri) {
                    try {
                        System.out.println("Downloading the image...........");
                        Thread.sleep(2000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    return FirmwareUpdateResult.SUCCESS;
                }

                @Override
                public FirmwareUpdateResult executeUpdateFirmware() {
                    return FirmwareUpdateResult.SUCCESS;
                }
            };

            client.setFirmwareUpdate(firmwareUpdate);
            client.start();

            Thread.sleep(1000);

            WriteResponse writeResponse = firmwareUpdate.write(FirmwareUpdateEnum.PACKAGE_URI.getResourceId(),
                    LwM2mSingleResource.newStringResource(FirmwareUpdateEnum.PACKAGE_URI.getResourceId(),
                            imageDownloadUrl));
            assertNotNull(writeResponse);

            // Download takes 2 seconds
            Thread.sleep(4000);

            ReadResponse readResponse = firmwareUpdate.read(FirmwareUpdateEnum.PACKAGE_URI.getResourceId());
            assertNotNull(readResponse);

            assertEquals(2L, getCurrentState(firmwareUpdate));

            ExecuteResponse executeResponse = firmwareUpdate.execute(FirmwareUpdateEnum.UPDATE.getResourceId(), "");
            assertNotNull(executeResponse);

            //Let the update finish successfully
            Thread.sleep(2000);

            assertEquals(0L, getCurrentState(firmwareUpdate));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void DownloadFailureTest() {

        FirmwareUpdate firmwareUpdate = new FirmwareUpdate() {
            @Override
            public FirmwareUpdateResult downloadPackage(String packageUri) throws Exception {
                throw new ConnectionLostException("Connection lost with the server");
            }

            @Override
            public FirmwareUpdateResult executeUpdateFirmware() {
                return null;
            }
        };

        try {
            ArtikCloudClient client = new ArtikCloudClient(deviceId, deviceToken, device);
            client.setFirmwareUpdate(firmwareUpdate);
            client.start();

            Thread.sleep(1000);

            WriteResponse writeResponse = firmwareUpdate.write(FirmwareUpdateEnum.PACKAGE_URI.getResourceId(),
                    LwM2mSingleResource.newStringResource(FirmwareUpdateEnum.PACKAGE_URI.getResourceId(),
                            imageDownloadUrl));
            assertNotNull(writeResponse);

            // Download takes 2 seconds
            Thread.sleep(4000);

            assertEquals(0L, getCurrentState(firmwareUpdate));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
