package cloud.artik.lwm2m.exception;

public class NoCoapCipherSuiteSupportedException extends RuntimeException {

    public NoCoapCipherSuiteSupportedException() {
        super("None of the CoAP cipher suites are supported");
    }
}