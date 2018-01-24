package cloud.artik.lwm2m.enums;

/*
 * This LWM2M Object provides a range of device related information which can be queried by the LWM2M Server, and a device reboot and factory reset function.
 *
 * @link http://technical.openmobilealliance.org/tech/profiles/LWM2M_Device-v1_0.xml 
 */
public enum DeviceEnum implements Lwm2mEnum {
    MANUFACTURER(0), // Human readable manufacturer name
    MODEL_NUMBER(1), // A model identifier (manufacturer specified string)
    SERIAL_NUMBER(2), // Serial Number
    FIRMWARE_VERSION(3), // Current firmware version
    REBOOT(4), // Reboot the LWM2M Device to restore the Device from unexpected
               // firmware failure.
    FACTORY_RESET(5), // Perform factory reset of the LWM2M Device to make the
                      // LWM2M Device have the same configuration as at the
                      // initial deployment. When this Resource is executed,
                      // “De-register” operation MAY be sent to the LWM2M
                      // Server(s) before factory reset of the LWM2M Device.
    AVAILABLE_POWER_SOURCES(6), //  0 – DC power 1 – Internal Battery 2 –
                                // External Battery 4 – Power over Ethernet 5 –
                                // USB 6 – AC (Mains) power 7 – Solar
    POWER_SOURCE_VOLTAGE(7), // Present voltage for each Available Power Sources
                             // Resource Instance. Each Resource Instance ID
                             // MUST map to the value of Available Power Sources
                             // Resource.
    POWER_SOURCE_CURRENT(8), // Present current for each Available Power Source
    BATTERY_LEVEL(9), // Contains the current battery level as a percentage
                      // (with a range from 0 to 100). This value is only valid
                      // when the value of Available Power Sources Resource is 1.
    MEMORY_FREE(10), // Estimated current available amount of storage space
                     // which can store data and software in the LWM2M Device
                     // (expressed in kilobytes).
    ERROR_CODE(11), /*
                     * 0=No error 1=Low battery power 2=External power supply
                     * off 3=GPS module failure 4=Low received signal strength
                     * 5=Out of memory 6=SMS failure 7=IP connectivity failure
                     * 8=Peripheral malfunction
                     * 
                     * When the single Device Object Instance is initiated,
                     * there is only one error code Resource Instance whose
                     * value is equal to 0 that means no error. When the first
                     * error happens, the LWM2M Client changes error code
                     * Resource Instance to any non-zero value to indicate the
                     * error type. When any other error happens, a new error
                     * code Resource Instance is created. This error code
                     * Resource MAY be observed by the LWM2M Server. How to deal
                     * with LWM2M Client’s error report depends on the policy of
                     * the LWM2M Server.
                     */
    RESET_ERROR_CODE(12), // Delete all error code Resource Instances and create
                          // only one zero-value error code that implies no
                          // error.
    CURRENT_TIME(13), // Current UNIX time of the LWM2M Client. The LWM2M Client
                      // should be responsible to increase this time value as
                      // every second elapses. The LWM2M Server is able to write
                      // this Resource to make the LWM2M Client synchronized
                      // with the LWM2M Server.
    UTC_OFFSET(14), // Indicates the UTC offset currently in effect for this
                    // LWM2M Device. UTC+X [ISO 8601]
    TIMEZONE(15), // Indicates in which time zone the LWM2M Device is located,
                  // in IANA Timezone (TZ) database format.
    SUPPORTED_BINDING(16), // Indicates which bindings and modes are supported in
                          // the LWM2M Client. The possible values of Resource
                          // are combination of "U" or "UQ" and "S" or "SQ"
    DEVICE_TYPE(17), // Type of the device (manufacturer specified string : e.g. smart meters / dev Class)
    HARDWARE_VERSION(18), // Current hardware version of the device
    SOFTWARE_VERSION(19), // Current software version of the device. (manufacturer specified
                          // string). On elaborated LWM2M device, SW could be split in 2 parts : a 
                          // firmware one and a higher level software on top.
                          // Both pieces ofSoftware are together managed by LWM2M Firmware  
                          // Update Object (Object ID 5)
    BATTERY_STATUS(20),  // This value is only valid when the value of Available Power Sources Resource is 1.
                         // 0 Normal - The battery is operating normally and not on power. 
                         // 1 Charging - The battery is currently charging. 
                         // 2 Charge Complete - The battery is fully charged and still on power.
                         // 3 Damaged - The battery has some problem.
                         // 4 Low Battery - The battery is low on charge. 
                         // 5 Not Installed - The battery is not installed. 
                         // 6 Unknown - The battery information is not available.
    MEMORY_TOTAL(21), // Total amount of storage    space which can store data and software
                     // in the  LWM2M Device (expressed in kilobytes).
    EXT_DEV_INFO(22) // Reference to external “Device” object instance containing information. For example, such an external device can be a Host Device, which is a device into which the Device containing the LwM2M client is embedded. This Resource may be used to retrieve information about the Host Device.
    ;

    private final int resourceId;

    private DeviceEnum(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public int getResourceId() {
        return this.resourceId;
    }
}
