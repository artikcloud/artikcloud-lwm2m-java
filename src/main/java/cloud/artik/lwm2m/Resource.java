package cloud.artik.lwm2m;

import java.util.Date;
import java.util.HashMap;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.node.LwM2mMultipleResource;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;

import cloud.artik.lwm2m.enums.Lwm2mEnum;

/**
 * Base class for Artik Cloud LWM2M Resources
 * 
 * @author Maneesh Sahu
 * 
 */
abstract class Resource extends BaseInstanceEnabler {
    protected final HashMap<Lwm2mEnum, LwM2mResource> resources = new HashMap<Lwm2mEnum, LwM2mResource>();

    protected void updateResources(int... resourceIds) {
        fireResourcesChange(resourceIds);
    }
    
    protected void setResourceValue(Lwm2mEnum resource, LwM2mResource value, boolean fireResourceChange) {
        this.resources.put(resource, value);
        if (fireResourceChange) {
            updateResources(resource.getResourceId());
        }
    }

    protected void setResourceValue(Lwm2mEnum resource, String value,
            boolean fireResourceChange) {
        LwM2mResource rValue = LwM2mSingleResource.newStringResource(
                resource.getResourceId(), value);
        setResourceValue(resource, rValue, fireResourceChange);
    }

    protected void setResourceValue(Lwm2mEnum resource, Long value,
            boolean fireResourceChange) {
        LwM2mResource rValue = LwM2mSingleResource.newIntegerResource(
                resource.getResourceId(), value);
        setResourceValue(resource, rValue, fireResourceChange);
    }
    
    protected void setResourceValue(Lwm2mEnum resource, Integer value,
            boolean fireResourceChange) {
        LwM2mResource rValue = LwM2mSingleResource.newIntegerResource(
                resource.getResourceId(), value);
        setResourceValue(resource, rValue, fireResourceChange);
    }

    protected void setResourceValue(Lwm2mEnum resource, boolean value,
            boolean fireResourceChange) {
        LwM2mResource rValue = LwM2mSingleResource.newBooleanResource(
                resource.getResourceId(), value);
        setResourceValue(resource, rValue, fireResourceChange);
    }
    
    protected void setResourceValue(Lwm2mEnum resource, Date value,
            boolean fireResourceChange) {
        LwM2mResource rValue = LwM2mSingleResource.newDateResource(
                resource.getResourceId(), value);
        setResourceValue(resource, rValue, fireResourceChange);
    }

    protected void setResourceValue(Lwm2mEnum resource,
            HashMap<Integer, Long> value, boolean fireResourceChange) {
        LwM2mResource rValue = LwM2mMultipleResource.newIntegerResource(
                resource.getResourceId(), value);
        setResourceValue(resource, rValue, fireResourceChange);
    }
}
