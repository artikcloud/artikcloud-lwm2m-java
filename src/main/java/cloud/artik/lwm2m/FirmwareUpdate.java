package cloud.artik.lwm2m;

import static cloud.artik.lwm2m.enums.FirmwareUpdateEnum.STATE;
import static cloud.artik.lwm2m.enums.FirmwareUpdateEnum.UPDATE_RESULT;
import static cloud.artik.lwm2m.enums.FirmwareUpdateEnum.UPDATE_SUPPORTED_OBJECTS;

import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.artik.lwm2m.enums.FirmwareUpdateEnum;
import cloud.artik.lwm2m.enums.FirmwareUpdateResult;
import cloud.artik.lwm2m.enums.FirmwareUpdateState;

public abstract class FirmwareUpdate extends Resource {
    public final static Logger LOGGER = LoggerFactory.getLogger(FirmwareUpdate.class);

    public FirmwareUpdate() {
        setState(FirmwareUpdateState.IDLE, false);
        setUpdateResult(FirmwareUpdateResult.DEFAULT, false);
    }
    
    public abstract FirmwareUpdateResult downloadPackage(String packageUri);
    
    public abstract FirmwareUpdateResult updateFirmware();

    
    @Override
    public ReadResponse read(int resourceId) {
        FirmwareUpdateEnum firmwareUpdate = FirmwareUpdateEnum.values()[resourceId];
        LOGGER.info("read( resourceId: " + firmwareUpdate + ")");
        if (this.resources.containsKey(firmwareUpdate)) {
            LwM2mResource value = this.resources.get(firmwareUpdate);
            LOGGER.info("value: " + value);
            return ReadResponse.success(value);
        } else {
            switch (firmwareUpdate) {
            default:
                LOGGER.info(" default");
                return super.read(resourceId);
            }
        }
    }

