package cloud.artik.lwm2m;

import static cloud.artik.lwm2m.enums.FirmwareUpdateEnum.STATE;
import static cloud.artik.lwm2m.enums.FirmwareUpdateEnum.UPDATE_RESULT;
import static cloud.artik.lwm2m.enums.FirmwareUpdateEnum.UPDATE_SUPPORTED_OBJECTS;
import static cloud.artik.lwm2m.enums.FirmwareUpdateEnum.PKG_NAME;
import static cloud.artik.lwm2m.enums.FirmwareUpdateEnum.PKG_VERSION;

import cloud.artik.lwm2m.exception.ConnectionLostException;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.artik.lwm2m.enums.FirmwareUpdateEnum;
import cloud.artik.lwm2m.enums.FirmwareUpdateResult;
import cloud.artik.lwm2m.enums.FirmwareUpdateState;

/**
 * This LWM2M Object enables management of firmware which is to be updated. 
 * This Object includes installing firmware package, updating firmware, and performing actions after updating firmware.
 * 
 * @link http://technical.openmobilealliance.org/tech/profiles/LWM2M_Firmware_Update-v1_0.xml
 * @author Maneesh Sahu
 *
 */
public abstract class FirmwareUpdate extends Resource {
    public final static Logger LOGGER = LoggerFactory.getLogger(FirmwareUpdate.class);

    /**
     * Default Constructor. 
     * State initialized to IDLE(1), UpdateResult to DEFAULT(0)
     */
    public FirmwareUpdate() {
        setState(FirmwareUpdateState.IDLE, false);
        setUpdateResult(FirmwareUpdateResult.DEFAULT, false);
        setUpdateSupportedObjects(Boolean.FALSE, false);
    }
    
    /**
     * This method needs to be overriden, to download the firmware package.
     * 
     * @param packageUri - URI from where the device can download the firmware package by an alternative mechanism. 
     *        As soon the device has received the Package URI it performs the download at the next practical opportunity.
     * @return FirmwareUpdateResult
     */
    public abstract FirmwareUpdateResult downloadPackage(String packageUri) throws Exception;
    
    /**
     * This methods needs to be overridden, to update the firmware using the firmware package.
     * Updates firmware by using the firmware package stored in Package, or, by using the firmware downloaded from the Package URI. 
     * This Resource is only executable when the value of the State Resource is Downloaded.
     * 
     * @return FirmwareUpdateResult
     */
    public abstract FirmwareUpdateResult executeUpdateFirmware();
 
    @Override
    public ReadResponse read(int resourceId) {
        FirmwareUpdateEnum resource = FirmwareUpdateEnum.valueOf(resourceId);
        if (resource == null) {
            return ReadResponse.notFound();
        }

        LOGGER.info("read( resourceId: " + resource + ")");
        if (this.resources.containsKey(resource)) {
            LwM2mResource value = this.resources.get(resource);
            LOGGER.info("value: " + value);
            return ReadResponse.success(value);
        } else {
            LOGGER.info(" default");
            return super.read(resourceId);
        }
    }

