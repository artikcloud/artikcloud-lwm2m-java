package cloud.artik.lwm2m.enums;

/*
 * This LWM2M Objects provide a range of device related information which can be queried by the LWM2M Server, and a device reboot and factory reset function.
 * 
 * @link http://technical.openmobilealliance.org/tech/profiles/LWM2M_Location-v1_0.xml
 */
public enum LocationEnum implements Lwm2mEnum {
    LATITUDE(0), // The decimal notation of latitude, e.g. -43.5723 [World
                 // Geodetic System 1984].
    LONGITUDE(1), // The decimal notation of longitude, e.g. 153.21760 [World
                  // Geodetic System 1984].
    ALTITUDE(2), // The decimal notation of altitude in meters above sea level.
    UNCERTAINITY(3), // The accuracy of the position in meters.
    VELOCITY(4), // The velocity of the device as defined in 3GPP 23.032 GAD
                 // specification. This set of values may not be available if
                 // the device is static.
    TIMESTAMP(5) // The timestamp of when the location measurement was
                 // performed.
    ;

    private final int resourceId;

    private LocationEnum(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public int getResourceId() {
        return this.resourceId;
    }
}
