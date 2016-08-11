package cloud.artik.lwm2m.enums;

/**
 * Interface to be implemented by all Lwm2m objects with resources.
 * 
 * @see Device
 * @see FirmwareUpdate
 * @see Location
 * 
 * @author maneesh.sahu
 */
public interface Lwm2mEnum {
    public int getResourceId();
}
