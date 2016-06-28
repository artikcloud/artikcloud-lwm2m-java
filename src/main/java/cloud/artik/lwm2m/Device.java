package cloud.artik.lwm2m;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.artik.lwm2m.enums.DeviceEnum;
import cloud.artik.lwm2m.enums.SupportedBinding;

/*
 * This LWM2M Object provides a range of device related information which can be queried by the LWM2M Server, and a device reboot and factory reset function.
 *
 * @link http://technical.openmobilealliance.org/tech/profiles/LWM2M_Device-v1_0.xml 
 */
public class Device extends Resource {
    private static final Logger LOG = LoggerFactory.getLogger(Device.class);

    /*
     * Creates a ArtikCloud Device.
     * 
     * Default binding = UDP
     */
    public Device() {
        setUtcOffset(new SimpleDateFormat("X").format(Calendar.getInstance()
                .getTime()), false);
        setTimeZone(TimeZone.getDefault().getID(), false);
        setCurrentTime(new Date(), false);
        setErrorCode(0l, false);
    }

    /*
     * Creates an ArtikCloud Device with 'static' information.
     */
    public Device(String manufacturer, String modelNumber, String serialNumber,
            SupportedBinding supportedBinding) {
        this();
        setManufacturer(manufacturer, false);
        setModelNumber(modelNumber, false);
        setSerialNumber(serialNumber, false);
        setSupportedBinding(supportedBinding, false);
    }

