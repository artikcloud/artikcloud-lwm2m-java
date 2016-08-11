package cloud.artik.lwm2m.enums;

/*
 * This LWM2M Object enables management of firmware which is to be updated. This Object includes installing firmware package, updating firmware, and performing actions after updating firmware.
 *   
 * @link http://technical.openmobilealliance.org/tech/profiles/LWM2M_Firmware_Update-v1_0.xml
 */
public enum FirmwareUpdateEnum implements Lwm2mEnum {
    PACKAGE(0), // Firmware Package
    PACKAGE_URI(1), // URI from where the device can download the firmware package by an alternative mechanism. 
                    // As soon the device has received the Package URI it performs the download at the next practical opportunity.
    UPDATE(2), // Updates firmware by using the firmware package stored in Package, 
               // or, by using the firmware downloaded from the Package URI.
               // This Resource is only executable when the value of the State Resource is Downloaded.
    STATE(3), // Indicates current state with respect to this firmware update.
    UPDATE_SUPPORTED_OBJECTS(4), // If this value is true, the LWM2M Client MUST inform the registered LWM2M Servers 
                                 // of Objects and Object Instances parameter by sending an Update or Registration 
                                 // message after the firmware update operation at the next practical opportunity 
                                 // if supported Objects in the LWM2M Client have changed
    UPDATE_RESULT(5) // Contains the result of downloading or updating the firmware.
    ;

    private final int resourceId;

    private FirmwareUpdateEnum(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public int getResourceId() {
        return this.resourceId;
    }
}