    @Override
    public WriteResponse write(int resourceId, LwM2mResource value) {
        FirmwareUpdateEnum resource = FirmwareUpdateEnum.valueOf(resourceId);
        if (resource == null) {
            return WriteResponse.notFound();
        }

        LOGGER.info("write " + resourceId + ", " + value);
        switch (resource) {
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
                            
                            if (result == FirmwareUpdateResult.DEFAULT || result == FirmwareUpdateResult.SUCCESS) {
                                setState(FirmwareUpdateState.DOWNLOADED, false);
                                setUpdateResult(FirmwareUpdateResult.DEFAULT, false);
                            } else {
                                setState(FirmwareUpdateState.IDLE, false);
                                setUpdateResult(result, false);
                            }
                            fireResourcesChange(STATE.getResourceId(), UPDATE_RESULT.getResourceId());
                        } catch (ConnectionLostException cle){
                            setState(FirmwareUpdateState.IDLE, false);
                            setUpdateResult(FirmwareUpdateResult.CONNECTION_LOST, false);
                            fireResourcesChange(STATE.getResourceId(), UPDATE_RESULT.getResourceId());
                        }
                        catch (Exception e) {
                            LOGGER.error("Error Downloading Package URI " + packageUri, e);
                            setState(FirmwareUpdateState.IDLE, false);
                            setUpdateResult(FirmwareUpdateResult.FAILED, false);
                            fireResourcesChange(STATE.getResourceId(), UPDATE_RESULT.getResourceId());
                        }
                    }
                }).start();
            }
            
            return WriteResponse.success();
            
        case UPDATE_SUPPORTED_OBJECTS:
            setUpdateSupportedObjects((Boolean) value.getValue(), true);
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
        FirmwareUpdateEnum firmwareUpdateEnum = FirmwareUpdateEnum.valueOf(resourceId);
        if (firmwareUpdateEnum == null) {
            return ExecuteResponse.notFound();
        }

        switch (firmwareUpdateEnum) {
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
                        setState(FirmwareUpdateState.UPDATING, false);
                        setUpdateResult(FirmwareUpdateResult.DEFAULT, false);
                        fireResourcesChange(STATE.getResourceId(), UPDATE_RESULT.getResourceId());
                        
                        FirmwareUpdateResult result = executeUpdateFirmware();
                            
                        if (result == FirmwareUpdateResult.DEFAULT || result == FirmwareUpdateResult.SUCCESS) {
                            setState(FirmwareUpdateState.IDLE, false);
                            setUpdateResult(FirmwareUpdateResult.SUCCESS, false);
                        } else {
                            setState(FirmwareUpdateState.DOWNLOADED, false);
                            setUpdateResult(result, false);
                        }
                        fireResourcesChange(STATE.getResourceId(), UPDATE_RESULT.getResourceId());
                    } catch (Exception e) {
                        LOGGER.error("Error applying update", e);
                        setState(FirmwareUpdateState.DOWNLOADED, false);
                        setUpdateResult(FirmwareUpdateResult.FAILED, false);
                        fireResourcesChange(STATE.getResourceId(), UPDATE_RESULT.getResourceId());
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
     * 0: Idle (before downloading or after updating)
     * 1: Downloading (The data sequence is on the way)
     * 2: Downloaded
     * 3: Updating
     * 
     * If writing the firmware package to Package Resource is done, or, if the device has downloaded the firmware package from the Package
     *   URI the state changes to Downloaded.
     * Writing an empty string to Package Resource or to Package URI Resource, resets the Firmware Update State Machine: the State Resource
     *   value is set to Idle and the Update Result Resource value is set to 0.
     * When in Downloaded state, and the executable Resource Update is triggered, the state changes to Updating.
     * If the Update Resource failed, the state returns at Downloaded.
     * If performing the Update Resource was successful, the state changes from Updating to Idle.
     */
    public FirmwareUpdateState getState() {
        return FirmwareUpdateState.values()[((Long) this.resources.get(STATE).getValue()).intValue()];
    }
    
    /*
     * Indicates current state with respect to this firmware update. This value is set by the LWM2M Client.
     * 0: Idle (before downloading or after updating)
     * 1: Downloading (The data sequence is on the way)
     * 2: Downloaded
     * 3: Updating
     * 
     * If writing the firmware package to Package Resource is done, or, if the device has downloaded the firmware package from the Package
     *   URI the state changes to Downloaded.
     * Writing an empty string to Package Resource or to Package URI Resource, resets the Firmware Update State Machine: the State Resource
     *   value is set to Idle and the Update Result Resource value is set to 0.
     * When in Downloaded state, and the executable Resource Update is triggered, the state changes to Updating.
     * If the Update Resource failed, the state returns at Downloaded.
     * If performing the Update Resource was successful, the state changes from Updating to Idle.
     */
    protected void setState(FirmwareUpdateState state, boolean fireResourceChange) {
        setResourceValue(STATE, state.getStateAsLong(), fireResourceChange);
    }
    
    /**
     * Indicates the current name of the package
     * @return
     */
    public String getPkgName() {
        return (String) this.resources.get(PKG_NAME).getValue();
    }
    
    public void setPkgName(String pkgName, boolean fireResourceChange) {
        setResourceValue(PKG_NAME, pkgName, fireResourceChange);
    }
    
    /**
     * Indicates the current version of the package
     * @return
     */
    public String getPkgVersion() {
        return (String) this.resources.get(PKG_VERSION).getValue();
    }
    
    public void setPkgVersion(String pkgVersion, boolean fireResourceChange) {
        setResourceValue(PKG_VERSION, pkgVersion, fireResourceChange);
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
        return (Boolean) this.resources.get(UPDATE_SUPPORTED_OBJECTS).getValue();
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
        return FirmwareUpdateResult.values()[((Long) this.resources.get(UPDATE_RESULT).getValue()).intValue()];
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
        setResourceValue(UPDATE_RESULT, updateResult.getResultAsLong(), fireResourceChange);
    }

}