    @Override
    public WriteResponse write(int resourceId, LwM2mResource value) {
        LOGGER.info("write " + resourceId + ", " + value);
        switch (FirmwareUpdateEnum.values()[resourceId]) {
        case PACKAGE_URI:
            final String packageUri = (String) value.getValue();
            //if (LOGGER.isTraceEnabled()) {
                LOGGER.info("Package URI to download: [", packageUri + "]");
            //}
            
            if (packageUri == null || packageUri.trim().length() == 0) {
                setState(FirmwareUpdateState.IDLE, true);
            } else {            
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Downloading
                            setState(FirmwareUpdateState.DOWNLOADING, false);
                            setUpdateResult(FirmwareUpdateResult.DEFAULT, false);
                            fireResourcesChange(STATE.getResourceId(), UPDATE_RESULT.getResourceId());
            
                            // Download the resource
                            FirmwareUpdateResult result = downloadPackage(packageUri);
                            
                            setUpdateResult(result, false);                            
                            if (result == FirmwareUpdateResult.SUCCESS) {                           
                                setState(FirmwareUpdateState.DOWNLOADED, false);
                            } else {
                                setState(FirmwareUpdateState.IDLE, false);
                            }
                            fireResourcesChange(STATE.getResourceId(), UPDATE_RESULT.getResourceId());
                        } catch (Exception exc) {
                            LOGGER.error("Error Downloading Package URI " + packageUri, exc);
                            setUpdateResult(FirmwareUpdateResult.FAILED, true);                       
                        }
                    }
                }).start();      
            }
            
            return WriteResponse.success();
        default:
            LOGGER.info(" default");
            return super.write(resourceId, value);
        }
    }

    /**
     * Perform Firmware Update 
     */
    @Override
    public ExecuteResponse execute(int resourceId, String params) {
        switch (FirmwareUpdateEnum.values()[resourceId]) {
        case UPDATE: 
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Perform Firmware Update");
            }
            
            setUpdateResult(FirmwareUpdateResult.DEFAULT, true);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // perform upgrade
                    try {
                        FirmwareUpdateResult result = updateFirmware();
                            
                        setUpdateResult(result, true);
                        if (result == FirmwareUpdateResult.SUCCESS) {
                            setState(FirmwareUpdateState.IDLE, true);
                        }
                    } catch (Exception e) {
                        setUpdateResult(FirmwareUpdateResult.FAILED, true);
                    }
                }
            }).start();

            return ExecuteResponse.success();

        default:
            return super.execute(resourceId, params);
        }
    }
    
    /*
     * Indicates current state with respect to this firmware update. This value is set by the LWM2M Client.
     * 1: Idle (before downloading or after updating)
     * 2: Downloading (The data sequence is on the way)
     * 3: Downloaded
     * 
     * If writing the firmware package to Package Resource is done, or, if the device has downloaded the firmware package from the Package URI the state changes to Downloaded.
     * If writing an empty string to Package Resource is done or writing an empty string to Package URI is done, the state changes to Idle.
     * If performing the Update Resource failed, the state remains at Downloaded.
     * If performing the Update Resource was successful, the state changes from Downloaded to Idle.
     */
    public FirmwareUpdateState getState() {
        return FirmwareUpdateState.values()[((Integer) this.resources.get(UPDATE_RESULT).getValue()).intValue() - 1];
    }
    
    protected void setState(FirmwareUpdateState state, boolean fireResourceChange) {
        setResourceValue(STATE, state.getStateAsInteger(), fireResourceChange);
    }
    
    /*
     * If this value is true, the LWM2M Client MUST inform the registered LWM2M Servers of Objects and 
     * Object Instances parameter by sending an Update or Registration message after the firmware update 
     * operation at the next practical opportunity if supported Objects in the LWM2M Client have changed, 
     * in order for the LWM2M Servers to promptly manage newly installed Objects.
     * 
     * If false, Objects and Object Instances parameter MUST be reported at the next periodic Update 
     * message. The default value is false.
     * 
     */
    public Boolean getUpdateSupportedObjects() {
        if (this.resources.containsKey(UPDATE_SUPPORTED_OBJECTS)) {
            return (Boolean) this.resources.get(UPDATE_SUPPORTED_OBJECTS).getValue();
        } else {
            return Boolean.FALSE;
        }
    }
    
    /*
     * If this value is true, the LWM2M Client MUST inform the registered LWM2M Servers of Objects and 
     * Object Instances parameter by sending an Update or Registration message after the firmware update 
     * operation at the next practical opportunity if supported Objects in the LWM2M Client have changed, 
     * in order for the LWM2M Servers to promptly manage newly installed Objects.
     * 
     * If false, Objects and Object Instances parameter MUST be reported at the next periodic Update 
     * message. The default value is false.
     * 
     */
    public void setUpdateSupportedObjects(Boolean updateSupportedObjects, boolean fireResourceChange) {
        setResourceValue(UPDATE_SUPPORTED_OBJECTS, updateSupportedObjects, fireResourceChange);
    }
    
    /*
     * Contains the result of downloading or updating the firmware
     * 
     * 0: Default value. Once the updating process is initiated, this Resource SHOULD be reset to default value.
     * 1: Firmware updated successfully,
     * 2: Not enough storage for the new firmware package.
     * 3. Out of memory during downloading process.
     * 4: Connection lost during downloading process.
     * 5: CRC check failure for new downloaded package.
     * 6: Unsupported package type.
     * 7: Invalid URI
     * 
     * This Resource MAY be reported by sending Observe operation.
     */
    public FirmwareUpdateResult getUpdateResult() {
        return FirmwareUpdateResult.values()[((Integer) this.resources.get(UPDATE_RESULT).getValue()).intValue()];
    }
    
    /*
     * Contains the result of downloading or updating the firmware
     * 
     * 0: Default value. Once the updating process is initiated, this Resource SHOULD be reset to default value.
     * 1: Firmware updated successfully,
     * 2: Not enough storage for the new firmware package.
     * 3. Out of memory during downloading process.
     * 4: Connection lost during downloading process.
     * 5: CRC check failure for new downloaded package.
     * 6: Unsupported package type.
     * 7: Invalid URI
     * 
     * This Resource MAY be reported by sending Observe operation.
     */
    public void setUpdateResult(FirmwareUpdateResult updateResult, boolean fireResourceChange) {
        setResourceValue(UPDATE_RESULT, updateResult.getResultAsInteger(), fireResourceChange);
    }

}