    /*
     * Notifies Current Time every period starting after the specified delay
     * 
     * @param delay - delay in milliseconds before the current time is notified
     * 
     * @param period - time in milliseconds between successive notifications
     */
    public Device(int delay, int period) {
        this();
        // Notify new date every 'period' milliseconds.
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourcesChange(DeviceEnum.CURRENT_TIME.getResourceId());
            }
        }, delay, period);
    }

    @Override
    public ReadResponse read(int resourceId) {
        DeviceEnum resource = DeviceEnum.values()[resourceId];
        LOG.info("read( resourceId: " + resource + ")");
        if (this.resources.containsKey(resource)) {
            LwM2mResource value = this.resources.get(resource);
            LOG.info("value: " + value);
            return ReadResponse.success(value);
        } else {
            switch (resource) {
            case CURRENT_TIME:
                Date value = getCurrentTime();
                LOG.info("value: " + value);
                return ReadResponse.success(resource.getResourceId(), value);
            case REBOOT:
            case FACTORY_RESET:
            default:
                LOG.info(" default");
                return super.read(resourceId);
            }
        }
    }

    @Override
    public ExecuteResponse execute(int resourceId, String params) {
        if (params != null && params.length() != 0)
            LOG.info("params: " + params);
        return ExecuteResponse.success();
    }

    @Override
    public WriteResponse write(int resourceId, LwM2mResource value) {
        switch (DeviceEnum.values()[resourceId]) {
        case CURRENT_TIME:
            return WriteResponse.notFound();
        case UTC_OFFSET:
            setUtcOffset((String) value.getValue(), true);
            return WriteResponse.success();
        case TIMEZONE:
            setTimeZone((String) value.getValue(), true);
            return WriteResponse.success();
        default:
            return super.write(resourceId, value);
        }
    }

    /*
     * Human readable manufacturer name
     */
    public String getManufacturer() {
        return (String) this.resources.get(DeviceEnum.MANUFACTURER).getValue();
    }

    /*
     * A model identifier (manufacturer specified string)
     */
    public String getModelNumber() {
        return (String) this.resources.get(DeviceEnum.MODEL_NUMBER).getValue();
    }

    /*
     * Serial Number
     */
    public String getSerialNumber() {
        return (String) this.resources.get(DeviceEnum.SERIAL_NUMBER).getValue();
    }

    /*
     * Current firmware version
     */
    public String getFirmwareVersion() {
        return (String) this.resources.get(DeviceEnum.FIRMWARE_VERSION)
                .getValue();
    }

    /*
     * 0 – DC power 1 – Internal Battery 2 – External Battery 4 – Power over
     * Ethernet 5 – USB 6 – AC (Mains) power 7 – Solar
     */
    @SuppressWarnings("unchecked")
    public Map<Integer, Long> getAvailablePowerSources() {
        return (Map<Integer, Long>) this.resources
                .get(DeviceEnum.AVAILABLE_POWER_SOURCES);
    }

    /*
     * Present voltage for each Available Power Sources Resource Instance. Each
     * Resource Instance ID MUST map to the value of Available Power Sources
     * Resource.
     */
    @SuppressWarnings("unchecked")
    public Map<Integer, Long> getPowerSourceVoltage() {
        return (Map<Integer, Long>) this.resources
                .get(DeviceEnum.POWER_SOURCE_VOLTAGE);
    }

    /*
     * Present current for each Available Power Source. Each Resource Instance
     * ID MUST map to the value of Available Power Sources Resource.
     */
    @SuppressWarnings("unchecked")
    public Map<Integer, Long> getPowerSourceCurrent() {
        return (Map<Integer, Long>) this.resources
                .get(DeviceEnum.POWER_SOURCE_CURRENT);
    }

    /*
     * 0=No error 1=Low battery power 2=External power supply off 3=GPS module
     * failure 4=Low received signal strength 5=Out of memory 6=SMS failure 7=IP
     * connectivity failure 8=Peripheral malfunction
     * 
     * When the ArtikCloudDevice is initiated, this value should be 0 that means
     * no error. When the first error happens, the LWM2M Client changes error
     * code Resource Instance to any non-zero value to indicate the error type.
     * When any other error happens, a new error code Resource Instance is
     * created. This error code Resource MAY be observed by the LWM2M Server.
     * How to deal with LWM2M Client’s error report depends on the policy of the
     * LWM2M Server. Override this to return an error
     */
    public Long getErrorCode() {
        return (Long) this.resources.get(DeviceEnum.ERROR_CODE).getValue();
    }

    /*
     * Contains the current battery level as a percentage (with a range from 0
     * to 100). This value is only valid when the value of Available Power
     * Sources Resource is 1.
     */
    public Long getBatteryLevel() {
        return (Long) this.resources.get(DeviceEnum.BATTERY_LEVEL).getValue();
    }

    /*
     * Estimated current available amount of storage space which can store data
     * and software in the LWM2M Device (expressed in kilobytes).
     */
    public Long getMemoryFree() {
        return (Long) this.resources.get(DeviceEnum.MEMORY_FREE).getValue();
    }

    /*
     * Current UNIX time of the ArtikCloudDevice. The LWM2M Server is able to
     * write this Resource to make the LWM2M Client synchronized with the LWM2M
     * Server.
     */
    public Date getCurrentTime() {
        return new Date();
    }

    /*
     * Indicates the UTC offset currently in effect for this LWM2M Device. UTC+X
     * [ISO 8601]
     */
    public String getUtcOffset() {
        return (String) this.resources.get(DeviceEnum.UTC_OFFSET).getValue();
    }

    /*
     * Indicates in which time zone the LWM2M Device is located, in IANA
     * Timezone (TZ) database format.
     */
    public String getTimeZone() {
        return (String) this.resources.get(DeviceEnum.TIMEZONE).getValue();
    }

    /*
     * Indicates which bindings and modes are supported in the LWM2M Client. The
     * possible values of Resource are combination of "U" or "C"
     */
    public SupportedBinding getSupportedBinding() {
        String binding = (String) this.resources.get(DeviceEnum.SUPPORTED_BINDING)
                .getValue();
        
        if (binding != null) {
            if (binding.equalsIgnoreCase("U")) {
                return SupportedBinding.UDP;
            } else {
                return SupportedBinding.TCP;
            }
        } else {
            return null;
        }
    }

    /**
     * This is a NOOP. Currently setting current time doesn't synch the clocks.
     * 
     * @param date
     */
    public void setCurrentTime(Date date, boolean fireResourceChange) {
        // To-Do: Synch time
    }

    /*
     * Indicates the UTC offset currently in effect for this LWM2M Device. UTC+X
     * [ISO 8601]
     */
    public void setUtcOffset(String utcOffset, boolean fireResourceChange) {
        setResourceValue(DeviceEnum.UTC_OFFSET, utcOffset, fireResourceChange);
    }

    /*
     * Indicates in which time zone the LWM2M Device is located, in IANA
     * Timezone (TZ) database format.
     */
    public void setTimeZone(String timeZone, boolean fireResourceChange) {
        setResourceValue(DeviceEnum.TIMEZONE, timeZone, fireResourceChange);
    }

    /*
     * Human readable manufacturer name
     */
    public void setManufacturer(String manufacturer, boolean fireResourceChange) {
        setResourceValue(DeviceEnum.MANUFACTURER, manufacturer,
                fireResourceChange);
    }

    /*
     * A model identifier (manufacturer specified string)
     */
    public void setModelNumber(String modelNumber, boolean fireResourceChange) {
        setResourceValue(DeviceEnum.MODEL_NUMBER, modelNumber,
                fireResourceChange);
    }

    /*
     * Serial Number
     */
    public void setSerialNumber(String serialNumber, boolean fireResourceChange) {
        setResourceValue(DeviceEnum.SERIAL_NUMBER, serialNumber,
                fireResourceChange);
    }

    /*
     * Current firmware version
     */
    public void setFirmwareVersion(String firmwareVersion,
            boolean fireResourceChange) {
        setResourceValue(DeviceEnum.FIRMWARE_VERSION, firmwareVersion,
                fireResourceChange);
    }

    /*
     * Indicates which bindings and modes are supported in the LWM2M Client. The
     * possible values of Resource are combination of "U" or "T".
     * 
     * Default binding = UDP
     */
    public void setSupportedBinding(SupportedBinding supportedBinding,
            boolean fireResourceChange) {
        if (supportedBinding == null) {
            supportedBinding = SupportedBinding.UDP;
        }
        setResourceValue(DeviceEnum.SUPPORTED_BINDING, supportedBinding.getBindingId(),
                fireResourceChange);
    }

    /*
     * 0 – DC power 1 – Internal Battery 2 – External Battery 4 – Power over
     * Ethernet 5 – USB 6 – AC (Mains) power 7 – Solar
     */
    public void setAvailablePowerSources(
            HashMap<Integer, Long> availablePowerSources,
            boolean fireResourceChange) {
        setResourceValue(DeviceEnum.AVAILABLE_POWER_SOURCES,
                availablePowerSources, fireResourceChange);
    }

    /*
     * Present voltage for each Available Power Sources Resource Instance. Each
     * Resource Instance ID MUST map to the value of Available Power Sources
     * Resource.
     */
    public void setPowerSourceVoltage(
            HashMap<Integer, Long> powerSourceVoltage,
            boolean fireResourceChange) {
        setResourceValue(DeviceEnum.POWER_SOURCE_VOLTAGE, powerSourceVoltage,
                fireResourceChange);
    }

    /*
     * Present current for each Available Power Source. Each Resource Instance
     * ID MUST map to the value of Available Power Sources Resource.
     */
    public void setPowerSourceCurrent(
            HashMap<Integer, Long> powerSourceCurrent,
            boolean fireResourceChange) {
        setResourceValue(DeviceEnum.POWER_SOURCE_CURRENT, powerSourceCurrent,
                fireResourceChange);
    }

    /*
     * 0=No error 1=Low battery power 2=External power supply off 3=GPS module
     * failure 4=Low received signal strength 5=Out of memory 6=SMS failure 7=IP
     * connectivity failure 8=Peripheral malfunction
     * 
     * When the ArtikCloudDevice is initiated, this value should be 0 that means
     * no error. When the first error happens, the LWM2M Client changes error
     * code Resource Instance to any non-zero value to indicate the error type.
     * When any other error happens, a new error code Resource Instance is
     * created. This error code Resource MAY be observed by the LWM2M Server.
     * How to deal with LWM2M Client’s error report depends on the policy of the
     * LWM2M Server. Override this to return an error
     */
    public void setErrorCode(long errorCode, boolean fireResourceChange) {
        setResourceValue(DeviceEnum.ERROR_CODE, errorCode, fireResourceChange);
    }

    /*
     * Contains the current battery level as a percentage (with a range from 0
     * to 100). This value is only valid when the value of Available Power
     * Sources Resource is 1.
     */
    public void setBatteryLevel(long batteryLevel, boolean fireResourceChange) {
        setResourceValue(DeviceEnum.BATTERY_LEVEL, batteryLevel,
                fireResourceChange);
    }

    /*
     * Estimated current available amount of storage space which can store data
     * and software in the LWM2M Device (expressed in kilobytes).
     */
    public void setMemoryFree(long memoryFree, boolean fireResourceChange) {
        setResourceValue(DeviceEnum.MEMORY_FREE, memoryFree, fireResourceChange);
    }
}
