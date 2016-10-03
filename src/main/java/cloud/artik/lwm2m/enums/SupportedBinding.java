package cloud.artik.lwm2m.enums;

import org.eclipse.leshan.core.request.BindingMode;

/**
 * Specifies the binding Style - 
 *  - U: UDP
 *  - T: TCP
 *  
 * @author Maneesh Sahu
 *
 */
public enum SupportedBinding {
    UDP("U"),
    TCP("T")
    ;
    private final String bindingId;
    private SupportedBinding(String bindingId) {
        this.bindingId = bindingId;
    }

    public String getBindingId() {
        return this.bindingId;
    }
    
    public BindingMode toBindingMode() {
        if (bindingId.equalsIgnoreCase("U")) {
            return BindingMode.U;
        } else {
            return BindingMode.T;
        }
    }
}
