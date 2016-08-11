package cloud.artik.lwm2m;

import static cloud.artik.lwm2m.enums.LocationEnum.ALTITUDE;
import static cloud.artik.lwm2m.enums.LocationEnum.LATITUDE;
import static cloud.artik.lwm2m.enums.LocationEnum.LONGITUDE;
import static cloud.artik.lwm2m.enums.LocationEnum.TIMESTAMP;
import static cloud.artik.lwm2m.enums.LocationEnum.UNCERTAINITY;
import static cloud.artik.lwm2m.enums.LocationEnum.VELOCITY;

import java.util.Arrays;
import java.util.Date;

import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ReadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.artik.lwm2m.enums.LocationEnum;

/*
 * This LWM2M Objects provide a range of device related information which can be queried by the LWM2M Server, and a device reboot and factory reset function.
 * 
 * @link http://technical.openmobilealliance.org/tech/profiles/LWM2M_Location-v1_0.xml
 */
public abstract class Location extends Resource {
    private static final Logger LOG = LoggerFactory.getLogger(Location.class);

    /*
     * Default constructor.
     */
    public Location() {
    }

    @Override
    public ReadResponse read(int resourceId) {
        LocationEnum location = LocationEnum.values()[resourceId];
        LOG.info("read( resourceId: " + location + ")");
        if (this.resources.containsKey(location)) {
            LwM2mResource value = this.resources.get(location);
            LOG.info("value: " + value);
            return ReadResponse.success(value);
        } else {
            switch (location) {
            case TIMESTAMP:
                Date value = getTimestamp();
                LOG.info("value: " + value);
                return ReadResponse.success(location.getResourceId(), value);
            default:
                LOG.info(" default");
                return super.read(resourceId);
            }
        }
    }

    public void setLocation(String latitude, String longitude, String altitude,
            String velocity, String uncertainity) {
        setLatitude(latitude, false);
        setLongitude(longitude, false);
        setAltitude(altitude, false);
        setVelocity(velocity, false);
        setUncertainity(uncertainity, false);
        updateResources(LATITUDE.getResourceId(), LONGITUDE.getResourceId(),
                ALTITUDE.getResourceId(), VELOCITY.getResourceId(),
                UNCERTAINITY.getResourceId());
    }

    /*
     * The decimal notation of latitude, e.g. -43.5723 [World Geodetic System
     * 1984].
     */
    public String getLatitude() {
        return (String) this.resources.get(LATITUDE).getValue();
    }

    /*
     * The decimal notation of latitude, e.g. -43.5723 [World Geodetic System
     * 1984].
     */
    public void setLatitude(String latitude, boolean fireResourceChange) {
        setResourceValue(LATITUDE, latitude, fireResourceChange);
    }

    /*
     * The decimal notation of longitude, e.g. 153.21760 [World Geodetic System
     * 1984].
     */
    public String getLongitude() {
        return (String) this.resources.get(LONGITUDE).getValue();
    }

    /*
     * The decimal notation of longitude, e.g. 153.21760 [World Geodetic System
     * 1984].
     */
    public void setLongitude(String longitude, boolean fireResourceChange) {
        setResourceValue(LONGITUDE, longitude, fireResourceChange);
    }

    /*
     * The decimal notation of altitude in meters above sea level.
     */
    public String getAltitude() {
        return (String) this.resources.get(ALTITUDE).getValue();

    }

    /*
     * The decimal notation of altitude in meters above sea level.
     */
    public void setAltitude(String altitude, boolean fireResourceChange) {
        setResourceValue(ALTITUDE, altitude, fireResourceChange);
    }

    /*
     * The accuracy of the position in meters.
     */
    public String getUncertainity() {
        return (String) this.resources.get(UNCERTAINITY).getValue();

    }

    /*
     * The accuracy of the position in meters.
     */
    public void setUncertainity(String uncertainity, boolean fireResourceChange) {
        setResourceValue(UNCERTAINITY, uncertainity, fireResourceChange);
    }

    /*
     * The velocity of the device as defined in 3GPP 23.032 GAD specification.
     * This set of values may not be available if the device is static.
     */
    public String getVelocity() {
        return (String) this.resources.get(VELOCITY).getValue();
    }

    /*
     * The velocity of the device as defined in 3GPP 23.032 GAD specification.
     * This set of values may not be available if the device is static.
     */
    public void setVelocity(String velocity, boolean fireResourceChange) {
        setResourceValue(VELOCITY, velocity, fireResourceChange);
    }

    /*
     * The timestamp of when the location measurement was performed.
     */
    public Date getTimestamp() {
        return (Date) this.resources.get(TIMESTAMP).getValue();
    }

    /**
     * Overrides the base class, because the timestamp needs to be shown as an
     * updated resource.
     */
    @Override
    protected void updateResources(int... resourceIds) {
        setResourceValue(TIMESTAMP, new Date(), false);

        int[] resourceIdsWithTimestamp = Arrays.copyOf(resourceIds,
                resourceIds.length + 1);
        resourceIdsWithTimestamp[resourceIds.length] = TIMESTAMP
                .getResourceId();
        fireResourcesChange(resourceIdsWithTimestamp);
    }
}
